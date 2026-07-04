package com.searchengine.index;

import com.searchengine.tokenizer.Tokenizer;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe inverted index that maps terms to posting lists.
 */
public class InvertedIndex implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Map<String, List<Posting>> index;
    private final Map<Integer, Document> documents;
    private final AtomicInteger nextDocId;

    public InvertedIndex() {
        this.index = new ConcurrentHashMap<>();
        this.documents = new ConcurrentHashMap<>();
        this.nextDocId = new AtomicInteger(1);
    }

    /**
     * Index a document and return its assigned ID.
     */
    public synchronized int addDocument(String title, String content) {
        Tokenizer tokenizer = new Tokenizer();
        List<String> tokens = tokenizer.tokenize(content);

        int docId = nextDocId.getAndIncrement();
        Document doc = new Document(docId, title, content, tokens.size());
        documents.put(docId, doc);

        for (int pos = 0; pos < tokens.size(); pos++) {
            String term = tokens.get(pos);
            List<Posting> postings = index.computeIfAbsent(term, k -> new ArrayList<>());
            Posting posting = findPosting(postings, docId);
            if (posting == null) {
                posting = new Posting(docId);
                postings.add(posting);
            }
            posting.addOccurrence(pos);
        }

        return docId;
    }

    private Posting findPosting(List<Posting> postings, int docId) {
        for (Posting p : postings) {
            if (p.getDocId() == docId) {
                return p;
            }
        }
        return null;
    }

    /**
     * Get postings for a term.
     */
    public List<Posting> getPostings(String term) {
        Tokenizer tokenizer = new Tokenizer();
        String stemmed = tokenizer.stem(term.toLowerCase(Locale.ROOT));
        return index.getOrDefault(stemmed, Collections.emptyList());
    }

    /**
     * Get all document IDs in the index.
     */
    public Set<Integer> getAllDocIds() {
        return Collections.unmodifiableSet(documents.keySet());
    }

    public Document getDocument(int docId) {
        return documents.get(docId);
    }

    public int getDocumentCount() {
        return documents.size();
    }

    public int getTermCount() {
        return index.size();
    }

    public Map<String, List<Posting>> getFullIndex() {
        return Collections.unmodifiableMap(index);
    }

    public Map<Integer, Document> getDocuments() {
        return Collections.unmodifiableMap(documents);
    }
}
