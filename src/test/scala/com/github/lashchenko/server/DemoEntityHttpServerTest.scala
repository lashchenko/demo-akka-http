package com.github.lashchenko.server

import java.util.UUID
import javax.inject.{ Inject, Named }

import org.scalatest.{ Matchers, WordSpec }
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.server.Route
import com.github.lashchenko.guice.{ ConfigModule, DemoAkkaModule }
import com.github.lashchenko.model.DemoEntity
import com.github.lashchenko.processing.{ DemoEntityRepository, DemoRepository }
import com.github.lashchenko.util.DemoApiJsonProtocol
import com.google.inject.Guice
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import net.codingwell.scalaguice.ScalaModule
import spray.json._

import scala.concurrent.ExecutionContext

object DemoEntityHttpServerTest {

  private val TestId = UUID.randomUUID()
  private val TestIdToBeRemoved = UUID.randomUUID()

  class DemoEntityRepositoryTest @Inject() (
      implicit
      @Named("DemoProcessingEc") override val ec: ExecutionContext)
    extends DemoEntityRepository() {

    // previously saved test data ...
    db += TestId -> DemoEntity(id = Some(TestId), string = Some("test"))
    db += TestIdToBeRemoved -> DemoEntity(id = Some(TestIdToBeRemoved), int = Some(-100), long = Some(2048L), string = Some("ToBeRemoved"))

  }

  class DemoModuleTest extends ScalaModule {
    override def configure(): Unit = {
      bind[DemoRepository[UUID, DemoEntity]].to[DemoEntityRepositoryTest].asEagerSingleton()
      bind[DemoEntityHttpServer].asEagerSingleton()
    }
  }

  val injector: ScalaInjector = Guice.createInjector(
    new ConfigModule,
    new DemoAkkaModule,
    new DemoModuleTest)

}

class DemoEntityHttpServerTest extends WordSpec with Matchers with ScalatestRouteTest with DemoApiJsonProtocol with SprayJsonSupport {

  import DemoEntityHttpServerTest._

  private val server = injector.instance[DemoEntityHttpServer]

