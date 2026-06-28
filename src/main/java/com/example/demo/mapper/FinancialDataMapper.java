package com.example.demo.mapper;

import com.example.demo.domain.FinancialMarketData;
import com.example.demo.dto.AlphaVantageResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface FinancialDataMapper {

    // 1. Map individual Alpha Vantage stock day data to your unified Entity
    @Mapping(target = "id", ignore = true) // DB Sequence handles this
    @Mapping(target = "symbol", source = "symbol")
    @Mapping(target = "price", expression = "java(new java.math.BigDecimal(stockData.closePrice()))")
    @Mapping(target = "observedAt", expression = "java(java.time.LocalDate.parse(dateStr).atStartOfDay())")
    @Mapping(target = "dataSource", constant = "ALPHAVANTAGE")
    FinancialMarketData toEntityFromAlphaVantage(AlphaVantageResponseDto.StockData stockData, String symbol, String dateStr);

    // 2. Map individual ExchangeRate conversion entries to your unified Entity
    @Mapping(target = "id", ignore = true) 
    @Mapping(target = "symbol", source = "targetSymbol")
    @Mapping(target = "price", source = "rate")
    @Mapping(target = "observedAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "dataSource", constant = "EXCHANGERATE")
    FinancialMarketData toEntityFromExchangeRate(BigDecimal rate, String targetSymbol);
}