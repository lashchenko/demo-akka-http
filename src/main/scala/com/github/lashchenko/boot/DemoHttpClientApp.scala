package com.github.lashchenko.boot

import com.github.lashchenko.client.{ DemoEntityHttpClient, DemoHttpClient }
import com.github.lashchenko.guice.DemoGuiceInjector
import com.github.lashchenko.model.DemoEntity
import com.google.inject.name.Names

import scala.concurrent.ExecutionContext

object DemoHttpClientApp extends App {
  private val injector = DemoGuiceInjector.injector
  implicit val ec: ExecutionContext = injector.instance[ExecutionContext](Names.named("DemoProcessingEc"))

  private val client = injector.instance[DemoHttpClient[DemoEntity]]

  val create = client.create(DemoEntity(int = Some(1), long = Some(1024L), string = Some("0xABC")))
  create.onComplete(x ⇒ println("create result: " + x))
  System.in.read()

  val readNew = create.flatMap(client.update)
  readNew.onComplete(x ⇒ println("readNew result: " + x))
  System.in.read()

  val update = readNew.flatMap(x ⇒ client.update(x.copy(bigDecimal = Some(3.14))))
  update.onComplete(x ⇒ println("update result: " + x))
  System.in.read()

  val readUpd = update.flatMap(client.read)
  readUpd.onComplete(x ⇒ println("readUpd result: " + x))
  System.in.read()

  val delete = readUpd.flatMap(client.delete)
  delete.onComplete(x ⇒ println("delete result: " + x))
  System.in.read()

  sys.exit(0)
}
