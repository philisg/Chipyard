package cgrame

import Chisel._
import chisel3.util.{HasBlackBoxResource}
import chisel3.experimental.Analog
import freechips.rocketchip.tile._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.rocket.{TLBConfig, HellaCacheReq}

case object CgrameWidthP extends Field[Int]
case object CgrameStages extends Field[Int]
case object CgrameFastMem extends Field[Boolean]
case object CgrameBufferSram extends Field[Boolean]

/*
 * Use a Blackbox verilog version of the CGRA-ME accelerator
 */
case object CgrameBlackBox extends Field[Boolean](false)

// Lasse: examples on how to implement RoCC interfaces:
// /generators/rocket-chip/src/main/scala/tile/LazyRoCC.scala

// A BlackBox wrapper.
// All we do is expose the Verilog io.
class CgrameBlackBox(implicit p: Parameters) extends BlackBox with HasBlackBoxResource{
  val io = IO(new Bundle {
	  val Config_Clock = Input(Bool())
	  val Config_Reset = Input(Bool())
	  // Reset()?
	  val ConfigIn  = Input(UInt(1.W))
	  val ConfigOut = Output(UInt(1.W))

	  val CGRA_Clock = Input(Clock())
	  val CGRA_Reset = Input(Bool())
    
    val write     = Input(Bool())
      // Chisel Analog => Verilog inout
    val dataIn0   = Input(UInt(32.W))
    val dataIn1   = Input(UInt(32.W))
    val dataIn2   = Input(UInt(32.W))
    val dataIn3   = Input(UInt(32.W))
    val dataOut0  = Output(UInt(32.W))
    val dataOut1  = Output(UInt(32.W))
    val dataOut2  = Output(UInt(32.W))
    val dataOut3  = Output(UInt(32.W))

    val write_rq0 = Output(Bool())
    val from_mem0 = Input(UInt(32.W))
    val to_mem0   = Output(UInt(32.W))
    val addr0     = Output(UInt(32.W))
    
    val write_rq1 = Output(Bool())
    val from_mem1 = Input(UInt(32.W))
    val to_mem1   = Output(UInt(32.W))
    val addr1     = Output(UInt(32.W))
    
    val write_rq2 = Output(Bool())
    val from_mem2 = Input(UInt(32.W))
    val to_mem2   = Output(UInt(32.W))
    val addr2     = Output(UInt(32.W))
    
    val write_rq3 = Output(Bool())
    val from_mem3 = Input(UInt(32.W))
    val to_mem3   = Output(UInt(32.W))
    val addr3     = Output(UInt(32.W))
  })

  // Lots of files. The examples have monolithic files.
  addResource("/CgrameBlackBox.v")
  addResource("/adres_5in_vliw.v")
  addResource("/adres_6in_vliw.v")
  addResource("/const_32b.v")
  addResource("/func_32b_add_multiply_sub_divide_and_or_xor_shl_ashr_lshr.v")
  addResource("/ConfigCell.v")
  addResource("/io_32b.v")
  // addResource("/memUnit_32b.v")
  addResource("/memoryPort_4connect_32b.v")
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
  addResource("/registerFile_4in_8out_32b.v")
  addResource("/register_32b.v")
  addResource("/tristate_32b.v")
}

class CgrameAccel(opcodes: OpcodeSet)(implicit p: Parameters) extends LazyRoCC(
    opcodes = opcodes, nPTWPorts = 0) {
    override lazy val module = new CgrameAccelImp(this)
}

class CgrameAccelImp(outer: CgrameAccel)(implicit p: Parameters) extends LazyRoCCModuleImp(outer){
  
  val cgramebb    = Module(new CgrameBlackBox)
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
  cgramebb.io.CGRA_Clock    := clock
  // cgramebb.io.CGRA_Clock    := ctrl.io.CGRA_Clock
  cgramebb.io.Config_Clock  := ctrl.io.Config_Clock
  cgramebb.io.CGRA_Reset    := reset
  cgramebb.io.Config_Reset  := ctrl.io.Config_Reset
  cgramebb.io.ConfigIn      := ctrl.io.cgra_Inconfig
  ctrl.io.cgra_Outconfig    := cgramebb.io.ConfigOut
  ctrl.io.from_cgra0        <> cgramebb.io.dataOut0
  ctrl.io.from_cgra1        <> cgramebb.io.dataOut1
  ctrl.io.from_cgra2        <> cgramebb.io.dataOut2
  ctrl.io.from_cgra3        <> cgramebb.io.dataOut3
  cgramebb.io.dataIn0       := ctrl.io.to_cgra0
  cgramebb.io.dataIn1       := ctrl.io.to_cgra1
  cgramebb.io.dataIn2       := ctrl.io.to_cgra2
  cgramebb.io.dataIn3       := ctrl.io.to_cgra3

  ctrl.io.write_rq0     := cgramebb.io.write_rq0
  ctrl.io.write_rq1     := cgramebb.io.write_rq1
  ctrl.io.write_rq2     := cgramebb.io.write_rq2
  ctrl.io.write_rq3     := cgramebb.io.write_rq3
  ctrl.io.to_mem0       := cgramebb.io.to_mem0
  ctrl.io.to_mem1       := cgramebb.io.to_mem1
  ctrl.io.to_mem2       := cgramebb.io.to_mem2
  ctrl.io.to_mem3       := cgramebb.io.to_mem3
  ctrl.io.addr0         := cgramebb.io.addr0
  ctrl.io.addr1         := cgramebb.io.addr1
  ctrl.io.addr2         := cgramebb.io.addr2
  ctrl.io.addr3         := cgramebb.io.addr3
  cgramebb.io.from_mem0 := ctrl.io.from_mem0
  cgramebb.io.from_mem1 := ctrl.io.from_mem1
  cgramebb.io.from_mem2 := ctrl.io.from_mem2
  cgramebb.io.from_mem3 := ctrl.io.from_mem3

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

class WithCgrameAccel extends Config ((site, here, up) =>{
    case CgrameBlackBox => true
    case BuildRoCC      => up(BuildRoCC) ++ Seq(
    (p: Parameters) => {
      val cgrame = LazyModule.apply(new CgrameAccel(OpcodeSet.custom0)(p))
      cgrame
    }
  )
})

class WithCgrameBlackBox extends Config((site, here, up) => {
  case CgrameBlackBox => true
  // case CgrameTLB => None // Do not use the more correct DmemModule when blackboxing
}) 
