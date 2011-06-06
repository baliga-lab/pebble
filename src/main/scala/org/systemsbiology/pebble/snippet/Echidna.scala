package org.systemsbiology.pebble.snippet

import scala.xml.{Node, NodeSeq, Text}

import net.liftweb.common._
import net.liftweb.util._
import net.liftweb.util.Helpers._
import net.liftweb.http._

import java.net.URL
import scala.xml.{XML,Elem}

import org.systemsbiology.solr._

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
    println("Echidna.condition(), param = <<" + condition + ">>")
    val xmlResponse =
      XML.load(new URL(Props.get("solr.url").openOr("") + "/select?q=" + urlEncode(condition)))
    val solrResponse = new SolrResponse(xmlResponse)
    //println("SOLR HEADERS: " + solrResponse.responseHeader)
    //println("SOLR DOCS: ")
    //solrResponse.documents.foreach(println _)
    if (solrResponse.documents.length == 0) {
      <div>
        <div>
          <span class="echidna-caption">Condition: </span><span class="echidna-text">{condition}</span>
        </div>
        <span class="echidna-text">No information available</span>
      </div>
    } else {
      val doc = solrResponse.documents(0)
      val groupNames    = extractGroupNames(doc)
      val perturbations =
        doc.values.find(_.name == Some("perturbation")).getOrElse(SolrCollection("lst", Some("perturbation"), Nil))
      val properties    = doc.values.filter(_.name == Some("property_value"))(0)
      <div>
        <div>
          <span class="echidna-caption">Condition: </span><span class="echidna-text">{condition}</span>
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

  private def extractGroupNames(doc: SolrCollection) = {
    val results = doc.values.filter(_.name == Some("group_name"))
    if (results.length == 0) SolrCollection("arr", Some("group_name"), Nil) else results(0)
  }
}
