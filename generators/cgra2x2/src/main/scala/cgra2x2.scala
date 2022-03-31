package cgra2x2

import Chisel._
import chisel3.util.{HasBlackBoxResource}
import chisel3.experimental.Analog
import freechips.rocketchip.tile._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.rocket.{TLBConfig, HellaCacheReq}

case object Cgra2x2WidthP extends Field[Int]
case object Cgra2x2Stages extends Field[Int]
case object Cgra2x2FastMem extends Field[Boolean]
case object Cgra2x2BufferSram extends Field[Boolean]

/*
 * Use a Blackbox verilog version of the CGRA-ME accelerator
 */
case object Cgra2x2BlackBox extends Field[Boolean](false)

// Lasse: examples on how to implement RoCC interfaces:
// /generators/rocket-chip/src/main/scala/tile/LazyRoCC.scala

// A BlackBox wrapper.
// All we do is expose the Verilog io.
class Cgra2x2BlackBox(implicit p: Parameters) extends BlackBox with HasBlackBoxResource{
  val io = IO(new Bundle {
	  val Config_Clock = Input(Bool())
	  val Config_Reset = Input(Bool())
	  // Reset()?
	  val ConfigIn  = Input(UInt(1.W))
	  val ConfigOut = Output(UInt(1.W))

	  val CGRA_Clock = Input(Clock())
	  val CGRA_Reset = Input(Bool())
    
    val write     = Input(UInt(1.W))
      // Chisel Analog => Verilog inout
    val dataIn0   = Input(UInt(32.W))
    val dataIn1   = Input(UInt(32.W))
    val dataOut0  = Output(UInt(32.W))
    val dataOut1  = Output(UInt(32.W))

    val write_rq0 = Output(Bool())
    val from_mem0 = Input(UInt(32.W))
    val to_mem0   = Output(UInt(32.W))
    val addr0     = Output(UInt(32.W))
    
    val write_rq1 = Output(Bool())
    val from_mem1 = Input(UInt(32.W))
    val to_mem1   = Output(UInt(32.W))
    val addr1     = Output(UInt(32.W))
  })

  // Lots of files. The examples have monolithic files.
  addResource("/Cgra2x2BlackBox.v")
  addResource("/adres_5in_vliw.v")
  addResource("/adres_6in_vliw.v")
  addResource("/const_32b.v")
  addResource("/func_32b_add_multiply_sub_divide_and_or_xor_shl_ashr_lshr.v")
  addResource("/ConfigCell.v")
  addResource("/io_32b.v")
  // addResource("/memUnit_32b.v")
  addResource("/memoryPort_2connect_32b.v")
  addResource("/mux_2to1_32b.v")
  addResource("/mux_4to1_32b.v")
  addResource("/mux_5to1_32b.v")
  addResource("/mux_6to1_32b.v")
  addResource("/mux_7to1_32b.v")
  addResource("/mux_8to1_32b.v")
  addResource("/op_add_32b.v")
  addResource("/op_and_32b.v")
  addResource("/op_ashr_32b.v")
  addResource("/op_divide_32b.v")
  addResource("/op_lshr_32b.v")
  addResource("/op_multiply_32b.v")
  addResource("/op_or_32b.v")
  addResource("/op_shl_32b.v")
  addResource("/op_sub_32b.v")
  addResource("/op_xor_32b.v")
  addResource("/registerFile_1in_2out_32b.v")
  addResource("/registerFile_2in_4out_32b.v")
  addResource("/register_32b.v")
  addResource("/tristate_32b.v")
}

class Cgra2x2Accel(opcodes: OpcodeSet)(implicit p: Parameters) extends LazyRoCC(
    opcodes = opcodes, nPTWPorts = 0) {
    override lazy val module = new Cgra2x2AccelImp(this)
}

class Cgra2x2AccelImp(outer: Cgra2x2Accel)(implicit p: Parameters) extends LazyRoCCModuleImp(outer){
  
