package com.udacity.webcrawler.profiler;

import javax.inject.Inject;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

/**
 * Concrete implementation of the {@link Profiler}.
 */
final class ProfilerImpl implements Profiler {

  private final Clock clock;
  private final ProfilingState state = new ProfilingState();
  private final ZonedDateTime startTime;

  @Inject
  ProfilerImpl(Clock clock) {
    this.clock = Objects.requireNonNull(clock);
    this.startTime = ZonedDateTime.now(clock);
  }

  @Profiled
  public Boolean isAnnotatedProfiled(Class<?> klass) {
    for (Method method : klass.getDeclaredMethods()) {
      if (method.isAnnotationPresent(Profiled.class)) {
        return true;
      }
    }
    return false;
  }
  @Override
  public <T> T wrap(Class<T> klass, T delegate) {
    Objects.requireNonNull(klass, "Class cannot be null");
    if (!isAnnotatedProfiled(klass)) {
      throw new IllegalArgumentException(klass.getName() + " has no @Profiled annotated methods.");
    }

    ProfilingMethodInterceptor interceptor = new ProfilingMethodInterceptor(clock, delegate, state, startTime);

    return (T) Proxy.newProxyInstance(
            klass.getClassLoader(),
            new Class[]{klass},
            interceptor
    );
  }
  @Override
  public CompletableFuture<Void> writeDataAsync(Path path) {
    Objects.requireNonNull(path, "Path cannot be null");

    return CompletableFuture.runAsync(() -> {
      try (FileWriter fileWriter = new FileWriter(path.toFile(), true)) {
        writeDataAsync(fileWriter).join();
      } catch (IOException ex) {
        System.err.println("Failed to write data to file: " + ex.getMessage());
      }
    });
  }

  @Override
  public CompletableFuture<Void> writeDataAsync(Writer writer) {
    return CompletableFuture.runAsync(() -> {
      try {
        writer.write("Run at " + RFC_1123_DATE_TIME.format(startTime) + System.lineSeparator());
        state.write(writer);
        writer.write(System.lineSeparator());
      } catch (IOException ex) {
        throw new UncheckedIOException("Failed to write data", ex);
      }
    });
  }
}
