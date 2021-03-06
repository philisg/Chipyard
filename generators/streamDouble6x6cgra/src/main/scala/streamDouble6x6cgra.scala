package streamDouble6x6cgra

import Chisel._
import chisel3.util.{HasBlackBoxResource}
import freechips.rocketchip.tile._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.rocket.{TLBConfig, HellaCacheReq}

case object streamDouble6x6cgraWidthP extends Field[Int]
case object streamDouble6x6cgraStages extends Field[Int]
case object streamDouble6x6cgraFastMem extends Field[Boolean]
case object streamDouble6x6cgraBufferSram extends Field[Boolean]

/*
 * Use a Blackbox verilog version of the CGRA-ME accelerator
 */
case object Cgra6x6BlackBox extends Field[Boolean](false)

// Lasse: examples on how to implement RoCC interfaces:
// /generators/rocket-chip/src/main/scala/tile/LazyRoCC.scala

// A BlackBox wrapper.
// All we do is expose the Verilog io.
class Cgra6x6BlackBox(implicit p: Parameters) extends BlackBox with HasBlackBoxResource{
  val io = IO(new Bundle {
	  val Config_Clock = Input(Bool())
	  val Config_Reset = Input(Bool())
	  // Reset()?
	  val ConfigIn  = Input(UInt(1.W))
	  val ConfigOut = Output(UInt(1.W))

	  val CGRA_Clock = Input(Bool())
	  val CGRA_Reset = Input(Bool())
    
    val write0    = Input(Bool())
    val write1    = Input(Bool())
    val write2    = Input(Bool())
    val write3    = Input(Bool())
    val write4    = Input(Bool())
    val write5    = Input(Bool())
      // Chisel Analog => Verilog inout
    val dataIn0   = Input(UInt(32.W))
    val dataIn1   = Input(UInt(32.W))
    val dataIn2   = Input(UInt(32.W))
    val dataIn3   = Input(UInt(32.W))
    val dataIn4   = Input(UInt(32.W))
    val dataIn5   = Input(UInt(32.W))
    val dataOut0  = Output(UInt(32.W))
    val dataOut1  = Output(UInt(32.W))
    val dataOut2  = Output(UInt(32.W))
    val dataOut3  = Output(UInt(32.W))
    val dataOut4  = Output(UInt(32.W))
    val dataOut5  = Output(UInt(32.W))

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

    val write_rq4 = Output(Bool())
    val from_mem4 = Input(UInt(32.W))
    val to_mem4   = Output(UInt(32.W))
    val addr4     = Output(UInt(32.W))

    val write_rq5 = Output(Bool())
    val from_mem5 = Input(UInt(32.W))
    val to_mem5   = Output(UInt(32.W))
    val addr5     = Output(UInt(32.W))
  })

  // Lots of files. The examples have monolithic files.
  addResource("/Cgra6x6BlackBox.v")
  addResource("/adres_5in_vliw.v")
  addResource("/adres_6in_vliw.v")
  addResource("/const_32b.v")
  addResource("/func_32b_add_multiply_sub_divide_and_or_xor_shl_ashr_lshr.v")
  addResource("/ConfigCell.v")
  addResource("/io_32b.v")
  // addResource("/memUnit_32b.v")
  addResource("/memoryPort_6connect_32b.v")
  addResource("/mux_2to1_32b.v")
  // addResource("/mux_4to1_32b.v")
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
  addResource("/registerFile_6in_12out_32b.v")
  addResource("/register_32b.v")
  addResource("/tristate_32b.v")
}

class streamDouble6x6cgraAccel(opcodes: OpcodeSet)(implicit p: Parameters) extends LazyRoCC(
    opcodes = opcodes, nPTWPorts = 0) {
    override lazy val module = new streamDouble6x6cgraAccelImp(this)
}

class streamDouble6x6cgraAccelImp(outer: streamDouble6x6cgraAccel)(implicit p: Parameters) extends LazyRoCCModuleImp(outer){
  
