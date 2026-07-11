package com.searchengine;

import com.searchengine.index.InvertedIndex;
import com.searchengine.query.QueryParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class QueryParserBranchTest {

    private InvertedIndex index;
    private QueryParser parser;

    @BeforeEach
    void setUp() {
        index = new InvertedIndex();
        index.addDocument("Doc 1", "the quick brown fox jumps over the lazy dog");
        index.addDocument("Doc 2", "the quick red car drives fast");
        index.addDocument("Doc 3", "a lazy cat sleeps all day");
        parser = new QueryParser(index);
    }

    @Test
    void multiTermPhraseMatch() {
        // "quick brown" appears consecutively only in doc 1.
        Set<Integer> results = parser.parse("\"quick brown\"");
        assertEquals(1, results.size());
        assertTrue(results.contains(1));
    }

    @Test
    void multiTermPhraseNoAdjacencyMatch() {
        // "quick fox" exists in vocab but not adjacent.
        Set<Integer> results = parser.parse("\"quick fox\"");
        assertTrue(results.isEmpty());
    }

    @Test
    void phraseWithSingleTermActsAsTermQuery() {
        Set<Integer> results = parser.parse("\"quick\"");
        assertEquals(2, results.size());
    }

    @Test
    void phraseNoMatchAtAll() {
        Set<Integer> results = parser.parse("\"completely unknown\"");
        assertTrue(results.isEmpty());
    }

    @Test
    void unterminatedPhraseTreatsRestAsContent() {
        // No closing quote: tokenizer takes until end of string.
        Set<Integer> results = parser.parse("\"quick brown");
        // Still evaluated as phrase with "quick brown".
        assertEquals(1, results.size());
    }

    @Test
    void andWithMultipleTerms() {
        Set<Integer> results = parser.parse("quick AND the AND fox");
        assertEquals(1, results.size());
        assertTrue(results.contains(1));
    }

    @Test
    void orWithMultipleTerms() {
        Set<Integer> results = parser.parse("fox OR cat OR sleeps");
        assertEquals(2, results.size());
        assertTrue(results.contains(1));
        assertTrue(results.contains(3));
    }

    @Test
    void notReturnsAllMinusOperand() {
        Set<Integer> results = parser.parse("NOT the");
        // the appears in docs 1 and 2; only doc 3 lacks it.
        assertEquals(1, results.size());
        assertTrue(results.contains(3));
    }

    @Test
    void parseWhitespaceOnlyReturnsEmpty() {
        assertTrue(parser.parse("   ").isEmpty());
    }

    @Test
    void extractTermsReturnsCleanedLowercased() {
        List<String> terms = parser.extractTerms("Java AND \"the Programming\"");
        assertEquals(List.of("java", "the", "programming"), terms);
    }

    @Test
    void extractTermsEmptyForBlankQuery() {
        assertTrue(parser.extractTerms("").isEmpty());
        assertTrue(parser.extractTerms(null).isEmpty());
    }

    @Test
    void extractTermsStripsPunctuation() {
        List<String> terms = parser.extractTerms("hello, world!");
        assertEquals(List.of("hello", "world"), terms);
    }

    @Test
    void parseUnknownOperatorIsTreatedAsTerm() {
        // "quick brown" (no operator between) — both terms matched separately
        // but here it parses as two separate primaries in parseOr chain.
        Set<Integer> results = parser.parse("quick brown");
        // Both terms appear in docs 1 and 2, so the AND-style primary chain
        // yields their intersection (both have quick and brown? doc2 has no brown).
        // parsePrimary returns docs for "quick", then next token "brown" is parsed
        // via parseAnd since no AND keyword — actually parseAnd only continues on
        // explicit AND. So "brown" is a leftover; parseOr finishes. Result = quick docs.
        assertEquals(2, results.size());
    }
}