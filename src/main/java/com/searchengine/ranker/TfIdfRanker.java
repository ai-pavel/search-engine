package com.searchengine.ranker;

import com.searchengine.index.InvertedIndex;
import com.searchengine.index.Posting;

import java.util.*;

/**
 * Ranks documents using TF-IDF scoring.
 */
public class TfIdfRanker {

    private final InvertedIndex index;

    public TfIdfRanker(InvertedIndex index) {
        this.index = index;
    }

    /**
     * Compute TF-IDF score for a term in a specific document.
     */
    public double score(String term, int docId) {
        List<Posting> postings = index.getPostings(term);
        if (postings.isEmpty()) {
            return 0.0;
        }

        Posting posting = null;
        for (Posting p : postings) {
            if (p.getDocId() == docId) {
                posting = p;
                break;
            }
        }
        if (posting == null) {
            return 0.0;
        }

        // TF: log(1 + term frequency)
        double tf = Math.log(1.0 + posting.getTermFrequency());

        // IDF: log(N / df)
        int totalDocs = index.getDocumentCount();
        int docFreq = postings.size();
        double idf = Math.log((double) totalDocs / docFreq);

        return tf * idf;
    }

    /**
     * Rank a set of document IDs by their aggregate TF-IDF score for the given terms.
     */
    public List<SearchResult> rank(Set<Integer> docIds, List<String> queryTerms) {
        Map<Integer, Double> scores = new HashMap<>();

        for (int docId : docIds) {
            double totalScore = 0.0;
            for (String term : queryTerms) {
                totalScore += score(term, docId);
            }
            if (totalScore > 0) {
                scores.put(docId, totalScore);
            }
        }

        List<SearchResult> results = new ArrayList<>();
        for (Map.Entry<Integer, Double> entry : scores.entrySet()) {
            int docId = entry.getKey();
            var doc = index.getDocument(docId);
            if (doc != null) {
                results.add(new SearchResult(docId, doc.getTitle(), entry.getValue(),
                        snippet(doc.getContent())));
            }
        }

        results.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        return results;
    }

    private String snippet(String content) {
        if (content.length() <= 200) {
            return content;
        }
        return content.substring(0, 200) + "...";
    }
}
