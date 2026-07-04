package com.searchengine;

import com.searchengine.index.InvertedIndex;
import com.searchengine.index.Posting;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InvertedIndexTest {

    private InvertedIndex index;

    @BeforeEach
    void setUp() {
        index = new InvertedIndex();
    }

    @Test
    void addDocumentReturnsIncrementingIds() {
        int id1 = index.addDocument("Doc 1", "hello world");
        int id2 = index.addDocument("Doc 2", "foo bar");
        assertEquals(1, id1);
        assertEquals(2, id2);
    }

    @Test
    void getPostingsReturnsCorrectDocuments() {
        index.addDocument("Doc 1", "the quick brown fox");
        index.addDocument("Doc 2", "the lazy dog");

        List<Posting> postings = index.getPostings("the");
        assertEquals(2, postings.size());
    }

    @Test
    void getPostingsForMissingTermReturnsEmpty() {
        index.addDocument("Doc 1", "hello world");
        assertTrue(index.getPostings("nonexistent").isEmpty());
    }

    @Test
    void documentCountIsCorrect() {
        assertEquals(0, index.getDocumentCount());
        index.addDocument("Doc 1", "hello");
        assertEquals(1, index.getDocumentCount());
        index.addDocument("Doc 2", "world");
        assertEquals(2, index.getDocumentCount());
    }

    @Test
    void termFrequencyIsCorrect() {
        index.addDocument("Doc 1", "hello hello hello world");
        List<Posting> postings = index.getPostings("hello");
        assertEquals(1, postings.size());
        assertEquals(3, postings.get(0).getTermFrequency());
    }
}
