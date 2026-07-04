package com.searchengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SearchEngineApplication {

    public static void main(String[] args) {
        if (args.length > 0 && "--cli".equals(args[0])) {
            String[] remaining = new String[args.length - 1];
            System.arraycopy(args, 1, remaining, 0, remaining.length);
            com.searchengine.cli.CliRunner.run(remaining);
        } else {
            SpringApplication.run(SearchEngineApplication.class, args);
        }
    }
}
