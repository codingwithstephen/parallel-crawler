package com.udacity.webcrawler.profiler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * A method interceptor that checks whether {@link Method}s are annotated with the {@link Profiled}
 * annotation. If they are, the method interceptor records how long the method invocation took.
 */
final class ProfilingMethodInterceptor implements InvocationHandler {

  private final Clock clock;

  // TODO: You will need to add more instance fields and constructor arguments to this class.
  private final Object delegate;
  private final ProfilingState state;
  private final ZonedDateTime startTime;


  ProfilingMethodInterceptor(Clock clock, Object delegate,
                             ProfilingState state, ZonedDateTime startTime) {

    this.clock = Objects.requireNonNull(clock);
    this.delegate = Objects.requireNonNull(delegate);
    this.state = Objects.requireNonNull(state);
    this.startTime = Objects.requireNonNull(startTime);
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

    boolean isProfiled = method.isAnnotationPresent(Profiled.class);
    Instant startTime = isProfiled ? clock.instant() : null;

    try {

      return method.invoke(delegate, args);
    } catch (InvocationTargetException e) {
      throw e.getTargetException();
    } catch (IllegalAccessException e) {
      throw new RuntimeException("Method invocation failed due to illegal access", e);
    } finally {

      Optional.ofNullable(startTime).ifPresent(start -> {
        Duration duration = Duration.between(start, clock.instant());
        state.record(delegate.getClass(), method, duration);
      });
    }
  }
}
