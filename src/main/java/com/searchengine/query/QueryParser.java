package com.searchengine.query;

import com.searchengine.index.InvertedIndex;
import com.searchengine.index.Posting;
import com.searchengine.tokenizer.Tokenizer;

import java.util.*;

/**
 * Parses boolean queries with AND, OR, NOT operators and phrase queries (quoted strings).
 *
 * Grammar:
 *   query     = orExpr
 *   orExpr    = andExpr (OR andExpr)*
 *   andExpr   = notExpr (AND notExpr)*
 *   notExpr   = NOT? primary
 *   primary   = phrase | term
 *   phrase    = "term term ..."
 */
public class QueryParser {

    private final InvertedIndex index;
    private final Tokenizer tokenizer;

    public QueryParser(InvertedIndex index) {
        this.index = index;
        this.tokenizer = new Tokenizer();
    }

    /**
     * Parse and evaluate a query, returning matching document IDs.
     */
    public Set<Integer> parse(String query) {
        if (query == null || query.isBlank()) {
            return Collections.emptySet();
        }
        List<String> tokens = tokenize(query);
        int[] pos = {0};
        return parseOr(tokens, pos);
    }

    /**
     * Extract query terms (for TF-IDF scoring).
     */
    public List<String> extractTerms(String query) {
        if (query == null || query.isBlank()) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>();
        String cleaned = query.replaceAll("\"", " ")
                .replaceAll("\\b(AND|OR|NOT)\\b", " ");
        for (String word : cleaned.split("\\s+")) {
            String w = word.replaceAll("[^a-zA-Z0-9]", "").toLowerCase(Locale.ROOT);
            if (!w.isEmpty()) {
                result.add(w);
            }
        }
        return result;
    }

    private List<String> tokenize(String query) {
        List<String> tokens = new ArrayList<>();
        int i = 0;
        while (i < query.length()) {
            char c = query.charAt(i);
            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }
            if (c == '"') {
                int end = query.indexOf('"', i + 1);
                if (end == -1) end = query.length();
                tokens.add(query.substring(i, end + 1));
                i = end + 1;
            } else {
                int end = i;
                while (end < query.length() && !Character.isWhitespace(query.charAt(end)) && query.charAt(end) != '"') {
                    end++;
                }
                tokens.add(query.substring(i, end));
                i = end;
            }
        }
        return tokens;
    }

    private Set<Integer> parseOr(List<String> tokens, int[] pos) {
        Set<Integer> result = parseAnd(tokens, pos);
        while (pos[0] < tokens.size() && "OR".equals(tokens.get(pos[0]))) {
            pos[0]++;
            Set<Integer> right = parseAnd(tokens, pos);
            result = union(result, right);
        }
        return result;
    }

    private Set<Integer> parseAnd(List<String> tokens, int[] pos) {
        Set<Integer> result = parseNot(tokens, pos);
        while (pos[0] < tokens.size() && "AND".equals(tokens.get(pos[0]))) {
            pos[0]++;
            Set<Integer> right = parseNot(tokens, pos);
            result = intersect(result, right);
        }
        return result;
    }

    private Set<Integer> parseNot(List<String> tokens, int[] pos) {
        if (pos[0] < tokens.size() && "NOT".equals(tokens.get(pos[0]))) {
            pos[0]++;
            Set<Integer> operand = parsePrimary(tokens, pos);
            Set<Integer> all = new HashSet<>(index.getAllDocIds());
            all.removeAll(operand);
            return all;
        }
        return parsePrimary(tokens, pos);
    }

    private Set<Integer> parsePrimary(List<String> tokens, int[] pos) {
        if (pos[0] >= tokens.size()) {
            return Collections.emptySet();
        }
        String token = tokens.get(pos[0]);
        pos[0]++;

        if (token.startsWith("\"") && token.endsWith("\"")) {
            return evaluatePhrase(token.substring(1, token.length() - 1));
        }
        return evaluateTerm(token);
    }

    private Set<Integer> evaluateTerm(String term) {
        List<String> stemmed = tokenizer.tokenize(term);
        if (stemmed.isEmpty()) {
            return Collections.emptySet();
        }
        Set<Integer> result = new HashSet<>();
        for (Posting p : index.getPostings(stemmed.get(0))) {
            result.add(p.getDocId());
        }
        return result;
    }

    private Set<Integer> evaluatePhrase(String phrase) {
        List<String> terms = tokenizer.tokenize(phrase);
        if (terms.isEmpty()) {
            return Collections.emptySet();
        }
        if (terms.size() == 1) {
            return evaluateTerm(terms.get(0));
        }

        // Find docs that contain all terms
        Set<Integer> candidates = null;
        List<List<Posting>> allPostings = new ArrayList<>();
        for (String term : terms) {
            List<Posting> postings = index.getPostings(term);
            allPostings.add(postings);
            Set<Integer> docIds = new HashSet<>();
            for (Posting p : postings) {
                docIds.add(p.getDocId());
            }
            if (candidates == null) {
                candidates = docIds;
            } else {
                candidates.retainAll(docIds);
            }
        }

        if (candidates == null || candidates.isEmpty()) {
            return Collections.emptySet();
        }

        // Check positional adjacency
        Set<Integer> result = new HashSet<>();
        for (int docId : candidates) {
            if (checkPhrase(docId, allPostings)) {
                result.add(docId);
            }
        }
        return result;
    }

    private boolean checkPhrase(int docId, List<List<Posting>> allPostings) {
        // Get positions for first term
        List<Integer> firstPositions = getPositions(docId, allPostings.get(0));
        if (firstPositions.isEmpty()) return false;

        for (int startPos : firstPositions) {
            boolean match = true;
            for (int i = 1; i < allPostings.size(); i++) {
                List<Integer> positions = getPositions(docId, allPostings.get(i));
                if (!positions.contains(startPos + i)) {
                    match = false;
                    break;
                }
            }
            if (match) return true;
        }
        return false;
    }

    private List<Integer> getPositions(int docId, List<Posting> postings) {
        for (Posting p : postings) {
            if (p.getDocId() == docId) {
                return p.getPositions();
            }
        }
        return Collections.emptyList();
    }

    private Set<Integer> union(Set<Integer> a, Set<Integer> b) {
        Set<Integer> result = new HashSet<>(a);
        result.addAll(b);
        return result;
    }

    private Set<Integer> intersect(Set<Integer> a, Set<Integer> b) {
        Set<Integer> result = new HashSet<>(a);
        result.retainAll(b);
        return result;
    }
}
