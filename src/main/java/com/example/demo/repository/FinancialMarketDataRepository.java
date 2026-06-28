package com.example.demo.repository;

import com.example.demo.domain.FinancialMarketData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FinancialMarketDataRepository extends JpaRepository<FinancialMarketData, Long> {

    // ⚡ Fetches data by symbol with built-in pagination support to prevent memory overload
    Page<FinancialMarketData> findBySymbolIgnoreCaseOrderByObservedAtDesc(String symbol, Pageable pageable);
}