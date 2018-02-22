package com.es.repositories

import com.es.config.ElasticClientConfig
import com.es.models.{BaseEntity, GitRepo}
import com.sksamuel.elastic4s.ElasticDsl._
import com.typesafe.scalalogging.Logger

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.io.Source

trait ElasticRepository[T <: BaseEntity[A], A] {
  val logger = Logger(getClass.getName)

  val client = ElasticClientConfig.client
  val config = ElasticClientConfig.config

  val indexName: String
  val typeName: String

  def indexing(implicit ec: ExecutionContextExecutor): Unit = {
    val mapper = Source.fromResource("mapping.conf").getLines().mkString
    client.execute {
      createIndex(indexName) source mapper
    }
  }

  def indexWithType(entity: T)(implicit ec: ExecutionContextExecutor): Unit

  def indexBulk(entities: List[T])(implicit ec: ExecutionContextExecutor): Unit

  def find(field: String, q: String)(implicit ec: ExecutionContextExecutor): Future[List[T]]

}


object GitElasticRepository extends ElasticRepository[GitRepo, Long] {

  override val indexName = config.getString("elastic.index.name")
  override val typeName = config.getString("elastic.type.name")

  import com.sksamuel.elastic4s.jackson.ElasticJackson.Implicits._

  override def indexWithType(entity: GitRepo)(implicit ec: ExecutionContextExecutor) = {
    client.execute {
      indexInto(indexName / typeName) id entity.id.toString doc entity
    }.recover({
      case e: Exception => logger.error("error in index: " + e.getMessage)
    })
  }

  override def indexBulk(entities: List[GitRepo])(implicit ec: ExecutionContextExecutor) = {
    val bulkIndex = entities.map(entity => indexInto(indexName / typeName) id entity.id.toString doc entity)
    client.execute {
      bulk(bulkIndex)
    }.recover({
      case e: Exception => logger.error("error in indexBulk: " + e.getMessage)
    })
  }

  def find(field: String, q: String)(implicit ec: ExecutionContextExecutor): Future[List[GitRepo]] = {
    Future(List.empty[GitRepo])
  }

}