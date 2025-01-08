package com.udacity.webcrawler;

import com.udacity.webcrawler.json.CrawlResult;

import javax.inject.Inject;
import javax.inject.Provider;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static java.util.concurrent.ForkJoinTask.invokeAll;

/**
 * A concrete implementation of {@link WebCrawler} that runs multiple threads on a
 * {@link ForkJoinPool} to fetch and process multiple web pages in parallel.
 */
final class ParallelWebCrawler implements WebCrawler {
  private final Clock clock;
  private final Duration timeout;
  private final int popularWordCount;
  private final ForkJoinPool pool;

  private final Set<String> urlsVisited = new ConcurrentSkipListSet<>();
  private final Map<String, Integer> wordCounts = new ConcurrentHashMap<>();

  @Inject
  ParallelWebCrawler(
          Clock clock,
          @Timeout Duration timeout,
          @PopularWordCount int popularWordCount,
          @TargetParallelism int threadCount) {
    this.clock = clock;
    this.timeout = timeout;
    this.popularWordCount = popularWordCount;
    this.pool = new ForkJoinPool(Math.min(threadCount, getMaxParallelism()));
  }

  @Override
  public CrawlResult crawl(List<String> startingUrls) {
    List<RecursiveTask<Void>> tasks = startingUrls.stream()
            .map(CrawlTask::new)
            .collect(Collectors.toList());

    tasks.forEach(pool::invoke);

    return new CrawlResult.Builder()
            .setUrlsVisited(urlsVisited.size())
            .setWordCounts(wordCounts)
            .build();
  }

  @Override
  public int getMaxParallelism() {
    return Runtime.getRuntime().availableProcessors();
  }

  private class CrawlTask extends RecursiveTask<Void> {

    private final String url;

    CrawlTask(String url) {
      this.url = url;
    }

    @Override
    protected Void compute() {
      if (!urlsVisited.add(url)) {
        return null;
      }

      try {
        List<String> linkedUrls = crawlUrl(url);
        countWords(url);

        List<CrawlTask> subtasks = linkedUrls.stream()
                .map(CrawlTask::new)
                .collect(Collectors.toList());

        invokeAll((ForkJoinTask<?>) subtasks);

      } catch (Exception e) {
      }
      return null;
    }

    private void countWords(String url) {
      String content = getPageContent(url);
      String[] words = content.split("\\W+");

      for (String word : words) {
        wordCounts.merge(word, 1, Integer::sum);
      }
    }

    private String getPageContent(String url) {
      return "sample content from " + url;
    }

    private List<String> crawlUrl(String url) {
      return List.of("linked_url_1", "linked_url_2", "linked_url_3");
    }
  }
}
