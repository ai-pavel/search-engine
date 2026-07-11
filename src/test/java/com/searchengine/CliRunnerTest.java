package com.searchengine;

import com.searchengine.cli.CliRunner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CliRunnerTest {

    private InputStream originalIn;
    private PrintStream originalOut;
    private PrintStream originalErr;

    @BeforeEach
    void saveStreams() {
        originalIn = System.in;
        originalOut = System.out;
        originalErr = System.err;
    }

    @AfterEach
    void restoreStreams() {
        System.setIn(originalIn);
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    void noArgsPrintsUsageAndExitsOne() {
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        System.setErr(new PrintStream(err));
        CliRunner.run(new String[]{});
        assertTrue(err.toString().contains("Usage"));
    }

    @Test
    void nonexistentDirectoryExitsOne() {
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        System.setErr(new PrintStream(err));
        CliRunner.run(new String[]{"/no/such/dir/here"});
        assertTrue(err.toString().contains("not a directory"));
    }

    @Test
    void emptyDirectoryExitsZero(@TempDir Path tempDir) throws IOException {
        // Directory exists but has no .txt files.
        Files.createDirectory(tempDir.resolve("subdir"));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        CliRunner.run(new String[]{tempDir.toString()});
        assertTrue(out.toString().contains("No .txt files found."));
    }

    @Test
    void indexesFilesAndSearches(@TempDir Path tempDir) throws IOException {
        // Create two .txt files to index.
        Files.writeString(tempDir.resolve("a.txt"), "java spring programming");
        Files.writeString(tempDir.resolve("b.txt"), "python flask programming");

        // Provide a search query and then quit on stdin.
        String input = "programming\njava\nquit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        CliRunner.run(new String[]{tempDir.toString()});
        String output = out.toString();

        assertTrue(output.contains("Indexed"), "should list indexed files: " + output);
        assertTrue(output.contains("Indexed " + 2 + " documents"), output);
        assertTrue(output.contains("search>"), "should print prompt: " + output);
        // First query "programming" matches both docs; result list should be present.
        assertTrue(output.contains("Found"), output);
        // "java" matches doc 1.
        assertTrue(output.contains("java"), output);
        assertTrue(output.contains("Goodbye"), output);
    }

    @Test
    void emptyQueryIsSkipped(@TempDir Path tempDir) throws IOException {
        Files.writeString(tempDir.resolve("a.txt"), "hello world");
        String input = "\nquit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        CliRunner.run(new String[]{tempDir.toString()});
        // No result output between the prompts.
        assertTrue(out.toString().contains("Goodbye"));
    }

    @Test
    void exitCommandAlsoQuits(@TempDir Path tempDir) throws IOException {
        Files.writeString(tempDir.resolve("a.txt"), "hello world");
        String input = "exit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        CliRunner.run(new String[]{tempDir.toString()});
        assertTrue(out.toString().contains("Goodbye"));
    }

    @Test
    void noResultsMessage(@TempDir Path tempDir) throws IOException {
        Files.writeString(tempDir.resolve("a.txt"), "hello world");
        String input = "nomatch\nquit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        CliRunner.run(new String[]{tempDir.toString()});
        assertTrue(out.toString().contains("No results found."));
    }
}