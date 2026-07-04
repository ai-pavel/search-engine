package com.searchengine.ranker;

/**
 * A single search result with document metadata and score.
 */
public class SearchResult {

    private final int docId;
    private final String title;
    private final double score;
    private final String snippet;

    public SearchResult(int docId, String title, double score, String snippet) {
        this.docId = docId;
        this.title = title;
        this.score = score;
        this.snippet = snippet;
    }

    public int getDocId() {
        return docId;
    }

    public String getTitle() {
        return title;
    }

    public double getScore() {
        return score;
    }

    public String getSnippet() {
        return snippet;
    }
}
