package org.systemsbiology.pebble.model.gwap

import net.liftweb.mapper._
import org.systemsbiology.formats.common._

case object GWAPConnectionIdentifier extends ConnectionIdentifier {
  def jndiName: String = "gwap"
}

object Condition extends Condition with LongKeyedMetaMapper[Condition] {
  override def dbTableName = "conditions"
  override def dbDefaultConnectionIdentifier = GWAPConnectionIdentifier
}

class Condition extends LongKeyedMapper[Condition] with IdPK with OneToMany[Long, Condition] {
  def getSingleton = Condition
  object name extends MappedString(this, 255)

  object features extends MappedOneToMany(Feature, Feature.condition)
}

object Feature extends Feature with LongKeyedMetaMapper[Feature] {
  override def dbTableName = "features"
  override def dbDefaultConnectionIdentifier = GWAPConnectionIdentifier
}

class Feature extends LongKeyedMapper[Feature] with IdPK {
  def getSingleton = Feature

  object dataType extends MappedInt(this) {
    override def dbColumnName = "data_type"
  }
  object value extends MappedDouble(this)

  object condition extends MappedLongForeignKey(this, Condition) {
    override def dbColumnName = "condition_id"
  }
  object gene extends MappedLongForeignKey(this, Gene) {
    override def dbColumnName = "gene_id"
  }
}

object Gene extends Gene with LongKeyedMetaMapper[Gene] {
  override def dbTableName = "genes"
  override def dbDefaultConnectionIdentifier = GWAPConnectionIdentifier
}

class Gene extends LongKeyedMapper[Gene] with IdPK {
  def getSingleton = Gene

  object name     extends MappedString(this, 255)
  object alias    extends MappedString(this, 255)
}

object GWAPDatabase {
  def measurementFor(condition: String,
                     Vng2GeneNameMap: Map[String, String]): GeneExpressionMeasurement = {
    var vngNames: List[String] = Nil
    val dbCond = Condition.find(By(Condition.name, condition))

    if (dbCond != None) {
      val genes = extractGenesFromCondition(dbCond.get)
      var vngNames: List[String] = Nil
      for (gene <- genes) vngNames ::= gene.name
      val geneNames = vngNames.map(vngName => Vng2GeneNameMap(vngName))
      val result = new MutableGeneExpressionMeasurement(vngNames.toArray,
                                                        geneNames.toArray,
                                                        Array(condition))
      
      val numRows = genes.length

      for (row <- 0 until numRows) {
        val features = dbCond.get.features
        val log10ratios = features.filter(f => f.dataType == 1)
        val lambdas = features.filter(f =>  f.dataType == 2)

        result(row, 0) = GeneExpressionValue(log10ratios(row).value, lambdas(row).value)
      }
      result
    } else null
  }

  private def extractGenesFromCondition(condition: Condition): List[Gene] = {
    var result : List[Gene] = Nil
    val lambdas = condition.features.filter(f =>  f.dataType == 2)
    for (lambda <- lambdas) result ::= lambda.gene.obj.get
    result
  }
}
