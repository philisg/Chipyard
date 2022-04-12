package cgrame

import Chisel._
import scala.collection.mutable.ArrayBuffer
import scala.util.Random
import Chisel.ImplicitConversions._
import freechips.rocketchip.tile.HasCoreParameters
import freechips.rocketchip.rocket.constants.MemoryOpConstants
import freechips.rocketchip.config._

class MemCtrlModule(implicit val p: Parameters) extends Module with HasCoreParameters with MemoryOpConstants {
    
    val io = new Bundle {
        val addr_from_cgra      = Bits(INPUT,32)
        val data_from_cgra      = Bits(INPUT,32)
        val write_rq            = Bool(INPUT)
        val data_from_memory    = Bits(OUTPUT,32)
        val mem_req_val         = Bool(OUTPUT)
        val mem_req_rdy         = Bool(INPUT)
        val mem_req_tag         = Bits(OUTPUT, coreParams.dcacheReqTagBits)
        val mem_req_addr        = Bits(OUTPUT, coreMaxAddrBits)
        val mem_req_cmd         = Bits(OUTPUT, M_SZ)
        val mem_req_size        = Bits(OUTPUT, log2Ceil(coreDataBytes + 1))
        val mem_req_data        = Bits(OUTPUT, 64)

        val mem_resp_val        = Bool(INPUT)
        val mem_resp_tag        = Bits(INPUT, 7)
        val mem_resp_data       = Bits(INPUT, 64)
    }

    val m_idle :: m_read :: m_wait :: m_write :: Nil = Enum(UInt(),4)
    val mem_s = Reg(init=m_idle)

    val last_read_address   = Reg(init=UInt(0,64))
    val last_write_address  = Reg(init=UInt(0,64))

    switch(mem_s){
        is(m_idle){
            when(io.write_rq === Bool(true) && last_write_address =/= io.addr_from_cgra){
                mem_s := m_write
            }.elsewhen(last_read_address =/= io.addr_from_cgra){
                mem_s := m_read
            }
        }
        is(m_read){
            when(io.mem_req_rdy){
                io.mem_req_val  := Bool(true)
                io.mem_req_addr := io.addr_from_cgra
                io.mem_req_tag  := UInt(1)
                io.mem_req_cmd  := M_XRD
                io.mem_req_size := log2Ceil(64).U
                mem_s           := m_wait
                last_read_address := io.addr_from_cgra
            }
        }
        is(m_wait){
            when(io.mem_resp_val){
                io.data_from_memory     := io.mem_resp_data
                mem_s                   := m_idle
            }
        }
        is(m_write){
            io.mem_req_val  := Bool(true)
            io.mem_req_addr := io.addr_from_cgra
            io.mem_req_tag  := UInt(2)
            io.mem_req_cmd  := M_XWR
            io.mem_req_data := io.data_from_cgra << 32
            io.mem_req_size := log2Ceil(64).U
            last_write_address := io.addr_from_cgra

            when(io.mem_resp_val && io.mem_resp_tag === UInt(2)){
                mem_s   := m_idle
            }.otherwise{
                mem_s   := m_write
            }
        }
    }
 
}