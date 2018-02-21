package com.es.services

import com.es.models.{BaseEntity, GitRepo}
import com.es.repositories.GitElasticRepository

import scala.concurrent.{ExecutionContextExecutor, Future}

trait ElasticService[T <: BaseEntity[A], A] {

  def createIndex(implicit ec: ExecutionContextExecutor): Unit

  def index(entity: T)(implicit ec: ExecutionContextExecutor): Unit

  def indexBulk(entities: List[T])(implicit ec: ExecutionContextExecutor): Unit

  def find(field: String, q: String)(implicit ec: ExecutionContextExecutor): Future[List[T]]

}


trait GitElasticService extends ElasticService[GitRepo, Long] {

  override def createIndex(implicit ec: ExecutionContextExecutor) = {
    GitElasticRepository.indexing
  }

  override def index(entity: GitRepo)(implicit ec: ExecutionContextExecutor) = {
    GitElasticRepository.indexWithType(entity)
  }

  override def indexBulk(entities: List[GitRepo])(implicit ec: ExecutionContextExecutor) = {
    GitElasticRepository.indexBulk(entities)
  }

  override def find(field: String, q: String)(implicit ec: ExecutionContextExecutor): Future[List[GitRepo]] = {
    Future(List.empty[GitRepo])
  }

}
