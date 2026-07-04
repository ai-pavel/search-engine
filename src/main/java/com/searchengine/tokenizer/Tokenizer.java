package com.searchengine.tokenizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Tokenizer that splits text on whitespace, lowercases, removes punctuation,
 * and applies a simple Porter-style suffix stemmer.
 */
public class Tokenizer {

    /**
     * Tokenize and stem the input text.
     */
    public List<String> tokenize(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        String[] raw = text.toLowerCase(Locale.ROOT).split("\\s+");
        List<String> tokens = new ArrayList<>();
        for (String word : raw) {
            String cleaned = word.replaceAll("[^a-z0-9]", "");
            if (!cleaned.isEmpty()) {
                tokens.add(stem(cleaned));
            }
        }
        return tokens;
    }

    /**
     * Simple suffix-stripping stemmer (handles common English suffixes).
     */
    public String stem(String word) {
        if (word.length() <= 3) {
            return word;
        }
        if (word.endsWith("ies") && word.length() > 4) {
            return word.substring(0, word.length() - 3) + "y";
        }
        if (word.endsWith("sses")) {
            return word.substring(0, word.length() - 2);
        }
        if (word.endsWith("ing") && word.length() > 5) {
            return word.substring(0, word.length() - 3);
        }
        if (word.endsWith("tion") && word.length() > 5) {
            return word.substring(0, word.length() - 4) + "t";
        }
        if (word.endsWith("ment") && word.length() > 5) {
            return word.substring(0, word.length() - 4);
        }
        if (word.endsWith("ness") && word.length() > 5) {
            return word.substring(0, word.length() - 4);
        }
        if (word.endsWith("able") && word.length() > 5) {
            return word.substring(0, word.length() - 4);
        }
        if (word.endsWith("ly") && word.length() > 4) {
            return word.substring(0, word.length() - 2);
        }
        if (word.endsWith("ed") && word.length() > 4) {
            return word.substring(0, word.length() - 2);
        }
        if (word.endsWith("er") && word.length() > 4) {
            return word.substring(0, word.length() - 2);
        }
        if (word.endsWith("es") && word.length() > 4) {
            return word.substring(0, word.length() - 2);
        }
        if (word.endsWith("s") && !word.endsWith("ss") && word.length() > 3) {
            return word.substring(0, word.length() - 1);
        }
        return word;
    }
}
