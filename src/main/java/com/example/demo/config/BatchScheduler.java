package com.example.demo.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job financialIngestionJob;

    // This Cron expression makes the batch pipeline execute automatically at 12:00 AM every single day
    @Scheduled(cron = "0 0 0 * * *") 
    public void runIngestionPipeline() {
        try {
            System.out.println("Triggering Batch Processing Pipeline...");
            
            JobParameters params = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis()) // Ensures every run is treated uniquely
                    .toJobParameters();
            
            jobLauncher.run(financialIngestionJob, params);
            
            System.out.println("✅ Batch Processing Pipeline executed successfully.");
        } catch (Exception e) {
            System.err.println("❌ Failed to manually trigger Spring Batch Job: " + e.getMessage());
        }
    }
}