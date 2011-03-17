package code.snippet

import java.util.Date

import scala.xml.{Node, NodeSeq, Text}

import net.liftweb.common._
import net.liftweb.util._
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

  def table(in: NodeSeq): NodeSeq = {
    val datasource = S.param("datasource")
    logger.warn("Table for data source = " + datasource)
    RequestHelper.sbeamsMeasurementTable
  }

  /**
   * A snippet that returns the current query in a Javascript variable.
   */
  def thisQuery(in: NodeSeq): NodeSeq = {
    val query = "var thisQuery = " + S.param("query").get + ";"
    <script type="text/javascript">
    {query}
    </script>
  }
}
