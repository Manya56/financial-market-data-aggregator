package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Map;

public record ExchangeRateResponseDto(
    @JsonProperty("base_code")
    String baseCode,
    
    @JsonProperty("conversion_rates")
    Map<String, BigDecimal> conversionRates
) {}