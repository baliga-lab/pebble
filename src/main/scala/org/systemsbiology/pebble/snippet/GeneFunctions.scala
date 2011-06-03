package org.systemsbiology.pebble.snippet

import scala.xml.NodeSeq
import net.liftweb.http._
import java.util.ResourceBundle

object GeneFunctions {

  private val resourceBundle = ResourceBundle.getBundle("gene_function")

  def getFunctionFor(orfname: String): Option[String] = {
    try {
      Some(resourceBundle.getString(orfname))
    } catch {
      case _ => None
    }
  }
}


class GeneFunctions {
  import GeneFunctions._

  def geneFunction(html: NodeSeq): NodeSeq = {
    getFunctionFor(S.param("orfname").openOr("")) match {
      case Some(str) => <div>{str}</div>
      case _ => <div>not available</div>
    }
  }
}
