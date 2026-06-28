package com.example.demo.service;

import com.example.demo.domain.FinancialMarketData;
import com.example.demo.repository.FinancialMarketDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FinancialDataService {

    private final FinancialMarketDataRepository repository;

    public Page<FinancialMarketData> getMarketDataBySymbol(String symbol, int page, int size) {
        // Enforces safe pagination parameters
        PageRequest pageRequest = PageRequest.of(page, size);
        return repository.findBySymbolIgnoreCaseOrderByObservedAtDesc(symbol, pageRequest);
    }
}