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

/**
 * A web service to provide HTML snippets that view experiment data.
 */
object HtmlSnippetRestService extends RestHelper {
  val logger = Logger(getClass)

  private def makeDataTableSnippet = RequestHelper.sbeamsMeasurementTable

  /**
   * Wrap a XHTML object in a LiftResponse object that sets the
   * "Access-Control-Allow-Origin" header to "*". RestHelper will
   * implicitly box the response.
   * @param node the XHTML node to wrap
   * @return a LiftResponse
   */
  def crossDomainHtmlResponse(node: Node): LiftResponse = {
    XhtmlResponse(node, Empty,
                  List(("Access-Control-Allow-Origin", "*")),
                  Nil, 200, false)
  }

  serve {
    case "datatable" :: _ Get _ => crossDomainHtmlResponse(makeDataTableSnippet)
  }
}
