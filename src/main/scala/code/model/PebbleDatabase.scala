package code.model

import net.liftweb.json._
import net.liftweb.json.JsonParser._

import org.systemsbiology.formats.common._

case class Query(datasource: String, params: Map[String, String], conditions : List[String])

trait GeneExpressionDatabase {
  def geneExpressionsFor(query: String): GeneExpressionMeasurement
}

/**
 * A class which can be used to combine measurements from various sources.
 * @constructor creates a MutableGeneExpressionMeasurement instance
 * @param vngNames the VNG names
 * @param "real" gene names
 * @param conditions the conditions
 */
class MutableGeneExpressionMeasurement(val vngNames: Array[String],
                                       val geneNames: Array[String],
                                       val conditions: Array[String])
extends GeneExpressionMeasurement {
  val data = Array.ofDim[GeneExpressionValue](vngNames.length, conditions.length)
  def apply(geneIndex: Int, conditionIndex: Int) = data(geneIndex)(conditionIndex)
  def update(geneIndex: Int, conditionIndex: Int, value: GeneExpressionValue) {
    data(geneIndex)(conditionIndex) = value
  }
}

/**
 * A facade where we pull out the data from the various sources.
 */
object PebbleDatabase extends GeneExpressionDatabase {
  implicit val formats = DefaultFormats

  def geneExpressionsFor(queryString: String): GeneExpressionMeasurement = {
    val queries = parseJsonQuery(queryString)
    val query0 = queries(0)
    sbeamsGeneExpressionsFor(query0.params("projectId"), query0.params("timestamp"), query0.conditions)
    // TODO: combine gene expressions to a result
  }

  private def sbeamsGeneExpressionsFor(projectId: String, timestamp: String,
                                       conditions: List[String]): GeneExpressionMeasurement = {
    val allExps = DatasourceHelper.sbeamsMeasurementFor(projectId, timestamp)
    if (conditions == Nil) allExps
    else {
      val condArray = conditions.toArray
      val result = new MutableGeneExpressionMeasurement(allExps.vngNames,
                                                        allExps.geneNames,
                                                        condArray)

      for (row <- 0 until allExps.vngNames.length) {
        for (col <- 0 until condArray.length) {
          result(row, col) = allExps(row, allExps.conditions.indexOf(condArray(col)))
        }
      }
      result
    }
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
        val conditions = if (comps.length >= 5) List(comps(4)) else Nil
        println("PROCESS URI QUERY, URI: " + uri + " comps =  [" + comps.toList + "]")
        Query(comps(1), Map("projectId" -> comps(2), "timestamp" -> comps(3)), conditions)
      case _ =>
        queryElement.extract[Query]
    }
  }
}
