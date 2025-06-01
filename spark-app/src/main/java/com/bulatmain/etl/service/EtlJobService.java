package com.bulatmain.etl.service;
import com.bulatmain.etl.job.EtlJob;
import com.bulatmain.etl.scheduler.SchedulerService;
import com.bulatmain.etl.scheduler.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class EtlJobService {
    private final Map<String, EtlJob> jobs;
    private final SchedulerService scheduler;
    private final int DEFAULT_RETRIES_COUNT = 3;

    public EtlJobService(Collection<EtlJob> jobs, SchedulerService scheduler) {
        this.jobs = jobs.stream()
                .collect(Collectors.toMap(EtlJob::getId, Function.identity()));
        this.scheduler = scheduler;
    }

    public void schedule(Map<String, Duration> intervals) {
        List<Task> tasks = convertToTasks(intervals);
        retry(() -> scheduler.addTasks(tasks));
        List<String> ids = tasks.stream()
                .map(Task::getId)
                .toList();
        retry(() -> scheduler.scheduleTasks(ids));
    }

    public void run(Collection<String> ids) {
        List<Task> tasks = convertToTasks(ids);
        retry(() -> scheduler.addTasks(tasks));
        retry(() -> scheduler.runTasks(ids));
    }

    public void cancel(Collection<String> ids) {
        retry(() -> scheduler.cancelTasks(ids));
    }

    public void cancel() {
        retry(scheduler::cancelAllTasks);
    }

    private List<Task> convertToTasks(Map<String, Duration> intervals) {
        return intervals.entrySet().stream()
                .flatMap(e -> {
                    String id = e.getKey();
                    Duration interval = e.getValue();
                    EtlJob job = jobs.get(id);
                    if (job == null) {
                        log.warn("No job with such id {}", id);
                        return Stream.empty();
                    }
                    return Stream.<Task>of(new EtlTaskMapping(job, interval));
                })
                .toList();
    }

    private List<Task> convertToTasks(Collection<String> ids) {
        return ids.stream()
                .flatMap(id -> {
                    EtlJob job = jobs.get(id);
                    if (job == null) {
                        log.warn("No job with such id {}", id);
                        return Stream.empty();
                    }
                    return Stream.<Task>of(new EtlTaskMapping(job, null));
                })
                .toList();
    }

    private void retry(Runnable r) {
        retry(r, DEFAULT_RETRIES_COUNT);
    }

    private void retry(Runnable r, int count) {
        if (count < 1) {
            throw new IllegalArgumentException("Count can not be lower than 1");
        }
        boolean done = false;
        while (!done && 0 < count) {
            try {
                r.run();
                done = true;
            } catch (Exception e) {
                --count;
                if (count == 0) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @RequiredArgsConstructor
    private static class EtlTaskMapping implements Task {
        private final EtlJob job;
        private final Duration interval;
        @Override
        public String getId() {
            return job.getId();
        }

        @Override
        public void execute() {
            job.run();
        }

        @Override
        public Duration getInterval() {
            return interval;
        }
    }
}
