package com.github.lashchenko.model

import java.util.UUID

sealed trait DemoApiFormat[T <: DemoApiFormat[T]] {
  self: T ⇒
  def toCreate: T = this

  def toRead: T = this

  def toUpdate: T = this

  def toDelete: T = this
}

case class DemoEntity(
    id: Option[UUID] = None,
    int: Option[Int] = None,
    long: Option[Long] = None,
    string: Option[String] = None,
    bigDecimal: Option[BigDecimal] = None)
  extends DemoApiFormat[DemoEntity] {

  override def toCreate: DemoEntity = {
    require(id.isEmpty, "Empty 'id' field required to create an object.")
    this
  }

  override def toRead: DemoEntity = {
    require(id.nonEmpty, "Non empty 'id' field required to read an object.")
    this
  }

  override def toUpdate: DemoEntity = {
    require(id.nonEmpty, "Non empty 'id' field required to update an object.")
    this
  }

  override def toDelete: DemoEntity = {
    require(id.nonEmpty, "Non empty 'id' field required to delete an object.")
    this
  }
}

case class DemoError(message: String)
