package simulations

import io.gatling.core.Predef._
import io.gatling.core.structure.PopulationBuilder
import scala.concurrent.duration._

object LoadProfiles {
  
  def normalLoad(scenario: io.gatling.core.structure.ScenarioBuilder): PopulationBuilder = {
    scenario.inject(
      rampUsers(50).during(30.seconds),
      constantUsersPerSec(5).during(2.minutes)
    )
  }
  
  def highLoad(scenario: io.gatling.core.structure.ScenarioBuilder): PopulationBuilder = {
    scenario.inject(
      rampUsers(100).during(60.seconds),
      constantUsersPerSec(10).during(3.minutes)
    )
  }
  
  def spikeLoad(scenario: io.gatling.core.structure.ScenarioBuilder): PopulationBuilder = {
    scenario.inject(
      atOnceUsers(50),
      nothingFor(5.seconds),
      atOnceUsers(100),
      nothingFor(5.seconds),
      atOnceUsers(150)
    )
  }
  
  def stressTest(scenario: io.gatling.core.structure.ScenarioBuilder): PopulationBuilder = {
    scenario.inject(
      rampUsersPerSec(10).to(50).during(5.minutes)
    )
  }
  
  def enduranceTest(scenario: io.gatling.core.structure.ScenarioBuilder): PopulationBuilder = {
    scenario.inject(
      constantUsersPerSec(20).during(15.minutes)
    )
  }
}
