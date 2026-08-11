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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CustomersServiceImpl implements ICustomersService {

    private IAccountsService accountsService;
    private CardsFeignClient cardsFeignClient;
    private LoansFeignClient loansFeignClient;

    /**
     * @param mobileNumber - Input Mobile Number
     * @return Customer Details based on a given mobileNumber
     */
    @Override
    public CustomerDetailsDto fetchCustomerDetails(String mobileNumber) {

        CustomerDto customerDto = accountsService.fetchAccount(mobileNumber);
        ResponseEntity<CardsDto> cardsDtoResponseEntity = cardsFeignClient.fetchCardDetails(mobileNumber);
        ResponseEntity<LoansDto> loansDtoResponseEntity = loansFeignClient.fetchLoanDetails(mobileNumber);

        return CustomerDetailsDto.builder()
                .customerDto(customerDto)
                .cardsDto(cardsDtoResponseEntity.getBody())
                .loansDto(loansDtoResponseEntity.getBody())
                .build();
    }
}
