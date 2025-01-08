package com.udacity.webcrawler.json;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * A static utility class that loads a JSON configuration file.
 */
public final class ConfigurationLoader {

  private final Path path;

  /**
   * Create a {@link ConfigurationLoader} that loads configuration from the given {@link Path}.
   */
  public ConfigurationLoader(Path path) {

    this.path = Objects.requireNonNull(path, "Path must not be null");
  }

  /**
   * Loads configuration from this {@link ConfigurationLoader}'s path
   *
   * @return the loaded {@link CrawlerConfiguration}.
   */
  public CrawlerConfiguration load() {
    System.out.println("path");
    System.out.println(path);
    try (Reader reader = Files.newBufferedReader(path)) {
      return read(reader);
    } catch (IOException e) {
      throw new RuntimeException("Failed to load configuration from path: " + path, e);
    }
  }

  /**
   * Loads crawler configuration from the given reader.
   *
   * @param reader a Reader pointing to a JSON string that contains crawler configuration.
   * @return a crawler configuration
   */
  public static CrawlerConfiguration read(Reader reader) throws IOException {

    Objects.requireNonNull(reader, "Reader must not be null");

    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.readValue(reader, CrawlerConfiguration.class);
  }
}