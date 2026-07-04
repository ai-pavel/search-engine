package com.searchengine.index;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a posting: a document ID, the term frequency in that document,
 * and the positions where the term appears.
 */
public class Posting implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int docId;
    private int termFrequency;
    private final List<Integer> positions;

    public Posting(int docId) {
        this.docId = docId;
        this.termFrequency = 0;
        this.positions = new ArrayList<>();
    }

    public void addOccurrence(int position) {
        termFrequency++;
        positions.add(position);
    }

    public int getDocId() {
        return docId;
    }

    public int getTermFrequency() {
        return termFrequency;
    }

    public List<Integer> getPositions() {
        return positions;
    }
}
