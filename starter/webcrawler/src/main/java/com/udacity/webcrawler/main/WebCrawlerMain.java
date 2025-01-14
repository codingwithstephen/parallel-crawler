package com.udacity.webcrawler.main;

import com.google.inject.Guice;
import com.udacity.webcrawler.WebCrawler;
import com.udacity.webcrawler.WebCrawlerModule;
import com.udacity.webcrawler.json.ConfigurationLoader;
import com.udacity.webcrawler.json.CrawlResult;
import com.udacity.webcrawler.json.CrawlResultWriter;
import com.udacity.webcrawler.json.CrawlerConfiguration;
import com.udacity.webcrawler.profiler.Profiler;
import com.udacity.webcrawler.profiler.ProfilerModule;

import javax.inject.Inject;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.util.Objects;

public final class WebCrawlerMain {

  private final CrawlerConfiguration config;

  private WebCrawlerMain(CrawlerConfiguration config) {
    this.config = Objects.requireNonNull(config);
  }

  @Inject
  private WebCrawler crawler;

  @Inject
  private Profiler profiler;

  private void run() throws Exception {
    // Inject dependencies
    Guice.createInjector(new WebCrawlerModule(config), new ProfilerModule()).injectMembers(this);

    // Perform crawling and process results
    CrawlResult result = crawler.crawl(config.getStartPages());
    processCrawlResults(result);

    // Process profiler data
    processProfilerData();
  }

  /**
   * Processes the crawl result and writes it to a file or System.out.
   *
   * @param result The result of the crawler.
   * @throws Exception If an error occurs during writing.
   */
  private void processCrawlResults(CrawlResult result) throws Exception {
    CrawlResultWriter resultWriter = new CrawlResultWriter(result);
    if (!config.getResultPath().isEmpty()) {
      Path pathCrawlResult = Path.of(config.getResultPath());
      resultWriter.write(pathCrawlResult);
    } else {
      try (Writer stdWriterResultPath = new OutputStreamWriter(System.out)) {
        resultWriter.write(stdWriterResultPath);
      }
    }
  }

  /**
   * Processes and writes the profiler data to a file or System.out.
   *
   * @throws Exception If an error occurs during writing.
   */
  private void processProfilerData() throws Exception {
    if (!config.getProfileOutputPath().isEmpty()) {
      Path pathProfileOutput = Path.of(config.getProfileOutputPath());
      profiler.writeDataAsync(pathProfileOutput)
              .exceptionally(ex -> {
                System.err.println("Failed to write profiler data to file: " + ex.getMessage());
                return null;
              })
              .join();
    } else {
      try (Writer stdWriterProfileOutput = new OutputStreamWriter(System.out)) {
        profiler.writeDataAsync(stdWriterProfileOutput)
                .exceptionally(ex -> {
                  System.err.println("Failed to write profiler data to output stream: " + ex.getMessage());
                  return null;
                })
                .join();
      }
    }
  }

  public static void main(String[] args) throws Exception {
    CrawlerConfiguration config = new ConfigurationLoader(Path.of(args[0])).load();
    new WebCrawlerMain(config).run();
  }
}
