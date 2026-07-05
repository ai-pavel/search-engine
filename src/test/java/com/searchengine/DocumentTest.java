package com.searchengine;

import com.searchengine.index.Document;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DocumentTest {

    @Test
    void constructorSetsAllFields() {
        Document doc = new Document(1, "Title", "Content body", 2);
        assertEquals(1, doc.getId());
        assertEquals("Title", doc.getTitle());
        assertEquals("Content body", doc.getContent());
        assertEquals(2, doc.getTotalTerms());
    }

    @Test
    void getIdReturnsCorrectValue() {
        Document doc = new Document(99, "T", "C", 1);
        assertEquals(99, doc.getId());
    }

    @Test
    void getTitleReturnsCorrectValue() {
        Document doc = new Document(1, "My Document", "text", 1);
        assertEquals("My Document", doc.getTitle());
    }

    @Test
    void getContentReturnsCorrectValue() {
        Document doc = new Document(1, "T", "some long content here", 4);
        assertEquals("some long content here", doc.getContent());
    }

    @Test
    void getTotalTermsReturnsCorrectValue() {
        Document doc = new Document(1, "T", "C", 42);
        assertEquals(42, doc.getTotalTerms());
    }

    @Test
    void worksWithEmptyStrings() {
        Document doc = new Document(1, "", "", 0);
        assertEquals("", doc.getTitle());
        assertEquals("", doc.getContent());
        assertEquals(0, doc.getTotalTerms());
    }

    @Test
    void worksWithLargeContent() {
        String largeContent = "word ".repeat(10000).trim();
        Document doc = new Document(1, "Big Doc", largeContent, 10000);
        assertEquals(largeContent, doc.getContent());
        assertEquals(10000, doc.getTotalTerms());
    }
}
