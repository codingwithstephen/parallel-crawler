package com.udacity.webcrawler.main;

import com.fasterxml.jackson.core.exc.StreamWriteException;
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
import java.util.logging.Level;
import java.util.logging.Logger;

public final class WebCrawlerMain {

    private static final Logger logger = Logger.getLogger(WebCrawlerMain.class.getName());

    private final CrawlerConfiguration config;

    private WebCrawlerMain(CrawlerConfiguration config) {
        this.config = Objects.requireNonNull(config);
    }

    @Inject
    private WebCrawler crawler;

    @Inject
    private Profiler profiler;

    private void run() throws StreamWriteException {
        Guice.createInjector(new WebCrawlerModule(config), new ProfilerModule()).injectMembers(this);

        try {
            CrawlResult result = crawler.crawl(config.getStartPages());
            CrawlResultWriter resultWriter = new CrawlResultWriter(result);

            try (Writer stdWriterResultPath = new OutputStreamWriter(System.out)) {
                logger.info("Writing crawl result to standard output");
                resultWriter.write(stdWriterResultPath);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error writing crawl result to standard output", e);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error during crawling process", e);
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("Usage: WebCrawlerMain [starting-url]");
            return;
        }

        try {
            CrawlerConfiguration config = new ConfigurationLoader(Path.of(args[0])).load();
            new WebCrawlerMain(config).run();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error loading configuration or running the crawler", e);
        }
    }
}