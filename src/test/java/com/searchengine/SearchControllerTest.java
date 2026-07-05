package com.searchengine;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Path;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(properties = "search-engine.index.persist-path=${java.io.tmpdir}/search-controller-test-${random.uuid}/index.ser")
class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void indexDocumentWithMissingTitleDefaultsToUntitled() throws Exception {
        mockMvc.perform(post("/index")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\": \"some content here\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Untitled"))
                .andExpect(jsonPath("$.docId").exists())
                .andExpect(jsonPath("$.message").value("Document indexed successfully"));
    }

    @Test
    void indexDocumentWithWhitespaceTextReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/index")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"T\", \"text\": \"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("text field is required"));
    }

    @Test
    void searchReturnsCorrectStructure() throws Exception {
        mockMvc.perform(post("/index")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Structured Doc\", \"text\": \"elasticsearch distributed search engine\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/search").param("q", "elasticsearch"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.query").value("elasticsearch"))
                .andExpect(jsonPath("$.totalResults").value(1))
                .andExpect(jsonPath("$.results[0].docId").exists())
                .andExpect(jsonPath("$.results[0].title").value("Structured Doc"))
                .andExpect(jsonPath("$.results[0].score").exists())
                .andExpect(jsonPath("$.results[0].snippet").exists());
    }

    @Test
    void searchForNonExistentTermReturnsZeroResults() throws Exception {
        mockMvc.perform(post("/index")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Doc\", \"text\": \"hello world\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/search").param("q", "xyznonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalResults").value(0))
                .andExpect(jsonPath("$.results").isEmpty());
    }

    @Test
    void statsEndpointShowsCorrectCountsAfterIndexing() throws Exception {
        mockMvc.perform(post("/index")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"First\", \"text\": \"alpha beta gamma\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/index")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Second\", \"text\": \"delta epsilon zeta\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDocuments").value(2))
                .andExpect(jsonPath("$.totalTerms").value(6));
    }

    @Test
    void indexMultipleDocumentsAndSearchForCommonTerm() throws Exception {
        mockMvc.perform(post("/index")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Doc A\", \"text\": \"database query optimization\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/index")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Doc B\", \"text\": \"database indexing strategy\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/search").param("q", "database"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalResults").value(2))
                .andExpect(jsonPath("$.results.length()").value(2));
    }

    @Test
    void searchWithBooleanAndQuery() throws Exception {
        mockMvc.perform(post("/index")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Java Doc\", \"text\": \"java programming language\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/index")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Python Doc\", \"text\": \"python programming language\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/search").param("q", "java AND programming"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalResults").value(1))
                .andExpect(jsonPath("$.results[0].title").value("Java Doc"));
    }

    @Test
    void searchWithBooleanOrQuery() throws Exception {
        mockMvc.perform(post("/index")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Cat Doc\", \"text\": \"fluffy cat sleeps\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/index")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Dog Doc\", \"text\": \"loyal dog barks\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/search").param("q", "cat OR dog"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalResults").value(2));
    }

    @Test
    void searchWithPhraseQuery() throws Exception {
        mockMvc.perform(post("/index")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"ML Doc\", \"text\": \"machine learning is transforming industries\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/index")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Other Doc\", \"text\": \"learning about machine tools\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/search").param("q", "\"machine learning\""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalResults").value(1))
                .andExpect(jsonPath("$.results[0].title").value("ML Doc"));
    }

    @Test
    void indexDocumentResponseContainsCorrectDocId() throws Exception {
        mockMvc.perform(post("/index")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"First\", \"text\": \"content one\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.docId").value(1));

        mockMvc.perform(post("/index")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Second\", \"text\": \"content two\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.docId").value(2));
    }
}
