package com.github.lashchenko.processing

import java.util.UUID
import javax.inject.{ Inject, Named }

import com.github.lashchenko.model.DemoEntity

import scala.collection.concurrent.TrieMap
import scala.concurrent.{ ExecutionContext, Future }

class DemoEntityRepository @Inject() (
    implicit
    @Named("DemoProcessingEc") val ec: ExecutionContext)
  extends DemoRepository[UUID, DemoEntity] {

  protected val db = new TrieMap[UUID, DemoEntity]()

  override def create(obj: DemoEntity): Future[DemoEntity] = Future {
    val xId = UUID.randomUUID()
    val x = obj.toCreate.copy(id = Some(xId))
    db += xId -> x
    x
  }

  override def update(id: UUID, obj: DemoEntity): Future[DemoEntity] = Future {
    val x = obj.toUpdate
    require(x.id.contains(id), "You must to use correct 'id' field value to update an object.")
    require(db.contains(id), "You must to use correct 'id' field value to update an object.")
    db += id -> x
    x
  }

  override def read(id: UUID): Future[DemoEntity] = Future {
    require(db.contains(id), "You must to use correct 'id' field value to read an object.")
    db(id)
  }

  override def delete(id: UUID, obj: DemoEntity): Future[DemoEntity] = Future {
    val x = obj.toDelete
    require(x.id.contains(id), "You must to use correct 'id' field value to delete an object.")
    require(db.contains(id), "You must to use correct 'id' field value to delete an object.")
    val removed = db(id)
    db.remove(id, x)
    removed
  }

}
