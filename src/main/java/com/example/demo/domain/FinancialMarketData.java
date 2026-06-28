package com.example.demo.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Column;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "financial_market_data")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor
public class FinancialMarketData {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "fin_data_seq")
    @SequenceGenerator(name = "fin_data_seq", sequenceName = "financial_data_id_seq", allocationSize = 100)
    private Long id;

    @Column(nullable = false)
    private String symbol; 

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal price; 

    @Column(nullable = false)
    private LocalDateTime observedAt; 

    @Column(nullable = false)
    private String dataSource; 
}