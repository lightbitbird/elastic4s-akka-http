package com.es.services

import com.es.config.ActorSystemConfig
import com.es.models.{BaseEntity, GitRepo}
import com.es.repositories.GitElasticRepository

import scala.concurrent.Future

trait ElasticService[T <: BaseEntity[A], A] extends ActorSystemConfig {

  def createIndex: Unit

  def index(entity: T): Unit

  def indexBulk(entities: List[T]): Unit

  def find(field: String, q: String): Future[List[T]]

}


trait GitElasticService extends ElasticService[GitRepo, Long] {

  override def createIndex = {
    GitElasticRepository.indexing
  }

  override def index(entity: GitRepo) = {
    GitElasticRepository.indexWithType(entity)
  }

  override def indexBulk(entities: List[GitRepo]) = {
    GitElasticRepository.indexBulk(entities)
  }

  override def find(field: String, q: String): Future[List[GitRepo]] = {
    Future(List.empty[GitRepo])
  }

}
