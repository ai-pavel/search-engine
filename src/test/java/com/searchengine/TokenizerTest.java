package com.searchengine;

import com.searchengine.tokenizer.Tokenizer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TokenizerTest {

    private final Tokenizer tokenizer = new Tokenizer();

    @Test
    void tokenizeBasicText() {
        List<String> tokens = tokenizer.tokenize("Hello World");
        assertEquals(2, tokens.size());
        assertEquals("hello", tokens.get(0));
        assertEquals("world", tokens.get(1));
    }

    @Test
    void tokenizeRemovesPunctuation() {
        List<String> tokens = tokenizer.tokenize("Hello, world! How are you?");
        assertTrue(tokens.contains("hello"));
        assertTrue(tokens.contains("world"));
        assertFalse(tokens.stream().anyMatch(t -> t.contains(",")));
    }

    @Test
    void tokenizeHandlesNull() {
        assertTrue(tokenizer.tokenize(null).isEmpty());
    }

    @Test
    void tokenizeHandlesBlank() {
        assertTrue(tokenizer.tokenize("   ").isEmpty());
    }

    @Test
    void stemReducesSuffixes() {
        assertEquals("happi", tokenizer.stem("happiness"));
        assertEquals("run", tokenizer.stem("running"));
        assertEquals("creat", tokenizer.stem("creation"));
    }

    @Test
    void stemShortWordsUnchanged() {
        assertEquals("the", tokenizer.stem("the"));
        assertEquals("a", tokenizer.stem("a"));
    }
}
