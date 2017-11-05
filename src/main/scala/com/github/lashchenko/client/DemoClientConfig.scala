package com.github.lashchenko.client

import javax.inject.{ Inject, Named }

import com.typesafe.config.Config

class DemoClientConfig @Inject() (@Named("DemoConfigClient") cfg: Config) {
  def endpoint: String = cfg.getString("DemoApiEndpoint")
}
