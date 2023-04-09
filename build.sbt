ThisBuild / scalaVersion := "3.2.2"

ThisBuild / organization := "one.estrondo"
ThisBuild / version      := "0.0.1-SNAPSHOT"

ThisBuild / scalacOptions ++= Seq(
  "-explain"
)

lazy val root = project
  .in(file("."))
  .settings(
    name := "root"
  )
  .aggregate(
    core,
    `test-kit`
  )

lazy val core = project
  .in(file("core"))
  .settings(
    name                     := "jotawt",
    isSnapshot               := true,
    libraryDependencies ++= Seq(
      "org.specs2"   %% "specs2-core"   % "5.1.0" % Test,
      "one.estrondo" %% "sweet-mockito" % "1.2.0" % Test
    ),
    Test / parallelExecution := false
  )
  .dependsOn(
    `test-kit` % Test
  )

lazy val `test-kit` = project
  .in(file("test-kit"))
  .settings(
    name := "jotawt-test-kit"
  )
