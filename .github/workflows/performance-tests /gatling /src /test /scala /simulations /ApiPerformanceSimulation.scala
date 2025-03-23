package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class ApiPerformanceSimulation extends Simulation {

  val baseUrl = System.getProperty("baseUrl", "http://localhost:8080")
  val apiToken = System.getProperty("apiToken", "test-token")
  
  val httpProtocol = http
    .baseUrl(baseUrl)
    .header("Content-Type", "application/json")
    .header("Accept", "application/json")
    .header("Authorization", s"Bearer $apiToken")

  // Scenario for GET endpoints
  val getEndpointScenario = scenario("GET Endpoints Scenario")
    .exec(
      http("Get Endpoint 1")
        .get("/api/v1/endpoint1")
        .check(status.is(200))
        .check(jsonPath("$.status").is("success"))
    )
    .pause(1)
    .exec(
      http("Get Endpoint with Parameters")
        .get("/api/v1/endpoint3?param=value&limit=10")
        .check(status.is(200))
    )

  // Scenario for POST endpoints
  val postEndpointScenario = scenario("POST Endpoints Scenario")
    .exec(
      http("Create Resource")
        .post("/api/v1/endpoint2")
        .body(StringBody("""
          {
            "param1": "value1",
            "param2": "value2"
          }
        """))
        .check(status.is(201))
    )

  // Normal load test
  val normalLoadTest = setUp(
    getEndpointScenario.inject(
      rampUsers(50).during(30.seconds)
    ),
    postEndpointScenario.inject(
      rampUsers(30).during(30.seconds)
    )
  ).protocols(httpProtocol)
    .assertions(
      global.responseTime.max.lt(2000),
      global.successfulRequests.percent.gt(95)
    )

  // High load test
  val highLoadTest = setUp(
    getEndpointScenario.inject(
      rampUsers(200).during(60.seconds),
      constantUsersPerSec(10).during(3.minutes)
    ),
    postEndpointScenario.inject(
      rampUsers(100).during(60.seconds),
      constantUsersPerSec(5).during(3.minutes)
    )
  ).protocols(httpProtocol)
    .assertions(
      global.responseTime.mean.lt(2000),
      global.successfulRequests.percent.gt(90)
    )

  // Spike test
  val spikeTest = setUp(
    getEndpointScenario.inject(
      atOnceUsers(50),
      nothingFor(5.seconds),
      atOnceUsers(100),
      nothingFor(5.seconds),
      atOnceUsers(150)
    )
  ).protocols(httpProtocol)
    .assertions(
      global.responseTime.mean.lt(4000),
      global.successfulRequests.percent.gt(80)
    )

  // Uncomment the test you want to run
  // normalLoadTest
  // highLoadTest
  // spikeTest
  
  // Or run all tests in sequence
  normalLoadTest.andThen(highLoadTest).andThen(spikeTest)
}
