package org.systemsbiology.pebble.view

import scala.collection.JavaConversions
import scala.xml.Node

import net.liftweb.common._
import net.liftweb.http._

import net.liftweb.http.rest.RestHelper
import net.liftweb.http.js.JsExp

import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._

import org.systemsbiology.formats.common._
import org.systemsbiology.pebble.model._

object HighchartsDataRestService extends RestHelper {

  val logger = Logger(getClass)

  private def lambdaSeriesFor(measurement: GeneExpressionMeasurement, vngName: String) = {
    var data: List[Double] = Nil
    val row = measurement.vngNames.indexOf(vngName)
    for (i <- 0 until measurement.conditions.length) {
      data ::= measurement(row, i).lambda
    }
    ("name" -> measurement.vngNames(row)) ~ ("data" -> data.reverse)
  }

  private def selectedLambdaSeries(measurement: GeneExpressionMeasurement, vngNames: List[String]) = {
    vngNames.map(vngName => lambdaSeriesFor(measurement, vngName))
  }

  private def ratioSeriesFor(measurement: GeneExpressionMeasurement, vngName: String) = {
    val row = measurement.vngNames.indexOf(vngName)
    var data: List[Double] = Nil
    for (i <- 0 until measurement.conditions.length) {
      data ::= measurement(row, i).ratio
    }
    ("name" -> measurement.vngNames(row)) ~ ("data" -> data.reverse)
  }

  private def selectedRatioSeries(measurement: GeneExpressionMeasurement, vngNames: List[String]) = {
    vngNames.map(vngName => ratioSeriesFor(measurement, vngName))
  }

  private def xTitles(measurement: GeneExpressionMeasurement): List[String] = measurement.conditions.toList

  private def vngNames = S.param("vngNames").get.split(",").map(_.trim).map(_.replaceAll("'", "")).toList
  private def chartId = S.param("chartId").get

  def lambdaData2HighchartsJson: JValue = {
    val measurement = RequestHelper.sbeamsMeasurement

    ("chart" -> (("renderTo" -> chartId) ~ ("defaultSeriesType" -> "line"))) ~
    ("title" -> ("text" -> "Lambdas")) ~
    ("xAxis" -> ("title" -> "Conditions") ~ ("categories" -> xTitles(measurement))) ~
    ("yAxis" -> ("title" -> "Lambda")) ~
    ("series" -> selectedLambdaSeries(measurement, vngNames))
  }

  def ratioData2HighchartsJson: JValue = {
    val measurement = RequestHelper.sbeamsMeasurement

    ("chart" -> (("renderTo" -> chartId) ~ ("defaultSeriesType" -> "line"))) ~
    ("title" -> ("text" -> "Ratios")) ~
    ("xAxis" -> ("title" -> "Conditions") ~ ("categories" -> xTitles(measurement))) ~
    ("yAxis" -> ("title" -> "Ratio")) ~
    ("series" -> selectedRatioSeries(measurement, vngNames))
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
