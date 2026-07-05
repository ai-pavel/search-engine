package com.searchengine;

import com.searchengine.index.InvertedIndex;
import com.searchengine.persistence.IndexPersistence;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class IndexPersistenceTest {

    @TempDir
    Path tempDir;

    @Test
    void loadReturnsEmptyIndexWhenFileDoesNotExist() {
        IndexPersistence persistence = new IndexPersistence(tempDir.resolve("nonexistent.ser").toString());
        InvertedIndex index = persistence.load();
        assertNotNull(index);
        assertEquals(0, index.getDocumentCount());
        assertEquals(0, index.getTermCount());
    }

    @Test
    void saveAndLoadRoundTrip() throws IOException {
        String filePath = tempDir.resolve("index.ser").toString();
        IndexPersistence persistence = new IndexPersistence(filePath);

        InvertedIndex original = new InvertedIndex();
        original.addDocument("Doc 1", "hello world");
        original.addDocument("Doc 2", "foo bar baz");

        persistence.save(original);

        InvertedIndex loaded = persistence.load();
        assertEquals(original.getDocumentCount(), loaded.getDocumentCount());
        assertEquals(original.getTermCount(), loaded.getTermCount());
    }

    @Test
    void saveCreatesParentDirectories() throws IOException {
        String filePath = tempDir.resolve("nested/dirs/index.ser").toString();
        IndexPersistence persistence = new IndexPersistence(filePath);

        InvertedIndex index = new InvertedIndex();
        index.addDocument("Doc", "test content");

        persistence.save(index);

        assertTrue(Files.exists(Path.of(filePath)));
        InvertedIndex loaded = persistence.load();
        assertEquals(1, loaded.getDocumentCount());
    }

    @Test
    void loadReturnsEmptyIndexWhenFileIsCorrupted() throws IOException {
        Path corruptedFile = tempDir.resolve("corrupted.ser");
        Files.write(corruptedFile, new byte[]{0x00, 0x01, 0x02, 0x03, 0x04});

        IndexPersistence persistence = new IndexPersistence(corruptedFile.toString());
        InvertedIndex index = persistence.load();
        assertNotNull(index);
        assertEquals(0, index.getDocumentCount());
    }

    @Test
    void saveAndLoadPreservesDocumentData() throws IOException {
        String filePath = tempDir.resolve("index.ser").toString();
        IndexPersistence persistence = new IndexPersistence(filePath);

        InvertedIndex original = new InvertedIndex();
        original.addDocument("My Title", "some important content here");

        persistence.save(original);

        InvertedIndex loaded = persistence.load();
        var doc = loaded.getDocument(1);
        assertNotNull(doc);
        assertEquals("My Title", doc.getTitle());
        assertEquals("some important content here", doc.getContent());
    }
}
