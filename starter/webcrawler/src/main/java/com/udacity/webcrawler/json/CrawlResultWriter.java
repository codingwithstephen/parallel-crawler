package com.udacity.webcrawler.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

/**
 * Utility class to write a {@link CrawlResult} to file.
 */
public final class CrawlResultWriter {
  private final CrawlResult result;
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
          .enable(SerializationFeature.INDENT_OUTPUT);

  /**
   * Creates a new {@link CrawlResultWriter} that will write the given {@link CrawlResult}.
   */
  public CrawlResultWriter(CrawlResult result) {
    this.result = Objects.requireNonNull(result);
  }

  /**
   * Formats the {@link CrawlResult} as JSON and writes it to the given {@link Path}.
   *
   * <p>If a file already exists at the path, the existing file should not be deleted; new data
   * should be appended to it.
   *
   * @param path the file path where the crawl result data should be written.
   * @throws IOException If an I/O error occurs.
   */
  public void write(Path path) throws IOException {
    Objects.requireNonNull(path, "Path cannot be null");

    if (path.getParent() != null) {
      Files.createDirectories(path.getParent());
    }

    try (Writer writer = Files.newBufferedWriter(path,
            StandardOpenOption.CREATE,
            StandardOpenOption.APPEND)) {
      OBJECT_MAPPER.writeValue(writer, result);
      writer.write(System.lineSeparator());
    }
  }

  /**
   * Formats the {@link CrawlResult} as JSON and writes it to the given {@link Writer}.
   *
   * @param writer the destination where the crawl result data should be written.
   * @throws IOException If an I/O error occurs.
   */
  public void write(Writer writer) throws IOException {
    Objects.requireNonNull(writer, "Writer cannot be null");

    OBJECT_MAPPER.writeValue(writer, result);

  }
}