package code.view

import java.io._
import scala.collection.JavaConversions

import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._
import net.liftweb.util.Props

import org.systemsbiology.formats.common._
import org.systemsbiology.formats.sbeams._
import org.systemsbiology.formats.legacy._
import code.model._

/**
 * This is a helper mixin containing serialization function for GeneExpressionMeasurent objects.
 */
trait GeneExpressionSerializer {
  def measurementConditions2JSON(measurements: GeneExpressionMeasurement) = {
    JObject(List(JField("conditions", JArray(measurements.conditions.map(cond => JString(cond)).toList))))
  }

  def geneExpressionMeasurement2Json(measurement: GeneExpressionMeasurement) = {
    ("genes", measurement.vngNames.toSeq) ~ ("conditions", measurement.conditions.toSeq) ~
      ("data", makeSeqFrom(measurement))
  }

  def geneExpressionMeasurement2Json(measurement: GeneExpressionMeasurement,
                                             condition: String) = {
    ("genes", measurement.vngNames.toSeq) ~ ("condition", condition) ~
      ("data", makeSeqFrom(measurement, condition))
  }

  // here we simply make sure that we convert that nested two dimensional structure
  private def makeSeqFrom(measurement: GeneExpressionMeasurement) = {
    var result: List[JArray] = Nil
    for (rowIndex <- 0 until measurement.vngNames.length) {
      var row: List[JField] = Nil
      for (conditionIndex <- 0 until measurement.conditions.length) {
        val value = measurement(rowIndex, conditionIndex)
        row ::= JField("value",
                       JObject(List(JField("ratio", value.ratio), JField("lambda", value.lambda))))
      }
      result ::= JArray(row.reverse)
    }
    JArray(result.reverse)
  }

  private def makeSeqFrom(measurement: GeneExpressionMeasurement, condition: String) = {
    var result: List[JField] = Nil
    val conditionIndex = measurement.conditions.indexOf(condition)
    for (rowIndex <- 0 until measurement.vngNames.length) {
      val value = measurement(rowIndex, conditionIndex)
      result ::= JField("value",
                        JObject(List(JField("ratio", value.ratio), JField("lambda", value.lambda))))
    }
    JArray(result.reverse)
  }
}

/**
 * JSON provider for SBEAMS gene expression data.
 */
object SbeamsDataProvider extends GeneExpressionSerializer {

  import DatasourceHelper._

  // SBEAMS
  def sbeamsConditions2JSON(projectIdStr: String, timestamp: String) = {
    measurementConditions2JSON(sbeamsMeasurementFor(projectIdStr, timestamp))
  }
  def sbeamsJSON(projectIdStr: String, timestamp: String) = {
    geneExpressionMeasurement2Json(sbeamsMeasurementFor(projectIdStr, timestamp))
  }
  def sbeamsJSON(projectIdStr: String, timestamp: String, conditionName: String) = {
    geneExpressionMeasurement2Json(sbeamsMeasurementFor(projectIdStr, timestamp), conditionName)
  }
}

/**
 * JSON provider for pre-SBEAMS gene expression data.
 */
object LegacyDataProvider extends GeneExpressionSerializer {

  import DatasourceHelper._

  def legacyConditions2JSON(baseName: String) = {
    measurementConditions2JSON(LegacyMeasurementReader.readMeasurement(LegacyDirectory, baseName))
  }
  def legacyJSON(baseName: String) = {
    val measurement = LegacyMeasurementReader.readMeasurement(LegacyDirectory, baseName)
    geneExpressionMeasurement2Json(measurement)
  }

  def legacyJSON(baseName: String, conditionName: String) = {
    val measurement = LegacyMeasurementReader.readMeasurement(LegacyDirectory, baseName)
    geneExpressionMeasurement2Json(measurement, conditionName)
  }
}