  "DemoEntityHttpServer" should {

    "Create valid DemoEntity" in {
      val e = HttpEntity(`application/json`, s"""{"int":1, "long": 1024, "bigDecimal": 3.14, "string":"test"}""")
      Post("/v1/demo-server/entities", e) ~> server.route ~> check {
        status shouldBe OK
        contentType shouldBe `application/json`
        responseAs[DemoEntity].id shouldBe defined
        responseAs[DemoEntity].int shouldBe Some(1)
        responseAs[DemoEntity].long shouldBe Some(1024)
        responseAs[DemoEntity].bigDecimal shouldBe Some(3.14)
        responseAs[DemoEntity].string shouldBe Some("test")
      }
    }

    "Read response with BadRequest for incorrect 'id' in url" in {
      val incorrectId = UUID.randomUUID()
      Get("/v1/demo-server/entities/" + incorrectId) ~> Route.seal(server.route) ~> check {
        status shouldBe BadRequest
        contentType shouldBe `application/json`
        responseAs[String] shouldEqual
          """{"message":"requirement failed: You must to use correct 'id' field value to read an object."}"""
      }
    }

    "Read response with JSON for correct 'id'" in {
      Get("/v1/demo-server/entities/" + TestId) ~> server.route ~> check {
        status shouldBe OK
        contentType shouldBe `application/json`
        responseAs[String] shouldEqual s"""{"id":"$TestId","string":"test"}"""
      }
    }

    "Update response with BadRequest for incorrect 'id' in url and body" in {
      val incorrectId = UUID.randomUUID()
      val e = HttpEntity(`application/json`, s"""{"id": "$incorrectId", "unknown": "test"}""")
      Put("/v1/demo-server/entities/" + incorrectId, e) ~> Route.seal(server.route) ~> check {
        status shouldBe BadRequest
        contentType shouldBe `application/json`
        responseAs[String] shouldBe """{"message":"requirement failed: You must to use correct 'id' field value to update an object."}"""
      }
    }

    "Update response with BadRequest for incorrect 'id' in url" in {
      val incorrectId = UUID.randomUUID()
      val e = HttpEntity(`application/json`, s"""{"id": "$TestId", "unknown": "test"}""")
      Put("/v1/demo-server/entities/" + incorrectId, e) ~> Route.seal(server.route) ~> check {
        status shouldBe BadRequest
        contentType shouldBe `application/json`
        responseAs[String] shouldBe """{"message":"requirement failed: You must to use correct 'id' field value to update an object."}"""
      }
    }

    "Update response with BadRequest for incorrect 'id' in body" in {
      val incorrectId = UUID.randomUUID()
      val e = HttpEntity(`application/json`, s"""{"id": "$incorrectId", "unknown": "test"}""")
      Put("/v1/demo-server/entities/" + TestId, e) ~> Route.seal(server.route) ~> check {
        status shouldBe BadRequest
        contentType shouldBe `application/json`
        responseAs[String] shouldBe """{"message":"requirement failed: You must to use correct 'id' field value to update an object."}"""
      }
    }

    "Update response with JSON for correct 'id' in url and body" in {
      val e = HttpEntity(`application/json`, s"""{"id": "$TestId", "bigDecimal": 3.14}""")
      Put("/v1/demo-server/entities/" + TestId, e) ~> Route.seal(server.route) ~> check {
        status shouldBe OK
        contentType shouldBe `application/json`
        responseAs[String] shouldBe s"""{"id":"$TestId","bigDecimal":3.14}"""
      }
    }

    "Delete response with BadRequest for incorrect 'id' in url and body" in {
      val incorrectId = UUID.randomUUID()
      val e = HttpEntity(`application/json`, s"""{"id": "$incorrectId", "unknown": "test"}""")
      Delete("/v1/demo-server/entities/" + incorrectId, e) ~> Route.seal(server.route) ~> check {
        status shouldBe BadRequest
        contentType shouldBe `application/json`
        responseAs[String] shouldBe """{"message":"requirement failed: You must to use correct 'id' field value to delete an object."}"""
      }
    }

    "Delete response with BadRequest for incorrect 'id' in url" in {
      val incorrectId = UUID.randomUUID()
      val e = HttpEntity(`application/json`, s"""{"id": "$TestIdToBeRemoved", "unknown": "test"}""")
      Delete("/v1/demo-server/entities/" + incorrectId, e) ~> Route.seal(server.route) ~> check {
        status shouldBe BadRequest
        contentType shouldBe `application/json`
        responseAs[String] shouldBe """{"message":"requirement failed: You must to use correct 'id' field value to delete an object."}"""
      }
    }

    "Delete response with BadRequest for incorrect 'id' in body" in {
      val incorrectId = UUID.randomUUID()
      val e = HttpEntity(`application/json`, s"""{"id": "$incorrectId", "unknown": "test"}""")
      Delete("/v1/demo-server/entities/" + TestIdToBeRemoved, e) ~> Route.seal(server.route) ~> check {
        status shouldBe BadRequest
        contentType shouldBe `application/json`
        responseAs[String] shouldBe """{"message":"requirement failed: You must to use correct 'id' field value to delete an object."}"""
      }
    }

    "Delete response with JSON for correct 'id' in url and body" in {
      val e = HttpEntity(`application/json`, s"""{"id": "$TestIdToBeRemoved"}""")
      Delete("/v1/demo-server/entities/" + TestIdToBeRemoved, e) ~> Route.seal(server.route) ~> check {
        status shouldBe OK
        contentType shouldBe `application/json`
        responseAs[String] shouldBe s"""{"id":"$TestIdToBeRemoved","int":-100,"long":2048,"string":"ToBeRemoved"}"""
      }
    }

  }

}
