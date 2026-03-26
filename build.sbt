resolvers += Resolver.sonatypeCentralRepo("snapshots")

import sbt.Keys.scalaVersion
import scala.language.postfixOps
import scala.scalanative.build.*
import scala.sys.process.*
import scala.language.postfixOps
import bindgen.interface.Binding
import bindgen.plugin.BindgenMode
import com.indoorvivants.detective.Platform

val scala3Version = "3.8.1"

ThisBuild / scalaVersion := scala3Version

val sharedSettings = Seq(
  version := "0.0.1",
  javacOptions ++= Seq("-source", "25", "-target", "25"),
  scalaVersion := scala3Version,
  scalacOptions ++= Seq(
    "-new-syntax",
    "-deprecation",
    "-explain",
    "-explain-cyclic",
    "-source:future",
    // "-Yexplicit-nulls",
    "-Wvalue-discard",
    "-Wunused:all",
    "-experimental"
    // "-language:experimental.captureChecking",
    // "-language:experimental.into",
    // "-language:experimental.pureFunctions",
    // "-language:experimental.modularity",
    // "-language:experimental.multiSpreads",
    // "-language:experimental.subCases",
    // "-language:experimental.relaxedLambdaSyntax",
    // "-language.experimental.separationChecking"
    // "-no-indent",
    // "-rewrite",
  ),
  // set to Debug for compilation details (Info is default)
  logLevel := Level.Info,
  usePipelining := true
)

// deps
//tasks
lazy val appStop = inputKey[Unit]("stop app")
lazy val appRestart = inputKey[Unit]("run app")
lazy val showPid = inputKey[Unit]("show app PID")

lazy val `fast4s-common` =
  crossProject(JSPlatform, JVMPlatform, NativePlatform)
    .crossType(CrossType.Full)
    .in(file("fast4s-common"))
    .settings(sharedSettings *)
    .settings(
      name := "fast4s-common",
      organization := "io.fast4s.common",
      libraryDependencies ++= Seq(
        "org.scalatest" %%% "scalatest" % "3.2.20" % "test"
      )
    )

lazy val `fast4s-data` =
  crossProject(JSPlatform, JVMPlatform, NativePlatform)
    .crossType(CrossType.Full)
    .in(file("fast4s-data"))
    .dependsOn(`fast4s-common`)
    .settings(sharedSettings *)
    .settings(
      name := "fast4s-data",
      organization := "io.fast4s.data",
      libraryDependencies ++= Seq(
        "io.via" %%% "via" % "0.0.1",
        "org.scalatest" %%% "scalatest" % "3.2.20" % "test"
      )
    )

lazy val `fast4s-core` =
  crossProject(JSPlatform, JVMPlatform, NativePlatform)
    .crossType(CrossType.Full)
    .in(file("fast4s-core"))
    .dependsOn(`fast4s-common`)
    .dependsOn(`fast4s-data`)
    .settings(sharedSettings *)
    .settings(
      name := "fast4s-core",
      organization := "io.fast4s.core",
      libraryDependencies ++= Seq(
        "io.via" %%% "via" % "0.0.1",
        "org.scalatest" %%% "scalatest" % "3.2.20" % "test"
      )
    )

lazy val `fast4s-api` =
  crossProject(JSPlatform, JVMPlatform, NativePlatform)
    .crossType(CrossType.Full)
    .in(file("fast4s-api"))
    .dependsOn(`fast4s-core`)
    .settings(sharedSettings *)
    .settings(
      name := "fast4s-api",
      organization := "io.fast4s.api",
      libraryDependencies ++= Seq(
        "io.via" %%% "via" % "0.0.1",
        "org.scalatest" %%% "scalatest" % "3.2.20" % "test"
      )
    )

lazy val `fast4s-beast` =
  crossProject(JSPlatform, JVMPlatform, NativePlatform)
    .crossType(CrossType.Full)
    .in(file("fast4s-beast"))
    .dependsOn(`fast4s-api`)
    .settings(sharedSettings *)
    .settings(
      name := "fast4s-beast",
      organization := "io.fast4s.backend.beast",
      libraryDependencies ++= Seq(
        "org.scalatest" %%% "scalatest" % "3.2.20" % "test"
      )
    )

lazy val `fast4s-node` =
  crossProject(JSPlatform, JVMPlatform, NativePlatform)
    .crossType(CrossType.Full)
    .in(file("fast4s-node"))
    .dependsOn(`fast4s-api`)
    .settings(sharedSettings *)
    .settings(
      name := "fast4s-node",
      organization := "io.fast4s.backend.node",
      libraryDependencies ++= Seq(
        "org.scalatest" %%% "scalatest" % "3.2.20" % "test"
      )
    )

