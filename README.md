# Search Engine

[![CI](https://github.com/pavel-genai/search-engine/actions/workflows/ci.yml/badge.svg)](https://github.com/pavel-genai/search-engine/actions/workflows/ci.yml)

A Java 17 inverted index search engine with TF-IDF ranking, boolean query support, and a REST API powered by Spring Boot.

## Features

- **Tokenizer**: Whitespace splitting with punctuation removal and suffix-based stemming
- **Inverted Index**: Maps terms to document postings with term frequency and positional data
- **TF-IDF Ranker**: Ranks search results using Term Frequency-Inverse Document Frequency scoring
- **Query Parser**: Supports boolean operators (AND, OR, NOT) and phrase queries (`"exact phrase"`)
- **REST API**: Spring Boot endpoints for indexing, searching, and stats
- **Persistence**: Index is serialized to disk automatically
- **CLI Mode**: Index a directory of `.txt` files and search interactively

## Build

```bash
./gradlew build
```

## Run (REST API)

```bash
./gradlew bootRun
```

The server starts on `http://localhost:8080`.

### API Endpoints

**POST /index** - Index a document
```bash
curl -X POST http://localhost:8080/index \
  -H "Content-Type: application/json" \
  -d '{"title": "My Document", "text": "The content of my document"}'
```

**GET /search?q=query** - Search the index
```bash
curl "http://localhost:8080/search?q=java"
curl "http://localhost:8080/search?q=java+AND+programming"
curl "http://localhost:8080/search?q=\"inverted+index\""
```

**GET /stats** - Index statistics
```bash
curl http://localhost:8080/stats
```

## Run (CLI Mode)

Index a directory of `.txt` files and search interactively:

```bash
java -jar build/libs/search-engine-1.0.0.jar --cli sample-docs/
```

## Query Syntax

| Syntax | Example | Description |
|--------|---------|-------------|
| Single term | `java` | Documents containing "java" |
| AND | `java AND programming` | Documents containing both terms |
| OR | `java OR python` | Documents containing either term |
| NOT | `NOT deprecated` | Documents not containing the term |
| Phrase | `"search engine"` | Documents with exact phrase |
| Combined | `"search engine" AND NOT deprecated` | Complex boolean expressions |

## Project Structure

```
src/main/java/com/searchengine/
  tokenizer/   - Tokenizer with stemming
  index/       - InvertedIndex, Document, Posting
  ranker/      - TF-IDF ranking and SearchResult
  query/       - QueryParser with boolean/phrase support
  api/         - Spring Boot REST controller
  cli/         - CLI runner for directory indexing
  persistence/ - Disk serialization
```

## Testing

```bash
./gradlew test
```

Includes unit tests for Tokenizer, InvertedIndex, QueryParser, and integration tests for the REST API.
