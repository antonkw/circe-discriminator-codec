import java.time.format.DateTimeFormatter
import java.time.{ ZoneOffset, ZonedDateTime }
import sbt.Package.ManifestAttributes
import sbtcrossproject.CrossType
import sbtcrossproject.CrossPlugin.autoImport.crossProject

name := "circe-tagged-adt-codec-root"

ThisBuild / version := "0.0.7-SNAPSHOT"

ThisBuild / versionScheme := Some("semver-spec")

ThisBuild / organization := "io.github.antonkw"

ThisBuild / homepage := Some( url( "https://github.com/antonkw/circe-discriminator-codec" ) )
sonatypeCredentialHost := "s01.oss.sonatype.org"
sonatypeRepository := "https://s01.oss.sonatype.org/service/local"

ThisBuild / licenses := Seq(
  (
    "Apache License v2.0",
    url( "http://www.apache.org/licenses/LICENSE-2.0.html" )
  )
)

ThisBuild / crossScalaVersions := Seq( "2.12.13", "2.13.6" )

ThisBuild / scalaVersion := (ThisBuild / crossScalaVersions).value.head

ThisBuild / exportJars := true

ThisBuild / publishTo := {
  val nexus = "https://s01.oss.sonatype.org/"
  if (isSnapshot.value)
    Some( "snapshots" at nexus + "content/repositories/snapshots" )
  else
    Some( "releases" at nexus + "service/local/staging/deploy/maven2" )
}

ThisBuild / pomExtra := (
  <scm>
	<url>https://github.com/antonkw/circe-discriminator-codec</url>
	<connection>scm:git:https://github.com/antonkw/circe-discriminator-codec</connection>
	<developerConnection>scm:git:https://github.com/antonkw/circe-discriminator-codec</developerConnection>
  </scm>
  <developers>
	<developer>
	<id>antonkw</id>
	<name>Anton Kovalevsky</name>
	<url>https://antonkw.github.io</url>
	</developer>
  </developers>
)

ThisBuild / resolvers ++= Seq(
  "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/",
  "Sonatype OSS Releases" at "https://s01.oss.sonatype.org/content/repositories/releases/"
)

def baseScalacOptions(scalaVersionStr: String) = Seq(
  "-deprecation",
  "-unchecked",
  "-feature",
  "-language:higherKinds",
) ++ (CrossVersion.partialVersion( scalaVersionStr ) match {
  case Some( ( 2, n ) ) if n >= 13 => Seq( "-Xsource:3" )
  case Some( ( 2, n ) ) if n < 13  => Seq( "-Ypartial-unification" )
  case _                           => Seq()
})

ThisBuild / javacOptions ++= Seq(
  "-Xlint:deprecation",
  "-source",
  "1.8",
  "-target",
  "1.8",
  "-Xlint"
)

val scalacJsOptions = Seq()

ThisBuild / packageOptions := Seq(
  ManifestAttributes(
    ( "Build-Jdk", System.getProperty( "java.version" ) ),
    (
      "Build-Date",
      ZonedDateTime
        .now( ZoneOffset.UTC )
        .format( DateTimeFormatter.ISO_OFFSET_DATE_TIME )
    )
  )
)

val circeVersion = "0.14.0"
val scalaTestVersion = "3.1.1"

val baseJvmDependencies =
  Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser",
    "io.circe" %% "circe-generic-extras"
  ).map( _ % circeVersion ) ++
    Seq(
      "org.scalactic" %% "scalactic",
      "org.scalatest" %% "scalatest"
    ).map( _ % scalaTestVersion % Test )

val baseJsDependencies =
  Def.setting(
    Seq(
      "io.circe" %%% "circe-core",
      "io.circe" %%% "circe-generic",
      "io.circe" %%% "circe-parser",
      "io.circe" %%% "circe-generic-extras"
    ).map( _ % circeVersion ) ++
      Seq(
        "org.scalactic" %%% "scalactic",
        "org.scalatest" %%% "scalatest"
      ).map( _ % scalaTestVersion % Test )
  )

