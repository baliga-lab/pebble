package bootstrap.liftweb

import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.sitemap._
import net.liftweb.mapper._
import net.liftweb.util._
import net.liftweb.util.Helpers._

import code.view._

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {

  def boot {
    initJndi
    //initMapper

    // code search path for snippets and views
    LiftRules.addToPackages("code")

    buildSitemap
    initAjaxSettings

    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    // What is the function to test if a user is logged in?
    //LiftRules.loggedInTest = Full(() => User.loggedIn_?)

    LiftRules.htmlProperties.default.set((r: Req) =>
      new Html5Properties(r.userAgent))

    // Make a transaction span the whole HTTP request
    S.addAround(DB.buildLoanWrapper)
    
    setErrorHandlers
    addRestServices
  }

  def buildSitemap {
    val entries = List(Menu.i("Home") / "index",
                       Menu.i("Dmv") / "dmv",
                       Menu.i("Echidna") / "echidna",
                       Menu.i("About") / "about") // ::: User.sitemap
    LiftRules.setSiteMap(SiteMap(entries:_*))
  }

  // it seems that this is the minimal amount of code that Lift needs for JNDI/JDBC
  // configuration without falling on its head
  def initJndi {
    if (!DB.jndiJdbcConnAvailable_?) {
      val vendor = 
	      new StandardDBVendor(Props.get("db.driver") openOr "org.h2.Driver",
			                       Props.get("db.url") openOr 
			                       "jdbc:h2:lift_proto.db;AUTO_SERVER=TRUE",
			                       Props.get("db.user"), Props.get("db.password"))

      LiftRules.unloadHooks.append(vendor.closeAllConnections_! _)
      DB.defineConnectionManager(DefaultConnectionIdentifier, vendor)
    }
  }
/*
  def initMapper {
    // Use Lift's Mapper ORM to populate the database
    // you don't need to use Mapper to use Lift... use
    // any ORM you want
    Schemifier.schemify(true, Schemifier.infoF _, User)
  }
*/
  private def setErrorHandlers {
    LiftRules.uriNotFound.prepend(NamedPF("404handler") {
      case (req, failure) =>
        NotFoundAsTemplate(ParsePath(List("404"), "html", false, false))
    })
    // When the REST service is called with an invalid request, we return
    // a 404 response instead of dumping the output to the user
    LiftRules.exceptionHandler.prepend {
      case (Props.RunModes.Production, Req(path, "", GetRequest), exception: Throwable) => {
        Logger("Boot").error("Exception occurred", exception)
        NotFoundResponse("Error while retrieving URI")
      }
    }
  }

  private def addRestServices {
    // Pebble's services are stateless
    // note that in order to achieve cross-domain AJAX calls and ensure
    // Pebble's embeddability, our HTTP responses need to include the
    // "Access-Control-Allow-Origin" header
    LiftRules.statelessDispatchTable.append(GeneExpressionRestService)
    LiftRules.statelessDispatchTable.append(HighchartsDataRestService)
    LiftRules.statelessDispatchTable.append(HtmlSnippetRestService)

    // from Lift mailing list:
    // adding the services to the normal dispatch table
    // this allows us to easily set headers like "Access-Control-Allow-Origin", so
    // we can use it in AJAX. If we need stateless dispatch, we can use JsonResponse
    //LiftRules.dispatch.append(GeneExpressionRestService)
    //LiftRules.dispatch.append(HighchartsDataRestService)
  }

  private def initAjaxSettings {
    LiftRules.ajaxStart =
      Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)    
    LiftRules.ajaxEnd =
      Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)    
  }
}
