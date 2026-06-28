package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public record AlphaVantageResponseDto(
    @JsonProperty("Meta Data")
    MetaData metaData,
    
    @JsonProperty("Time Series (Daily)")
    Map<String, StockData> timeSeries
) {
    public record MetaData(
        @JsonProperty("2. Symbol")
        String symbol
    ) {}

    public record StockData(
        @JsonProperty("4. close")
        String closePrice
    ) {}
}