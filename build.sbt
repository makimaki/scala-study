name := "scala-study"

cancelable in Global := true

lazy val AllTest = "test,it"

lazy val AllConfigurations = "test->test;it->test;it->it;compile->compile"

lazy val scalaSettings = Seq(
  scalaVersion := "2.13.2"
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
      // @formatter:off
      // @formatter:on
    )
  )

/** Scalatra */
lazy val batch = Project(id = "scalatra", base = file("./scalatra"))
  .enablePlugins(JettyPlugin)
  .dependsOn(shared % AllConfigurations)
  .settings(
    commonSettings,
    // HACK: commonSettings(itSettings) で Play Framework の構成に合わせてしまっているので、元に戻す
    scalaSource in IntegrationTest := baseDirectory.value / "src" / "it" / "scala",
    resourceDirectory in IntegrationTest := baseDirectory.value / "src" / "it" / "resources",
    assemblyJarName in assembly := s"scala-study-scalatra.jar",
    test in assembly := {},
    containerPort := 9000,
    libraryDependencies ++= Seq(
      // @formatter:off
      "com.typesafe.play"            %% "play-ahc-ws-standalone"             % "2.1.2",
      "org.eclipse.jetty"             % "jetty-webapp"                       % "9.4.29.v20200521",
      "javax.servlet"                 % "javax.servlet-api"                  % "4.0.1",
      "org.scalatra"                 %% "scalatra"                           % "2.7.0"
      // @formatter:on
    ),
    // ライブラリ依存関係が重複している際に解決するためのルールを設定
    // 参考: http://qiita.com/ytanak/items/97ecc67786ed7c5557bb
    assemblyMergeStrategy in assembly := {
      // @formatter:off
      case PathList("javax", "servlet", _ @ _*)                => MergeStrategy.first
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
      // @formatter:on
    }
  )

/** Play Framework */
lazy val messageApi = Project(id = "play", base = file("./play"))
  .enablePlugins(PlayScala)
  .dependsOn(shared % AllConfigurations)
  .settings(
    commonSettings,
    packageName in Universal := "scala-study-play",
    libraryDependencies ++= Seq(
      // @formatter:off
      ws,
      guice,
      "com.typesafe.play" %% "play-json" % "2.8.1"
      // @formatter:on
    )
  )
