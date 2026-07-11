package com.searchengine;

import com.searchengine.index.Document;
import com.searchengine.index.Posting;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DocumentAndPostingTest {

    @Test
    void documentGettersReturnConstructorValues() {
        Document doc = new Document(7, "Title", "content body", 5);
        assertEquals(7, doc.getId());
        assertEquals("Title", doc.getTitle());
        assertEquals("content body", doc.getContent());
        assertEquals(5, doc.getTotalTerms());
    }

    @Test
    void postingRecordsTermFrequencyAndPositions() {
        Posting p = new Posting(3);
        assertEquals(3, p.getDocId());
        assertEquals(0, p.getTermFrequency());
        assertTrue(p.getPositions().isEmpty());

        p.addOccurrence(0);
        p.addOccurrence(5);
        p.addOccurrence(10);
        assertEquals(3, p.getTermFrequency());
        assertEquals(java.util.List.of(0, 5, 10), p.getPositions());
    }
}