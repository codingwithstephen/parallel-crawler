package com.udacity.webcrawler.profiler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Objects;


// The interceptor class
final class ProfilingMethodInterceptor implements InvocationHandler {

  private final Clock clock;
  private final Object target;
  private final ProfilingState profilingState;
  private final ZonedDateTime startTime;


  ProfilingMethodInterceptor(Clock clock, Object delegate,
                             ProfilingState state, ZonedDateTime startTime) {

    this.clock = Objects.requireNonNull(clock);
    this.target = Objects.requireNonNull(delegate);
    this.profilingState = Objects.requireNonNull(state);
    this.startTime = Objects.requireNonNull(startTime);
  }
  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

    if (method.isAnnotationPresent(Profiled.class)) {

      Instant startTime = clock.instant();

      try {

        return method.invoke(target, args);
      } finally {

        Instant endTime = clock.instant();
        Duration duration = Duration.between(startTime, endTime);


        profilingState.record(target.getClass(), method, duration);
      }
    } else {
      return method.invoke(target, args);
    }
  }
}