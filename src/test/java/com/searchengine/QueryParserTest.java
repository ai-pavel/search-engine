package com.searchengine;

import com.searchengine.index.InvertedIndex;
import com.searchengine.query.QueryParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class QueryParserTest {

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
    void singleTermQuery() {
        Set<Integer> results = parser.parse("quick");
        assertEquals(2, results.size());
        assertTrue(results.contains(1));
        assertTrue(results.contains(2));
    }

    @Test
    void andQuery() {
        Set<Integer> results = parser.parse("quick AND fox");
        assertEquals(1, results.size());
        assertTrue(results.contains(1));
    }

    @Test
    void orQuery() {
        Set<Integer> results = parser.parse("fox OR cat");
        assertEquals(2, results.size());
        assertTrue(results.contains(1));
        assertTrue(results.contains(3));
    }

    @Test
    void notQuery() {
        Set<Integer> results = parser.parse("NOT quick");
        assertEquals(1, results.size());
        assertTrue(results.contains(3));
    }

    @Test
    void phraseQuery() {
        Set<Integer> results = parser.parse("\"quick brown\"");
        assertEquals(1, results.size());
        assertTrue(results.contains(1));
    }

    @Test
    void emptyQueryReturnsEmpty() {
        assertTrue(parser.parse("").isEmpty());
        assertTrue(parser.parse(null).isEmpty());
    }
}
