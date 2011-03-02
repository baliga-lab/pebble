package code.view

import net.liftweb.http.rest.RestHelper

/**
 * A very simple JSON REST service written for Lift.
 * We set up the routes here that provide information about experiment data
 */
object GeneExpressionRestService extends RestHelper {
  import LegacyDataProvider._
  import SbeamsDataProvider._

  // routes for specific condition, we have to use two serve clauses and declare the
  // longer match in the first clause in order to "overload" paths
  serve {
    case "api" :: "1" :: "sbeams" :: projectId :: timestamp :: "measurements" :: condition ::
      _ Get _ => sbeamsJSON(projectId, timestamp, condition)
    case "api" :: "1" :: "pre-sbeams" :: baseName :: "measurements" :: condition ::
      _ Get _ => legacyJSON(baseName, condition)
  }

  // routes for everything contained in an array
  serve {
    case "api" :: "1" :: "sbeams" :: projectId :: timestamp :: "measurements" ::
      _ Get _ => sbeamsJSON(projectId, timestamp)
    case "api" :: "1" :: "pre-sbeams" :: baseName :: "measurements" ::
      _ Get _ => legacyJSON(baseName)

    // we might want to know the conditions contained in a batch
    case "api" :: "1" :: "sbeams" :: projectId :: timestamp :: "conditions" ::
      _ Get _ => sbeamsConditions2JSON(projectId, timestamp)
    case "api" :: "1" :: "pre-sbeams" :: baseName :: "conditions" ::
      _ Get _ => legacyConditions2JSON(baseName)
  }
}
