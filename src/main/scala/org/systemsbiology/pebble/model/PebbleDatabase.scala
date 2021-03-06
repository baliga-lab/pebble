package org.systemsbiology.pebble.model

import java.io._
import net.liftweb.json._
import net.liftweb.json.JsonParser._

import net.liftweb.util.Props
import org.systemsbiology.formats.common._
import org.systemsbiology.formats.legacy._
import org.systemsbiology.formats.sbeams._
import org.systemsbiology.pebble.model.echidna.EchidnaDatabase
import org.systemsbiology.pebble.model.gwap.GWAPDatabase

case class Query(datasource: String, params: Map[String, String], conditions : List[String])

trait GeneExpressionDatabase {
  def geneExpressionsFor(query: Query): GeneExpressionMeasurement

  def filterMeasurementByConditions(measurement: GeneExpressionMeasurement,
                                    conditions: List[String]) = {
    if (conditions == Nil) measurement
    else {
      val condArray = conditions.toArray
      val result = new MutableGeneExpressionMeasurement(measurement.vngNames,
                                                        measurement.geneNames,
                                                        condArray)

      for (row <- 0 until measurement.vngNames.length) {
        for (col <- 0 until condArray.length) {
          result(row, col) = measurement(row, measurement.conditions.indexOf(condArray(col)))
        }
      }
      result
    }
  }
}

/**
 * A facade where we pull out the data from the various sources.
 */
object PebbleDatabase {
  implicit val formats   = DefaultFormats
  val ArraysDirectory    = Props.get("arrays.dir").get
  // this is for pre-sbeams data only (XML files)
  val LegacyDirectory    = new File("%s/emi/halobacterium/repos".format(ArraysDirectory))
  val OligoMapDirectory  = new File("%s/Slide_Templates".format(ArraysDirectory))
  private val OligoMapDb = new OligoMapDatabase(OligoMapDirectory)
  val OligoMap           = OligoMapDb.latestMap
  val Vng2GeneNameMap    = OligoMapDb.latestVng2GeneNameMap

  def geneExpressionsFor(queryString: String): GeneExpressionMeasurement = {
    val queries = parseJsonQuery(queryString)
    GeneExpressionMeasurementMerger(
      queries.map(query => query.datasource match {
        case "sbeams"     => SBEAMSDatabase.geneExpressionsFor(query)
        case "pre-sbeams" => PreSBEAMSDatabase.geneExpressionsFor(query)
        case "gwap"       => GWAPDB.geneExpressionsFor(query)
        case _ =>
          throw new UnsupportedOperationException("Unsupported format: " + query.datasource)
      })).mergedMeasurements
  }

  /**
   * Converts the specified JSON string into a Query object.
   */
  private def parseJsonQuery(queryString: String) = {
    println("parseJsonQuery(), QUERY STRING: <<" + queryString + ">>")
    val jsonObj = JsonParser.parse(queryString)
    jsonObj.children.map(q => extractQueryElement(q)).reverse
  }
  private def extractQueryElement(queryElement: JValue) = {
    println("QUERY ELEM: " + queryElement)
    queryElement match {
      case JObject(List(JField("uri", JString(uri)))) =>
        val comps = uri.split("/")
        println("PROCESS URI QUERY, URI: " + uri + " comps =  [" + comps.toList + "]")

        if (comps(1) == "sbeams") {
          val conditions = if (comps.length >= 5) List(comps(4)) else Nil
          Query(comps(1), Map("projectId" -> comps(2), "timestamp" -> comps(3)), conditions)
        } else if (comps(1) == "gwap") {
          Query(comps(1), Map("condition" -> comps(2)), Nil)
        } else if (comps(1) == "pre-sbeams") {
          val conditions = if (comps.length >= 4) List(comps(3)) else Nil
          Query(comps(1), Map("xmlfile" -> comps(2)), conditions)
        } else {
          throw new UnsupportedOperationException("unknown datasource: " + comps(1))
        }
      case _ =>
        queryElement.extract[Query]
    }
  }
}

object SBEAMSDatabase extends GeneExpressionDatabase {

  def geneExpressionsFor(query: Query) = {
    measurementFor(query.params("projectId"), query.params("timestamp"), query.conditions)
  }

  def measurementFor(projectId: String, timestamp: String, conditions: List[String] = Nil): GeneExpressionMeasurement = {
    try {
      sbeamsGeneExpressionsFileSystem(projectId, timestamp, conditions)
    } catch {
      case _:ArrayIndexOutOfBoundsException =>
        println("NOT FOUND (because of Echidna 1.0/GWAP inconsistency)")
        printf("sbeamsGeneExpressions (fallback db) project: %s, timestamp: %s, cond: %s\n",
               projectId, timestamp, conditions)
        EchidnaDatabase.sbeamsMeasurementFor(projectId, timestamp,
                                             conditions,
                                             PebbleDatabase.Vng2GeneNameMap)
      case ex =>
        ex.printStackTrace
        null
    }
  }

  private def sbeamsMatrixOutputFileFor(projectId: String, timestamp: String): File = {
    new File("%s/Pipeline/output/project_id/%s/%s/matrix_output".format(PebbleDatabase.ArraysDirectory,
                                                                        projectId, timestamp))
  }

  private def sbeamsGeneExpressionsFileSystem(projectId: String, timestamp: String,
                                              conditions: List[String]): GeneExpressionMeasurement = {
    printf("sbeamsGeneExpressions (file system) project: %s, timestamp: %s, cond: %s\n",
           projectId, timestamp, conditions)
    filterMeasurementByConditions(measurementFromFileSystemFor(projectId, timestamp),
                                  conditions)
  }
  private def measurementFromFileSystemFor(projectId: String, timestamp: String): GeneExpressionMeasurement = {
    val matrix = DataMatrixReader.createFromFile(sbeamsMatrixOutputFileFor(projectId, timestamp))
    new SbeamsMeasurement(PebbleDatabase.OligoMap, matrix)
  }
}

object PreSBEAMSDatabase extends GeneExpressionDatabase {

  def geneExpressionsFor(query: Query) = {
    filterMeasurementByConditions(measurementFor(query.params("xmlfile")), query.conditions)
  }

  def measurementFor(xmlFileName: String) = {
    LegacyMeasurementReader.readMeasurement(PebbleDatabase.LegacyDirectory, xmlFileName.replaceAll(".xml", ""),
                                            PebbleDatabase.OligoMap)
  }
}

object GWAPDB extends GeneExpressionDatabase {

  def geneExpressionsFor(query: Query) = {
    val conditionName = query.params("condition")
    measurementFor(query.params("condition"))
  }

  def measurementFor(condition: String) = {
    GWAPDatabase.measurementFor(condition, PebbleDatabase.Vng2GeneNameMap)
  }
}
