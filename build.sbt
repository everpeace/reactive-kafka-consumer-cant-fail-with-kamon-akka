import com.typesafe.sbt.packager.Keys.bashScriptExtraDefines

lazy val root = (project in file(".")).
  enablePlugins(AshScriptPlugin).
  settings(
    organization := "com.github.everpeace",
    scalaVersion := "2.11.8",
    version := "0.1.0-SNAPSHOT",
    name := "reactive-kafka-cant-stop-with-kamon-akka",
    fork in run := true,
    libraryDependencies ++= Seq(
      // reactive kafka
      "com.typesafe.akka" %% "akka-stream-kafka" % "0.16",

      // kamon dependencies.
      "io.kamon" %% "kamon-akka-2.4" % "0.6.7",
      "org.aspectj" % "aspectjweaver" % "1.8.10",

      // log
      "com.typesafe.akka" %% "akka-slf4j" % "2.4.18",
      "ch.qos.logback" % "logback-classic" % "1.2.3"
    ),
    // docker container settings
    dockerBaseImage := "frolvlad/alpine-oraclejdk8:8.131.11-cleaned", // glibc based alpine for kamon
    bashScriptExtraDefines += """set -x""",
    bashScriptExtraDefines += """if [ -z ${KAMON} ] || [ ${KAMON} = "true" ] || [ ${KAMON} = "yes" ]; then addJava "-javaagent:${lib_dir}/org.aspectj.aspectjweaver-1.8.10.jar"; fi""",
    bashScriptExtraDefines += """if [ -n ${DEBUG} ]; then addJava "-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=4000"; fi""",
    dockerExposedPorts := Seq(4000)
  )
