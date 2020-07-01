name := "scala-study"

cancelable in Global := true

lazy val AllTest = "test,it"

lazy val scala213 = "2.13.2"
lazy val scala212 = "2.12.11"
lazy val scala211 = "2.11.12"
lazy val supportedScalaVersions = List(scala213, scala212, scala211)
lazy val scalaSettings = Seq(
  scalaVersion := scala213,
  crossScalaVersions := supportedScalaVersions
)

lazy val commonSettings = scalaSettings ++ testSettings ++ itSettings ++ Seq(
  scalacOptions ++= Seq("-encoding", "UTF-8"),
  scalacOptions += "-feature",
  scalacOptions in Default += "-deprecation",
  javacOptions ++= Seq("-encoding", "UTF-8"),
  javacOptions ++= Seq("-source", "11"),
  javacOptions ++= Seq("-target", "11"),
  javacOptions += "-Xlint",
  scalafmtOnCompile in ThisBuild := true,
  sources in (Compile, doc) := Seq.empty,
  publishArtifact in (Compile, packageDoc) := false
)

lazy val testSettings = inConfig(Test)(
  Seq(
    // テスト中のアプリケーションログを表示したい場合は次行をコメントアウト
    // ただし batch はコメントアウトしても表示されないので、
    // shared/src/test/resources/logback-test.xml を消して実行します。
    javaOptions += "-Dlogger.resource=logback-test.xml",
    javaOptions += "-Dconfig.resource=application.ut.conf",
    logBuffered := false,
    testOptions += Tests.Argument(TestFrameworks.Specs2, "junitxml", "console"),
    fork := true
  )
)

lazy val itSettings = Defaults.itSettings ++ inConfig(IntegrationTest)(
  Seq(
    // テスト中のアプリケーションログを表示したい場合は次行をコメントアウト
    // ただし batch はコメントアウトしても表示されないので、
    // shared/src/test/resources/logback-test.xml を消して実行します。
    javaOptions += "-Dlogger.resource=logback-test.xml",
    javaOptions += "-Dconfig.resource=application.it.conf",
    logBuffered := false,
    javaSource := baseDirectory.value / "test-integration",
    scalaSource := javaSource.value,
    resourceDirectory := baseDirectory.value / "test-integration" / "resources",
    testOptions += Tests.Argument(TestFrameworks.Specs2, "junitxml", "console"),
    fork := true,
    parallelExecution := false
  ) ++ org.scalafmt.sbt.ScalafmtPlugin.scalafmtConfigSettings
)

lazy val shared = Project(id = "shared", base = file("./shared"))
  .configs(IntegrationTest)
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
//      "com.typesafe.play" %% "play-json" % "2.8.1"
      "com.typesafe.play" %% "play-json" % "2.7.4"
    )
  )

/** Scalatra */
lazy val batch = Project(id = "scalatra", base = file("./scalatra"))
  .enablePlugins(JettyPlugin)
  .dependsOn(shared)
  .settings(
    commonSettings,
    // HACK: commonSettings(itSettings) で Play Framework の構成に合わせてしまっているので、元に戻す
    scalaSource in IntegrationTest := baseDirectory.value / "src" / "it" / "scala",
    resourceDirectory in IntegrationTest := baseDirectory.value / "src" / "it" / "resources",
    assemblyJarName in assembly := s"scala-study-scalatra.jar",
    test in assembly := {},
    containerPort := 9000,
    libraryDependencies ++= Seq(
//      "com.typesafe.play" %% "play-ahc-ws-standalone" % "2.1.2",
//      "com.typesafe.play" %% "play-ws-standalone-json" % "2.1.2",
      "com.typesafe.play" %% "play-ahc-ws-standalone" % "2.0.8",
      "com.typesafe.play" %% "play-ws-standalone-json" % "2.0.8",
      "org.eclipse.jetty" % "jetty-webapp" % "9.4.29.v20200521",
      "javax.servlet" % "javax.servlet-api" % "3.1.0",
      "org.scalatra" %% "scalatra" % "2.7.0",
      "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2"
    ),
    // ライブラリ依存関係が重複している際に解決するためのルールを設定
    // 参考: http://qiita.com/ytanak/items/97ecc67786ed7c5557bb
    assemblyMergeStrategy in assembly := {
      case PathList("javax", "servlet", _ @_*)                 => MergeStrategy.first
      case PathList(ps @ _*) if ps.last endsWith ".properties" => MergeStrategy.first
      case PathList(ps @ _*) if ps.last endsWith ".xml"        => MergeStrategy.first
      case PathList(ps @ _*) if ps.last endsWith ".types"      => MergeStrategy.first
      case PathList(ps @ _*) if ps.last endsWith ".class"      => MergeStrategy.first
      case PathList(ps @ _*) if ps.last endsWith ".proto"      => MergeStrategy.first
      case "application.conf"                                  => MergeStrategy.concat
      case "unwanted.txt"                                      => MergeStrategy.discard
      case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
    }
  )

/** Play Framework */
lazy val messageApi = Project(id = "play", base = file("./play"))
  .enablePlugins(PlayScala)
  .dependsOn(shared)
  .settings(
    commonSettings,
    packageName in Universal := "scala-study-play",
    libraryDependencies ++= Seq(
      ws,
      guice
    )
  )

lazy val gatling = Project(id = "gatling", base = file("./gatling"))
  .enablePlugins(GatlingPlugin)
  .settings(
    scalaVersion := "2.12.11",
    scalafmtOnCompile in ThisBuild := true,
    libraryDependencies ++= Seq(
      "io.gatling.highcharts" % "gatling-charts-highcharts" % "3.3.1",
      "io.gatling" % "gatling-test-framework" % "3.3.1",
      "com.typesafe.play" %% "play-json" % "2.8.1"
    ).map(_ % "test,it")
  )
