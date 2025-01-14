package com.udacity.webcrawler;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.lang.Math.min;
import static java.util.stream.Collectors.toMap;

/**
 * Utility class that sorts the map of word counts.
 *
 * <p>TODO: Reimplement the sort() method using only the Stream API and lambdas and/or method
 *          references.
 */
final class WordCounts {

  /**
   * Given an unsorted map of word counts, returns a new map whose word counts are sorted according
   * to the provided {@link WordCountComparator}, and includes only the top
   * {@param popluarWordCount} words and counts.
   *
   * <p>TODO: Reimplement this method using only the Stream API and lambdas and/or method
   *          references.
   *
   * @param wordCounts       the unsorted map of word counts.
   * @param popularWordCount the number of popular words to include in the result map.
   * @return a map containing the top {@param popularWordCount} words and counts in the right order.
   */
  static Map<String, Integer> sort(Map<String, Integer> wordCounts, int popularWordCount) {

    // TODO: Reimplement this method using only the Stream API and lambdas and/or method references.
/*
    PriorityQueue<Map.Entry<String, Integer>> sortedCounts =
        new PriorityQueue<>(wordCounts.size(), new WordCountComparator());
    sortedCounts.addAll(wordCounts.entrySet());
    Map<String, Integer> topCounts = new LinkedHashMap<>();
    for (int i = 0; i < Math.min(popularWordCount, wordCounts.size()); i++) {
      Map.Entry<String, Integer> entry = sortedCounts.poll();
      topCounts.put(entry.getKey(), entry.getValue());
    }
    return topCounts;

 */

/*
Your new sort method must return a new Map that contains up to popularWordCount entries
from the original map, but sorted according to the following specification (which should look familiar):
    •	The keys and values should be sorted so that the more frequent words come first.
    •   If multiple words have the same frequency, prefer longer words rank higher.
    •   If multiple words have the same frequency and length,
        use alphabetical order to break ties (the word that comes first in the alphabet ranks higher).

But wait, what exactly does it mean to use only functional programming techniques?
Simple: Don't use any for loops. That's right, no for loops at all.
Instead, you will have to process the wordCounts map using only the
Java Stream API, lambdas, and method references.
The new method should be a single return statement with a "chain" of Stream operations.
You are allowed to reuse the WordCountComparator class.
 */
    return wordCounts.entrySet().stream()
            .sorted((a, b) -> b.getValue().compareTo(a.getValue()) != 0 ?
                    b.getValue().compareTo(a.getValue()) : a.getKey().length() != b.getKey().length() ?
                    Integer.compare(b.getKey().length(), a.getKey().length()) : a.getKey().compareTo(b.getKey()))
            .limit(Math.min(popularWordCount, wordCounts.size()))
            .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (k, v) -> k, LinkedHashMap::new));
  }

  /**
   * A {@link Comparator} that sorts word count pairs correctly:
   *
   * <p>
   * <ol>
   *   <li>First sorting by word count, ranking more frequent words higher.</li>
   *   <li>Then sorting by word length, ranking longer words higher.</li>
   *   <li>Finally, breaking ties using alphabetical order.</li>
   * </ol>
   */
  private static final class WordCountComparator implements Comparator<Map.Entry<String, Integer>> {
    @Override
    public int compare(Map.Entry<String, Integer> a, Map.Entry<String, Integer> b) {
      if (!a.getValue().equals(b.getValue())) {
        return b.getValue() - a.getValue();
      }
      if (a.getKey().length() != b.getKey().length()) {
        return b.getKey().length() - a.getKey().length();
      }
      return a.getKey().compareTo(b.getKey());
    }
  }

  private WordCounts() {
    // This class cannot be instantiated
  }
}