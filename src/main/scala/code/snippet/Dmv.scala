package code.snippet

import java.util.Date

import scala.xml.{NodeSeq, Text}

import net.liftweb.common._
import net.liftweb.util._
import net.liftweb.http._
import net.liftweb.http.js.JsCmds.Alert


import code.lib._
import Helpers._

import org.systemsbiology.formats.common._
import org.systemsbiology.formats.legacy.ExperimentDirectory
import code.model._

/**
 */
class Dmv {
  val logger = Logger(classOf[Dmv])

  def numRows = 3

  private def measurementTable(measurement: GeneExpressionMeasurement): NodeSeq = {
    <div>
    {ajaxLink}
    <table class="data_table">
    { htmlHeaders(measurement.conditions) }
    {for (i <- 0 until measurement.vngNames.length) yield
      <tr>
     <td>{measurement.vngNames(i)}</td>
     {for (j <- 0 until measurement.conditions.length) yield
       <td>r: {measurement(i, j).ratio}<br/>&lambda;: {measurement(i, j).lambda}</td>
    }
     </tr>
    }
    </table>
    </div>
  }

  private def sbeamsTable: NodeSeq = {
    val projectId = S.param("projectId")
    val timestamp = S.param("timestamp")
    val conditions = S.param("conditions")
    val condArg = if (conditions != Empty) {
      val condstr = conditions.get
      condstr.substring(1, condstr.length - 1).split(",").toList
    } else Nil

    if (projectId != Empty && timestamp != Empty) {
      measurementTable(PebbleDatabase.sbeamsGeneExpressionsFor(projectId.get, timestamp.get, condArg))
    } else <p>No project id or timestamp</p>
  }

  private def ajaxLink: NodeSeq = SHtml.a(() => Alert("you clicked me !"), Text("Click me !"))

  def table(in: NodeSeq): NodeSeq = {
    val datasource = S.param("datasource")
    logger.warn("Table for data source = " + datasource)
    val query = S.param("query")
    if (query != Empty) {
      PebbleDatabase.geneExpressionsFor(query.get)
    } else logger.warn("NO QUERY DATA")
   
    sbeamsTable
  }

  private def htmlHeaders(conditionNames: Array[String]): NodeSeq = {
    <tr>
      <th>Gene</th>
    {for (conditionName <- conditionNames) yield
      <th>{conditionName}</th>
    }</tr>
  }
}
