package bootstrap.liftweb

import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.sitemap._
import net.liftweb.mapper._
import net.liftweb.util._
import net.liftweb.util.Helpers._

import org.systemsbiology.pebble.view._
import org.systemsbiology.pebble.model.echidna.EchidnaConnectionIdentifier
import org.systemsbiology.pebble.model.gwap.GWAPConnectionIdentifier

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {

  def boot {
    initJndi
    initMapper

    // code search path for snippets and views
    LiftRules.addToPackages("org.systemsbiology.pebble")

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
                       Menu.i("Gene Functions") / "gene_functions",
                       Menu.i("About") / "about") // ::: User.sitemap
    LiftRules.setSiteMap(SiteMap(entries:_*))
  }

  // it seems that this is the minimal amount of code that Lift needs for JNDI/JDBC
  // configuration without falling on its head
  def initJndi {
    //DefaultConnectionIdentifier.jndiName = "jdbc/echidna"
    println("INIT JNDI: Database is " + Props.get("db.url"))

    if (!DB.jndiJdbcConnAvailable_?) {
      val echidnaDB = 
	      new StandardDBVendor(Props.get("db.driver") openOr "org.h2.Driver",
			                       Props.get("echidna.db.url") openOr 
			                       "jdbc:h2:lift_proto.db;AUTO_SERVER=TRUE",
			                       Props.get("echidna.db.user"), Props.get("echidna.db.password"))
      val gwapDB = 
	      new StandardDBVendor(Props.get("db.driver") openOr "org.h2.Driver",
			                       Props.get("gwap.db.url") openOr 
			                       "jdbc:h2:lift_proto.db;AUTO_SERVER=TRUE",
			                       Props.get("gwap.db.user"), Props.get("gwap.db.password"))

      LiftRules.unloadHooks.append(echidnaDB.closeAllConnections_! _)
      LiftRules.unloadHooks.append(gwapDB.closeAllConnections_! _)
      DB.defineConnectionManager(DefaultConnectionIdentifier, echidnaDB)
      DB.defineConnectionManager(EchidnaConnectionIdentifier, echidnaDB)
      DB.defineConnectionManager(GWAPConnectionIdentifier, gwapDB)
    }
  }

  def initMapper {
    //Schemifier.schemify(true, Schemifier.infoF _, User)
  }

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
    println("ADDING REST SERVICES")
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
