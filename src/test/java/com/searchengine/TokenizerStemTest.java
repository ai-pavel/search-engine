package com.searchengine;

import com.searchengine.tokenizer.Tokenizer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TokenizerStemTest {

    private final Tokenizer tokenizer = new Tokenizer();

    // --- stem: endsWith("ies") && length > 4 ---

    @Test
    void stemIesSuffix() {
        assertEquals("berry", tokenizer.stem("berries"));
        assertEquals("puppy", tokenizer.stem("puppies"));
    }

    @Test
    void stemIesEdgeTooShort() {
        // "ties" length 4, NOT > 4 -> falls through to "s" rule -> "tie"
        assertEquals("tie", tokenizer.stem("ties"));
    }

    // --- stem: endsWith("sses") ---

    @Test
    void stemSsesSuffix() {
        assertEquals("dress", tokenizer.stem("dresses"));
        assertEquals("grass", tokenizer.stem("grasses"));
    }

    // --- stem: endsWith("ing") && length > 5 ---

    @Test
    void stemIngWithDoubledConsonant() {
        assertEquals("sit", tokenizer.stem("sitting"));
        assertEquals("run", tokenizer.stem("running"));
    }

    @Test
    void stemIngWithoutDoubledConsonant() {
        assertEquals("act", tokenizer.stem("acting"));
    }

    @Test
    void stemIngEdgeTooShort() {
        // "bing" length 4, NOT > 5 -> no match, returns "bing"
        assertEquals("bing", tokenizer.stem("bing"));
        // "doing" length 5, NOT > 5 -> no match, returns "doing"
        assertEquals("doing", tokenizer.stem("doing"));
    }

    // --- stem: endsWith("tion") && length > 5 ---

    @Test
    void stemTionSuffix() {
        assertEquals("act", tokenizer.stem("action"));
        assertEquals("nat", tokenizer.stem("nation"));
    }

    @Test
    void stemTionEdgeTooShort() {
        // "tion" length 4, <= 3 is false but length NOT > 5 -> falls through all rules
        assertEquals("tion", tokenizer.stem("tion"));
    }

    // --- stem: endsWith("ment") && length > 5 ---

    @Test
    void stemMentSuffix() {
        assertEquals("move", tokenizer.stem("movement"));
        assertEquals("agree", tokenizer.stem("agreement"));
    }

    @Test
    void stemMentEdgeTooShort() {
        // "ament" length 5, NOT > 5 -> falls through
        assertEquals("ament", tokenizer.stem("ament"));
    }

    // --- stem: endsWith("ness") && length > 5 ---

    @Test
    void stemNessSuffix() {
        assertEquals("sad", tokenizer.stem("sadness"));
        assertEquals("dark", tokenizer.stem("darkness"));
    }

    @Test
    void stemNessEdgeTooShort() {
        // length 5, NOT > 5 -> falls through, then endsWith("s") and !endsWith("ss") and length > 3 -> strip "s"
        assertEquals("enes", tokenizer.stem("eness"));
    }

    // --- stem: endsWith("able") && length > 5 ---

    @Test
    void stemAbleSuffix() {
        assertEquals("read", tokenizer.stem("readable"));
        assertEquals("mov", tokenizer.stem("movable"));
    }

    @Test
    void stemAbleEdgeTooShort() {
        // "cable" length 5, NOT > 5 -> falls through all rules
        assertEquals("cable", tokenizer.stem("cable"));
    }

    // --- stem: endsWith("ly") && length > 4 ---

    @Test
    void stemLySuffix() {
        assertEquals("quick", tokenizer.stem("quickly"));
        assertEquals("slow", tokenizer.stem("slowly"));
    }

    @Test
    void stemLyEdgeTooShort() {
        // "rely" length 4, NOT > 4
        assertEquals("rely", tokenizer.stem("rely"));
        assertEquals("ally", tokenizer.stem("ally"));
    }

    // --- stem: endsWith("ed") && length > 4 ---

    @Test
    void stemEdSuffix() {
        assertEquals("jump", tokenizer.stem("jumped"));
        assertEquals("play", tokenizer.stem("played"));
    }

    @Test
    void stemEdEdgeTooShort() {
        // "shed" length 4, NOT > 4
        assertEquals("shed", tokenizer.stem("shed"));
    }

    // --- stem: endsWith("er") && length > 4 ---

    @Test
    void stemErSuffix() {
        assertEquals("play", tokenizer.stem("player"));
        assertEquals("fast", tokenizer.stem("faster"));
    }

    @Test
    void stemErEdgeTooShort() {
        // "beer" length 4, NOT > 4
        assertEquals("beer", tokenizer.stem("beer"));
    }

    // --- stem: endsWith("es") && length > 4 ---

    @Test
    void stemEsSuffix() {
        assertEquals("box", tokenizer.stem("boxes"));
        assertEquals("watch", tokenizer.stem("watches"));
    }

    @Test
    void stemEsEdgeTooShort() {
        // "goes" length 4, NOT > 4 -> falls through to "s" rule -> "goe"
        assertEquals("goe", tokenizer.stem("goes"));
    }

    // --- stem: endsWith("s") && !endsWith("ss") && length > 3 ---

    @Test
    void stemTrailingS() {
        assertEquals("cat", tokenizer.stem("cats"));
        assertEquals("dog", tokenizer.stem("dogs"));
    }

    @Test
    void stemTrailingSBlockedByDoubleS() {
        // "boss" endsWith("ss") -> rule does NOT match -> returns "boss"
        assertEquals("boss", tokenizer.stem("boss"));
    }

    @Test
    void stemTrailingSEdgeTooShort() {
        // "as" length 2, <= 3 -> returned unchanged by the first guard
        assertEquals("as", tokenizer.stem("as"));
    }

    // --- stem: no suffix match -> return unchanged ---

    @Test
    void stemNoMatchReturnsUnchanged() {
        assertEquals("jump", tokenizer.stem("jump"));
        assertEquals("quick", tokenizer.stem("quick"));
    }

    // --- tokenize: mixed content edge cases ---

    @Test
    void tokenizeWithNumbers() {
        List<String> tokens = tokenizer.tokenize("test123 456abc");
        assertEquals(2, tokens.size());
        assertEquals("test123", tokens.get(0));
        assertEquals("456abc", tokens.get(1));
    }

    @Test
    void tokenizeStripsSpecialCharsEntirely() {
        // A token that is only punctuation should be removed
        List<String> tokens = tokenizer.tokenize("hello --- world");
        assertEquals(2, tokens.size());
        assertEquals("hello", tokens.get(0));
        assertEquals("world", tokens.get(1));
    }

    @Test
    void tokenizeEmptyString() {
        assertTrue(tokenizer.tokenize("").isEmpty());
    }

    @Test
    void tokenizeMultipleSpaces() {
        List<String> tokens = tokenizer.tokenize("  hello   world  ");
        assertEquals(2, tokens.size());
    }
}