lazy val `fast4s-redis` =
  crossProject(JSPlatform, JVMPlatform, NativePlatform)
    .crossType(CrossType.Full)
    .in(file("fast4s-redis"))
    .settings(sharedSettings *)
    .settings(
      name := "fast4s-redis",
      organization := "io.fast4s.redis",
      libraryDependencies ++= Seq(
        "org.scalatest" %%% "scalatest" % "3.2.20" % "test"
      )
    )
    .nativeEnablePlugins(
      BindgenPlugin,
      VcpkgNativePlugin,
      ScalaNativeJUnitPlugin
    )
    .nativeSettings(
      // vcpkg
      vcpkgDependencies := VcpkgDependencies("hiredis"),
      // vcpkgNativeConfig ~= { _.addRenamedLibrary("hiredis", "hiredis") },

      nativeConfig ~= { c =>
        c.withLinkingOptions(c.linkingOptions.flatMap {
          case "-lresolv-lresolv" => Some("-lresolv")
          case "-lm-lresolv"      => None
          case other              => Some(other)
        } ++ Seq("-lhiredis"))
      },
      bindgenBindings += {
        val actualIncludeFolder = new File(
          vcpkgConfigurator.value.pkgConfig
            .compilationFlags("hiredis")
            .toList
            .filter(_.contains("include/hiredis"))
            .head
            .stripPrefix("-I")
        )
        Binding(actualIncludeFolder / "hiredis.h", "hiredis")
          .withLinkName("hiredis")
          .withCImports(List("hiredis/hiredis.h"))
          .withClangFlags(
            vcpkgConfigurator.value.pkgConfig
              .updateCompilationFlags(List("-std=gnu99"), "hiredis")
              .toList
          )
      }
    )
    .nativeSettings(bindgenSettings)
    .nativeSettings(configurePlatform())

val bindgenSettings = Seq(
  bindgenMode := BindgenMode.Manual(
    scalaDir =
      (Compile / sourceDirectory).value / "scala" / "io" / "fast4s" / "hiredis",
    cDir = (Compile / resourceDirectory).value / "scala-native" / "hiredis"
  ),
  bindgenBindings := {
    bindgenBindings.value.map(_.withNoLocation(true))
  }
)

def configurePlatform(rename: String => String = identity) = Seq(
  nativeConfig := {
    val conf = nativeConfig.value
    val arch64 =
      if (
        Platform.arch == Platform.Arch.Arm && Platform.bits == Platform.Bits.x64
      )
        List("-arch", "arm64")
      else Nil

    conf
      .withLinkingOptions(conf.linkingOptions ++ arch64)
      .withCompileOptions(conf.compileOptions ++ arch64)
  }
)

lazy val `fast4s-example` =
  crossProject(JSPlatform, JVMPlatform, NativePlatform)
    .crossType(CrossType.Full)
    .in(file("fast4s-example"))
    .dependsOn(`fast4s-beast`)
    .dependsOn(`fast4s-node`)
    .dependsOn(`fast4s-api`)
    .settings(sharedSettings *)
    .settings(
      name := "fast4s-example",
      organization := "io.fast4s.example",
      libraryDependencies ++= Seq(
        "io.decoda" %%% "decoda" % "0.0.1",
        "org.scalatest" %%% "scalatest" % "3.2.20" % "test"
      )
    )
    .jsSettings(
      scalaJSUseMainModuleInitializer := true,
      scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) }
    )
    .nativeSettings(
      nativeConfig ~= { c =>
        c.withLTO(LTO.none) // thin
          .withMode(Mode.debug) // releaseFast
          .withGC(GC.immix)
      },
      appStop := {
        val logger: TaskStreams = streams.value
        val shell: Seq[String] =
          if (sys.props("os.name").contains("Windows")) Seq("cmd", "/c")
          else Seq("bash", "-c")
        val cmdGetPid = Seq(
          "ps",
          "-ef",
          "|",
          "grep",
          name.value,
          "|",
          "grep",
          "-v",
          "grep",
          "|",
          "awk",
          "'{print $2}'"
        ).mkString(" ")

        // logger.log.info(s"execute: ${cmdGetPid.mkString(" ")}")
        val pid = ((shell :+ cmdGetPid) !!)
        if (pid.nonEmpty) {

          logger.log.info(s"PID=$pid")

          val cmd = Seq(
            "kill",
            "-s",
            "9",
            pid
          ).mkString(" ")

          val result = ((shell :+ cmd) ! logger.log)
          if (result == 0) {
            logger.log.success(s"stop app successful")
          } else {
            logger.log.success("stop app failure")
          }
        } else {
          logger.log.info("app is not running")
        }
      },
      showPid := {
        val logger: TaskStreams = streams.value
        val shell: Seq[String] =
          if (sys.props("os.name").contains("Windows")) Seq("cmd", "/c")
          else Seq("bash", "-c")
        val cmd = Seq(
          "ps",
          "-ef",
          "|",
          "grep",
          name.value,
          "|",
          "grep",
          "-v",
          "grep",
          "|",
          "awk",
          "'{print $2}'"
        ).mkString(" ")

        // logger.log.info(s"execute: ${cmd.mkString(" ")}")
        val pid = (shell :+ cmd) !!

        if (pid.nonEmpty) {
          logger.log.info(s"PID=$pid")
        } else {
          logger.log.info(s"pid not found")
        }
      },
      appRestart := {
        val logger: TaskStreams = streams.value
        logger.log.info("app restart..")
        appStop.evaluated
        (Compile / run).evaluated
      }
    )

addCommandAlias("run", "app/appStart")
