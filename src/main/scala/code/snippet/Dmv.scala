package code.snippet

import java.util.Date

import scala.xml.{Node, NodeSeq, Text}

import net.liftweb.common._
import net.liftweb.util._
import net.liftweb.util.Helpers._
import net.liftweb.http._
import net.liftweb.http.js.JsCmds.Alert


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
    val query = "var thisQuery = " + S.param("query").get + ";"
    <script type="text/javascript">
    {query}
    </script>
  }

  def shorten(str: String, maxlen: Int) = {
    if (str.length <= maxlen) str
    else (str.substring(0, maxlen - 4) + "...")
  }

  /**
   * Render a DMV table using CSS transformers.
   */
  def table  = {
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