lazy val circeTaggedAdtCodecRoot =
  (project in file( "." ))
    .aggregate( circeTaggedAdtCodecModels, circeTaggedAdtCodecMacros, circeTaggedAdtCodecLib )
    .settings(
      publish := {},
      publishLocal := {},
      crossScalaVersions := List(),
      scalacOptions := baseScalacOptions(scalaVersion.value)
    )

lazy val circeTaggedAdtCodecModels =
  project
    .in( file( "models" ) )
    .aggregate( circeTaggedAdtCodecModelsJVM, circeTaggedAdtCodecModelsJS )
    .settings(
      name := "circe-tagged-adt-codec-models",
      publish := {},
      publishLocal := {},
      crossScalaVersions := List(),
      scalacOptions := baseScalacOptions(scalaVersion.value)
    )

lazy val circeTaggedAdtCodecModelsCross = crossProject( JSPlatform, JVMPlatform )
  .withoutSuffixFor( JVMPlatform )
  .crossType( CrossType.Full )
  .in( file( "models" ) )
  .settings(
    name := "circe-tagged-adt-codec-models",
    scalacOptions := baseScalacOptions(scalaVersion.value)
  )
  .jvmSettings(
    libraryDependencies ++= baseJvmDependencies ++ Seq()
  )
  .jsSettings(
    libraryDependencies ++= baseJsDependencies.value ++ Seq(),
    scalacOptions ++= scalacJsOptions
  )

lazy val circeTaggedAdtCodecModelsJVM = circeTaggedAdtCodecModelsCross.jvm
lazy val circeTaggedAdtCodecModelsJS = circeTaggedAdtCodecModelsCross.js

lazy val circeTaggedAdtCodecMacros =
  (project in file( "macros" ))
    .settings(
      name := "circe-tagged-adt-codec-macros",
      libraryDependencies ++= baseJvmDependencies ++ Seq(
        "org.scala-lang" % "scala-reflect" % scalaVersion.value
      ),
      scalacOptions := baseScalacOptions(scalaVersion.value)
    )
    .dependsOn( circeTaggedAdtCodecModelsJVM )

lazy val circeTaggedAdtCodecLib =
  project
    .in( file( "lib" ) )
    .aggregate( circeTaggedAdtCodecLibJVM, circeTaggedAdtCodecLibJS )
    .settings(
      name := "circe-tagged-adt-codec",
      publish := {},
      publishLocal := {},
      crossScalaVersions := List(),
      scalacOptions := baseScalacOptions(scalaVersion.value)
    )

lazy val circeTaggedAdtCodecLibCross = crossProject( JSPlatform, JVMPlatform )
  .withoutSuffixFor( JVMPlatform )
  .crossType( CrossType.Full )
  .in( file( "lib" ) )
  .settings(
    name := "circe-tagged-adt-codec",
    scalacOptions := baseScalacOptions(scalaVersion.value)
  )
  .jvmSettings(
    libraryDependencies ++= baseJvmDependencies ++ Seq()
  )
  .jsSettings(
    libraryDependencies ++= baseJsDependencies.value ++ Seq(),
    scalacOptions ++= scalacJsOptions
  )
  .jvmConfigure( _.dependsOn( circeTaggedAdtCodecModelsJVM, circeTaggedAdtCodecMacros ) )
  .jsConfigure( _.dependsOn( circeTaggedAdtCodecModelsJS, circeTaggedAdtCodecMacros ) )

lazy val circeTaggedAdtCodecLibJVM = circeTaggedAdtCodecLibCross.jvm
lazy val circeTaggedAdtCodecLibJS = circeTaggedAdtCodecLibCross.js

addCommandAlias(
  "publishScalaJsOnly",
  s";+${circeTaggedAdtCodecModelsJS.id}/publishSigned;+${circeTaggedAdtCodecLibJS.id}/publishSigned;sonatypeRelease"
)
