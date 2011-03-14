package code.snippet

import scala.xml.NodeSeq

import net.liftweb.http.js.JsCmds.{Script,JsCrVar}
import net.liftweb.http.S

/**
 * A generic snippet that renders the application's context path into a Javascript
 * variable.
 * The snippet expects the "name" to be set, which defines the Javascript variable
 * which will be used to define the context path.
 */
class ContextPathJs {
  def variable(in: NodeSeq) = {
    Script(JsCrVar(S.attr("name").openOr("contextPath"), S.contextPath))
  }
}
