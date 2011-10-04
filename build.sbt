name := "pebble"

scalaVersion := "2.9.1"

seq(webSettings :_*)

jettyScanDirs := Nil

resolvers ++= Seq(
  "Java.net Maven2 Repository" at "http://scala-tools.org/repo-releases/net/liftweb/",
  "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"
  )

libraryDependencies ++= {
  val liftVersion = "2.4-M4"
  Seq(
    "net.liftweb" %% "lift-webkit" % liftVersion % "compile->default",
    "net.liftweb" %% "lift-mapper" % liftVersion % "compile->default",
    "org.mortbay.jetty" % "jetty" % "6.1.26" % "jetty",
    "junit" % "junit" % "4.8.2" % "test->default",
    "org.slf4j" % "slf4j-jdk14" % "1.6.1" % "compile->default",
    "org.scalatest" % "scalatest_2.9.0" % "1.4.1" % "test->default",
    //"com.h2database" % "h2" % "1.3.146" % "compile->default",
    "org.systemsbiology" % "isb-dataformats" % "1.0-SNAPSHOT",
    "org.systemsbiology" % "scala-solr-simple" % "1.0-SNAPSHOT",
    "mysql" % "mysql-connector-java" % "5.1.16"
  )
}

