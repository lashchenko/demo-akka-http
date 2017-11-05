package com.github.lashchenko.server

import javax.inject.{ Inject, Named }

import com.typesafe.config.Config

class DemoServerConfig @Inject() (@Named("DemoConfigServer") cfg: Config) {
  def host: String = cfg.getString("host")
  def port: Int = cfg.getInt("port")
}
