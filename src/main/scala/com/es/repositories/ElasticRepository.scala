package com.es.repositories

import akka.http.scaladsl.model.{HttpEntity, MediaTypes}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import com.es.config.ElasticClientConfig
import com.es.models._
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.bulk.RichBulkResponse
import com.typesafe.scalalogging.Logger

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.io.Source
import scala.util.{Failure, Success}

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

  def indexBulk(entities: List[T])(implicit ec: ExecutionContextExecutor): Future[RichBulkResponse]

  def findAll()(implicit ec: ExecutionContextExecutor, mate: ActorMaterializer): Future[List[T]]

  def find(wheres: (String, Any)*)(implicit ec: ExecutionContextExecutor,
                                   mate: ActorMaterializer): Future[List[T]]

  def findSingle(field: String, value: Any)(implicit ec: ExecutionContextExecutor,
                                            mate: ActorMaterializer): Future[List[T]]

  def findOr(wheres: (String, Any)*)(implicit ec: ExecutionContextExecutor,
                                   mate: ActorMaterializer): Future[List[T]]
}


object GitElasticRepository extends ElasticRepository[GitRepo, Long] with JsonSupport {

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

  override def indexBulk(entities: List[GitRepo])
                        (implicit ec: ExecutionContextExecutor): Future[RichBulkResponse] = {
    implicit val owFormat = jsonFormat3(Owner.apply)
    implicit val gitRepoFormat = jsonFormat5(GitRepo.apply)

    val bulkIndex = entities.map(entity => indexInto(indexName / typeName) id entity.id.toString doc entity)
    client.execute {
      bulk(bulkIndex)
      //    }.recover{
      //      case e: Exception => e
    }
  }

  def findAll()(implicit ec: ExecutionContextExecutor,
                mate: ActorMaterializer): Future[List[GitRepo]] = {

    implicit val owFormat = jsonFormat3(Owner.apply)
    implicit val gitRepoFormat = jsonFormat5(GitRepo.apply)
    client.execute {
      val searchDefinition = searchWithType(indexName / typeName)
      val builder = searchDefinition matchAllQuery()
      builder
    }.flatMap(res => {
      val entities = res.hits.map(hit => {
        val entity = HttpEntity(MediaTypes.`application/json`, hit.sourceAsString)
        Unmarshal(entity).to[GitRepo]
      })
      Future.sequence(entities.toList)
    })
  }

  def find(wheres: (String, Any)*)(implicit ec: ExecutionContextExecutor,
                                   mate: ActorMaterializer): Future[List[GitRepo]] = {
    implicit val owFormat = jsonFormat3(Owner.apply)
    implicit val gitRepoFormat = jsonFormat5(GitRepo.apply)
    client.execute {
      val bQuery = boolQuery().should {
        for {
          (f, v) <- wheres
        } yield matchPhraseQuery(fieldName(f), v)
      }
      search(indexName / typeName) query (bQuery)
    }.flatMap(res => {
      val entities = res.hits.map(hit => {
        //Unmarshal to an entity after convert to Json format as string
        Unmarshal(HttpEntity(MediaTypes.`application/json`, hit.sourceAsString)).to[GitRepo]
      })
      Future.sequence(entities.toList)
    })
  }

  def findSingle(field: String, value: Any)(implicit ec: ExecutionContextExecutor,
                                            mate: ActorMaterializer): Future[List[GitRepo]] = {
    implicit val owFormat = jsonFormat3(Owner.apply)
    implicit val gitRepoFormat = jsonFormat5(GitRepo.apply)
    client.execute {
      val searchDefinition = searchWithType(indexName / typeName)
      searchDefinition matchQuery("name", "play-zipkin-tracing")
    }.flatMap(res => {
      val entities = res.hits.map(hit => {
        //Unmarshal to an entity after convert to Json format as string
        Unmarshal(HttpEntity(MediaTypes.`application/json`, hit.sourceAsString)).to[GitRepo]
      })
      Future.sequence(entities.toList)
    })
  }

  def findOr(wheres: (String, Any)*)(implicit ec: ExecutionContextExecutor,
                                   mate: ActorMaterializer): Future[List[GitRepo]] = {
    implicit val owFormat = jsonFormat3(Owner.apply)
    implicit val gitRepoFormat = jsonFormat5(GitRepo.apply)
    client.execute {
      val bQuery = boolQuery().should {
        for {
          (f, v) <- wheres
        } yield matchPhraseQuery(fieldName(f), v)
      }
      search(indexName / typeName) query (bQuery)
    }.flatMap(res => {
      val entities = res.hits.map(hit => {
        //Unmarshal to an entity after convert to Json format as string
        Unmarshal(HttpEntity(MediaTypes.`application/json`, hit.sourceAsString)).to[GitRepo]
      })
      Future.sequence(entities.toList)
    })
  }

  def fieldName(name: String) = {
    if (name == "user") "owner.login"
    else if (name == "title") "name"
    else name
  }

}