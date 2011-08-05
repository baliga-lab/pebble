package org.systemsbiology.pebble.view

import java.io._
import scala.collection.JavaConversions

import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._
import net.liftweb.util.Props

import org.systemsbiology.formats.common._
import org.systemsbiology.formats.sbeams._
import org.systemsbiology.formats.legacy._
import org.systemsbiology.pebble.model._

/**
 * This is a helper mixin containing serialization function for GeneExpressionMeasurent objects.
 */
trait GeneExpressionSerializer {
  def measurementConditions2JSON(measurements: GeneExpressionMeasurement) = {
    JObject(List(JField("conditions", JArray(measurements.conditions.map(cond => JString(cond)).toList))))
  }

  def geneExpressionMeasurement2JSON(measurement: GeneExpressionMeasurement) = {
    ("genes", measurement.vngNames.toSeq) ~ ("conditions", measurement.conditions.toSeq) ~
      ("data", makeSeqFrom(measurement))
  }

  def geneExpressionMeasurement2JSON(measurement: GeneExpressionMeasurement,
                                     condition: String) = {
    ("genes", measurement.vngNames.toSeq) ~ ("condition", condition) ~
      ("data", makeSeqFrom(measurement, condition))
  }

  // here we simply make sure that we convert that nested two dimensional structure
  private def makeSeqFrom(measurement: GeneExpressionMeasurement) = {
    var result: List[JArray] = Nil
    for (rowIndex <- 0 until measurement.vngNames.length) {
      var row: List[JObject] = Nil
      for (conditionIndex <- 0 until measurement.conditions.length) {
        val value = measurement(rowIndex, conditionIndex)
        row ::= JObject(List(JField("ratio", value.ratio), JField("lambda", value.lambda)))
      }
      result ::= JArray(row.reverse)
    }
    JArray(result.reverse)
  }

  private def makeSeqFrom(measurement: GeneExpressionMeasurement, condition: String) = {
    var result: List[JObject] = Nil
    val conditionIndex = measurement.conditions.indexOf(condition)
    for (rowIndex <- 0 until measurement.vngNames.length) {
      val value = measurement(rowIndex, conditionIndex)
      result ::= JObject(List(JField("ratio", value.ratio), JField("lambda", value.lambda)))
    }
    JArray(result.reverse)
  }
}

/**
 * JSON provider for SBEAMS gene expression data.
 */

object SbeamsDataProvider extends GeneExpressionSerializer {

  // SBEAMS
  def sbeamsConditions2JSON(projectIdStr: String, timestamp: String) = {
    measurementConditions2JSON(SBEAMSDatabase.measurementFor(projectIdStr, timestamp))
  }
  def sbeamsJSON(projectIdStr: String, timestamp: String) = {
    geneExpressionMeasurement2JSON(SBEAMSDatabase.measurementFor(projectIdStr, timestamp))
  }
  def sbeamsJSON(projectIdStr: String, timestamp: String, conditionName: String) = {
    geneExpressionMeasurement2JSON(SBEAMSDatabase.measurementFor(projectIdStr, timestamp), conditionName)
  }
}

/**
 * JSON provider for pre-SBEAMS gene expression data.
 */
object LegacyDataProvider extends GeneExpressionSerializer {

  def legacyConditions2JSON(baseName: String) = {
    measurementConditions2JSON(PreSBEAMSDatabase.measurementFor(baseName))
  }
  def legacyJSON(baseName: String) = {
    geneExpressionMeasurement2JSON(PreSBEAMSDatabase.measurementFor(baseName))
  }

  def legacyJSON(baseName: String, conditionName: String) = {
    geneExpressionMeasurement2JSON(PreSBEAMSDatabase.measurementFor(baseName), conditionName)
  }
}
