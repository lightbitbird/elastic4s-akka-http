package com.es.services

import akka.stream.ActorMaterializer
import com.es.models.{BaseEntity, GitRepo}
import com.es.repositories.GitElasticRepository
import com.sksamuel.elastic4s.bulk.RichBulkResponse

import scala.concurrent.{ExecutionContextExecutor, Future}

trait ElasticService[T <: BaseEntity[A], A] {

  def createIndex(implicit ec: ExecutionContextExecutor): Unit

  def index(entity: T)(implicit ec: ExecutionContextExecutor): Unit

  def indexBulk(entities: List[T])(implicit ec: ExecutionContextExecutor): Future[RichBulkResponse]

  def findAll()(implicit ec: ExecutionContextExecutor, mate: ActorMaterializer): Future[List[GitRepo]]

  def findSingle(field: String, value: Any)(implicit ec: ExecutionContextExecutor,
                                            mate: ActorMaterializer): Future[List[GitRepo]]

  def find(fields: (String, Any)*)(implicit ec: ExecutionContextExecutor,
                                   mate: ActorMaterializer): Future[List[T]]

  def findWithQuery(field: String, q: String)(implicit ec: ExecutionContextExecutor,
                                              mate: ActorMaterializer): Future[List[T]]
}


object GitElasticService extends ElasticService[GitRepo, Long] {

  override def createIndex(implicit ec: ExecutionContextExecutor) = {
    GitElasticRepository.indexing
  }

  override def index(entity: GitRepo)(implicit ec: ExecutionContextExecutor) = {
    GitElasticRepository.indexWithType(entity)
  }

  override def indexBulk(entities: List[GitRepo])(implicit ec: ExecutionContextExecutor): Future[RichBulkResponse] = {
    GitElasticRepository.indexBulk(entities)
  }

  override def findAll()(implicit ec: ExecutionContextExecutor,
                         mate: ActorMaterializer): Future[List[GitRepo]] = {
    GitElasticRepository.findAll()
  }

  override def findSingle(field: String, value: Any)(implicit ec: ExecutionContextExecutor,
                                                     mate: ActorMaterializer): Future[List[GitRepo]] = {
    GitElasticRepository.findSingle(field, value)
  }

  override def find(fields: (String, Any)*)(implicit ec: ExecutionContextExecutor,
                                            mate: ActorMaterializer): Future[List[GitRepo]] = {
    GitElasticRepository.findOr(fields: _*)
  }

  override def findWithQuery(field: String, q: String)(implicit ec: ExecutionContextExecutor,
                                                       mate: ActorMaterializer): Future[List[GitRepo]] = {
    GitElasticRepository.find(("", ""))
  }
}
