package com.example.splitbill.service;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.splitbill.repository.BillRepository;

@Service
public class StatisticsService {

    private final BillRepository billRepository;

    public StatisticsService(BillRepository billRepository) {
        this.billRepository = billRepository;
    }

    public Map<String, Object> getBillStatistics() {
        Map<String, Object> statistics = new LinkedHashMap<>();

        statistics.put("totalBills",
                billRepository.countAllBills());

        statistics.put("totalAmount",
                billRepository.sumTotalAmount());

        return statistics;
    }
}

