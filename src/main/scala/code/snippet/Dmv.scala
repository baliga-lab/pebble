package code.snippet

import java.util.Date

import scala.xml.{Node, NodeSeq, Text}

import net.liftweb.common._
import net.liftweb.util._
import net.liftweb.util.Helpers._
import net.liftweb.http._
import net.liftweb.http.js.JsCmds.Alert
import net.liftweb.json._

import code.lib._
import Helpers._

import org.systemsbiology.formats.common._
import org.systemsbiology.formats.legacy.ExperimentDirectory
import code.model._
import code.view._

/**
 */
class Dmv {

  val logger = Logger(classOf[Dmv])

  def numRows = 3


  private def ajaxLink: NodeSeq = SHtml.a(() => Alert("you clicked me !"), Text("Click me !"))

  /**
   * A snippet that returns the current query in a Javascript variable.
   */
  def thisQuery(in: NodeSeq): NodeSeq = {
    import net.liftweb.json.JsonAST._
    import net.liftweb.json.Printer._

    println("PARAMETERS: " + S.request.get.params)
    val jsonQuery = pretty(render(urlEncodeJsonParams(JsonParser.parse(S.param("query").openOr("")))))
    printf("JSON QUERY: %s\n", jsonQuery)

    val query = "var thisQuery = " + jsonQuery + ";"
    <script type="text/javascript">
    {query}
    </script>
  }

  private def urlEncodeJsonParams(jsonValue: JValue): JValue = {
    jsonValue match {
      case JArray(arr)         => JArray(arr.map(value => urlEncodeJsonParams(value)))
      case JObject(obj)        => JObject(obj.map(field => urlEncodeJsonParams(field).asInstanceOf[JField]))
      case JField(name, value) => JField(name, urlEncodeJsonParams(value))
      case JString(s)          => JString(urlEncode(s))
      case value               => value
    }
  }

  def shorten(str: String, maxlen: Int) = {
    if (str.length <= maxlen) str
    else (str.substring(0, maxlen - 4) + "...")
  }

  /**
   * Render a DMV table using CSS transformers.
   */
  def table  = {
    // DEBUGGIN'
/*
    val allConditions = Condition.findAll
    println("# CONDITIONS FOUND: " + allConditions.length)
    println("# FEATURES(0) FOUND: " + allConditions(0).features.length)
    val gene = allConditions(0).features(0).gene.obj.get
    printf("one gene: %s, alias: %s, gene_name: %s\n", gene.name, gene.alias, gene.geneName)
*/
    // DEBUGGIN'
    val measurement = RequestHelper.sbeamsMeasurement

    def transformHead = {
      var result: List[CssSel] = List(".dmvheaditem *" #> "Gene")
      for (i <- 0 until measurement.conditions.length) {
        val name = measurement.conditions(i)
        result ::= (".dmvheaditem [id+]" #> name &
                    ".dmvheaditem [class+]" #> "condition" &
                    ".dmvheaditem *" #> shorten(name, 20))
      }
      ".dmvheaditem" #> result.reverse
    }

    def measurementColumns(row: Int) = {
      var result: List[NodeSeq] = Nil
      for (col <- 0 until measurement.conditions.length) {
        val label = <span>r: {measurement(row, col).ratio}<br/>&lambda;: {measurement(row, col).lambda}</span>
        result ::= label
      }
      result
    }

    def transformBody = {
      def geneNames(row: Int) =
        <span>{measurement.vngNames(row)}<br/>{measurement.geneNames(row)}</span>

      var result: List[CssSel] = Nil
      for (i <- 0 until measurement.vngNames.length) {
        result ::= (".dmvrow [id+]" #> measurement.vngNames(i) &
                    ".dmvgene *" #> geneNames(i) &
                  ".dmvvalue *" #> measurementColumns(i))
      }
      ".dmvrow" #> result.reverse
    }
    transformHead & transformBody
  }
}
