package com.github.lashchenko.client

import javax.inject.{ Inject, Named }

import akka.actor.ActorSystem
import akka.http.scaladsl.client.RequestBuilding._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import com.github.lashchenko.client.DemoHttpClient.HttpReqResHandler
import com.github.lashchenko.model.DemoEntity
import com.github.lashchenko.util.DemoApiJsonProtocol
import spray.json._

import scala.concurrent.{ ExecutionContext, Future }

class DemoEntityHttpClient @Inject() (
    implicit
    val system: ActorSystem,
    val materializer: ActorMaterializer,
    @Named("DemoHttpClientEc") val ec: ExecutionContext,
    val cfg: DemoClientConfig,
    override val createHandler: DemoEntityHttpClient.DemoEntityHttpClientCreate,
    override val readHandler: DemoEntityHttpClient.DemoEntityHttpClientRead,
    override val updateHandler: DemoEntityHttpClient.DemoEntityHttpClientUpdate,
    override val deleteHandler: DemoEntityHttpClient.DemoEntityHttpClientDelete)
  extends DemoHttpClient[DemoEntity]

object DemoEntityHttpClient extends DemoApiJsonProtocol {

  class DemoEntityHttpClientCreate @Inject() (
      implicit
      val system: ActorSystem,
      val materializer: ActorMaterializer,
      @Named("DemoHttpClientEc") val ec: ExecutionContext,
      val cfg: DemoClientConfig)
    extends HttpReqResHandler[DemoEntity] {

    override def httpServiceEndpoint(obj: DemoEntity): String =
      cfg.endpoint + "/entities"

    override def httpRequestHandler(obj: DemoEntity): Future[HttpRequest] = Future {
      Post(httpServiceEndpoint(obj), HttpEntity(`application/json`, obj.toCreate.toJson.compactPrint))
    }

    override def httpResponseHandler(res: HttpResponse): Future[DemoEntity] =
      Unmarshal(res.entity).to[DemoEntity]
  }

  class DemoEntityHttpClientRead @Inject() (
      implicit
      val system: ActorSystem,
      val materializer: ActorMaterializer,
      @Named("DemoHttpClientEc") val ec: ExecutionContext,
      val cfg: DemoClientConfig)
    extends HttpReqResHandler[DemoEntity] {

    override protected def httpServiceEndpoint(obj: DemoEntity): String =
      cfg.endpoint + "/entities/" + obj.toRead.id.getOrElse {
        throw new IllegalStateException("Missing id")
      }

    override def httpRequestHandler(obj: DemoEntity): Future[HttpRequest] = Future {
      Get(httpServiceEndpoint(obj))
    }

    override def httpResponseHandler(res: HttpResponse): Future[DemoEntity] =
      Unmarshal(res.entity).to[DemoEntity]
  }

  class DemoEntityHttpClientUpdate @Inject() (
      implicit
      val system: ActorSystem,
      val materializer: ActorMaterializer,
      @Named("DemoHttpClientEc") val ec: ExecutionContext,
      val cfg: DemoClientConfig)
    extends HttpReqResHandler[DemoEntity] {

    override protected def httpServiceEndpoint(obj: DemoEntity): String =
      cfg.endpoint + "/entities/" + obj.toUpdate.id.getOrElse {
        throw new IllegalStateException("Missing id")
      }

    override def httpRequestHandler(obj: DemoEntity): Future[HttpRequest] = Future {
      Put(httpServiceEndpoint(obj), HttpEntity(`application/json`, obj.toUpdate.toJson.compactPrint))
    }

    override def httpResponseHandler(res: HttpResponse): Future[DemoEntity] =
      Unmarshal(res.entity).to[DemoEntity]
  }

  class DemoEntityHttpClientDelete @Inject() (
      implicit
      val system: ActorSystem,
      val materializer: ActorMaterializer,
      @Named("DemoHttpClientEc") val ec: ExecutionContext,
      val cfg: DemoClientConfig)
    extends HttpReqResHandler[DemoEntity] {

    override protected def httpServiceEndpoint(obj: DemoEntity): String =
      cfg.endpoint + "/entities/" + obj.toDelete.id.getOrElse {
        throw new IllegalStateException("Missing id")
      }

    override def httpRequestHandler(obj: DemoEntity): Future[HttpRequest] = Future {
      Delete(httpServiceEndpoint(obj), HttpEntity(`application/json`, obj.toDelete.toJson.compactPrint))
    }

    override def httpResponseHandler(res: HttpResponse): Future[DemoEntity] =
      Unmarshal(res.entity).to[DemoEntity]
  }

}
