package code.view

import scala.collection.JavaConversions
import scala.xml.Node

import net.liftweb.common._
import net.liftweb.http._

import net.liftweb.http.rest.RestHelper
import net.liftweb.http.js.JsExp

import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._

import org.systemsbiology.formats.common._
import code.model._

object HighchartsDataRestService extends RestHelper {

  val logger = Logger(getClass)

  private def lambdaSeriesFor(measurement: GeneExpressionMeasurement, row: Int) = {
    var data: List[Double] = Nil
    for (i <- 0 until measurement.conditions.length) {
      data ::= measurement(row, i).lambda
    }
    ("name" -> measurement.vngNames(row)) ~ ("data" -> data.reverse)
  }

  private def selectedLambdaSeries(measurement: GeneExpressionMeasurement, rows: List[Int]) = {
    rows.map(row => lambdaSeriesFor(measurement, row - 1))
  }

  private def ratioSeriesFor(measurement: GeneExpressionMeasurement, row: Int) = {
    var data: List[Double] = Nil
    for (i <- 0 until measurement.conditions.length) {
      data ::= measurement(row, i).ratio
    }
    ("name" -> measurement.vngNames(row)) ~ ("data" -> data.reverse)
  }

  private def selectedRatioSeries(measurement: GeneExpressionMeasurement, rows: List[Int]) = {
    rows.map(row => ratioSeriesFor(measurement, row - 1))
  }

  private def xTitles(measurement: GeneExpressionMeasurement): List[String] = measurement.conditions.toList

  private def rows = S.param("rows").get.split(",").map(_.trim).map(str => java.lang.Integer.parseInt(str)).toList
  private def chartId = S.param("chartId").get

  def lambdaData2HighchartsJson: JValue = {
    val measurement = RequestHelper.sbeamsMeasurement

    ("chart" -> (("renderTo" -> chartId) ~ ("defaultSeriesType" -> "line"))) ~
    ("title" -> ("text" -> "Lambdas")) ~
    ("xAxis" -> ("title" -> "Conditions") ~ ("categories" -> xTitles(measurement))) ~
    ("yAxis" -> ("title" -> "Lambda")) ~
    ("series" -> selectedLambdaSeries(measurement, rows))
  }

  def ratioData2HighchartsJson: JValue = {
    val measurement = RequestHelper.sbeamsMeasurement

    ("chart" -> (("renderTo" -> chartId) ~ ("defaultSeriesType" -> "line"))) ~
    ("title" -> ("text" -> "Ratios")) ~
    ("xAxis" -> ("title" -> "Conditions") ~ ("categories" -> xTitles(measurement))) ~
    ("yAxis" -> ("title" -> "Ratio")) ~
    ("series" -> selectedRatioSeries(measurement, rows))
  }

  /**
   * Wrap a JSON object in a LiftResponse object that sets the
   * "Access-Control-Allow-Origin" header to "*". RestHelper will
   * implicitly box the response.
   * @param json the JSON object to wrap
   * @return a LiftResponse
   */
  def crossDomainJsonResponse(json: JValue): LiftResponse = {
    JsonResponse(json, List(("Access-Control-Allow-Origin", "*")), Nil, 200)
  }

  serve {
    case "highcharts" :: "lambdas" :: _ Get _ => crossDomainJsonResponse(lambdaData2HighchartsJson)
    case "highcharts" :: "ratios" :: _ Get _ => crossDomainJsonResponse(ratioData2HighchartsJson)
  }
}
