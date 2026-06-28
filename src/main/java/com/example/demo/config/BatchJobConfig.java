package com.example.demo.config;

import com.example.demo.domain.FinancialMarketData;
import com.example.demo.dto.AlphaVantageResponseDto;
import com.example.demo.dto.ExchangeRateResponseDto;
import com.example.demo.mapper.FinancialDataMapper;
import com.example.demo.repository.FinancialMarketDataRepository;
import com.example.demo.service.FinancialApiClient;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;

@Configuration
public class BatchJobConfig {

    @Bean
    public Job financialIngestionJob(JobRepository jobRepository, Step exchangeRateStep, Step alphaVantageStep) {
        return new JobBuilder("financialIngestionJob", jobRepository)
                .start(exchangeRateStep)
                .next(alphaVantageStep)
                .build();
    }

    // --- UPGRADED STEP 1: FAULT-TOLERANT EXCHANGE RATE STEP ---
    @Bean
    public Step exchangeRateStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                                ItemReader<Map.Entry<String, BigDecimal>> exchangeRateReader,
                                ItemProcessor<Map.Entry<String, BigDecimal>, FinancialMarketData> exchangeRateProcessor,
                                ItemWriter<FinancialMarketData> financialItemWriter) {
        return new StepBuilder("exchangeRateStep", jobRepository)
                .<Map.Entry<String, BigDecimal>, FinancialMarketData>chunk(100, transactionManager)
                .reader(exchangeRateReader)
                .processor(exchangeRateProcessor)
                .writer(financialItemWriter)
                .faultTolerant() // 🛡️ Enables fault tolerance capabilities
                .skip(NullPointerException.class) // Skip row if individual values are cleanly broken
                .skipLimit(10) // Allow up to 10 broken data elements before stopping the step
                .retry(RestClientException.class) // Retry network timeouts
                .retryLimit(3) // Try reconnecting up to 3 times automatically
                .build();
    }

    @Bean
    public ListItemReader<Map.Entry<String, BigDecimal>> exchangeRateReader(FinancialApiClient apiClient) {
        String apiKey = System.getenv("9e346792516074e01762c31b");
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("⚠️ Warning: EXCHANGE_RATE_KEY environment variable is missing!");
            return new ListItemReader<>(new ArrayList<>());
        }

        try {
            ExchangeRateResponseDto response = apiClient.fetchExchangeRates(apiKey);
            if (response != null && response.conversionRates() != null) {
                return new ListItemReader<>(new ArrayList<>(response.conversionRates().entrySet()));
            }
        } catch (Exception e) {
            System.err.println("ExchangeRate Ingestion failed natively: " + e.getMessage());
        }
        return new ListItemReader<>(new ArrayList<>());
    }

    @Bean
    public ItemProcessor<Map.Entry<String, BigDecimal>, FinancialMarketData> exchangeRateProcessor(FinancialDataMapper mapper) {
        return entry -> (entry.getValue() == null || entry.getValue().compareTo(BigDecimal.ZERO) <= 0) ? null : 
                mapper.toEntityFromExchangeRate(entry.getValue(), entry.getKey());
    }

    // --- UPGRADED STEP 2: FAULT-TOLERANT ALPHA VANTAGE STEP ---
    @Bean
    public Step alphaVantageStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                                ItemReader<Map.Entry<String, AlphaVantageResponseDto.StockData>> alphaVantageReader,
                                ItemProcessor<Map.Entry<String, AlphaVantageResponseDto.StockData>, FinancialMarketData> alphaVantageProcessor,
                                ItemWriter<FinancialMarketData> financialItemWriter) {
        return new StepBuilder("alphaVantageStep", jobRepository)
                .<Map.Entry<String, AlphaVantageResponseDto.StockData>, FinancialMarketData>chunk(50, transactionManager)
                .reader(alphaVantageReader)
                .processor(alphaVantageProcessor)
                .writer(financialItemWriter)
                .faultTolerant() // 🛡️ Enables fault tolerance capabilities
                .skip(IllegalArgumentException.class) // Skip item if date format or numbers are corrupt
                .skipLimit(5) // Don't let a few weird payload dates ruin the entire stock history run
                .retry(RestClientException.class) // Retry on standard API network connection Drops
                .retryLimit(3)
                .build();
    }

    @Bean
    public ListItemReader<Map.Entry<String, AlphaVantageResponseDto.StockData>> alphaVantageReader(FinancialApiClient apiClient) {
        String apiKey = System.getenv("9e346792516074e01762c31b"); 
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("⚠️ Warning: ALPHA_VANTAGE_KEY environment variable is missing!");
            return new ListItemReader<>(new ArrayList<>());
        }

        try {
            AlphaVantageResponseDto response = apiClient.fetchStockData("IBM", apiKey);
            if (response != null && response.timeSeries() != null) {
                return new ListItemReader<>(new ArrayList<>(response.timeSeries().entrySet()));
            }
        } catch (Exception e) {
            System.err.println("AlphaVantage Ingestion failed natively: " + e.getMessage());
        }
        return new ListItemReader<>(new ArrayList<>());
    }

    @Bean
    public ItemProcessor<Map.Entry<String, AlphaVantageResponseDto.StockData>, FinancialMarketData> alphaVantageProcessor(FinancialDataMapper mapper) {
        return entry -> {
            if (entry.getValue() == null || entry.getValue().closePrice() == null) {
                return null;
            }
            return mapper.toEntityFromAlphaVantage(entry.getValue(), "IBM", entry.getKey());
        };
    }

    @Bean
    public ItemWriter<FinancialMarketData> financialItemWriter(FinancialMarketDataRepository repository) {
        return chunk -> repository.saveAll(chunk.getItems());
    }
}