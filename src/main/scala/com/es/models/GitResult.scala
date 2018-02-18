package com.es.models

case class Owner(id: Long, login: String, url: String) extends BaseEntity[Long]
case class GitRepo(id: Long, name: String, owner: Owner, url: String, language: String) extends BaseEntity[Long]
case class GitResult(total_count: Long, items: List[GitRepo]) extends BaseResult[GitRepo, Long]

