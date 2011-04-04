package code.model

import net.liftweb.json._
import net.liftweb.json.JsonParser._

import org.systemsbiology.formats.common._

case class Query(datasource: String, params: Map[String, String], conditions : List[String])

trait GeneExpressionDatabase {
  def geneExpressionsFor(query: String): GeneExpressionMeasurement
}

/**
 * A facade where we pull out the data from the various sources.
 */
object PebbleDatabase extends GeneExpressionDatabase {
  implicit val formats = DefaultFormats

  def geneExpressionsFor(queryString: String): GeneExpressionMeasurement = {
    val queries = parseJsonQuery(queryString)
    GeneExpressionMeasurementMerger(
      queries.map(query => sbeamsGeneExpressionsFor(query.params("projectId"),
                                                    query.params("timestamp"),
                                                    query.conditions))).mergedMeasurements
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
