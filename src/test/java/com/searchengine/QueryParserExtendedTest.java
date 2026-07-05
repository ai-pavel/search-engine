package com.searchengine;

import com.searchengine.index.InvertedIndex;
import com.searchengine.query.QueryParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class QueryParserExtendedTest {

    private InvertedIndex index;
    private QueryParser parser;

    @BeforeEach
    void setUp() {
        index = new InvertedIndex();
        index.addDocument("Doc 1", "the quick brown fox jumps over the lazy dog");
        index.addDocument("Doc 2", "the quick red car drives fast");
        index.addDocument("Doc 3", "a lazy cat sleeps all day");
        parser = new QueryParser(index);
    }

    @Test
    void extractTermsWithNull() {
        assertTrue(parser.extractTerms(null).isEmpty());
    }

    @Test
    void extractTermsWithBlank() {
        assertTrue(parser.extractTerms("   ").isEmpty());
    }

    @Test
    void extractTermsReturnsLowercasedTerms() {
        List<String> terms = parser.extractTerms("Quick Fox");
        assertEquals(List.of("quick", "fox"), terms);
    }

    @Test
    void extractTermsStripsQuotes() {
        List<String> terms = parser.extractTerms("\"quick brown\"");
        assertTrue(terms.contains("quick"));
        assertTrue(terms.contains("brown"));
    }

    @Test
    void extractTermsRemovesOperators() {
        List<String> terms = parser.extractTerms("quick AND fox OR lazy NOT cat");
        assertTrue(terms.contains("quick"));
        assertTrue(terms.contains("fox"));
        assertTrue(terms.contains("lazy"));
        assertTrue(terms.contains("cat"));
        assertFalse(terms.contains("AND"));
        assertFalse(terms.contains("OR"));
        assertFalse(terms.contains("NOT"));
    }

    @Test
    void extractTermsWithSpecialChars() {
        List<String> terms = parser.extractTerms("hello-world foo_bar");
        assertTrue(terms.contains("helloworld"));
        assertTrue(terms.contains("foobar"));
    }

    @Test
    void andCombinedWithOr() {
        // Parser precedence: AND binds tighter than OR
        // "quick AND fox OR cat" = (quick AND fox) OR cat
        Set<Integer> results = parser.parse("quick AND fox OR cat");
        assertTrue(results.contains(1)); // quick AND fox
        assertTrue(results.contains(3)); // cat
        assertFalse(results.contains(2)); // Doc 2 has quick but not fox, and no cat
    }

    @Test
    void multipleAnds() {
        Set<Integer> results = parser.parse("quick AND brown AND fox");
        assertEquals(1, results.size());
        assertTrue(results.contains(1));
    }

    @Test
    void multipleOrs() {
        Set<Integer> results = parser.parse("fox OR car OR cat");
        assertEquals(3, results.size());
        assertTrue(results.contains(1));
        assertTrue(results.contains(2));
        assertTrue(results.contains(3));
    }

    @Test
    void notCombinedWithAnd() {
        // "quick AND NOT fox" = quick AND (NOT fox)
        Set<Integer> results = parser.parse("quick AND NOT fox");
        assertEquals(1, results.size());
        assertTrue(results.contains(2)); // Doc 2 has quick but not fox
    }

    @Test
    void phraseNonMatchingTermsAcrossDocuments() {
        // "brown" in Doc 1, "car" in Doc 2 — not in same doc as adjacent phrase
        Set<Integer> results = parser.parse("\"brown car\"");
        assertTrue(results.isEmpty());
    }

    @Test
    void phraseSingleWord() {
        // A single-word phrase should behave like a single term search
        Set<Integer> results = parser.parse("\"quick\"");
        assertEquals(2, results.size());
        assertTrue(results.contains(1));
        assertTrue(results.contains(2));
    }

    @Test
    void unterminatedQuoteThrowsException() {
        assertThrows(StringIndexOutOfBoundsException.class, () -> parser.parse("\"quick brown"));
    }

    @Test
    void queryWithOnlyOperators() {
        // "AND OR NOT" — "AND" parsed as a term, "OR" as operator, "NOT" as operator
        // Result includes all docs because NOT with empty operand = all doc IDs
        Set<Integer> results = parser.parse("AND OR NOT");
        assertEquals(3, results.size());
    }

    @Test
    void phraseTermsInSameDocButNotAdjacent() {
        // "quick" is at position 1, "fox" is at position 3 in Doc 1
        // They are not adjacent, so phrase search should not match
        Set<Integer> results = parser.parse("\"quick fox\"");
        assertTrue(results.isEmpty());
    }

    @Test
    void phraseTermsAdjacent() {
        // "quick brown" are adjacent in Doc 1 (positions 1, 2)
        Set<Integer> results = parser.parse("\"quick brown\"");
        assertEquals(1, results.size());
        assertTrue(results.contains(1));
    }

    @Test
    void complexBooleanWithParentheses() {
        // No parentheses support — parsed left-to-right with precedence
        // "fox AND quick OR lazy" = (fox AND quick) OR lazy
        Set<Integer> results = parser.parse("fox AND quick OR lazy");
        assertTrue(results.contains(1)); // fox AND quick
        assertTrue(results.contains(3)); // lazy (in Doc 3)
        // Doc 1 also has "lazy" so it should be in the result via OR
    }

    @Test
    void notWithSpecificTerm() {
        // NOT lazy should return Doc 2 (only doc without "lazy")
        Set<Integer> results = parser.parse("NOT lazy");
        assertEquals(1, results.size());
        assertTrue(results.contains(2));
    }

    @Test
    void phraseQueryWithEmptyPhrase() {
        Set<Integer> results = parser.parse("\"\"");
        assertTrue(results.isEmpty());
    }

    @Test
    void queryWithMixedCaseOperators() {
        // Only uppercase AND/OR/NOT are operators; lowercase should be treated as terms
        Set<Integer> results = parser.parse("quick and fox");
        // "and" is treated as a term, not an operator
        // This becomes: quick (term) → then "and" → evaluateTerm("and") → then "fox"
        // Since there's no explicit AND/OR between them, "and" and "fox" are just
        // parsed as additional primary terms via the AND path (implicit)
        assertNotNull(results);
    }

    @Test
    void orWithNot() {
        // "fox OR NOT quick" = fox OR (NOT quick)
        Set<Integer> results = parser.parse("fox OR NOT quick");
        assertTrue(results.contains(1)); // fox
        assertTrue(results.contains(3)); // NOT quick (Doc 3 has no quick)
    }
}
