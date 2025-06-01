package com.bulatmain.etl.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.ReentrantLock;
import java.util.ConcurrentModificationException;

@Slf4j
public class SchedulerService implements AutoCloseable {
    public static final String CONCURRENT_MODIFICATION_EXCEPTION_MESSAGE =
            "Cannot schedule tasks: another scheduling or canceling operation is in progress";
    public static final int DEFAULT_POOL_SIZE = 10;
    private final Map<String, Task> tasks;
    private final ThreadPoolTaskScheduler taskScheduler;
    private final Map<String, ScheduledFuture<?>> scheduledTasks;
    private final ReentrantLock lock;

    public SchedulerService() {
        this(DEFAULT_POOL_SIZE);
    }

    public SchedulerService(int poolSize) {
        if (poolSize < 1) {
            throw new IllegalArgumentException("Pool size can not be lower than 1");
        }
        this.tasks = new ConcurrentHashMap<>();
        this.taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(poolSize);
        taskScheduler.setThreadNamePrefix("TaskScheduler-");
        taskScheduler.initialize();
        this.scheduledTasks = new ConcurrentHashMap<>();
        this.lock = new ReentrantLock();
    }

    public void addTasks(Collection<Task> tasks) {
        if (!lock.tryLock()) {
            throw new ConcurrentModificationException(CONCURRENT_MODIFICATION_EXCEPTION_MESSAGE);
        }
        try {
            tasks.forEach(task -> this.tasks.put(task.getId(), task));
        } finally {
            lock.unlock();
        }
    }

    public void scheduleAllTasks() {
        scheduleTasks(tasks.keySet());
    }

    public void scheduleTasks(Collection<String> taskIds) {
        if (!lock.tryLock()) {
            throw new ConcurrentModificationException(CONCURRENT_MODIFICATION_EXCEPTION_MESSAGE);
        }
        try {
            taskIds.forEach(this::scheduleTask);
        } finally {
            lock.unlock();
        }
    }

    private void scheduleTask(String taskId) {
        Task task = tasks.get(taskId);
        if (task != null) {
            if (scheduledTasks.containsKey(taskId)) {
                return;
            }
            ScheduledFuture<?> future = taskScheduler.scheduleAtFixedRate(task::execute, task.getInterval());
            scheduledTasks.put(taskId, future);
        } else {
            log.warn("No task with such id {}", taskId);
        }
    }

    public void cancelAllTasks() {
        cancelTasks(scheduledTasks.keySet());
    }

    public void cancelTasks(Collection<String> taskIds) {
        if (!lock.tryLock()) {
            throw new ConcurrentModificationException(CONCURRENT_MODIFICATION_EXCEPTION_MESSAGE);
        }
        try {
            taskIds.forEach(this::cancelTask);
        } finally {
            lock.unlock();
        }
    }

    private void cancelTask(String taskId) {
        ScheduledFuture<?> future = scheduledTasks.get(taskId);
        if (future != null) {
            future.cancel(false);
            scheduledTasks.remove(taskId);
        } else {
            log.warn("No task with such id {}", taskId);
        }
    }

    public void runAllTasks() {
        runTasks(tasks.keySet());
    }

    public void runTasks(Collection<String> taskIds) {
        taskIds.forEach(this::runTask);
    }

    private void runTask(String taskId) {
        Task task = tasks.get(taskId);
        if (task != null) {
            taskScheduler.execute(task::execute);
        } else {
            log.warn("No task with such id {}", taskId);
        }
    }

    // Get list of available task IDs (read-only)
    public Set<String> getTaskIds() {
        return Collections.unmodifiableSet(tasks.keySet());
    }

    @Override
    public void close() {
        cancelAllTasks();
        taskScheduler.destroy();
    }
}