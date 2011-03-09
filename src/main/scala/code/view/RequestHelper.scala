package code.view

import scala.collection.JavaConversions
import net.liftweb.common._
import net.liftweb.http._

import code.model._

object RequestHelper {
  def extractValueList(paramName: String): List[String] = {
    val paramValue = S.param(paramName)
    if (paramValue != Empty) {
      val paramStr = paramValue.get
      paramStr.substring(1, paramStr.length - 1).split(",").toList
    } else Nil
  }

  def conditions = extractValueList("conditions")
  def measurement = {
    val query = S.param("query")
    if (query != Empty) {
      PebbleDatabase.geneExpressionsFor(query.get)
    } else {
      val projectId = S.param("projectId")
      val timestamp = S.param("timestamp")
      PebbleDatabase.sbeamsGeneExpressionsFor(projectId.get, timestamp.get, conditions)
    }
  }

}
