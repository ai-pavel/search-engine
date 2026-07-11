package com.searchengine.cli;

import com.searchengine.index.InvertedIndex;
import com.searchengine.persistence.IndexPersistence;
import com.searchengine.query.QueryParser;
import com.searchengine.ranker.SearchResult;
import com.searchengine.ranker.TfIdfRanker;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

/**
 * CLI mode: indexes a directory of .txt files and provides interactive search.
 *
 * Usage: java -jar search-engine.jar --cli <directory>
 */
public class CliRunner {

    private static final String INDEX_PATH = "index-data/index.ser";

    public static void run(String[] args) {
        System.exit(runReturningExitCode(args));
    }

    public static int runReturningExitCode(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: --cli <directory-of-txt-files>");
            System.out.println("  Indexes all .txt files in the given directory.");
            return 1;
        }

        String dirPath = args[0];
        File dir = new File(dirPath);
        if (!dir.isDirectory()) {
            System.err.println("Error: " + dirPath + " is not a directory.");
            return 1;
        }

        IndexPersistence persistence = new IndexPersistence(INDEX_PATH);
        InvertedIndex index = new InvertedIndex();

        System.out.println("Indexing .txt files from: " + dir.getAbsolutePath());

        File[] files = dir.listFiles((d, name) -> name.endsWith(".txt"));
        if (files == null || files.length == 0) {
            System.out.println("No .txt files found.");
            return 0;
        }

        for (File file : files) {
            try {
                String content = Files.readString(file.toPath());
                int docId = index.addDocument(file.getName(), content);
                System.out.println("  Indexed: " + file.getName() + " (docId=" + docId + ")");
            } catch (IOException e) {
                System.err.println("  Error reading " + file.getName() + ": " + e.getMessage());
            }
        }

        try {
            persistence.save(index);
            System.out.println("Index saved to " + INDEX_PATH);
        } catch (IOException e) {
            System.err.println("Warning: could not save index: " + e.getMessage());
        }

        System.out.println("\nIndexed " + index.getDocumentCount() + " documents, "
                + index.getTermCount() + " unique terms.");
        System.out.println("Enter search queries (type 'quit' to exit):\n");

        Scanner scanner = new Scanner(System.in);
        QueryParser parser = new QueryParser(index);
        TfIdfRanker ranker = new TfIdfRanker(index);

        while (true) {
            System.out.print("search> ");
            if (!scanner.hasNextLine()) break;
            String query = scanner.nextLine().trim();
            if ("quit".equalsIgnoreCase(query) || "exit".equalsIgnoreCase(query)) {
                break;
            }
            if (query.isEmpty()) continue;

            Set<Integer> matchingDocs = parser.parse(query);
            List<String> queryTerms = parser.extractTerms(query);
            List<SearchResult> results = ranker.rank(matchingDocs, queryTerms);

            if (results.isEmpty()) {
                System.out.println("  No results found.\n");
            } else {
                System.out.println("  Found " + results.size() + " result(s):");
                for (SearchResult r : results) {
                    System.out.printf("    [%d] %s (score: %.4f)%n", r.getDocId(), r.getTitle(), r.getScore());
                    System.out.println("        " + r.getSnippet());
                }
                System.out.println();
            }
        }
        scanner.close();
        System.out.println("Goodbye.");
        return 0;
    }
}
