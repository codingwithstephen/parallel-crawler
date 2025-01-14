package com.udacity.webcrawler;
import java.nio.file.Files;
import com.udacity.webcrawler.profiler.Profiler;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * A fake {@link Profiler} implementation that does nothing.
 */
public final class NoOpProfiler implements Profiler {

  @Override
  public <T> T wrap(Class<T> klass, T delegate) {
    Objects.requireNonNull(klass);
    return Objects.requireNonNull(delegate);
  }

  @Override
  public CompletableFuture<Void> writeDataAsync(Path path) {
    Objects.requireNonNull(path, "Path cannot be null");
    return CompletableFuture.runAsync(() -> {
      try {

        Files.writeString(path, "Sample profiling data");
      } catch (IOException e) {
        throw new CompletionException(e);
      }
    });
  }

  @Override
  public CompletableFuture<Void> writeDataAsync(Writer writer) {
    Objects.requireNonNull(writer, "Writer cannot be null");
    return CompletableFuture.runAsync(() -> {
      try {

        writer.write("Sample profiling data");
        writer.flush();
      } catch (IOException e) {
        throw new CompletionException(e);
      }
    });
  }
}
