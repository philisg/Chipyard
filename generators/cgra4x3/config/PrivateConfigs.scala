//see LICENSE for license
package rocketchip

import Chisel._
import uncore._
import rocket._
//import cde._
import cde.{Parameters, Field, Config, Knob, Dump, World, Ex, ViewSym}
import cde.Implicits._
import cgrame._

//case object Width extends Field[Int]
//case object Stages extends Field[Int]
//case object FastMem extends Field[Boolean]
//case object BufferSram extends Field[Boolean]

class CgrameConfig extends Config {
  override val topDefinitions:World.TopDefs = {
    (pname,site,here) => pname match {
      case WidthP => 64
      case Stages => Knob("stages")
      case FastMem => Knob("fast_mem")
      case BufferSram => Dump(Knob("buffer_sram"))
      case RoccMaxTaggedMemXacts => 32
      case BuildRoCC => Seq( 
                          RoccParameters(    
                            opcodes = OpcodeSet.custom0,
                            generator = (p: Parameters) => (Module(new CgrameAccel()(p.alterPartial({ case CoreName => "Rocket" })))) ))
    }
  }
 
  override val topConstraints:List[ViewSym=>Ex[Boolean]] = List(
    ex => ex(WidthP) === 64,
    ex => ex(Stages) >= 1 && ex(Stages) <= 4 && (ex(Stages)%2 === 0 || ex(Stages) === 1),
    ex => ex(FastMem) === ex(FastMem),
    ex => ex(BufferSram) === ex(BufferSram)
    //ex => ex[Boolean]("multi_vt") === ex[Boolean]("multi_vt")
  )
  override val knobValues:Any=>Any = {
    case "stages" => 1
    case "fast_mem" => false //was true
    case "buffer_sram" => false
    case "multi_vt" => false //was true 
  }
}

class CgrameVLSIConfig extends Config(new CgrameConfig ++ new DefaultVLSIConfig)
class CgrameFPGAConfig extends Config(new CgrameConfig ++ new DefaultFPGAConfig) 
class CgrameCPPConfig extends Config(new CgrameConfig ++ new DefaultCPPConfig) 
