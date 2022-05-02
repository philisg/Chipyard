package stream6x6cgra

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
    val CGRA_Reset        = Bool(OUTPUT)
    val Config_Reset      = Bool(OUTPUT)
    val Config_Clock      = Bool(OUTPUT)
    val cgra_Inconfig     = Bits(OUTPUT)
    val cgra_Outconfig    = Bool(INPUT)
    val write0            = Bits(OUTPUT)
    val write1            = Bits(OUTPUT)
    val write2            = Bits(OUTPUT)
    val write3            = Bits(OUTPUT)
    val write4            = Bits(OUTPUT)
    val write5            = Bits(OUTPUT)
    val from_cgra0        = Bits(INPUT, 32)
    val from_cgra1        = Bits(INPUT, 32)
    val from_cgra2        = Bits(INPUT, 32)
    val from_cgra3        = Bits(INPUT, 32)
    val from_cgra4        = Bits(INPUT, 32)
    val from_cgra5        = Bits(INPUT, 32)
    val to_cgra0          = Bits(OUTPUT,32)
    val to_cgra1          = Bits(OUTPUT,32)
    val to_cgra2          = Bits(OUTPUT,32)
    val to_cgra3          = Bits(OUTPUT,32)
    val to_cgra4          = Bits(OUTPUT,32)
    val to_cgra5          = Bits(OUTPUT,32)

    val write_rq0         = Bool(INPUT)
    val from_mem0         = Bits(OUTPUT,32)
    val to_mem0           = Bits(INPUT, 32)
    val addr0             = Bits(INPUT, 32)

    val write_rq1         = Bool(INPUT)
    val from_mem1         = Bits(OUTPUT,32)
    val to_mem1           = Bits(INPUT, 32)
    val addr1             = Bits(INPUT, 32)

    val write_rq2         = Bool(INPUT)
    val from_mem2         = Bits(OUTPUT,32)
    val to_mem2           = Bits(INPUT, 32)
    val addr2             = Bits(INPUT, 32)

    val write_rq3         = Bool(INPUT)
    val from_mem3         = Bits(OUTPUT,32)
    val to_mem3           = Bits(INPUT, 32)
    val addr3             = Bits(INPUT, 32)

    val write_rq4         = Bool(INPUT)
    val from_mem4         = Bits(OUTPUT,32)
    val to_mem4           = Bits(INPUT, 32)
    val addr4             = Bits(INPUT, 32)

    val write_rq5         = Bool(INPUT)
    val from_mem5         = Bits(OUTPUT,32)
    val to_mem5           = Bits(INPUT, 32)
    val addr5             = Bits(INPUT, 32)
  }
  //RoCC HANDLER
  //rocc pipe state
  val r_idle :: r_eat_addr :: r_eat_len :: Nil = Enum(UInt(), 3)
  val rocc_s = Reg(init=r_idle)

  //Config states
  val s_idle :: s_CGRA_config :: s_wait_for_correct_output :: s_finished :: Nil = Enum(UInt(), 4)
  val state   = Reg(init = s_idle)

  //CGRA control
  val c_idle :: c_get_address :: c_fetch_data :: c_data_to_cgra :: Nil = Enum(UInt(), 4)
  val cgra_state  = Reg(init = c_idle)

  //memory handler
  val m_idle :: m_write_output :: Nil = Enum(UInt(), 2)
  val mem_s = Reg(init=m_idle)

  val input_len         = Reg(init = Vec.fill(3) { 0.U(64.W) })
  val output_data       = Reg(init = UInt(0,64))
  val busy              = Reg(init = Bool(false))
  val interrupt         = Reg(init = Bool(false))

  val cgra_config       = Reg(init = Vec.fill(31) { 0.U(64.W) })
  val config_clock_en   = Reg(init = Bool(false))
  val cgra_clock_en     = Reg(init = Bool(false))

  val mem_resp_val_reg  = Reg(next = io.mem_resp_val)
  val mem_resp_tag_reg  = Reg(next = io.mem_resp_tag)

  val i                 = Reg(init = Bits(0,3)) 
  val j                 = Reg(init = Bits(0,32)) 
  val k                 = Reg(init = Bits(0,32)) 
  val received_vec      = Reg(init = Bits(0,32))
  val clock_reg         = Reg(init = Bool(false))
  val sampling_clock    = Reg(init = Bool(false))
  val output_adress     = Reg(init = UInt(0,39))
  val input1_adress     = Reg(init = UInt(0,39))
  val input2_adress     = Reg(init = UInt(0,39))
  val has_output        = Reg(init = Bool(false))
  val has_input1        = Reg(init = Bool(false))
  val has_input2        = Reg(init = Bool(false))
  val address_counter   = Reg(init = Bits(0,32))
  val sampling_en       = Reg(init = Bool(false))
  val output_counter    = Reg(init = Bits(0,32))
  val last_address      = Reg(init = UInt(0,39))
  val tag               = Reg(init = Bits(0,5))
  val done_calculating  = Reg(init = Bool(false))
  
  //default
  io.CGRA_Reset         := false.B
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
  io.to_cgra2           := UInt(0)
  io.to_cgra3           := UInt(0)
  io.to_cgra4           := UInt(0)
  io.to_cgra5           := UInt(0)
  io.write0             := UInt(0)
  io.write1             := UInt(0)
  io.write2             := UInt(0)
  io.write3             := UInt(0)
  io.write4             := UInt(0)
  io.write5             := UInt(0)


  clock_reg             := !clock_reg
  io.Config_Clock       := Mux(config_clock_en, clock_reg   , Bool(false))
  io.CGRA_Clock         := Mux(cgra_clock_en  , clock.asBool, Bool(false))

  when(clock_reg){
    sampling_clock  := !sampling_clock
  }
  /* instruction		    roccinst	src1		      src2	          dst	  custom-N
  configure			        0			    config        config	        -	    0
  one input&output	    0			    src1(O)		    src2(O)	        -	    1 output = src2, input = src1
  input #2			        0			    src2(I)		    -               -     2 (Used when we have two inputs)
  input length #1	      0			    src1(lenI1)	  0         	    -     3


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
          when(received_vec===UInt(28)){
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
          cgra_state                      := c_get_address
          done_calculating                := false.B
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
      when(j === UInt(29) && k === UInt(32)){
        state           := s_finished
        mem_s           := m_idle
        config_clock_en := false.B
        cgra_clock_en   := false.B
      }
    }
    is{s_finished}{
      k                 := 0
      j                 := 0
      busy              := false.B
      state             := s_idle
    }
  } //end state
  val memory_data = Reg(init = UInt(0,32))
  io.from_mem0    := memory_data

  switch(cgra_state){
    is(c_idle){
      output_counter  := 0
      address_counter := 0
    }
    is(c_get_address){
      when(io.addr0 =/= last_address && address_counter =/= input_len(1)){
        address_counter := address_counter + 1
        io.mem_req_val  := true.B
        io.mem_req_addr := input1_adress.asUInt + io.addr0.asUInt - 4
        io.mem_req_tag  := tag
        io.mem_req_cmd  := M_XRD
        io.mem_req_size := log2Ceil(32).U
        cgra_clock_en   := false.B
        when(io.mem_req_rdy){
          cgra_state    := c_fetch_data
          last_address  :=  io.addr0
        }
      }
    }
    is(c_fetch_data){
      when(io.mem_resp_val && io.mem_resp_tag === tag){
        io.from_mem0    := io.mem_resp_data
        memory_data     := io.mem_resp_data
        cgra_clock_en   := true.B
        cgra_state      := c_get_address
        tag             := tag + 1 
      }
    }
  }
  
  when((io.from_cgra5 =/= output_data) && !done_calculating){
    sampling_en   := true.B
  }
  when((sampling_en && io.addr0 =/= last_address && (output_counter =/= input_len(1)))){
    output_counter  := output_counter + 1
    output_data     := io.from_cgra5
  }.elsewhen(sampling_en && (output_counter === input_len(1))){
    when(has_output){
      mem_s       := m_write_output
    }
    cgra_state    := c_idle
    sampling_en   := false.B
    cgra_clock_en := false.B
  }
  

  //Memory handler
  switch(mem_s){
    is(m_idle){

    }
    is(m_write_output){
      busy            := true.B
      io.mem_req_val  := true.B
      io.mem_req_addr := output_adress.asUInt
      io.mem_req_tag  := 10
      io.mem_req_cmd  := M_XWR
      io.mem_req_data := output_data.asUInt << 32
      io.mem_req_size := log2Ceil(32).U
      when(io.mem_resp_val && io.mem_resp_tag === 10){
        mem_s             := m_idle
        cgra_clock_en     := true.B
        busy              := false.B
        done_calculating  := true.B
      }.otherwise{
        mem_s             := m_write_output
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
