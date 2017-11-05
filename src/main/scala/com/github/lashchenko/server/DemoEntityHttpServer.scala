package com.github.lashchenko.server

import java.util.UUID
import javax.inject.{ Inject, Named }

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{ HttpEntity, HttpResponse }
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.server._
import akka.stream.ActorMaterializer
import com.github.lashchenko.model.{ DemoEntity, DemoError }
import com.github.lashchenko.processing.DemoRepository
import com.github.lashchenko.util.DemoApiJsonProtocol
import spray.json._

import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal

class DemoEntityHttpServer @Inject() (
    implicit
    val actorSystem: ActorSystem,
    val actorMaterializer: ActorMaterializer,
    @Named("DemoHttpServerEc") val executionContext: ExecutionContext,
    val cfg: DemoServerConfig,
    private val repo: DemoRepository[UUID, DemoEntity])
  extends Directives
  with DemoApiJsonProtocol
  with SprayJsonSupport {

  private val demoExceptionHandler: ExceptionHandler = ExceptionHandler {
    case e @ (_: IllegalArgumentException | _: DeserializationException) ⇒
      extractUri { uri ⇒
        val res = HttpResponse(
          status = BadRequest,
          entity = HttpEntity(`application/json`, DemoError(e.getMessage).toJson.compactPrint))
        // TODO better logging
        println(s"Request to $uri could not be handled normally. ${e.getMessage}")
        complete(res)
      }
    case NonFatal(e) ⇒
      extractUri { uri ⇒
        val res = HttpResponse(
          status = InternalServerError,
          entity = HttpEntity(`application/json`, DemoError(e.getMessage).toJson.compactPrint))
        // TODO better logging
        println(s"Request to $uri could not be handled normally. ${e.getMessage}")
        complete(res)
      }
  }

  private val createEntity = path("v1" / "demo-server" / "entities") {
    post {
      entity(as[DemoEntity]) { entity ⇒
        onSuccess(repo.create(entity)) { complete(_) }
      }
    }
  }

  private val readEntity = path("v1" / "demo-server" / "entities" / JavaUUID) { id ⇒
    get {
      onSuccess(repo.read(id)) { complete(_) }
    }
  }

  private val updateEntity = path("v1" / "demo-server" / "entities" / JavaUUID) { id ⇒
    put {
      entity(as[DemoEntity]) { entity ⇒
        onSuccess(repo.update(id, entity)) { complete(_) }
      }
    }
  }

  private val deleteEntity = path("v1" / "demo-server" / "entities" / JavaUUID) { id ⇒
    delete {
      entity(as[DemoEntity]) { entity ⇒
        onSuccess(repo.delete(id, entity)) { complete(_) }
      }
    }
  }

  val route: Route = handleExceptions(demoExceptionHandler) {
    createEntity ~ readEntity ~ updateEntity ~ deleteEntity
  }

}
