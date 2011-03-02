package code.model

import net.liftweb.util.Props
import org.systemsbiology.formats.sbeams._
import java.io._

object DatasourceHelper {
  private val ArraysDirectory = Props.get("arrays.dir").get
  private val OligoMapDirectory = new File("%s/Slide_Templates".format(ArraysDirectory))
  private val OligoMap = new OligoMapDatabase(OligoMapDirectory).latestMap

  private def sbeamsMatrixOutputFileFor(projectId: String, timestamp: String): File = {
    new File("%s/Pipeline/output/project_id/%s/%s/matrix_output".format(ArraysDirectory,
                                                                        projectId, timestamp))
  }
  def sbeamsMeasurementFor(projectId: String, timestamp: String): SbeamsMeasurement = {
    val matrix = DataMatrixReader.createFromFile(sbeamsMatrixOutputFileFor(projectId, timestamp))
    new SbeamsMeasurement(OligoMap, matrix)
  }
  val LegacyDirectory = new File("%s/emi/halobacterium/repos".format(ArraysDirectory))
}
