package com.searchengine.persistence;

import com.searchengine.index.InvertedIndex;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Persists and loads the inverted index to/from disk using Java serialization.
 */
public class IndexPersistence {

    private final String filePath;

    public IndexPersistence(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Save the index to disk.
     */
    public void save(InvertedIndex index) throws IOException {
        Path path = Paths.get(filePath);
        Files.createDirectories(path.getParent());
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(filePath)))) {
            oos.writeObject(index);
        }
    }

    /**
     * Load the index from disk. Returns a new empty index if file does not exist.
     */
    public InvertedIndex load() {
        File file = new File(filePath);
        if (!file.exists()) {
            return new InvertedIndex();
        }
        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(file)))) {
            return (InvertedIndex) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Warning: Could not load index from disk, starting fresh. " + e.getMessage());
            return new InvertedIndex();
        }
    }
}
