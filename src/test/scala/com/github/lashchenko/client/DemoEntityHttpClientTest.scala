package com.github.lashchenko.client

import java.util.UUID
import javax.inject.{ Inject, Named }

import akka.actor.ActorSystem
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.{ HttpEntity, HttpRequest, HttpResponse }
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import com.github.lashchenko.client.DemoEntityHttpClient._
import com.github.lashchenko.guice.{ ConfigModule, DemoAkkaModule }
import com.github.lashchenko.model.DemoEntity
import com.google.inject.Guice
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import net.codingwell.scalaguice.ScalaModule
import org.scalatest.{ AsyncWordSpec, Matchers }
import spray.json._

import scala.concurrent.{ ExecutionContext, Future }

object DemoEntityHttpClientTest {
  private val TestId = UUID.fromString("4f0970a2-c50e-4a8b-b5ff-d0229e95905e")

  class DemoEntityHttpClientCreateTest @Inject() (
      implicit
      override val system: ActorSystem,
      override val materializer: ActorMaterializer,
      @Named("DemoHttpClientEc") override val ec: ExecutionContext,
      override val cfg: DemoClientConfig)
    extends DemoEntityHttpClientCreate() {
    override def httpReqToRes(req: HttpRequest): Future[HttpResponse] = {
      Unmarshal(req.entity).to[DemoEntity]
        .map(e ⇒ e.copy(id = Some(TestId)))
        .map(e ⇒ HttpResponse(entity = HttpEntity(`application/json`, e.toJson.compactPrint)))
    }
  }

  class DemoEntityHttpClientReadTest @Inject() (
      implicit
      override val system: ActorSystem,
      override val materializer: ActorMaterializer,
      @Named("DemoHttpClientEc") override val ec: ExecutionContext,
      override val cfg: DemoClientConfig)
    extends DemoEntityHttpClientRead() {
    override def httpReqToRes(req: HttpRequest): Future[HttpResponse] = Future {
      // This is GET request without JSON body
      val e = DemoEntity(id = Some(TestId), int = Some(1), long = Some(1024L), string = Some("0xABC"))
      HttpResponse(entity = HttpEntity(`application/json`, e.toJson.compactPrint))
    }
  }

  class DemoEntityHttpClientUpdateTest @Inject() (
      implicit
      override val system: ActorSystem,
      override val materializer: ActorMaterializer,
      @Named("DemoHttpClientEc") override val ec: ExecutionContext,
      override val cfg: DemoClientConfig)
    extends DemoEntityHttpClientUpdate() {
    override def httpReqToRes(req: HttpRequest): Future[HttpResponse] = {
      Unmarshal(req.entity).to[DemoEntity]
        .map(e ⇒ HttpResponse(entity = HttpEntity(`application/json`, e.toJson.compactPrint)))
    }
  }

  class DemoEntityHttpClientDeleteTest @Inject() (
      implicit
      override val system: ActorSystem,
      override val materializer: ActorMaterializer,
      @Named("DemoHttpClientEc") override val ec: ExecutionContext,
      override val cfg: DemoClientConfig)
    extends DemoEntityHttpClientDelete() {
    override def httpReqToRes(req: HttpRequest): Future[HttpResponse] = {
      Unmarshal(req.entity).to[DemoEntity]
        .map(e ⇒ HttpResponse(entity = HttpEntity(`application/json`, e.toJson.compactPrint)))
    }
  }

  class DemoModuleTest extends ScalaModule {
    override def configure(): Unit = {
      bind[DemoEntityHttpClientCreate].to[DemoEntityHttpClientCreateTest].asEagerSingleton()
      bind[DemoEntityHttpClientRead].to[DemoEntityHttpClientReadTest].asEagerSingleton()
      bind[DemoEntityHttpClientUpdate].to[DemoEntityHttpClientUpdateTest].asEagerSingleton()
      bind[DemoEntityHttpClientDelete].to[DemoEntityHttpClientDeleteTest].asEagerSingleton()
      bind[DemoHttpClient[DemoEntity]].to[DemoEntityHttpClient].asEagerSingleton()
    }
  }

  val injector: ScalaInjector = Guice.createInjector(
    new ConfigModule,
    new DemoAkkaModule,
    new DemoModuleTest)
}

class DemoEntityHttpClientTest extends AsyncWordSpec with Matchers {

  import DemoEntityHttpClientTest._

  private val client = injector.instance[DemoHttpClient[DemoEntity]]

  "DemoEntityHttpClient" should {

    "create DemoEntity with empty 'id' field" in {
      client.create(DemoEntity())
        .map(e ⇒ e.id shouldBe Some(TestId))
    }

    "not create DemoEntity with non empty 'id' field" in {
      recoverToExceptionIf[IllegalArgumentException] {
        client.create(DemoEntity(id = Some(TestId)))
      }.map(e ⇒ e.getMessage shouldBe "requirement failed: Empty 'id' field required to create an object.")
    }

    "read DemoEntity with non empty 'id' field" in {
      client.read(DemoEntity(id = Some(TestId)))
        .map { e ⇒ println("??? ===> " + e); e }
        .map(e ⇒ e shouldBe DemoEntity(id = Some(TestId), int = Some(1), long = Some(1024L), string = Some("0xABC")))
    }

    "not read DemoEntity with empty 'id' field" in {
      recoverToExceptionIf[IllegalArgumentException] {
        client.read(DemoEntity())
      }.map(e ⇒ e.getMessage shouldBe "requirement failed: Non empty 'id' field required to read an object.")
    }

    "update DemoEntity with non empty 'id' field" in {
      client.update(DemoEntity(id = Some(TestId), string = Some("updated")))
        .map(e ⇒ e shouldBe DemoEntity(id = Some(TestId), string = Some("updated")))
    }

    "not update DemoEntity with empty 'id' field" in {
      recoverToExceptionIf[IllegalArgumentException] {
        client.update(DemoEntity(string = Some("updated")))
      }.map(e ⇒ e.getMessage shouldBe "requirement failed: Non empty 'id' field required to update an object.")
    }

    "delete DemoEntity with non empty 'id' field" in {
      client.delete(DemoEntity(id = Some(TestId), string = Some("deleted")))
        .map(e ⇒ e shouldBe DemoEntity(id = Some(TestId), string = Some("deleted")))
    }

    "not delete DemoEntity with empty 'id' field" in {
      recoverToExceptionIf[IllegalArgumentException] {
        client.delete(DemoEntity(string = Some("deleted")))
      }.map(e ⇒ e.getMessage shouldBe "requirement failed: Non empty 'id' field required to delete an object.")
    }

  }

}
