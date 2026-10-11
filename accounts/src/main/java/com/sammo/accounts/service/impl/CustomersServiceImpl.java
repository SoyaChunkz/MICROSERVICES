package com.sammo.accounts.service.impl;

import com.sammo.accounts.dto.CardsDto;
import com.sammo.accounts.dto.CustomerDetailsDto;
import com.sammo.accounts.dto.CustomerDto;
import com.sammo.accounts.dto.LoansDto;
import com.sammo.accounts.service.IAccountsService;
import com.sammo.accounts.service.ICustomersService;
import com.sammo.accounts.service.client.CardsFeignClient;
import com.sammo.accounts.service.client.LoansFeignClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class CustomersServiceImpl implements ICustomersService {

    private IAccountsService accountsService;
    private CardsFeignClient cardsFeignClient;
    private LoansFeignClient loansFeignClient;

    /**
     * @param mobileNumber  - Input Mobile Number
     * @param correlationId - Correlation ID value generated at Edge server
     * @return Customer Details based on a given mobileNumber
     */
    @Override
    public CustomerDetailsDto fetchCustomerDetails(String mobileNumber, String correlationId) {
        log.info("Aggregating customer details for mobile number {} with correlation id {}", mobileNumber, correlationId);
        CustomerDto customerDto = accountsService.fetchAccount(mobileNumber);
        ResponseEntity<CardsDto> cardsDtoResponseEntity = cardsFeignClient.fetchCardDetails(correlationId, mobileNumber);
        ResponseEntity<LoansDto> loansDtoResponseEntity = loansFeignClient.fetchLoanDetails(correlationId, mobileNumber);

        if (cardsDtoResponseEntity.getStatusCode().is5xxServerError()) {
            log.warn("Cards service unavailable for correlation id {}", correlationId);
        }
        if (loansDtoResponseEntity.getStatusCode().is5xxServerError()) {
            log.warn("Loans service unavailable for correlation id {}", correlationId);
        }

        return CustomerDetailsDto.builder()
                .customerDto(customerDto)
                .cardsDto(cardsDtoResponseEntity.getBody())
                .loansDto(loansDtoResponseEntity.getBody())
                .build();
    }
}
