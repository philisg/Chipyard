package cgra2x2

import Chisel._
import scala.collection.mutable.ArrayBuffer
import scala.util.Random
import Chisel.ImplicitConversions._
import freechips.rocketchip.tile.HasCoreParameters
import freechips.rocketchip.rocket.constants.MemoryOpConstants
import freechips.rocketchip.config._

class CtrlModule(val W: Int, val S: Int)(implicit val p: Parameters) extends Module
  with HasCoreParameters
  with MemoryOpConstants {

  val r = 2*256
  val c = 25*W - r
  val round_size_words = c/W
  val rounds = 24 //12 + 2l
  val bytes_per_word = W/8

  val io = new Bundle {
    val rocc_req_val      = Bool(INPUT)
    val rocc_req_rdy      = Bool(OUTPUT)
    val rocc_funct        = Bits(INPUT, 2)
    val rocc_rs1          = Bits(INPUT, 64)
    val rocc_rs2          = Bits(INPUT, 64)
    val rocc_rd           = Bits(INPUT, 5)

    val rocc_resp_data    = Bits(OUTPUT,64)
    val rocc_resp_rd      = Bits(OUTPUT,5)
    val rocc_resp_val     = Bool(OUTPUT)
    val rocc_resp_rdy     = Bool(INPUT)

    val busy              = Bool(OUTPUT)
    val interrupt         = Bool(OUTPUT)

    val mem_req_val      = Bool(OUTPUT)
    val mem_req_rdy      = Bool(INPUT)
    val mem_req_tag      = Bits(OUTPUT, coreParams.dcacheReqTagBits)
    val mem_req_addr     = Bits(OUTPUT, coreMaxAddrBits)
    val mem_req_cmd      = Bits(OUTPUT, M_SZ)
    val mem_req_size     = Bits(OUTPUT, log2Ceil(coreDataBytes + 1))
    val mem_req_data     = Bits(OUTPUT, 64)

    val mem_resp_val     = Bool(INPUT)
    val mem_resp_tag     = Bits(INPUT, 7)
    val mem_resp_data    = Bits(INPUT, 64)

  }

  //RoCC HANDLER
  //rocc pipe state
  val r_idle :: r_eat_addr :: r_eat_len :: Nil = Enum(UInt(), 3)
  val rocc_s = Reg(init=r_idle)

  //Calcuation states
  val s_idle :: s_Calculate :: s_write :: s_finished :: Nil = Enum(UInt(), 4)
  val state   = Reg(init = s_idle)

  val m_idle :: m_read :: m_wait :: m_write :: Nil = Enum(UInt(), 4)
  val mem_s = Reg(init=m_idle)

  val rs1_addr          = Reg(init = UInt(0,64))
  val rs2_addr          = Reg(init = UInt(0,64))
  val rd_addr           = Reg(init = UInt(0,64))
  val rs1_data          = Reg(init = UInt(0,64))
  val rs2_data          = Reg(init = UInt(0,64))
  val rd_data           = Reg(init = UInt(0,64))

  val busy              = Reg(init=Bool(false))
  val interrupt         = Reg(init=Bool(false))

  val rocc_req_val_reg  = Reg(next=io.rocc_req_val)
  val rocc_funct_reg    = Reg(init = Bits(0,2))
  rocc_funct_reg        := io.rocc_funct
  val rocc_rs1_reg      = Reg(next=io.rocc_rs1)
  val rocc_rs2_reg      = Reg(next=io.rocc_rs2)
  val rocc_rd_reg       = Reg(next=io.rocc_rd)

  val data_valid        = Reg(init=Vec.fill(3) { Bool(false) })
  val data_buffer       = Reg(init=Vec.fill(3) { 0.U(64.W) })
  val rs1_data_valid    = Reg(init=Bool(false))
  val rs2_data_valid    = Reg(init=Bool(false))
  val rd_data_valid     = Reg(init=Bool(false))

  val mem_resp_val_reg = Reg(next=io.mem_resp_val)
  val mem_resp_tag_reg = Reg(next=io.mem_resp_tag)

  //default
  io.rocc_req_rdy     := Bool(false)
  io.interrupt        := Bool(false)
  io.busy             := busy
  io.mem_req_val      := Bool(false)
  io.mem_req_tag      := UInt(0)
  io.mem_req_addr     := Bits(0, 64)
  io.mem_req_cmd      := M_XRD
  io.mem_req_size     := log2Ceil(64).U
  io.mem_req_data     := UInt(55)

  switch(rocc_s) {
  is(r_idle) {
    io.rocc_req_rdy := !busy
    when(io.rocc_req_val && !busy){
      when(io.rocc_funct === UInt(0)){ //function = 0 Data addresses 
        io.rocc_req_rdy := Bool(true)
        rs1_addr        := io.rocc_rs1
        rs2_addr        := io.rocc_rs2
        rd_addr         := io.rocc_rd
        io.busy         := Bool(false)
        busy            := Bool(false)
      }.elsewhen(io.rocc_funct === UInt(1)){ //function = 1 destination address/ start accelerator
        io.rocc_req_rdy := Bool(true)
        rd_addr         := io.rocc_rs1
        busy            := Bool(true)
      }
    }
  }
  }

  switch(state){
    is(s_idle){
      
    }
    is(s_Calculate){
      rd_data       := rs1_data + rs2_data
      rd_data_valid := Bool(true)
      data_buffer(2):= data_buffer(0) + data_buffer(1) //
      data_valid(2) := Bool(true) //
      state         := s_write
    }
    is(s_write){
      io.mem_req_val  := Bool(true)
      io.mem_req_addr := rd_addr
      io.mem_req_tag  := UInt(4)
      io.mem_req_cmd  := M_XWR
      io.mem_req_data := data_buffer(2) << 32 //
      // io.mem_req_data := rd_data
      io.mem_req_size := log2Ceil(64).U

      when(mem_resp_val_reg && io.mem_req_rdy){
        mem_s         := m_idle
        state         := s_finished
      }.otherwise{
        state         := s_write
      }
    }
    is{s_finished}{
      io.mem_req_val    := Bool(false)
      busy              := Bool(false)
      io.rocc_resp_val  := Bool(false)
      rs1_data          := UInt(0)
      rs2_data          := UInt(0)
      rd_data           := UInt(0)
      rs1_data_valid    := Bool(false)
      rs2_data_valid    := Bool(false)
      rd_data_valid     := Bool(false)
      data_buffer       := Vec.fill(3){0.U(64.W)} //
      data_valid        := Vec.fill(3){Bool(false)} //
      rocc_s            := r_idle
      state             := s_idle
      
    }
  }
  

  //Memory handler
  switch(mem_s){
    is(m_idle){
      // val canRead = busy && (!rs1_data_valid || !rs2_data_valid)
      val canRead = busy && !data_valid.reduce(_&&_)
      when(canRead){
        mem_s         := m_read
      }.otherwise{
        mem_s         := m_idle
      }
    }
    is(m_read){
      when(state =/= s_write){
        // when(!rs1_data_valid){
        when(!data_valid(0)){
          io.mem_req_val  := Bool(true)
          io.mem_req_addr := rs1_addr
          io.mem_req_tag  := UInt(1)
          io.mem_req_cmd  := M_XRD
          io.mem_req_size := log2Ceil(64).U
        // }.elsewhen(!rs2_data_valid){
        }.elsewhen(!data_valid(1)){
          io.mem_req_val  := Bool(true)
          io.mem_req_addr := rs2_addr
          io.mem_req_tag  := UInt(2)
          io.mem_req_cmd  := M_XRD
          io.mem_req_size := log2Ceil(64).U
        }.otherwise{
          io.mem_req_val  := Bool(true)
          io.mem_req_addr := rd_addr
          io.mem_req_tag  := UInt(3)
          io.mem_req_cmd  := M_XRD
          io.mem_req_size := log2Ceil(64).U
        }

        when(io.mem_req_rdy){
          mem_s   := m_wait
        }.otherwise{
          mem_s   := m_read
        }
      }.otherwise{
        mem_s := m_read
      }
    }
    is(m_wait){
      when(io.mem_resp_val){

        // when(io.mem_resp_tag === UInt(3)){
        //   mem_s :=  m_idle
        //   state := s_Calculate
        // }
        // when(io.mem_resp_tag === UInt(1)){
        //   rs1_data        := io.mem_resp_data
        //   rs1_data_valid  := Bool(true)
        //   mem_s           := m_read
        // }.elsewhen(io.mem_resp_tag === UInt(2)){
        //   rs2_data        := io.mem_resp_data
        //   rs2_data_valid  := Bool(true)
        //   mem_s           := m_read
        // }.elsewhen(io.mem_resp_tag === UInt(3)){
        //   rd_data         := io.mem_resp_data
        //   mem_s           := m_idle
        //   state           := s_Calculate
        // }
        data_buffer(io.mem_resp_tag-1)  := io.mem_resp_data
        data_valid(io.mem_resp_tag-1)   := Bool(true)
        when(data_valid.reduce(_&&_)){
          state       := s_Calculate
          mem_s       := m_idle
        }.otherwise{
          mem_s       := m_read
        }
      }
    }
    is(m_write){
      
      // when(io.rocc_resp_rdy){ //Send data back to CPU
      //   io.rocc_resp_rd     := rd_addr
      //   io.rocc_resp_data   := rd_data
      //   io.rocc_resp_val    := Bool(true)
      //   mem_s               := m_idle 
      // }
      // when(rd_data_valid){ //Send data to memory
      
      
    }
  }
}
