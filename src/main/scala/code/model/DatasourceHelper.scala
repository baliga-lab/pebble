package code.model

import java.io._
import net.liftweb.util.Props
import net.liftweb.mapper._

import org.systemsbiology.formats.common._
import org.systemsbiology.formats.sbeams._
import code.model._

object DatasourceHelper {
  private val ArraysDirectory = Props.get("arrays.dir").get
  private val OligoMapDirectory = new File("%s/Slide_Templates".format(ArraysDirectory))
  private val oligoMapDb = new OligoMapDatabase(OligoMapDirectory)
  val OligoMap = oligoMapDb.latestMap
  val Vng2GeneNameMap = oligoMapDb.latestVng2GeneNameMap

  private def sbeamsMatrixOutputFileFor(projectId: String, timestamp: String): File = {
    new File("%s/Pipeline/output/project_id/%s/%s/matrix_output".format(ArraysDirectory,
                                                                        projectId, timestamp))
  }
  def sbeamsMeasurementFor(projectId: String, timestamp: String): GeneExpressionMeasurement = {
    val matrix = DataMatrixReader.createFromFile(sbeamsMatrixOutputFileFor(projectId, timestamp))
    new SbeamsMeasurement(OligoMap, matrix)
  }
  val LegacyDirectory = new File("%s/emi/halobacterium/repos".format(ArraysDirectory))

  def sbeamsMeasurementDbFor(projectId: String, timestamp: String,
                             conditions: List[String]): GeneExpressionMeasurement = {
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
