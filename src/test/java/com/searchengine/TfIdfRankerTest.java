package com.searchengine;

import com.searchengine.index.InvertedIndex;
import com.searchengine.ranker.SearchResult;
import com.searchengine.ranker.TfIdfRanker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class TfIdfRankerTest {

    private InvertedIndex index;
    private TfIdfRanker ranker;

    @BeforeEach
    void setUp() {
        index = new InvertedIndex();
        index.addDocument("Doc 1", "java java java programming language");
        index.addDocument("Doc 2", "python programming language scripting");
        index.addDocument("Doc 3", "java coffee beans roasting");
        ranker = new TfIdfRanker(index);
    }

    @Test
    void scoreReturnsZeroForTermNotInIndex() {
        assertEquals(0.0, ranker.score("nonexistent", 1));
    }

    @Test
    void scoreReturnsZeroForTermNotInSpecifiedDoc() {
        assertEquals(0.0, ranker.score("python", 1));
    }

    @Test
    void scoreReturnsPositiveForTermPresentInDoc() {
        double s = ranker.score("java", 1);
        assertTrue(s > 0.0, "Score should be positive for a term present in the document");
    }

    @Test
    void scoreHigherForHigherTermFrequency() {
        double scoreDoc1 = ranker.score("java", 1); // "java" appears 3 times in doc 1
        double scoreDoc3 = ranker.score("java", 3); // "java" appears 1 time in doc 3
        assertTrue(scoreDoc1 > scoreDoc3,
                "Doc with higher term frequency should have higher score");
    }

    @Test
    void scoreIdfHigherForRarerTerm() {
        // "python" appears in 1 doc, "program" (stemmed from "programming") appears in 2 docs
        // Both appear once in their respective docs, so TF is the same; IDF differs
        double scorePython = ranker.score("python", 2);
        double scoreProgramming = ranker.score("programming", 2);
        assertTrue(scorePython > scoreProgramming,
                "Rarer term should have higher IDF and thus higher score");
    }

    @Test
    void rankWithEmptyDocIdsReturnsEmptyList() {
        List<SearchResult> results = ranker.rank(Collections.emptySet(), List.of("java"));
        assertTrue(results.isEmpty());
    }

    @Test
    void rankWithEmptyQueryTermsReturnsEmptyList() {
        List<SearchResult> results = ranker.rank(Set.of(1, 2), Collections.emptyList());
        assertTrue(results.isEmpty());
    }

    @Test
    void rankReturnsSortedByScoreDescending() {
        // Doc 1 has "java" 3 times, Doc 3 has it once -> Doc 1 should rank higher
        Set<Integer> docIds = Set.of(1, 3);
        List<SearchResult> results = ranker.rank(docIds, List.of("java"));
        assertEquals(2, results.size());
        assertTrue(results.get(0).getScore() >= results.get(1).getScore(),
                "Results should be sorted by score descending");
        assertEquals(1, results.get(0).getDocId());
    }

    @Test
    void rankSnippetTruncatedForLongContent() {
        StringBuilder longContent = new StringBuilder();
        for (int i = 0; i < 50; i++) {
            longContent.append("word ");
        }
        longContent.append("uniqueterm");
        InvertedIndex bigIndex = new InvertedIndex();
        int docId = bigIndex.addDocument("Long Doc", longContent.toString());
        TfIdfRanker bigRanker = new TfIdfRanker(bigIndex);

        List<SearchResult> results = bigRanker.rank(Set.of(docId), List.of("uniqueterm"));
        assertEquals(1, results.size());
        assertTrue(results.get(0).getSnippet().endsWith("..."),
                "Long content snippet should be truncated with '...'");
        assertEquals(203, results.get(0).getSnippet().length()); // 200 chars + "..."
    }

    @Test
    void rankSnippetKeptFullForShortContent() {
        InvertedIndex smallIndex = new InvertedIndex();
        String shortContent = "hello world";
        int docId = smallIndex.addDocument("Short Doc", shortContent);
        TfIdfRanker smallRanker = new TfIdfRanker(smallIndex);

        List<SearchResult> results = smallRanker.rank(Set.of(docId), List.of("hello"));
        assertEquals(1, results.size());
        assertEquals(shortContent, results.get(0).getSnippet());
    }

    @Test
    void rankAggregatesScoresFromMultipleQueryTerms() {
        // Doc 1 has both "java" and "programming", Doc 3 has only "java"
        Set<Integer> docIds = Set.of(1, 3);
        List<SearchResult> results = ranker.rank(docIds, List.of("java", "programming"));

        assertEquals(2, results.size());
        assertEquals(1, results.get(0).getDocId(),
                "Doc with both query terms should rank higher");

        double singleTermScore = ranker.score("java", 1);
        assertTrue(results.get(0).getScore() > singleTermScore,
                "Aggregate score should be higher than single term score");
    }

    @Test
    void rankResultContainsCorrectTitle() {
        List<SearchResult> results = ranker.rank(Set.of(1), List.of("java"));
        assertEquals(1, results.size());
        assertEquals("Doc 1", results.get(0).getTitle());
    }

    @Test
    void rankSkipsDocsWithZeroScore() {
        // Doc 2 has "python" but not "java"; query for "java" on doc 2 should yield 0 score
        List<SearchResult> results = ranker.rank(Set.of(2), List.of("java"));
        assertTrue(results.isEmpty(), "Docs with zero total score should be excluded");
    }
}
