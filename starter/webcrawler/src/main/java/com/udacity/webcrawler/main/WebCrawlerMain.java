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
import java.io.IOException;
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
    initializeInjector();
    CrawlResult result = crawler.crawl(config.getStartPages());

    handleCrawlResult(result);
    handleProfilerOutput();
  }

  private void initializeInjector() {
    Guice.createInjector(new WebCrawlerModule(config), new ProfilerModule()).injectMembers(this);
  }

  private void handleCrawlResult(CrawlResult result) throws IOException {
    CrawlResultWriter resultWriter = new CrawlResultWriter(result);

    if (!config.getResultPath().isEmpty()) {
      writeCrawlResultToFile(resultWriter);
    } else {
      writeCrawlResultToStdOut(resultWriter);
    }
  }

  private void writeCrawlResultToFile(CrawlResultWriter resultWriter) throws IOException {
    Path pathCrawlResult = Path.of(config.getResultPath());
    resultWriter.write(pathCrawlResult);
  }

  private void writeCrawlResultToStdOut(CrawlResultWriter resultWriter) throws IOException {
    try (Writer stdWriterResultPath = new OutputStreamWriter(System.out)) {
      resultWriter.write(stdWriterResultPath);
      stdWriterResultPath.flush();
    }
  }

  private void handleProfilerOutput() throws IOException {
    if (!config.getProfileOutputPath().isEmpty()) {
      writeProfilerDataToFile();
    } else {
      writeProfilerDataToStdOut();
    }
  }

  private void writeProfilerDataToFile() throws IOException {
    Path pathProfileOutput = Path.of(config.getProfileOutputPath());
    profiler.writeData(pathProfileOutput);
  }

  private void writeProfilerDataToStdOut() throws IOException {
    try (Writer stdWriterProfileOutput = new OutputStreamWriter(System.out)) {
      profiler.writeData(stdWriterProfileOutput);
      stdWriterProfileOutput.flush();
    }
  }
  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.out.println("Usage: WebCrawlerMain [starting-url]");
      return;
    }

//    Path path = Path.of(args[0]);
//    Path path = Path.of("example.com/foo");
    CrawlerConfiguration config = new ConfigurationLoader(Path.of(args[0])).load();
//    CrawlerConfiguration config = new ConfigurationLoader(path).load();

    new WebCrawlerMain(config).run();
  }
}