  val cgra2x2bb    = Module(new Cgra2x2BlackBox)
  val ctrl        = Module(new CtrlBBModule)
  val cmd         = Queue(io.cmd) //Trengs denne?

  //RoCC
  io.resp.valid         <> ctrl.io.rocc_resp_val
  io.resp.bits.data     <> ctrl.io.rocc_resp_data
  io.resp.bits.rd       <> ctrl.io.rocc_resp_rd 
  cmd.ready             <> ctrl.io.rocc_req_rdy
  ctrl.io.rocc_resp_rdy <> io.resp.ready
  ctrl.io.rocc_req_val  <> cmd.valid
  ctrl.io.rocc_funct    <> cmd.bits.inst.funct
  ctrl.io.rocc_rs1      <> cmd.bits.rs1
  ctrl.io.rocc_rs2      <> cmd.bits.rs2
  ctrl.io.rocc_rd       <> cmd.bits.inst.rd
  io.busy               <> ctrl.io.busy
  io.interrupt          <> ctrl.io.interrupt     

  //CGRA IO
  cgra2x2bb.io.CGRA_Clock     := clock
  // cgra2x2bb.io.CGRA_Clock    := ctrl.io.CGRA_Clock
  cgra2x2bb.io.Config_Clock   := ctrl.io.Config_Clock
  cgra2x2bb.io.CGRA_Reset     := reset
  cgra2x2bb.io.Config_Reset   := ctrl.io.Config_Reset
  cgra2x2bb.io.ConfigIn       := ctrl.io.cgra_Inconfig
  ctrl.io.cgra_Outconfig      := cgra2x2bb.io.ConfigOut
  ctrl.io.from_cgra0          <> cgra2x2bb.io.dataOut0
  ctrl.io.from_cgra1          <> cgra2x2bb.io.dataOut1
  cgra2x2bb.io.dataIn0        := ctrl.io.to_cgra0
  cgra2x2bb.io.dataIn1        := ctrl.io.to_cgra1

  ctrl.io.write_rq0       := cgra2x2bb.io.write_rq0
  ctrl.io.write_rq1       := cgra2x2bb.io.write_rq1
  ctrl.io.to_mem0         := cgra2x2bb.io.to_mem0
  ctrl.io.to_mem1         := cgra2x2bb.io.to_mem1
  ctrl.io.addr0           := cgra2x2bb.io.addr0
  ctrl.io.addr1           := cgra2x2bb.io.addr1
  cgra2x2bb.io.from_mem0  := ctrl.io.from_mem0
  cgra2x2bb.io.from_mem1  := ctrl.io.from_mem1

  //Memory
  def mem_ctrl(req: DecoupledIO[HellaCacheReq]){
    req.valid             := ctrl.io.mem_req_val
    ctrl.io.mem_req_rdy   := req.ready
    req.bits.tag          := ctrl.io.mem_req_tag
    req.bits.addr         := ctrl.io.mem_req_addr
    req.bits.cmd          := ctrl.io.mem_req_cmd
    req.bits.size         := ctrl.io.mem_req_size
    req.bits.data         := ctrl.io.mem_req_data
    req.bits.signed       := Bool(false)
    req.bits.phys         := Bool(false)
  }

  mem_ctrl(io.mem.req)
  ctrl.io.mem_resp_val  <> io.mem.resp.valid
  ctrl.io.mem_resp_tag  <> io.mem.resp.bits.tag
  ctrl.io.mem_resp_data := io.mem.resp.bits.data
}

class WithCgra2x2Accel extends Config ((site, here, up) =>{
    case Cgra2x2BlackBox => true
    case BuildRoCC      => up(BuildRoCC) ++ Seq(
    (p: Parameters) => {
      val cgra2x2 = LazyModule.apply(new Cgra2x2Accel(OpcodeSet.custom0)(p))
      cgra2x2
    }
  )
})

class WithCgra2x2BlackBox extends Config((site, here, up) => {
  case Cgra2x2BlackBox => true
  // case Cgra2x2TLB => None // Do not use the more correct DmemModule when blackboxing
}) 
