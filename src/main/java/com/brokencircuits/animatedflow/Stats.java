package com.brokencircuits.animatedflow;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Stats {

  private static final Map<String, AtomicLong> timeTakenPerTask = new LinkedHashMap<>();
  private static final Map<String, Long> startTimePerTask = new HashMap<>();
  private static final AtomicLong totalTime = new AtomicLong(0);

  public static Task start(String task) {
    if (startTimePerTask.containsKey(task)) {
      throw new IllegalStateException("Cannot start task since it is already started: " + task);
    }
    startTimePerTask.put(task, System.nanoTime());
    return new Task(() -> Stats.stop(task));
  }

  public static void stop(String task) {
    if (!startTimePerTask.containsKey(task)) {
      throw new IllegalStateException("Cannot stop task since it is not started: " + task);
    }
    Long startTime = startTimePerTask.remove(task);
    long taskTime = System.nanoTime() - startTime;
    timeTakenPerTask.computeIfAbsent(task, k -> new AtomicLong()).addAndGet(taskTime);
    totalTime.addAndGet(taskTime);
  }

  public static void log() {
    timeTakenPerTask.forEach((k, v) -> {
      long taskTime = v.get();
      double pcntOfTotal = (double) taskTime / totalTime.get() * 100;
      log.info("Task: {}: {} - {}", k, taskTime, String.format("%.2f%%", pcntOfTotal));
    });
  }

  public static void reset() {
    timeTakenPerTask.clear();
    startTimePerTask.clear();
    totalTime.set(0);
  }

  @RequiredArgsConstructor
  public static class Task {
    private final Runnable onComplete;

    public void stop() {
      onComplete.run();
    }
  }
}
