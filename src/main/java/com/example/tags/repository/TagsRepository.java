package com.example.tags.repository;

import com.example.tags.model.Tags;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "tags", path = "tags")
public interface TagsRepository extends MongoRepository<Tags, String>, QuerydslPredicateExecutor<Tags> {}
