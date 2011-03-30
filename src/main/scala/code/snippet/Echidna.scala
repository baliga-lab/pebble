package code.snippet

import scala.xml.{Node, NodeSeq, Text}

import net.liftweb.common._
import net.liftweb.util._
import net.liftweb.util.Helpers._
import net.liftweb.http._

import java.net.URL
import scala.xml.{XML,Elem}

trait SolrValue {
  def valueType: String
  def name: String
}

case class SolrPrimitiveValue(valueType: String, name: String, value: String) extends SolrValue
case class SolrCollection(valueType: String, name: String, values: List[SolrValue]) extends SolrValue

class SolrResponse(solrResponseXml: Elem) {

  /**
   * Response header elements, currently this method does not recurse into nested lists/arrays (TODO).
   */
  def responseHeader: SolrCollection = {
    parseCollection((solrResponseXml \ "lst")(0))
  }

  def documents: List[SolrCollection] = {
    var result: List[SolrCollection] = Nil
    for (doc <- (solrResponseXml \\ "doc")) {
      result ::= parseCollection(doc)
    }
    result.reverse
  }

  private def parseCollection(xmlCollection: Node): SolrCollection = {
    var result: List[SolrValue] = Nil
    for (child <- xmlCollection.child) {
      child.label match {
        case "lst" => result ::= parseCollection(child)
        case "arr" => result ::= parseCollection(child)
        case _     =>
          result ::= SolrPrimitiveValue(child.label, (child \ "@name").text, child.text)
      }
    }
    SolrCollection(xmlCollection.label, (xmlCollection \ "@name").text, result.reverse)
  }
}
 
class Echidna {

  def collectionString(coll: SolrCollection): String = {
    val result = new StringBuilder
    var count = 0
    for (value <- coll.values) {
      if (count > 0) result.append(", ")
      result.append(value match {
        case v:SolrPrimitiveValue => v.value
        case c:SolrCollection     => collectionString(c)
      })
      count += 1
    }
    result.toString
  }

  def condition(html: NodeSeq): NodeSeq = {
    val condition = S.param("condition").openOr("CU_-5_vs_NRC-1.sig")
    val xmlResponse =
      XML.load(new URL(Props.get("solr.url").openOr("") + "/select?q=" + condition))
    val solrResponse = new SolrResponse(xmlResponse)
//    println("SOLR HEADERS: " + solrResponse.responseHeader)
//    println("SOLR DOCS: ")
//    solrResponse.documents.foreach(println _)
    if (solrResponse.documents.length == 0) {
      <div>
        <span class="echidna-text">No information available</span>
      </div>
    } else {
      val doc = solrResponse.documents(0)
      val conditionName = doc.values.filter(_.name == "condition_name")(0)
      val groupNames    = doc.values.filter(_.name == "group_name")(0)
      val perturbations = doc.values.filter(_.name == "perturbation")(0)
      val properties    = doc.values.filter(_.name == "property_value")(0)
      <div>
        <div>
          <span class="echidna-caption">Condition: </span><span class="echidna-text">{conditionName.asInstanceOf[SolrPrimitiveValue].value}</span>
        </div>
        <div>
          <span class="echidna-caption">Groups: </span><span class="echidna-text">{collectionString(groupNames.asInstanceOf[SolrCollection])}</span>
        </div>
        <div>
          <span class="echidna-caption">Perturbations: </span><span class="echidna-text">{collectionString(perturbations.asInstanceOf[SolrCollection])}</span>
        </div>
        <div>
          <span class="echidna-caption">Properties: </span><span class="echidna-text">{collectionString(properties.asInstanceOf[SolrCollection])}</span>
        </div>
      </div>
    }
  }
}
