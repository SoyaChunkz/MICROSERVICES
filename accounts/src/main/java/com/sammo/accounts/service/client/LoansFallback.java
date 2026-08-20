package com.sammo.accounts.service.client;

import com.sammo.accounts.dto.LoansDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class LoansFallback implements LoansFeignClient{
    @Override
    public ResponseEntity<LoansDto> fetchLoanDetails(String correlationId, String mobileNumber) {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .build();
    }
}
