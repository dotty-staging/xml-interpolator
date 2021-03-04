val dottyVersion = "0.24.0-RC1" // dottyLatestNightlyBuild.get

lazy val root = project
  .in(file("."))
  .settings(
    name := "dotty-xml-interpolator",
    version := "0.1.0",

    scalaVersion := dottyVersion,
    scalacOptions ++= Seq(
      "-Xprint-inline"
    ),
    libraryDependencies ++= Seq(
      ("org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2").cross(CrossVersion.for3Use2_13),
      ("org.scala-lang.modules" %% "scala-xml" % "1.2.0").cross(CrossVersion.for3Use2_13),
      "com.novocode" % "junit-interface" % "0.11" % "test"
    )
  )
