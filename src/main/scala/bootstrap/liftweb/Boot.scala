package bootstrap.liftweb

import net.liftweb._
import util._
import Helpers._

import common._
import http._
import sitemap._
import mapper._

//import net.liftweb.mongodb._

import code.view._

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {

  def buildSitemap {
    val entries = List(Menu.i("Home") / "index",
                       Menu.i("Dmv") / "dmv",
                       Menu.i("About") / "about") // ::: User.sitemap
    LiftRules.setSiteMap(SiteMap(entries:_*))
  }

/*
  def initMongoDb {
    MongoDB.defineDb(DefaultMongoIdentifier,
                     MongoAddress(MongoHost("localhost", 27017), "echidna"))
    LiftRules.unloadHooks.append(MongoDB.close _)
  }*/

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
  def addRestServices {
    LiftRules.dispatch.append(GeneExpressionRestService)
    LiftRules.statelessDispatchTable.append(GeneExpressionRestService)
  }

  def boot {
    initJndi
    //initMapper
    //initMongoDb

    // where to search snippet
    LiftRules.addToPackages("code")

    buildSitemap

    // Show the spinny image when an Ajax call starts
    LiftRules.ajaxStart =
      Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)
    
    // Make the spinny image go away when it ends
    LiftRules.ajaxEnd =
      Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)
    
    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    // What is the function to test if a user is logged in?
    //LiftRules.loggedInTest = Full(() => User.loggedIn_?)

    LiftRules.htmlProperties.default.set((r: Req) =>
      new Html5Properties(r.userAgent))

    // Make a transaction span the whole HTTP request
    S.addAround(DB.buildLoanWrapper)

    // other init
    addRestServices
  }
}
