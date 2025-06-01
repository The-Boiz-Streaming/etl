package com.bulatmain.etl.controller;

import com.bulatmain.etl.service.EtlJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/etl")
@RequiredArgsConstructor
public class EtlController {
    private final EtlJobService etlJobService;

    @PostMapping("/schedule")
    public ResponseEntity<Void> scheduleJobs(@RequestBody Map<String, Long> intervals) {
        Map<String, Duration> durationMap = intervals.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> Duration.ofMillis(e.getValue())));
        etlJobService.schedule(durationMap);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/run")
    public ResponseEntity<Void> runJobs(@RequestBody Collection<String> jobIds) {
        etlJobService.run(jobIds);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/cancel")
    public ResponseEntity<Void> cancelJobs(@RequestBody Collection<String> jobIds) {
        etlJobService.cancel(jobIds);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/cancel-all")
    public ResponseEntity<Void> cancelAllJobs() {
        etlJobService.cancel();
        return ResponseEntity.ok().build();
    }
}