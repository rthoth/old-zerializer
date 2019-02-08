ThisBuild / scalaVersion := "2.12.7"
ThisBuild / organization := "com.github.rthoth"

lazy val root = (project in file("."))
  .settings(
    name := "zerializer",
    version := "1.0.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.0.5"
    )
  )
  .enablePlugins(spray.boilerplate.BoilerplatePlugin)

lazy val sample = (project in file("sample"))
  .settings(
    name := "zerializer-sample",
    version := "1.0.0-SNAPSHOT"
  ).dependsOn(root)
