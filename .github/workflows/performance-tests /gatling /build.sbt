name := "api-performance-tests"
version := "1.0"
scalaVersion := "2.13.8"

enablePlugins(GatlingPlugin)

libraryDependencies ++= Seq(
  "io.gatling.highcharts" % "gatling-charts-highcharts" % "3.7.6" % "test,it",
  "io.gatling"            % "gatling-app"               % "3.7.6" % "test,it",
  "io.gatling"            % "gatling-test-framework"    % "3.7.6" % "test,it"
)
