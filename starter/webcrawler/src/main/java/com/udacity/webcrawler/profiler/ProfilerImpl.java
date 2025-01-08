package com.udacity.webcrawler.profiler;

import javax.inject.Inject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Objects;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

/**
 * Concrete implementation of the {@link Profiler}.
 */
final class ProfilerImpl implements Profiler {

  private final Clock clock;
  private final ProfilingState profilingState = new ProfilingState();
  private final ZonedDateTime startTime;

  @Inject
  ProfilerImpl(Clock clock) {
    this.clock = Objects.requireNonNull(clock);
    this.startTime = ZonedDateTime.now(clock);
  }

  @Override
  public <T> T wrap(Class<T> klass, T target) {
    Objects.requireNonNull(klass, "Class type cannot be null");
    Objects.requireNonNull(target, "Delegate object cannot be null");

    if (!klass.isInterface()) {
      throw new IllegalArgumentException("The provided class must be an interface");
    }

    ProfilingMethodInterceptor profilingMethodInterceptor =
            new ProfilingMethodInterceptor(this.clock, target, this.profilingState, this.startTime);

    Object proxy = Proxy.newProxyInstance(
            ProfilerImpl.class.getClassLoader(),
            new Class[]{Objects.requireNonNull(klass)},
            profilingMethodInterceptor
    );

    return (T) proxy;
  }

  @Override
  public void writeData(Path path) {
    Objects.requireNonNull(path, "Path cannot be null");

    try (BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
      writeData(writer);
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to write profiling data to file: " + path, e);
    }
  }
  @Override
  public void writeData(Writer writer) throws IOException {
    writer.write("Run at " + RFC_1123_DATE_TIME.format(startTime));
    writer.write(System.lineSeparator());
    profilingState.write(writer);
    writer.write(System.lineSeparator());
  }
}
