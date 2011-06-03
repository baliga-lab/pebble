package org.systemsbiology.pebble.model.echidna

import net.liftweb.mapper._
import org.systemsbiology.formats.common._

object Condition extends Condition with LongKeyedMetaMapper[Condition] {
  override def dbTableName = "conditions"
}

class Condition extends LongKeyedMapper[Condition] with IdPK with OneToMany[Long, Condition] {
  def getSingleton = Condition
  object name extends MappedString(this, 255)
  object sbeamsProjectId extends MappedInt(this) {
    override def dbColumnName = "sbeams_project_id"
  }
  object sbeamsTimestamp extends MappedString(this, 255) {
    override def dbColumnName = "sbeams_timestamp"
  }

  object features extends MappedOneToMany(Feature, Feature.condition)
}

object Feature extends Feature with LongKeyedMetaMapper[Feature] {
  override def dbTableName = "features"
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
}

class Gene extends LongKeyedMapper[Gene] with IdPK {
  def getSingleton = Gene

  object name     extends MappedString(this, 255)
  object alias    extends MappedString(this, 255)
  object geneName extends MappedString(this, 255) {
    override def dbColumnName = "gene_name"
  }
}

object EchidnaDatabase {
  def sbeamsMeasurementFor(projectId: String, timestamp: String,
                           conditions: List[String],
                           Vng2GeneNameMap: Map[String, String]): GeneExpressionMeasurement = {
    var vngNames: List[String] = Nil
    val dbConds = Condition.findAll(By(Condition.sbeamsProjectId, projectId.toInt),
                                    By(Condition.sbeamsTimestamp, timestamp),
                                    ByList(Condition.name, conditions))

    if (dbConds.length > 0) {
      val genes = extractGenesFromCondition(dbConds(0))
      var vngNames: List[String] = Nil
      for (gene <- genes) {
        vngNames ::= gene.name
      }
      val geneNames = vngNames.map(vngName => Vng2GeneNameMap(vngName))
      val result = new MutableGeneExpressionMeasurement(vngNames.toArray,
                                                        geneNames.toArray,
                                                        conditions.toArray)
      
      val numRows = genes.length

      for (row <- 0 until numRows) {
        for (col <- 0 until dbConds.length) {
          val features = dbConds(0).features
          val log10ratios = features.filter(f => f.dataType == 1)
          val lambdas = features.filter(f =>  f.dataType == 2)

          result(row, col) = GeneExpressionValue(log10ratios(row).value, lambdas(row).value)
        }
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
