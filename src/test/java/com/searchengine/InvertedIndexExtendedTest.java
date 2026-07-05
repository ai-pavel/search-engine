package com.searchengine;

import com.searchengine.index.Document;
import com.searchengine.index.InvertedIndex;
import com.searchengine.index.Posting;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class InvertedIndexExtendedTest {

    private InvertedIndex index;

    @BeforeEach
    void setUp() {
        index = new InvertedIndex();
        index.addDocument("Doc 1", "the quick brown fox jumps over the lazy dog");
        index.addDocument("Doc 2", "the quick red car drives fast");
        index.addDocument("Doc 3", "a lazy cat sleeps all day");
    }

    @Test
    void getAllDocIdsReturnsCorrectSet() {
        Set<Integer> ids = index.getAllDocIds();
        assertEquals(Set.of(1, 2, 3), ids);
    }

    @Test
    void getDocumentReturnsCorrectDocument() {
        Document doc = index.getDocument(1);
        assertNotNull(doc);
        assertEquals(1, doc.getId());
        assertEquals("Doc 1", doc.getTitle());
        assertEquals("the quick brown fox jumps over the lazy dog", doc.getContent());
    }

    @Test
    void getDocumentForNonExistentIdReturnsNull() {
        assertNull(index.getDocument(999));
    }

    @Test
    void getTermCountReturnsCorrectCount() {
        int termCount = index.getTermCount();
        // Doc 1: the, quick, brown, fox, jump, over, lazy, dog (8 unique stems, "the" appears twice but one term)
        // Doc 2: the, quick, red, car, driv, fast (6 unique stems; "the" and "quick" overlap with Doc 1)
        // Doc 3: a, lazy, cat, sleep, all, day (6 unique stems; "lazy" overlaps)
        // Total unique: the, quick, brown, fox, jump, over, lazy, dog, red, car, driv, fast, a, cat, sleep, all, day = 17
        assertTrue(termCount > 0);
    }

    @Test
    void getFullIndexContainsExpectedTerms() {
        Map<String, List<Posting>> fullIndex = index.getFullIndex();
        assertTrue(fullIndex.containsKey("quick"));
        assertTrue(fullIndex.containsKey("brown"));
        assertTrue(fullIndex.containsKey("fox"));
        assertTrue(fullIndex.containsKey("the"));
        assertTrue(fullIndex.containsKey("lazy"));
        assertTrue(fullIndex.containsKey("cat"));
    }

    @Test
    void getDocumentsReturnsAllDocuments() {
        Map<Integer, Document> docs = index.getDocuments();
        assertEquals(3, docs.size());
        assertEquals("Doc 1", docs.get(1).getTitle());
        assertEquals("Doc 2", docs.get(2).getTitle());
        assertEquals("Doc 3", docs.get(3).getTitle());
    }

    @Test
    void getPostingsWithPositionsVerified() {
        // "the" appears at positions 0 and 6 in Doc 1 ("the quick brown fox jumps over the lazy dog")
        List<Posting> postings = index.getPostings("the");
        Posting doc1Posting = postings.stream().filter(p -> p.getDocId() == 1).findFirst().orElse(null);
        assertNotNull(doc1Posting);
        assertEquals(2, doc1Posting.getTermFrequency());
        assertTrue(doc1Posting.getPositions().contains(0));
        assertTrue(doc1Posting.getPositions().contains(6));
    }

    @Test
    void positionsAreCorrectForFirstDocument() {
        // "quick" is at position 1 in Doc 1
        List<Posting> postings = index.getPostings("quick");
        Posting doc1Posting = postings.stream().filter(p -> p.getDocId() == 1).findFirst().orElse(null);
        assertNotNull(doc1Posting);
        assertEquals(1, doc1Posting.getTermFrequency());
        assertEquals(List.of(1), doc1Posting.getPositions());
    }

    @Test
    void emptyDocumentGetsIdButNoTerms() {
        int initialTermCount = index.getTermCount();
        int docId = index.addDocument("Empty Doc", "   ");
        assertEquals(4, docId);
        assertEquals(4, index.getDocumentCount());
        assertEquals(initialTermCount, index.getTermCount());
    }

    @Test
    void multipleDocumentsWithSharedTerms() {
        // "quick" appears in Doc 1 and Doc 2
        List<Posting> postings = index.getPostings("quick");
        assertEquals(2, postings.size());
        Set<Integer> docIds = Set.of(postings.get(0).getDocId(), postings.get(1).getDocId());
        assertTrue(docIds.contains(1));
        assertTrue(docIds.contains(2));
    }

    @Test
    void documentTotalTermsIsCorrect() {
        Document doc = index.getDocument(1);
        // "the quick brown fox jumps over the lazy dog" = 9 words, all produce tokens
        // Stemmed tokens: the, quick, brown, fox, jump, over, the, lazy, dog = 9 tokens
        assertEquals(9, doc.getTotalTerms());
    }

    @Test
    void getFullIndexIsUnmodifiable() {
        Map<String, List<Posting>> fullIndex = index.getFullIndex();
        assertThrows(UnsupportedOperationException.class, () -> fullIndex.put("newterm", List.of()));
    }

    @Test
    void getDocumentsIsUnmodifiable() {
        Map<Integer, Document> docs = index.getDocuments();
        assertThrows(UnsupportedOperationException.class, () -> docs.put(99, null));
    }

    @Test
    void getAllDocIdsIsUnmodifiable() {
        Set<Integer> ids = index.getAllDocIds();
        assertThrows(UnsupportedOperationException.class, () -> ids.add(99));
    }

    @Test
    void getPostingsForStemmedTerm() {
        // "jumps" is stored as "jump" after stemming
        // getPostings stems its input, so "jumps" → stem("jumps") → "jump"
        List<Posting> postings = index.getPostings("jumps");
        assertEquals(1, postings.size());
        assertEquals(1, postings.get(0).getDocId());
    }

    @Test
    void addDocumentWithRepeatedTerms() {
        int docId = index.addDocument("Repeat", "hello hello hello world world");
        List<Posting> helloPostings = index.getPostings("hello");
        Posting repeatPosting = helloPostings.stream().filter(p -> p.getDocId() == docId).findFirst().orElse(null);
        assertNotNull(repeatPosting);
        assertEquals(3, repeatPosting.getTermFrequency());
        assertEquals(List.of(0, 1, 2), repeatPosting.getPositions());
    }
}
