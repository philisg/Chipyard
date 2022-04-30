package cgra2x2

import Chisel._
import scala.collection.mutable.ArrayBuffer
import scala.util.Random
import Chisel.ImplicitConversions._
import freechips.rocketchip.tile.HasCoreParameters
import freechips.rocketchip.rocket.constants.MemoryOpConstants
import freechips.rocketchip.config._

class CtrlBBModule(implicit val p: Parameters) extends Module
  with HasCoreParameters
  with MemoryOpConstants {

  val arraySize = 2
  val bufferSize = 110

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

    val mem_req_val       = Bool(OUTPUT)
    val mem_req_rdy       = Bool(INPUT)
    val mem_req_tag       = Bits(OUTPUT, coreParams.dcacheReqTagBits)
    val mem_req_addr      = Bits(OUTPUT, coreMaxAddrBits)
    val mem_req_cmd       = Bits(OUTPUT, M_SZ)
    val mem_req_size      = Bits(OUTPUT, log2Ceil(coreDataBytes + 1))
    val mem_req_data      = Bits(OUTPUT, 64)

    val mem_resp_val      = Bool(INPUT)
    val mem_resp_tag      = Bits(INPUT, 7)
    val mem_resp_data     = Bits(INPUT, 64)

    //CGRA specific -------------------------
    val CGRA_Clock        = Bool(OUTPUT)
    val Config_Reset      = Bool(OUTPUT)
    val Config_Clock      = Bool(OUTPUT)
    val cgra_Inconfig     = Bits(OUTPUT)
    val cgra_Outconfig    = Bool(INPUT)
    val write0            = Bits(OUTPUT)
    val write1            = Bits(OUTPUT)
    val from_cgra0        = Bits(INPUT, 32)
    val from_cgra1        = Bits(INPUT, 32)
    val to_cgra0          = Bits(OUTPUT,32)
    val to_cgra1          = Bits(OUTPUT,32)

    val write_rq0         = Bool(INPUT)
    val from_mem0         = Bits(OUTPUT,32)
    val to_mem0           = Bits(INPUT, 32)
    val addr0             = Bits(INPUT, 32)

    val write_rq1         = Bool(INPUT)
    val from_mem1         = Bits(OUTPUT,32)
    val to_mem1           = Bits(INPUT, 32)
    val addr1             = Bits(INPUT, 32)
  }
  //RoCC HANDLER
  //rocc pipe state
  val r_idle :: r_eat_addr :: r_eat_len :: Nil = Enum(UInt(), 3)
  val rocc_s = Reg(init=r_idle)

  //Config states
  val s_idle :: s_CGRA_config :: s_wait_for_correct_output :: s_finished :: Nil = Enum(UInt(), 4)
  val state   = Reg(init = s_idle)

  //memory handler
  val m_idle :: m_accum_address :: m_accum_data :: m_receive_data_from_mem :: m_send_data_to_cgra :: m_read_CGRA :: m_wait_CGRA :: m_write_CGRA :: m_write_output :: m_write :: Nil = Enum(UInt(), 10)
  val mem_s = Reg(init=m_idle)

  val rs1_addr          = Reg(init = UInt(0,64))
  val rs2_addr          = Reg(init = UInt(0,64))
  val rd_addr           = Reg(init = UInt(0,64))
  val addr_buffer       = Reg(init = Vec.fill(3) { 0.U(39.W) })
  val input_len         = Reg(init = Vec.fill(3) { 0.U(64.W) })
  val data              = Reg(init = Vec.fill(3) { 0.U(64.W) })
  val output_data       = Reg(init = UInt(0,64))
  val busy              = Reg(init = Bool(false))
  val interrupt         = Reg(init = Bool(false))
  val rocc_req_val_reg  = Reg(next = io.rocc_req_val)
  val rocc_funct_reg    = Reg(init = Bits(0,2))
  rocc_funct_reg        := io.rocc_funct
  val rocc_rs1_reg      = Reg(next = io.rocc_rs1)
  val rocc_rs2_reg      = Reg(next = io.rocc_rs2)
  val rocc_rd_reg       = Reg(next = io.rocc_rd)

  val adress_valid      = Reg(init = Vec.fill(arraySize)  { Bool(false) })
  val cgra_config       = Reg(init = Vec.fill(14) { 0.U(64.W) })
  val config_clock_en   = Reg(init = Bool(false))
  val cgra_clock_en     = Reg(init = Bool(false))

  val mem_resp_val_reg  = Reg(next = io.mem_resp_val)
  val mem_resp_tag_reg  = Reg(next = io.mem_resp_tag)

  val data_from_cgra    = Reg(init = Vec.fill(arraySize) { 0.U(32.W) })
  val i                 = Reg(init = Bits(0,3)) 
  val j                 = Reg(init = Bits(0,32)) 
  val k                 = Reg(init = Bits(0,32)) 
  val received_vec      = Reg(init = Bits(0,32))
  val clock_reg         = Reg(init = Bool(false))
  val last_req_address  = Reg(init = Vec.fill(arraySize) { 0.U(39.W) })
  val write_rq_vec      = Reg(init = Vec.fill(arraySize) { Bool(false)})
  val data_from_memory  = Reg(init = Vec.fill(arraySize) { 0.U(32.W) })
  val data_to_memory    = Reg(init = Vec.fill(arraySize) { 0.U(32.W) })
  val memory_addr       = Reg(init = Vec.fill(arraySize) { 0.U(39.W) })
  val request_addr      = Reg(init = UInt(0,39))
  val output_adress     = Reg(init = UInt(0,39))
  val input1_adress     = Reg(init = UInt(0,39))
  val input2_adress     = Reg(init = UInt(0,39))
  val has_output        = Reg(init = Bool(false))
  val has_input1        = Reg(init = Bool(false))
  val has_input2        = Reg(init = Bool(false))
  val address_counter   = Reg(init = Bits(0,32))
  val sampling_en = Reg(init = Bool(false))
  val output_counter    = Reg(init = Bits(0,32))

  //default

  io.Config_Reset       := false.B
  config_clock_en       := false.B
  io.rocc_req_rdy       := false.B
  io.rocc_resp_val      := false.B
  io.interrupt          := false.B                                                                                                                     
  io.busy               := busy
  io.mem_req_val        := false.B
  io.mem_req_tag        := UInt(0)
  io.mem_req_addr       := Bits(0, 64)
  io.mem_req_cmd        := M_XRD
  io.mem_req_size       := log2Ceil(32).U
  io.mem_req_data       := UInt(0)
  io.cgra_Inconfig      := UInt(0)

  io.to_cgra0           := UInt(0)
  io.to_cgra1           := UInt(0)
  io.write0             := UInt(0)
  io.write1             := UInt(0)

  write_rq_vec(0)       := io.write_rq0
  write_rq_vec(1)       := io.write_rq1
  io.from_mem0          := data_from_memory(0)
  io.from_mem1          := data_from_memory(1)
  data_to_memory(0)     := io.to_mem0
  data_to_memory(1)     := io.to_mem1
  memory_addr(0)        := io.addr0
  memory_addr(1)        := io.addr1
  clock_reg             := !clock_reg
  io.Config_Clock       := Mux(config_clock_en, clock_reg   , Bool(false))
  io.CGRA_Clock         := Mux(cgra_clock_en  , clock.asBool, Bool(false))

  adress_valid(0) := io.addr0 =/= last_req_address(0)
  adress_valid(1) := io.addr1 =/= last_req_address(1)


  when(adress_valid(0)){
    i := 0
  }.elsewhen(adress_valid(1)){
    i := 1
  }


  /*  instruction		    roccinst	src1		      src2	        dst	  custom-N
      configure			    0			    config        config	      -	    0
      one input&output	0			    src1(O)		    src2(O)	      -	    1 output = src2, input = src1
      input #2			    0			    src2(I)		    -             -     2 (Used when we have two inputs)
      input length #1	  0			    src1(lenI1)	  0         	  -     3


  * ROCC_INSTRUCTION_SS(0,src1,src2, instruction)
  * configure: configure the CGRA with a mapping (`busy` while configuring)
  * one input: when the configuration demands a single input (pointer)
  * two inputs: setup two inputs for the configuration
  * input length: we tell the CGRA how long the input is, so that it knows
                  when it's done.
          (`busy` and starts the computation, `!busy` when completed)
  It's based on the following roccinst+opcode->RISC-V mapping:
  funct7		rs2		rs1		xd	xs1	xs2	rd	opcode
  roccinst	src2	src1				dst	custom-0/1/2/3 */

  switch(rocc_s) {
    is(r_idle) {
      io.rocc_req_rdy := !busy
      when(io.rocc_req_val && !busy){
        when(io.rocc_funct === UInt(0)){ //Config
          io.rocc_req_rdy             := true.B
          cgra_config(received_vec)   := Reverse(io.rocc_rs1)
          cgra_config(received_vec+1) := Reverse(io.rocc_rs2)
          received_vec                := received_vec + 2
          when(received_vec===UInt(2)){
            state         := s_CGRA_config
            busy          := true.B
            received_vec  := 0
          }
        }.elsewhen(io.rocc_funct === UInt(1)){ //output/input 
          io.rocc_req_rdy                 := true.B
          input1_adress                   := io.rocc_rs1 
          has_input1                      := io.rocc_rs1 =/= 0 //True if not 0
          output_adress                   := io.rocc_rs2
          has_output                      := io.rocc_rs2 =/= 0
          busy                            := false.B
        }.elsewhen(io.rocc_funct === UInt(2)){ //input#2
          io.rocc_req_rdy                 := true.B
          busy                            := false.B
          input2_adress                   := io.rocc_rs1
          has_input2                      := io.rocc_rs1 =/= 0 
        }.elsewhen(io.rocc_funct === UInt(3)){ //input length #2|#1  src2(lenI2)	src1(lenI1)
          io.rocc_req_rdy                 := true.B
          busy                            := true.B
          input_len(1)                    := io.rocc_rs1 //use only this as we just need to know how long we will run (iterations)
          input_len(2)                    := io.rocc_rs2
          cgra_clock_en                   := true.B
          mem_s                           := m_accum_address
        }
      }
    }
  }//end rocc_s

  switch(state){
    is(s_idle){
    }
    is(s_CGRA_config){
      busy            := true.B
      config_clock_en := true.B
      cgra_clock_en   := true.B
      
      when(clock_reg){
        io.cgra_Inconfig  := cgra_config(j)(k)
        k := k + 1
      }
      when(k === UInt(64)){
        k := 0
        j := j + 1
      }
      when(j === UInt(3) && k === UInt(32)){
        state           := s_finished
        // mem_s           := m_accum_address
        mem_s           := m_idle
        config_clock_en := false.B
        cgra_clock_en   := false.B
      }
    }
    is(s_wait_for_correct_output){
      when(io.from_cgra1 =/= output_data){
        sampling_en := true.B
      }
      when(sampling_en && clock_reg && (output_counter =/= address_counter)){
        output_counter  := output_counter + 1
        output_data     := io.from_cgra1
      }.elsewhen(output_counter === address_counter){
        cgra_clock_en   := false.B
        sampling_en     := false.B
        when(mem_s === (m_idle | m_send_data_to_cgra)){
          mem_s             := m_write_output
          has_output        := false.B
        }
        when(mem_s === m_write_output){
          state             := s_finished
        }
      }
    }
    is{s_finished}{
      k                 := 0
      j                 := 0
      busy              := false.B
      state             := s_idle
    }
    
  } //end state
  
  val request_adress_vec  = Reg(init = Vec.fill(bufferSize) { 0.U(39.W) })
  val data_vec            = Reg(init = Vec.fill(bufferSize) { 0.U(32.W) })
  val data_counter        = Reg(init = Bits(0,32))
  val receive_counter     = Reg(init = Bits(0,32))
  val send_counter        = Reg(init = Bits(0,32))
  val last_address        = Reg(init = UInt(0,39))


  //Memory handler
  when(io.mem_resp_val && ((mem_s === m_accum_data) || (mem_s === m_receive_data_from_mem))){
    data_vec(receive_counter) := io.mem_resp_data
    receive_counter := receive_counter + 1
  }

  switch(mem_s){
    is(m_accum_address){
      busy          := true.B
      cgra_clock_en := true.B
      when(io.addr1 =/= last_address && io.addr1 > "hffff0000".U){
        request_adress_vec(address_counter) := ("h3f".U << 32) | io.addr1.asUInt
        last_address    := io.addr1
        address_counter := address_counter + 1
      }.elsewhen(io.addr1 =/= last_address && io.addr1 < "hf0000".U){
        when(has_input1){
          request_adress_vec(address_counter) := input1_adress.asUInt + io.addr1.asUInt - 4         
        }.otherwise{
          request_adress_vec(address_counter) := io.addr1
        }
        last_address    := io.addr1
        address_counter := address_counter + 1
      }
      when(address_counter === input_len(1)){
        mem_s             := m_accum_data
        cgra_clock_en     := false.B
        receive_counter   := 0
        data_counter      := 0
      }
    }
    is(m_accum_data){
      io.mem_req_val  := (request_adress_vec(data_counter) =/= "h000000000".U)
      io.mem_req_addr := request_adress_vec(data_counter).asUInt //address from CGRA is only 32-bit
      io.mem_req_tag  := data_counter(4,0)
      io.mem_req_cmd  := M_XRD
      io.mem_req_size := log2Ceil(32).U
      when(io.mem_req_rdy){
        data_counter  := data_counter + 1
        mem_s         := m_accum_data
        when(data_counter === address_counter){
          mem_s       := m_receive_data_from_mem
          data_counter:= 0
        }
      }
    }
    is(m_receive_data_from_mem){
      when(receive_counter === address_counter){
        mem_s     := m_send_data_to_cgra
      }
    }
    is(m_send_data_to_cgra){
      cgra_clock_en := true.B
      when(has_output){
        state       := s_wait_for_correct_output
      }
      when(clock_reg){ //for every 2nd clock cycle
        data_from_memory(0) := data_vec(send_counter)
        data_from_memory(1) := data_vec(send_counter)
        send_counter        := send_counter + 1
      }
      when(send_counter === address_counter + 2){
        when(!has_output){
          mem_s         := m_idle
        }
        send_counter  := 0
        // busy          := false.B
      }
    }
    is(m_idle){
      when(state =/= s_CGRA_config){// Should not fetch when configuring cgra
        //Check if we want to write, that there is a new write and that address is within range
        when((write_rq_vec(i) === true.B) && (last_req_address(i) =/= memory_addr(i)) && (memory_addr(i) >= "hfffff000".U)){ 
          mem_s           := m_write_CGRA
          request_addr    := memory_addr(i)
          cgra_clock_en   := false.B

        //Since not write, we want to read, chack that there is a new read and that it is whitin the memory range
        }.elsewhen(last_req_address(i) =/= memory_addr(i) && (memory_addr(i) >= "hfffff000".U)){
          request_addr    := memory_addr(i)
          // mem_s           := m_read_CGRA
          // busy            := true.B
          cgra_clock_en   := false.B

        }.otherwise{
          //wrap around when checked every register!
          when(i === UInt(arraySize-1)){
            i := 0
          }.otherwise{
            i := i + 1
          }
        }
      }
    }
    is(m_read_CGRA){
      io.mem_req_val  := true.B
      io.mem_req_addr := ("h3f".U << 32) | request_addr.asUInt //address from CGRA is only 32-bit
      io.mem_req_tag  := i
      io.mem_req_cmd  := M_XRD
      io.mem_req_size := log2Ceil(32).U
      when(io.mem_req_rdy && io.mem_req_val){
        mem_s                 := m_wait_CGRA
        last_req_address(i)   := request_addr
      }
    }
    is(m_wait_CGRA){
      when(io.mem_resp_val){
        busy                    := false.B
        cgra_clock_en           := true.B
        data_from_memory(i)     := io.mem_resp_data
        mem_s                   := m_idle
      }
    }
    is(m_write_CGRA){
      busy            := true.B
      io.mem_req_val  := true.B
      io.mem_req_addr := ("h3f".U << 32) | request_addr.asUInt //address from CGRA is only 32-bit
      io.mem_req_tag  := i
      io.mem_req_cmd  := M_XWR
      io.mem_req_data := data_to_memory(i).asUInt
      io.mem_req_size := log2Ceil(32).U
      last_req_address(i) := request_addr.asUInt

      when(io.mem_resp_val && io.mem_resp_tag === i){
        mem_s             := m_idle
        cgra_clock_en     := true.B
        busy              := false.B
      }.otherwise{
        mem_s             := m_write_CGRA
      }
    }
    is(m_write_output){
      busy            := true.B
      io.mem_req_val  := true.B
      io.mem_req_addr := output_adress.asUInt
      io.mem_req_tag  := 10
      io.mem_req_cmd  := M_XWR
      io.mem_req_data := output_data.asUInt << 32 | output_data.asUInt
      io.mem_req_size := log2Ceil(32).U
      when(io.mem_resp_val && io.mem_resp_tag === 10){
        mem_s             := m_idle
        cgra_clock_en     := true.B
        busy              := false.B
      }.otherwise{
        mem_s             := m_write_output
      }
    }
    is(m_write){
      busy            := true.B
      io.mem_req_val  := true.B
      io.mem_req_addr := rs1_addr
      io.mem_req_tag  := 11
      io.mem_req_cmd  := M_XWR
      io.mem_req_data := "hff1234567".U
      io.mem_req_size := log2Ceil(64).U
      when(io.mem_resp_val && io.mem_resp_tag === 11){
        mem_s             := m_idle
        busy              := false.B
      }.otherwise{
        mem_s             := m_write
      }
    }
  } //end mem_s
}
/* 
psuedo kode
Få config fra CPU via RoCC (Denne kan vi hardcode en se lenge?)
configurere CGRA

Få input og output pointer fra CPU via RoCC 
evt input 2 fra CPU

FÅ lengde på kalkulasjon

skrive output til minne/ sende tilbake til CPU
 */
