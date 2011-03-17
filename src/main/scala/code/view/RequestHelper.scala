package code.view

import scala.xml.{Node, NodeSeq}
import scala.collection.JavaConversions
import net.liftweb.common._
import net.liftweb.http._

import code.model._

object RequestHelper {
  def extractValueList(paramName: String): List[String] = {
    val paramValue = S.param(paramName)
    if (paramValue != Empty) {
      val paramStr = paramValue.get
      paramStr.substring(1, paramStr.length - 1).split(",").toList
    } else Nil
  }

  def conditions = extractValueList("conditions")
  def sbeamsMeasurement = {
    val query = S.param("query")
    if (query != Empty) {
      PebbleDatabase.geneExpressionsFor(query.get)
    } else {
      val projectId = S.param("projectId")
      val timestamp = S.param("timestamp")
      PebbleDatabase.sbeamsGeneExpressionsFor(projectId.get, timestamp.get, conditions)
    }
  }

  private def htmlHeaders(conditionNames: Array[String]): NodeSeq = {
    <tr>
      <th>Gene</th>
    {for (conditionName <- conditionNames) yield
      <th>{conditionName}</th>
    }</tr>
  }

  def sbeamsMeasurementTable: Node = {
    val measurement = sbeamsMeasurement
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
}
