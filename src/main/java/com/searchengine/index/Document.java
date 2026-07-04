package com.searchengine.index;

import java.io.Serializable;

/**
 * Represents an indexed document with its ID, title, and content.
 */
public class Document implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int id;
    private final String title;
    private final String content;
    private final int totalTerms;

    public Document(int id, String title, String content, int totalTerms) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.totalTerms = totalTerms;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public int getTotalTerms() {
        return totalTerms;
    }
}