  val streamDoublecgra6x6bb = Module(new Cgra6x6BlackBox)
  val ctrl      = Module(new CtrlBBModule)
  val cmd       = Queue(io.cmd)

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
  // streamDoublecgra6x6bb.io.CGRA_Clock    := clock
  streamDoublecgra6x6bb.io.CGRA_Clock   := ctrl.io.CGRA_Clock
  streamDoublecgra6x6bb.io.Config_Clock := ctrl.io.Config_Clock
  streamDoublecgra6x6bb.io.CGRA_Reset   := ctrl.io.CGRA_Reset
  streamDoublecgra6x6bb.io.Config_Reset := ctrl.io.Config_Reset
  streamDoublecgra6x6bb.io.ConfigIn     := ctrl.io.cgra_Inconfig
  ctrl.io.cgra_Outconfig    := streamDoublecgra6x6bb.io.ConfigOut
  ctrl.io.from_cgra0        <> streamDoublecgra6x6bb.io.dataOut0
  ctrl.io.from_cgra1        <> streamDoublecgra6x6bb.io.dataOut1
  ctrl.io.from_cgra2        <> streamDoublecgra6x6bb.io.dataOut2
  ctrl.io.from_cgra3        <> streamDoublecgra6x6bb.io.dataOut3
  ctrl.io.from_cgra4        <> streamDoublecgra6x6bb.io.dataOut4
  ctrl.io.from_cgra5        <> streamDoublecgra6x6bb.io.dataOut5
  streamDoublecgra6x6bb.io.dataIn0      := ctrl.io.to_cgra0
  streamDoublecgra6x6bb.io.dataIn1      := ctrl.io.to_cgra1
  streamDoublecgra6x6bb.io.dataIn2      := ctrl.io.to_cgra2
  streamDoublecgra6x6bb.io.dataIn3      := ctrl.io.to_cgra3
  streamDoublecgra6x6bb.io.dataIn4      := ctrl.io.to_cgra4
  streamDoublecgra6x6bb.io.dataIn5      := ctrl.io.to_cgra5
  streamDoublecgra6x6bb.io.write0       := ctrl.io.write0
  streamDoublecgra6x6bb.io.write1       := ctrl.io.write1
  streamDoublecgra6x6bb.io.write2       := ctrl.io.write2
  streamDoublecgra6x6bb.io.write3       := ctrl.io.write3
  streamDoublecgra6x6bb.io.write4       := ctrl.io.write4
  streamDoublecgra6x6bb.io.write5       := ctrl.io.write5

  ctrl.io.write_rq0       := streamDoublecgra6x6bb.io.write_rq0
  ctrl.io.write_rq1       := streamDoublecgra6x6bb.io.write_rq1
  ctrl.io.write_rq2       := streamDoublecgra6x6bb.io.write_rq2
  ctrl.io.write_rq3       := streamDoublecgra6x6bb.io.write_rq3
  ctrl.io.write_rq4       := streamDoublecgra6x6bb.io.write_rq4
  ctrl.io.write_rq5       := streamDoublecgra6x6bb.io.write_rq5
  ctrl.io.to_mem0         := streamDoublecgra6x6bb.io.to_mem0
  ctrl.io.to_mem1         := streamDoublecgra6x6bb.io.to_mem1
  ctrl.io.to_mem2         := streamDoublecgra6x6bb.io.to_mem2
  ctrl.io.to_mem3         := streamDoublecgra6x6bb.io.to_mem3
  ctrl.io.to_mem4         := streamDoublecgra6x6bb.io.to_mem4
  ctrl.io.to_mem5         := streamDoublecgra6x6bb.io.to_mem5
  ctrl.io.addr0           := streamDoublecgra6x6bb.io.addr0
  ctrl.io.addr1           := streamDoublecgra6x6bb.io.addr1
  ctrl.io.addr2           := streamDoublecgra6x6bb.io.addr2
  ctrl.io.addr3           := streamDoublecgra6x6bb.io.addr3
  ctrl.io.addr4           := streamDoublecgra6x6bb.io.addr4
  ctrl.io.addr5           := streamDoublecgra6x6bb.io.addr5
  streamDoublecgra6x6bb.io.from_mem0  := ctrl.io.from_mem0
  streamDoublecgra6x6bb.io.from_mem1  := ctrl.io.from_mem1
  streamDoublecgra6x6bb.io.from_mem2  := ctrl.io.from_mem2
  streamDoublecgra6x6bb.io.from_mem3  := ctrl.io.from_mem3
  streamDoublecgra6x6bb.io.from_mem4  := ctrl.io.from_mem4
  streamDoublecgra6x6bb.io.from_mem5  := ctrl.io.from_mem5

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

class WithstreamDouble6x6cgraAccel extends Config ((site, here, up) =>{
    case Cgra6x6BlackBox => true
    case BuildRoCC      => up(BuildRoCC) ++ Seq(
    (p: Parameters) => {
      val streamDouble6x6cgra = LazyModule.apply(new streamDouble6x6cgraAccel(OpcodeSet.custom0)(p))
      streamDouble6x6cgra
    }
  )
})

class WithCgra6x6BlackBox extends Config((site, here, up) => {
  case Cgra6x6BlackBox => true
  // case streamDouble6x6cgraTLB => None // Do not use the more correct DmemModule when blackboxing
}) 
