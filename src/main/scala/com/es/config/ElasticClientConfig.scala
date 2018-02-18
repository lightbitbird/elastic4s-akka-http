package com.es.config

import com.sksamuel.elastic4s.{ElasticsearchClientUri, TcpClient}
import org.elasticsearch.common.settings.Settings

import scala.util.Try

object ElasticClientConfig extends ElasticConfig {
  private val host = Try(config.getString("elastic.host")).getOrElse("localhost")
  private val port = Try(config.getInt("elastic.port")).getOrElse(9300)
  private val cluster = Try(config.getString("elastic.cluster")).getOrElse("elastic-dev")

  private val settings = Settings.builder().put("cluster.name", cluster).build()
  lazy val client = TcpClient.transport(settings, ElasticsearchClientUri(host, port))
}
