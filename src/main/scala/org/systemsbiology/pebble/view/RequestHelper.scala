package org.systemsbiology.pebble.view

import scala.xml.{Node, NodeSeq}
import scala.collection.JavaConversions
import net.liftweb.common._
import net.liftweb.http._

import org.systemsbiology.pebble.model._

/**
 * A helper class that implements a lot of common functionality like
 * query processing and view rendering.
 */
object RequestHelper {

  /**
   * Retrieves a GeneExpressionMeasurement for the current HTTP request.
   * @return a measurement
   */
  def measurementFromRequest = {
    val query = S.param("query")
    if (query != Empty) {
      PebbleDatabase.geneExpressionsFor(query.get)
    } else {
      throw new IllegalArgumentException("no query provided")
    }
  }

  def sbeamsMeasurementTable: Node = {
    val measurement = measurementFromRequest
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

  private def htmlHeaders(conditionNames: Array[String]): NodeSeq = {
    <tr>
      <th>Gene</th>
    {for (conditionName <- conditionNames) yield
      <th>{conditionName}</th>
    }</tr>
  }
}
