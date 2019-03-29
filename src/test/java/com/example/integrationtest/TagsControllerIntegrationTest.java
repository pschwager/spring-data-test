package com.example.integrationtest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.tags.model.Tags;
import com.example.tags.repository.TagsRepository;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

public class TagsControllerIntegrationTest extends AbstractIntegrationTest {
    @Autowired
    private TagsRepository tagsRepository;

    @Test
    public void testSearchByTagsWithNoResult() throws Exception {
        String expectedJson = "{"
                + "  \"_embedded\" : {"
                + "    \"tags\" : [ ]"
                + "  },"
                + "  \"_links\" : {"
                + "    \"self\" : {"
                + "      \"href\" : \"http://localhost/api/tags/search?page=0&size=100\""
                + "    }"
                + "  },"
                + "  \"page\" : {"
                + "    \"size\" : 100,"
                + "    \"totalElements\" : 0,"
                + "    \"totalPages\" : 0,"
                + "    \"number\" : 0"
                + "  }"
                + "}";

        // Given
        Set<String> tags1 = new HashSet<>();
        tags1.add("alpha");
        tags1.add("beta");

        Set<String> tags2 = new HashSet<>();
        tags2.add("gamma");
        Tags a = new Tags();
        a.setTags(tags1);
        tagsRepository.insert(a);
        Tags b = new Tags();
        b.setTags(tags2);
        tagsRepository.insert(b);

        // It should not return a result because the tags are logical AND'd
        mockMvc.perform(get(URI.create("/api/tags/search")).param("tags", "beta").param("tags", "gamma"))//
            .andExpect(status().is(200))//
            .andExpect(content().json(expectedJson));
    }
}
