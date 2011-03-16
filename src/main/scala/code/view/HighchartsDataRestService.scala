package code.view

import scala.collection.JavaConversions
import net.liftweb.common._
import net.liftweb.http._

import net.liftweb.http.rest.RestHelper
import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._

import org.systemsbiology.formats.common._
import code.model._

object HighchartsDataRestService extends RestHelper {
  import RequestHelper._

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
    val meas = measurement
    S.setHeader("Access-Control-Allow-Origin", "*")

    ("chart" -> (("renderTo" -> chartId) ~ ("defaultSeriesType" -> "line"))) ~
    ("title" -> ("text" -> "Lambdas")) ~
    ("xAxis" -> ("title" -> "Conditions") ~ ("categories" -> xTitles(measurement))) ~
    ("yAxis" -> ("title" -> "Lambda")) ~
    ("series" -> selectedLambdaSeries(measurement, rows))
  }

  def ratioData2HighchartsJson: JValue = {
    val meas = measurement
    S.setHeader("Access-Control-Allow-Origin", "*")

    ("chart" -> (("renderTo" -> chartId) ~ ("defaultSeriesType" -> "line"))) ~
    ("title" -> ("text" -> "Ratios")) ~
    ("xAxis" -> ("title" -> "Conditions") ~ ("categories" -> xTitles(measurement))) ~
    ("yAxis" -> ("title" -> "Ratio")) ~
    ("series" -> selectedRatioSeries(measurement, rows))
  }


  serve {
    case "highcharts" :: "lambdas" :: _ Get _ => lambdaData2HighchartsJson
    case "highcharts" :: "ratios" :: _ Get _ => ratioData2HighchartsJson
  }
}
