package com.bulatmain.etl.scheduler;

import java.time.Duration;

public interface Task {
    String getId();
    void execute();
    Duration getInterval();
}