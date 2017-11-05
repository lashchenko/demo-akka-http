package com.github.lashchenko.guice

import java.util.UUID

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.github.lashchenko.client.DemoEntityHttpClient._
import com.github.lashchenko.client.{ DemoClientConfig, DemoEntityHttpClient, DemoHttpClient }
import com.github.lashchenko.model.DemoEntity
import com.github.lashchenko.processing.{ DemoEntityRepository, DemoRepository }
import com.github.lashchenko.server.{ DemoEntityHttpServer, DemoServerConfig }
import com.google.inject.{ Inject, Provider }
import com.google.inject.name.Names
import com.google.inject.Guice
import com.typesafe.config.{ Config, ConfigFactory }
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import net.codingwell.scalaguice.ScalaModule

import scala.concurrent.ExecutionContext

object DemoGuiceInjector {
  lazy val injector: ScalaInjector = Guice.createInjector(
    new ConfigModule,
    new DemoAkkaModule,
    new DemoModule)
}

class ConfigClientProvider extends Provider[Config] {
  override def get(): Config = ConfigFactory.load().getConfig("client")
}

class ConfigServerProvider extends Provider[Config] {
  override def get(): Config = ConfigFactory.load().getConfig("server")
}

class ConfigModule extends ScalaModule {
  override def configure() {
    bind[Config].annotatedWith(Names.named("DemoConfigClient")).toProvider[ConfigClientProvider].asEagerSingleton()
    bind[DemoClientConfig].asEagerSingleton()

    bind[Config].annotatedWith(Names.named("DemoConfigServer")).toProvider[ConfigServerProvider].asEagerSingleton()
    bind[DemoServerConfig].asEagerSingleton()
  }
}

class ActorSystemProvider extends Provider[ActorSystem] {
  override def get(): ActorSystem = ActorSystem("DemoAkkaSystem")
}

class ActorMatProvider @Inject() (implicit val system: ActorSystem) extends Provider[ActorMaterializer] {
  override def get(): ActorMaterializer = ActorMaterializer()
}

class DemoHttpClientEcProvider @Inject() (implicit val system: ActorSystem) extends Provider[ExecutionContext] {
  override def get(): ExecutionContext = system.dispatchers.lookup("akka.demo-http-client-dispatcher")
}

class DemoHttpServerEcProvider @Inject() (implicit val system: ActorSystem) extends Provider[ExecutionContext] {
  override def get(): ExecutionContext = system.dispatchers.lookup("akka.demo-http-server-dispatcher")
}

class DemoProcessingEcProvider @Inject() (implicit val system: ActorSystem) extends Provider[ExecutionContext] {
  override def get(): ExecutionContext = system.dispatchers.lookup("akka.demo-processing-dispatcher")
}

class DemoAkkaModule extends ScalaModule {
  override def configure() {
    bind[ActorSystem].toProvider[ActorSystemProvider].asEagerSingleton()
    bind[ActorMaterializer].toProvider[ActorMatProvider].asEagerSingleton()
    bind[ExecutionContext].annotatedWith(Names.named("DemoHttpClientEc")).toProvider[DemoHttpClientEcProvider].asEagerSingleton()
    bind[ExecutionContext].annotatedWith(Names.named("DemoHttpServerEc")).toProvider[DemoHttpServerEcProvider].asEagerSingleton()
    bind[ExecutionContext].annotatedWith(Names.named("DemoProcessingEc")).toProvider[DemoProcessingEcProvider].asEagerSingleton()
  }
}

class DemoModule extends ScalaModule {
  override def configure(): Unit = {
    bind[DemoRepository[UUID, DemoEntity]].to[DemoEntityRepository].asEagerSingleton()
    bind[DemoEntityHttpServer].asEagerSingleton()
    bind[DemoEntityHttpClientCreate].asEagerSingleton()
    bind[DemoEntityHttpClientRead].asEagerSingleton()
    bind[DemoEntityHttpClientUpdate].asEagerSingleton()
    bind[DemoEntityHttpClientDelete].asEagerSingleton()
    bind[DemoHttpClient[DemoEntity]].to[DemoEntityHttpClient].asEagerSingleton()
  }
}

