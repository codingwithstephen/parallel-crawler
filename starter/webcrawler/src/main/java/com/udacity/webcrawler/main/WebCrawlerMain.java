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
import java.io.*;
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
    Guice.createInjector(new WebCrawlerModule(config), new ProfilerModule()).injectMembers(this);

    CrawlResult result = crawler.crawl(config.getStartPages());
    CrawlResultWriter resultWriter = new CrawlResultWriter(result);

    writeCrawlResults(resultWriter);

    writeProfilerData();
  }


  private void writeCrawlResults(CrawlResultWriter resultWriter) {
    String resultPath = config.getResultPath();
    if (resultPath == null || resultPath.isEmpty()) {
      try (Writer writer = new java.io.OutputStreamWriter(System.out)) {
        resultWriter.write(writer);
        writer.flush();
      } catch (IOException e) {
        System.err.println("Error writing crawl results to System.out: " + e.getMessage());
      }
    } else {
      try (FileWriter writer = new FileWriter(resultPath)) {
        resultWriter.write(writer);
        System.out.println("Crawl results written to " + resultPath);
      } catch (IOException e) {
        System.err.println("Error writing crawl results to file '" + resultPath + "': " + e.getMessage());
      }
    }
  }


  private void writeProfilerData() {
    String profilePath = config.getProfileOutputPath();
    if (profilePath == null || profilePath.isEmpty()) {
      try (Writer writer = new java.io.OutputStreamWriter(System.out)) {
        profiler.writeData(writer);
        writer.flush();
      } catch (IOException e) {
        System.err.println("Error writing profiler data to System.out: " + e.getMessage());
      }
    } else {
      try (FileWriter writer = new FileWriter(profilePath, true)) {
        profiler.writeData(writer);
        System.out.println("Profiler data written to " + profilePath);
      } catch (IOException e) {
        System.err.println("Error writing profiler data to file '" + profilePath + "': " + e.getMessage());
      }
    }
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.out.println("Usage: WebCrawlerMain [starting-url]");
      return;
    }

    CrawlerConfiguration config = new ConfigurationLoader(Path.of(args[0])).load();
    new WebCrawlerMain(config).run();
  }
}
