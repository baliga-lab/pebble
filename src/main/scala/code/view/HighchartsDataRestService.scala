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

  val logger = Logger(getClass)

  private def seriesFor(measurement: GeneExpressionMeasurement, row: Int) = {
    var data: List[Double] = Nil
    for (i <- 0 until measurement.conditions.length) {
      data ::= measurement(row, i).lambda
    }
    ("name" -> measurement.vngNames(row)) ~ ("data" -> data.reverse)
  }

  private def selectedSeries(measurement: GeneExpressionMeasurement, rows: List[Int]) = {
    rows.map(row => seriesFor(measurement, row - 1))
  }

  private def xTitles(measurement: GeneExpressionMeasurement): List[String] = measurement.conditions.toList

  private def conditions: List[String] = {
    val conditions = S.param("conditions")
    if (conditions != Empty) {
      val condstr = conditions.get
      condstr.substring(1, condstr.length - 1).split(",").toList
    } else Nil
  }

  def sbeamsData2HighchartsJson: JValue = {
    val projectId = S.param("projectId").get
    val timestamp = S.param("timestamp").get
    val rows = S.param("rows").get.split(",").map(_.trim).map(str => java.lang.Integer.parseInt(str)).toList
    val measurement = PebbleDatabase.sbeamsGeneExpressionsFor(projectId, timestamp, conditions)

    logger.warn("Table for rows = " + rows)
    ("chart" -> (("renderTo" -> "chart1") ~ ("defaultSeriesType" -> "line"))) ~
    ("title" -> ("text" -> "Lambdas")) ~
    ("xAxis" -> ("title" -> "Conditions") ~ ("categories" -> xTitles(measurement))) ~
    ("yAxis" -> ("title" -> "Lambda")) ~
    ("series" -> selectedSeries(measurement, rows))
  }


  serve {
    case "highcharts" :: "sbeams" :: _ Get _ => sbeamsData2HighchartsJson
  }
}
