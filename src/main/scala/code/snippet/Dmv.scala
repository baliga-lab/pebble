package code.snippet

import _root_.scala.xml.{NodeSeq, Text}
import _root_.net.liftweb.util._
import _root_.net.liftweb.common._
import _root_.java.util.Date

import net.liftweb.common._
import net.liftweb.http._
import code.lib._
import Helpers._

import org.systemsbiology.formats.legacy.ExperimentDirectory
import code.model.DatasourceHelper

class Dmv {
  val logger = Logger(classOf[Dmv])

  def numRows = 3

  def table(in: NodeSeq): NodeSeq = {
    logger.warn("Table for type= " + S.param("type"))
    val measurement = DatasourceHelper.sbeamsMeasurementFor("1064", "20100804_163517")
    val result =
      <table class="data-table">
        { htmlHeaders(measurement.conditions) }
        {for (i <- 0 until measurement.vngNames.length) yield
          <tr>
           <td>{measurement.vngNames(i)}</td>
          {for (j <- 0 until measurement.conditions.length) yield
           <td>r: {measurement(i, j).ratio}<br/>l: {measurement(i, j).lambda}</td>
          }
          </tr>
        }
      </table>
    
    result
  }

  private def htmlHeaders(conditionNames: Array[String]): NodeSeq = {
    <tr>
      <th>Gene</th>
    {for (conditionName <- conditionNames) yield
      <th>{conditionName}</th>
    }</tr>
  }
}
