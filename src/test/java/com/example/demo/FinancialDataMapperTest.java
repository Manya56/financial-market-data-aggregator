package com.example.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import com.example.demo.domain.FinancialMarketData;
import com.example.demo.dto.AlphaVantageResponseDto;
import com.example.demo.mapper.FinancialDataMapper;
import com.example.demo.mapper.FinancialDataMapperImpl; // Import the generated implementation class
import org.junit.jupiter.api.Test;

class FinancialDataMapperTest {

    // ⚡ Direct instantiation: No heavy Spring context or database connections required!
    private final FinancialDataMapper mapper = new FinancialDataMapperImpl();

    @Test
    void testToEntityFromExchangeRate_ShouldMapCorrectly() {
        BigDecimal mockRate = new BigDecimal("83.52");
        String targetSymbol = "INR";

        FinancialMarketData entity = mapper.toEntityFromExchangeRate(mockRate, targetSymbol);

        assertNotNull(entity);
        assertEquals("INR", entity.getSymbol());
        assertEquals(mockRate, entity.getPrice());
        assertEquals("EXCHANGERATE", entity.getDataSource());
        assertNotNull(entity.getObservedAt());
    }

    @Test
    void testToEntityFromAlphaVantage_ShouldMapCorrectly() {
        AlphaVantageResponseDto.StockData mockStock = new AlphaVantageResponseDto.StockData("145.20");
        String symbol = "IBM";
        String dateStr = "2026-06-25";

        FinancialMarketData entity = mapper.toEntityFromAlphaVantage(mockStock, symbol, dateStr);

        assertNotNull(entity);
        assertEquals("IBM", entity.getSymbol());
        assertEquals(new BigDecimal("145.20"), entity.getPrice());
        assertEquals("ALPHAVANTAGE", entity.getDataSource());
        assertEquals("2026-06-25T00:00", entity.getObservedAt().toString());
    }
}