package com.searchengine;

import com.searchengine.index.Posting;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PostingTest {

    @Test
    void constructorSetsDocId() {
        Posting posting = new Posting(42);
        assertEquals(42, posting.getDocId());
    }

    @Test
    void constructorStartsWithZeroFrequencyAndEmptyPositions() {
        Posting posting = new Posting(1);
        assertEquals(0, posting.getTermFrequency());
        assertTrue(posting.getPositions().isEmpty());
    }

    @Test
    void addOccurrenceIncrementsFrequencyAndAddsPosition() {
        Posting posting = new Posting(1);
        posting.addOccurrence(5);
        assertEquals(1, posting.getTermFrequency());
        assertEquals(List.of(5), posting.getPositions());
    }

    @Test
    void multipleOccurrencesAccumulateCorrectly() {
        Posting posting = new Posting(1);
        posting.addOccurrence(0);
        posting.addOccurrence(3);
        posting.addOccurrence(7);
        posting.addOccurrence(15);

        assertEquals(4, posting.getTermFrequency());
        assertEquals(List.of(0, 3, 7, 15), posting.getPositions());
    }

    @Test
    void positionsPreserveInsertionOrder() {
        Posting posting = new Posting(1);
        posting.addOccurrence(10);
        posting.addOccurrence(2);
        posting.addOccurrence(8);

        List<Integer> positions = posting.getPositions();
        assertEquals(10, positions.get(0));
        assertEquals(2, positions.get(1));
        assertEquals(8, positions.get(2));
    }

    @Test
    void differentDocIdsAreIndependent() {
        Posting p1 = new Posting(1);
        Posting p2 = new Posting(2);
        p1.addOccurrence(0);

        assertEquals(1, p1.getTermFrequency());
        assertEquals(0, p2.getTermFrequency());
    }
}
