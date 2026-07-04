package com.searchengine.api;

import com.searchengine.index.InvertedIndex;
import com.searchengine.persistence.IndexPersistence;
import com.searchengine.query.QueryParser;
import com.searchengine.ranker.SearchResult;
import com.searchengine.ranker.TfIdfRanker;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

@RestController
public class SearchController {

    @Value("${search-engine.index.persist-path:index-data/index.ser}")
    private String persistPath;

    private InvertedIndex index;
    private IndexPersistence persistence;

    @PostConstruct
    public void init() {
        persistence = new IndexPersistence(persistPath);
        index = persistence.load();
    }

    @PostMapping("/index")
    public ResponseEntity<Map<String, Object>> indexDocument(@RequestBody Map<String, String> body) {
        String text = body.getOrDefault("text", "");
        String title = body.getOrDefault("title", "Untitled");

        if (text.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "text field is required"));
        }

        int docId = index.addDocument(title, text);

        try {
            persistence.save(index);
        } catch (IOException e) {
            // Log but don't fail - index is still in memory
            System.err.println("Warning: could not persist index: " + e.getMessage());
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("docId", docId);
        response.put("title", title);
        response.put("message", "Document indexed successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> search(@RequestParam("q") String query) {
        if (query == null || query.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "query parameter q is required"));
        }

        QueryParser parser = new QueryParser(index);
        Set<Integer> matchingDocs = parser.parse(query);

        TfIdfRanker ranker = new TfIdfRanker(index);
        List<String> queryTerms = parser.extractTerms(query);
        List<SearchResult> results = ranker.rank(matchingDocs, queryTerms);

        List<Map<String, Object>> resultList = new ArrayList<>();
        for (SearchResult r : results) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("docId", r.getDocId());
            entry.put("title", r.getTitle());
            entry.put("score", Math.round(r.getScore() * 10000.0) / 10000.0);
            entry.put("snippet", r.getSnippet());
            resultList.add(entry);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("query", query);
        response.put("totalResults", results.size());
        response.put("results", resultList);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> stats() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("totalDocuments", index.getDocumentCount());
        response.put("totalTerms", index.getTermCount());
        response.put("indexPersistPath", persistPath);
        return ResponseEntity.ok(response);
    }
}
