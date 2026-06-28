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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Configuration
public class BatchJobConfig {

    // Update the Job to execute BOTH steps sequentially!
    @Bean
    public Job financialIngestionJob(JobRepository jobRepository, Step exchangeRateStep, Step alphaVantageStep) {
        return new JobBuilder("financialIngestionJob", jobRepository)
                .start(exchangeRateStep)
                .next(alphaVantageStep) // Chains the stock processing step right after currency
                .build();
    }

    // --- STEP 1: EXCHANGE RATE REGION ---
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
                .build();
    }

    @Bean
    public ListItemReader<Map.Entry<String, BigDecimal>> exchangeRateReader(FinancialApiClient apiClient) {
    	String apiKey = System.getenv("EXCHANGE_RATE_KEY"); 
        try {
            ExchangeRateResponseDto response = apiClient.fetchExchangeRates(apiKey);
            if (response != null && response.conversionRates() != null) {
                return new ListItemReader<>(new ArrayList<>(response.conversionRates().entrySet()));
            }
        } catch (Exception e) {
            System.err.println("ExchangeRate Ingestion failed: " + e.getMessage());
        }
        return new ListItemReader<>(new ArrayList<>());
    }

    @Bean
    public ItemProcessor<Map.Entry<String, BigDecimal>, FinancialMarketData> exchangeRateProcessor(FinancialDataMapper mapper) {
        return entry -> (entry.getValue() == null || entry.getValue().compareTo(BigDecimal.ZERO) <= 0) ? null : 
                mapper.toEntityFromExchangeRate(entry.getValue(), entry.getKey());
    }

    // --- STEP 2: ALPHA VANTAGE REGION ---
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
                .build();
    }

    @Bean
    public ListItemReader<Map.Entry<String, AlphaVantageResponseDto.StockData>> alphaVantageReader(FinancialApiClient apiClient) {
    	String apiKey = System.getenv("ALPHA_VANTAGE_KEY"); 
        try {
            AlphaVantageResponseDto response = apiClient.fetchStockData("IBM", apiKey);
            if (response != null && response.timeSeries() != null) {
                return new ListItemReader<>(new ArrayList<>(response.timeSeries().entrySet()));
            }
        } catch (Exception e) {
            System.err.println("AlphaVantage Ingestion failed: " + e.getMessage());
        }
        return new ListItemReader<>(new ArrayList<>());
    }

    @Bean
    public ItemProcessor<Map.Entry<String, AlphaVantageResponseDto.StockData>, FinancialMarketData> alphaVantageProcessor(FinancialDataMapper mapper) {
        return entry -> {
            if (entry.getValue() == null || entry.getValue().closePrice() == null) {
                return null; // Skip empty rows
            }
            // Pass the data object, stock symbol, and the date key to the mapper
            return mapper.toEntityFromAlphaVantage(entry.getValue(), "IBM", entry.getKey());
        };
    }

    // Unified Writer that writes data in fast database batches
    @Bean
    public ItemWriter<FinancialMarketData> financialItemWriter(FinancialMarketDataRepository repository) {
        return chunk -> repository.saveAll(chunk.getItems());
    }
}