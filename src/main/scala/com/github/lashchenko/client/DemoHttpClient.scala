package com.github.lashchenko.client

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import com.github.lashchenko.model.DemoApiFormat

import scala.concurrent.{ ExecutionContext, Future }

trait DemoHttpClient[T <: DemoApiFormat[T]] {

  implicit val ec: ExecutionContext

  import DemoHttpClient._

  protected val createHandler: HttpReqResHandler[T]
  def create(obj: T): Future[T] = createHandler(obj)

  protected val readHandler: HttpReqResHandler[T]
  def read(obj: T): Future[T] = readHandler(obj)

  protected val updateHandler: HttpReqResHandler[T]
  def update(obj: T): Future[T] = updateHandler(obj)

  protected val deleteHandler: HttpReqResHandler[T]
  def delete(obj: T): Future[T] = deleteHandler(obj)

}

object DemoHttpClient {

  trait HttpReqResHandler[T <: DemoApiFormat[T]] {

    implicit val system: ActorSystem
    implicit val materializer: ActorMaterializer
    implicit val ec: ExecutionContext

    protected def httpServiceEndpoint(obj: T): String

    def httpRequestHandler(obj: T): Future[HttpRequest]
    def httpResponseHandler(res: HttpResponse): Future[T]
    def httpReqToRes(req: HttpRequest): Future[HttpResponse] = Http().singleRequest(req)

    def apply(obj: T): Future[T] = {
      for {
        req ← httpRequestHandler(obj)
        res ← httpReqToRes(req)
        // TODO better logging
        _ = println(s"REQUEST: $req\nRESPONSE: $res")
        result ← httpResponseHandler(res)
      } yield result
    }
  }

}

