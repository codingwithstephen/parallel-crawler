package com.udacity.webcrawler;

import com.udacity.webcrawler.parser.PageParser;
import com.udacity.webcrawler.parser.PageParserFactory;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.RecursiveTask;
import java.util.regex.Pattern;

public final class CrawlInternalTask extends RecursiveTask<Boolean> {
    private final String url;
    private final Instant deadline;
    private final int maxDepth;
    private final ConcurrentMap<String, Integer> counts;
    private final ConcurrentSkipListSet<String> visitedUrls;
    private final Clock clock;
    private final PageParserFactory parserFactory;
    private final List<Pattern> ignoredUrls;

    public CrawlInternalTask(
            String url,
            Instant deadline,
            int maxDepth,
            ConcurrentMap<String, Integer> counts,
            ConcurrentSkipListSet<String> visitedUrls,
            Clock clock,
            PageParserFactory parserFactory,
            List<Pattern> ignoredUrls) {

        this.url = url;
        this.deadline = deadline;
        this.maxDepth = maxDepth;
        this.counts = counts;
        this.visitedUrls = visitedUrls;
        this.clock = clock;
        this.parserFactory = parserFactory;
        this.ignoredUrls = ignoredUrls;
    }

    @Override
    protected Boolean compute() {

        if (maxDepth == 0 || clock.instant().isAfter(deadline)) {
            return false;
        }

        for (Pattern pattern : ignoredUrls) {
            if (pattern.matcher(url).matches()) {
                return false;
            }
        }

        if (!visitedUrls.add(url)) {
            return false;
        }

        PageParser.Result result = parserFactory.get(url).parse();

        result.getWordCounts().forEach((key, value) ->
                counts.merge(key, value, Integer::sum)
        );


        List<CrawlInternalTask> subtasks = new ArrayList<>();
        for (String link : result.getLinks()) {
            subtasks.add(new CrawlInternalTask(link, deadline, maxDepth - 1, counts,
                    visitedUrls, clock, parserFactory, ignoredUrls));
        }

        invokeAll(subtasks);

        return true;
    }
}