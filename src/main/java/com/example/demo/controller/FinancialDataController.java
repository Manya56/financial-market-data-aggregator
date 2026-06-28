package com.example.demo.controller;

import com.example.demo.domain.FinancialMarketData;
import com.example.demo.service.FinancialDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/financial-data")
@RequiredArgsConstructor
public class FinancialDataController {

    private final FinancialDataService financialDataService;

    // Example URL: http://localhost:8080/api/v1/financial-data/IBM?page=0&size=10
    @GetMapping("/{symbol}")
    public ResponseEntity<Page<FinancialMarketData>> getMarketData(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
            
        Page<FinancialMarketData> data = financialDataService.getMarketDataBySymbol(symbol, page, size);
        return ResponseEntity.ok(data);
    }
}