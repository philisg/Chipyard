package cgrame

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

  def risingedge(x: Bool) = x && !RegNext(x)

  val arraySize = 4

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
    val write             = Bits(OUTPUT)
    val from_cgra0        = Bits(INPUT, 32)
    val from_cgra1        = Bits(INPUT, 32)
    val from_cgra2        = Bits(INPUT, 32)
    val from_cgra3        = Bits(INPUT, 32)
    val to_cgra0          = Bits(OUTPUT, 32)
    val to_cgra1          = Bits(OUTPUT, 32)
    val to_cgra2          = Bits(OUTPUT, 32)
    val to_cgra3          = Bits(OUTPUT, 32)

    val write_rq0         = Bool(INPUT)
    val from_mem0         = Bits(OUTPUT,  32)
    val to_mem0           = Bits(INPUT,   32)
    val addr0             = Bits(INPUT,   32)

    val write_rq1         = Bool(INPUT)
    val from_mem1         = Bits(OUTPUT,  32)
    val to_mem1           = Bits(INPUT,   32)
    val addr1             = Bits(INPUT,   32)

    val write_rq2         = Bool(INPUT)
    val from_mem2         = Bits(OUTPUT,  32)
    val to_mem2           = Bits(INPUT,   32)
    val addr2             = Bits(INPUT,   32)

    val write_rq3         = Bool(INPUT)
    val from_mem3         = Bits(OUTPUT,  32)
    val to_mem3           = Bits(INPUT,   32)
    val addr3             = Bits(INPUT,   32)
  }
  //RoCC HANDLER
  //rocc pipe state
  val r_idle :: r_eat_addr :: r_eat_len :: Nil = Enum(UInt(), 3)
  val rocc_s = Reg(init=r_idle)

  //Config states
  val s_idle :: s_CGRA_config :: s_finished :: Nil = Enum(UInt(), 3)
  val state   = Reg(init = s_idle)

  //memory handler
  val m_idle :: m_preRead :: m_read :: m_wait :: m_write :: Nil = Enum(UInt(), 5)
  val mem_s = Reg(init=m_idle)

  val rs1_addr          = Reg(init = UInt(0,64))
  val rs2_addr          = Reg(init = UInt(0,64))
  val rd_addr           = Reg(init = UInt(0,64))
  val addr_buffer       = Reg(init = Vec.fill(3) { 0.U(64.W) })
  val data_len          = Reg(init = Vec.fill(3) { 0.U(64.W) })
  val busy              = Reg(init = Bool(false))
  val interrupt         = Reg(init = Bool(false))
  val rocc_req_val_reg  = Reg(next = io.rocc_req_val)
  val rocc_funct_reg    = Reg(init = Bits(0,2))
  rocc_funct_reg        := io.rocc_funct
  val rocc_rs1_reg      = Reg(next = io.rocc_rs1)
  val rocc_rs2_reg      = Reg(next = io.rocc_rs2)
  val rocc_rd_reg       = Reg(next = io.rocc_rd)

  val data_valid        = Reg(init = Vec.fill(3)  { Bool(false) })
  val cgra_config       = Reg(init = Vec.fill(14) { 0.U(64.W) })
  val config_clock_en   = Reg(init = Bool(false))
  val cgra_clock_en     = Reg(init = Bool(false))

  val mem_resp_val_reg  = Reg(next = io.mem_resp_val)
  val mem_resp_tag_reg  = Reg(next = io.mem_resp_tag)

  val data_from_cgra    = Reg(init = Vec.fill(arraySize) { 0.U(32.W) })
  val i                 = Reg(init = Bits(0,3))  
  val k                 = Reg(init = Bits(0,10)) 
  val j                 = Reg(init = Bits(0,10)) 
  val received_vec      = Reg(init = Bits(0,10))
  val clock_reg         = Reg(init = Bool(false))
  val last_read_address = Reg(init = Vec.fill(arraySize) { 0.U(64.W) })
  val last_write_address= Reg(init = Vec.fill(arraySize) { 0.U(64.W) })
  val write_rq_vec      = Reg(init = Vec.fill(arraySize) { Bool(false)})
  val data_from_memory  = Reg(init = Vec.fill(arraySize) { 0.U(64.W) })
  val data_to_memory    = Reg(init = Vec.fill(arraySize) { 0.U(64.W) })
  val memory_addr       = Reg(init = Vec.fill(arraySize) { 0.U(64.W) })
  //default

  io.Config_Reset       := Bool(false)     
  // io.Config_Clock     := Bool(false)
  // io.Config_Clock     := Mux(config_clk_en, clock, Bool(false))
  // configLen           := 847
  io.rocc_req_rdy       := Bool(false)
  io.interrupt          := Bool(false)                                                                                                                      
  io.busy               := busy
  io.mem_req_val        := Bool(false)
  io.mem_req_tag        := UInt(0)
  io.mem_req_addr       := Bits(0, 64)
  io.mem_req_cmd        := M_XRD
  io.mem_req_size       := log2Ceil(64).U
  io.mem_req_data       := UInt(0)
  io.cgra_Inconfig      := UInt(0)

  io.to_cgra0           := UInt(123)
  io.to_cgra1           := UInt(123)
  io.to_cgra2           := UInt(123)
  io.to_cgra3           := UInt(123)
  io.write              := UInt(0)

  write_rq_vec(0)       := io.write_rq0
  write_rq_vec(1)       := io.write_rq1
  write_rq_vec(2)       := io.write_rq2
  write_rq_vec(3)       := io.write_rq3
  io.from_mem0          := data_from_memory(0)
  io.from_mem1          := data_from_memory(1)
  io.from_mem2          := data_from_memory(2)
  io.from_mem3          := data_from_memory(3)
  data_to_memory(0)     := io.to_mem0
  data_to_memory(1)     := io.to_mem1
  data_to_memory(2)     := io.to_mem2
  data_to_memory(3)     := io.to_mem3
  memory_addr(0)        := io.addr0
  memory_addr(1)        := io.addr1
  memory_addr(2)        := io.addr2
  memory_addr(3)        := io.addr3
  clock_reg             := !clock_reg
  io.Config_Clock       := Mux(config_clock_en, clock_reg, Bool(false))
  io.CGRA_Clock         := Mux(cgra_clock_en  , clock_reg, Bool(false))

  /* instruction			roccinst	src2		    src1	      dst	  custom-N
  configure			      2					-	          config	    -	    0
  one input&output	  2			    src2(O)		  src1(I)	    -	    1
  input #2			      2					src1(I)		  -           -     2 (Used when we have two inputs)
  input length #1|#2	2			    src2(lenI2)	src1(lenI1)	-     3

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
          io.rocc_req_rdy             := Bool(true)
          cgra_config(received_vec)   := Reverse(io.rocc_rs1)
          cgra_config(received_vec+1) := Reverse(io.rocc_rs2)
          busy                        := Bool(false)
          received_vec                := received_vec + 2
          when(received_vec===UInt(12)){
            state := s_CGRA_config
            busy  := Bool(true)
          }
        }.elsewhen(io.rocc_funct === UInt(1)){ //output/input 
          io.rocc_req_rdy                 := Bool(true)
          addr_buffer(0)                  := io.rocc_rs1
          addr_buffer(2)                  := io.rocc_rs2
          busy                            := Bool(false)
        }.elsewhen(io.rocc_funct === UInt(2)){ //input#2
          io.rocc_req_rdy                 := Bool(true)
          busy                            := Bool(false)
          addr_buffer(1)                  := io.rocc_rs1
        }.elsewhen(io.rocc_funct === UInt(3)){ //input length #1|#2  src2(lenI2)	src1(lenI1)
          io.rocc_req_rdy                 := Bool(true)
          busy                            := Bool(false)
          data_len(0)                     := io.rocc_rs1
          data_len(1)                     := io.rocc_rs2
        }
      }
    }
  }//end rocc_s

  switch(state){
    is(s_idle){
    }
    is(s_CGRA_config){
      io.Config_Reset := Bool(false)
      busy            := Bool(true)
      config_clock_en := Bool(true)
      cgra_clock_en   := Bool(true)
      
      when(clock_reg){
        io.cgra_Inconfig  := cgra_config(j)(k)
        k := k + 1
      }
      when(k === UInt(64)){
        k := 0
        j := j + 1
      }
      when(j === UInt(13) && k === UInt(16)){
        state           := s_finished
        config_clock_en := Bool(false)
      }
    }
    is{s_finished}{
      busy              := Bool(false)
      io.rocc_resp_val  := Bool(false)
      // cgra_clock_en   := Bool(false)
      rocc_s            := r_idle
      state             := s_idle
    }
  } //end state
  
  //Memory handler
  switch(mem_s){
    is(m_idle){
      when(state =/= s_CGRA_config){// Should not fetch when configuring cgra
        //Check if we want to write, that there is a new write and that address is within range
        when((write_rq_vec(i) === Bool(true)) && (last_write_address(i) =/= memory_addr(i)) && (memory_addr(i) > "hffff0000".U)){ 
          mem_s           := m_write

        //Since not write, we want to read, chack that there is a new read and that it is whitin the memory range
        }.elsewhen(last_read_address(i) =/= memory_addr(i) && (memory_addr(i) > "hffff0000".U)){
          mem_s           := m_read
          // cgra_clock_en   := Bool(false)

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
    is(m_read){
      // busy            :=  Bool(true)
      io.mem_req_val  := Bool(true)
      io.mem_req_addr := ("h3f".U << 32) | memory_addr(i) //address from CGRA is only 32-bit
      io.mem_req_tag  := i
      io.mem_req_cmd  := M_XRD
      io.mem_req_size := log2Ceil(64).U
      when(io.mem_req_rdy && io.mem_req_val){
        mem_s                 := m_wait
        last_read_address(i)  := memory_addr(i)
      }
    }
    is(m_wait){
      when(io.mem_resp_val && io.mem_resp_tag === i){
        // busy                    :=  Bool(true)
        data_from_memory(i)     := io.mem_resp_data
        mem_s                   := m_idle
        // cgra_clock_en           := Bool(true)
      }
    }
    is(m_write){
      // busy            :=  Bool(true)
      io.mem_req_val  := Bool(true)
      io.mem_req_addr := ("h3f".U << 32) | memory_addr(i)
      io.mem_req_tag  := i
      io.mem_req_cmd  := M_XWR
      io.mem_req_data := data_to_memory(i) << 32
      io.mem_req_size := log2Ceil(64).U
      last_write_address(i) := memory_addr(i)

      when(io.mem_resp_val && io.mem_resp_tag === i){
        mem_s             := m_idle
        cgra_clock_en     := Bool(true)
      }.otherwise{
        mem_s             := m_write
      }
    }
  } //end mem_s

  // switch(mem_s){
  //   is(m_idle){
  //     val canRead = busy && !data_valid.reduce(_&&_)
  //     when(canRead){
  //       // mem_s         := m_preRead
  //     }.otherwise{
  //       mem_s         := m_idle
  //     }
  //   }
  //   is(m_preRead){
  //     mem_s           := m_write      
  //   }
  //   is(m_write){
  //     when(io.cgra_Outconfig){
  //       data_from_cgra(0) := io.from_cgra0
  //       data_from_cgra(1) := io.from_cgra1
  //       data_from_cgra(2) := io.from_cgra2
  //       data_from_cgra(3) := io.from_cgra3
  //       mem_s             := m_read
  //     }.otherwise{
  //       mem_s             := m_write
  //     }
  //   }
  //   is(m_read){
  //     when(state =/= s_write){
  //       io.mem_req_val  := !data_valid(i)
  //       io.mem_req_addr := addr_buffer(i)
  //       io.mem_req_tag  := i
  //       io.mem_req_cmd  := M_XRD
  //       io.mem_req_size := log2Ceil(64).U

  //       when(io.mem_req_rdy && io.mem_req_val){
  //         i       := i + 1
  //         mem_s   := m_wait
  //       }.otherwise{
  //         mem_s   := m_read
  //       }
  //     }.otherwise{
  //       mem_s := m_read
  //     }
  //   }
  //   is(m_wait){
  //     when(io.mem_resp_val){
  //       data_buffer(io.mem_resp_tag)  := io.mem_resp_data
  //       data_valid(io.mem_resp_tag)   := Bool(true)
  //       when(data_valid.reduce(_&&_)){
  //         state       := s_Calculate
  //         mem_s       := m_idle
  //         i           := 0
  //       }.otherwise{
  //         mem_s       := m_read
  //       }
  //     }
  //   }
    
  // }
}

/* 
psuedo kode
Få konfig fra CPU via RoCC (Denne kan vi hardcode en se lenge?)
Få inputs fra CPU via RoCC 
Få output addresser fra CPU
Configurere cgra
Mate cgra med input
skrive output til minne/ sende tilbake til CPU
 */

/* Fra Lasse:
Hva trenger akseleratoren, i hvilken rekkefølge skal ting komme i?
Planen er at akseleratoren skal:
1. konfigureres. Her har du x antall bits som mates til akseleratoren samtidig som config-signalet er høyt.
2. beregne. Her får du 1 eller 2 inputs og 1 output/return (ikke helt bestemt enda). Når lengden på input er satt, så starter beregningen og cgra-en er "busy".
 
Hva trenger akseleratoren fra CPU og hva må være forhåndskompilert?
Den må ha config og peker til inputs og outputs og lengde på det samme.
Det som er konstant for cgra-en er design/verilog.
Det som er dynamisk er konfigurasjonen, det som bestemmer hvor data skal internt i cgra-en og hvilke funksjoner nodene skal ha. NoC og prosesseringselement (PE) skal programmeres hver kjøring/konfigurasjon.
Og videre er også hvert program dynamisk som i at inputs og outputs kan være forskjellige adresser i minnet.
 
Må CGRAm-BlackBox snakke direkte til minne eller får den det den trenger fra CPU?
Den må snakke med minnet.
Noder i cgra-en kan hete "Load" og de nodene må hente data fra minnet.
 */ 
