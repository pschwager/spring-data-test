package com.example.tags.controller;

import com.example.tags.model.Tags;
import com.example.tags.repository.TagsRepository;
import com.querydsl.core.types.Predicate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.data.rest.webmvc.support.RepositoryEntityLinks;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.CheckForNull;

@RestController
public class TagsController {
    @Autowired
    private RepositoryEntityLinks entityLinks;
    @Autowired
    private TagsRepository tagsRepository;

    @GetMapping("/api/tags/search")
    public PagedResources<?> searchTags(@CheckForNull @QuerydslPredicate(root = Tags.class) Predicate predicate,
                                        @PageableDefault(size = 100) final Pageable pageable,
                                        final PagedResourcesAssembler<Tags> pagedResourcesAssembler) {
        Page<Tags> tagsPage = tagsRepository.findAll(predicate, pageable);
        if (tagsPage.hasContent()) {
            return pagedResourcesAssembler.toResource(tagsPage, (tags) -> new Resource<>(tags, entityLinks.linkFor(Tags.class).slash(tags.getId()).withSelfRel()));
        }
        return pagedResourcesAssembler.toEmptyResource(tagsPage, Tags.class);
    }
}
