package com.github.lashchenko.processing

import scala.concurrent.Future

trait DemoRepository[Id, T] {

  def create(obj: T): Future[T]
  def update(id: Id, obj: T): Future[T]
  def read(id: Id): Future[T]
  def delete(id: Id, obj: T): Future[T]

}
