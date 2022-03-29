#!/bin/sh
exec scala "$0" "$@"
!#

import scala.util.{Success, Try}
import scala.util.matching.Regex

val port = raw"\s*(input|output|inout)\s+(wire\s*|reg\s*)?\s*(\[(\d+):(\d+)\])?\s*(\w+)?\s*;?".r

val p = scala.io.Source.fromFile(args(0)).getLines.foreach { f: String =>
  val b = f match {
    case port(d, tzpe, _, msb, lsb, name) =>
      val dir = d match {
        case "input" => "Input"
        case "output" => "Output"
        case "inout" => "Analog"
        case _ =>
      }
      val w = Try({msb.toInt - lsb.toInt + 1}) match {
        case Success(s) => s"%d".W.format(s)
        case _ => "1.W"
      }
      s"val $name = $dir(UInt($w))"
    case _ => ""
  }
  if (b.nonEmpty) println(b)
}
