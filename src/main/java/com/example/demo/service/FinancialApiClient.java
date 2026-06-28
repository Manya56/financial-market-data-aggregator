package com.example.demo.service;

import com.example.demo.dto.AlphaVantageResponseDto;
import com.example.demo.dto.ExchangeRateResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class FinancialApiClient {

    private final RestClient restClient = RestClient.create();

    public AlphaVantageResponseDto fetchStockData(String symbol, String apiKey) {
        String url = String.format("https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=%s&apikey=%s", symbol, apiKey);
        return restClient.get()
                .uri(url)
                .retrieve()
                .body(AlphaVantageResponseDto.class);
    }

    public ExchangeRateResponseDto fetchExchangeRates(String apiKey) {
        String url = String.format("https://v6.exchangerate-api.com/v6/%s/latest/USD", apiKey);
        return restClient.get()
                .uri(url)
                .retrieve()
                .body(ExchangeRateResponseDto.class);
    }
}