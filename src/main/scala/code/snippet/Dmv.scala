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
import code.view._

/**
 */
class Dmv {
  import RequestHelper._

  val logger = Logger(classOf[Dmv])

  def numRows = 3

  private def measurementTable(measurement: GeneExpressionMeasurement): NodeSeq = {
    <div>
    <table class="data_table">
    { htmlHeaders(measurement.conditions) }
    {for (i <- 0 until measurement.vngNames.length) yield
      <tr>
     <td>{measurement.vngNames(i)}<br/>{measurement.geneNames(i)}</td>
     {for (j <- 0 until measurement.conditions.length) yield
       <td>r: {measurement(i, j).ratio}<br/>&lambda;: {measurement(i, j).lambda}</td>
    }
     </tr>
    }
    </table>
    </div>
  }

  private def sbeamsTable: NodeSeq = measurementTable(measurement)

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

  /**
   * A snippet that returns the current query in a Javascript variable.
   */
  def thisQuery(in: NodeSeq): NodeSeq = {
    val query = "var thisQuery = " + S.param("query").get + ";"
    <script type="text/javascript">
    {query}
    </script>
  }
}
