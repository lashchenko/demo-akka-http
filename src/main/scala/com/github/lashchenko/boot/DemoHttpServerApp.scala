package com.github.lashchenko.boot

import akka.http.scaladsl.server.{ HttpApp, Route }
import com.github.lashchenko.guice.DemoGuiceInjector
import com.github.lashchenko.server.DemoEntityHttpServer

object DemoHttpServerApp extends HttpApp with App {

  private val injector = DemoGuiceInjector.injector

  private val demoServer = injector.instance[DemoEntityHttpServer]
  override def routes: Route = demoServer.route

  private val host = demoServer.cfg.host
  private val port = demoServer.cfg.port
  startServer(host, port)
}
