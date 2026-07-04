package com.searchengine;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SearchEngineIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void statsEndpointReturns200() throws Exception {
        mockMvc.perform(get("/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDocuments").exists())
                .andExpect(jsonPath("$.totalTerms").exists());
    }

    @Test
    void indexAndSearchWorkflow() throws Exception {
        // Index a document
        mockMvc.perform(post("/index")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Test Doc\", \"text\": \"Java programming language is powerful\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.docId").exists())
                .andExpect(jsonPath("$.message").value("Document indexed successfully"));

        // Search for it
        mockMvc.perform(get("/search").param("q", "java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalResults").exists())
                .andExpect(jsonPath("$.results").isArray());
    }

    @Test
    void indexRejectsEmptyText() throws Exception {
        mockMvc.perform(post("/index")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Empty\", \"text\": \"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchRejectsEmptyQuery() throws Exception {
        mockMvc.perform(get("/search").param("q", ""))
                .andExpect(status().isBadRequest());
    }
}
