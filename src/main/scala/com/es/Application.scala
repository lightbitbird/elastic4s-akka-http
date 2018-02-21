package com.es

import com.es.config.ActorSystemConfig
import com.es.routes.HttpServer

object Application extends App with ActorSystemConfig {

  new HttpServer().run()

}
