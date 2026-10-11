package com.sammo.loans.service.impl;

import com.sammo.loans.constants.LoansConstants;
import com.sammo.loans.dto.LoansDto;
import com.sammo.loans.entity.Loans;
import com.sammo.loans.exception.LoanAlreadyExistsException;
import com.sammo.loans.exception.ResourceNotFoundException;
import com.sammo.loans.mapper.LoansMapper;
import com.sammo.loans.repository.LoansRepository;
import com.sammo.loans.service.ILoansService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@AllArgsConstructor
@Slf4j
public class LoansServiceImpl implements ILoansService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private final LoansRepository loansRepository;

    /**
     * @param mobileNumber - Mobile Number of the Customer
     */
    @Override
    @Transactional
    public void createLoan(String mobileNumber) {
        log.info("Creating loan for mobile number {}", mobileNumber);
        if (loansRepository.findByMobileNumber(mobileNumber).isPresent()) {
            throw new LoanAlreadyExistsException("Loan already registered with given mobileNumber "+mobileNumber);
        }
        Loans loan = loansRepository.save(createNewLoan(mobileNumber));
        log.info("Created loan {} for mobile number {}", loan.getLoanNumber(), mobileNumber);
    }

    /**
     * @param mobileNumber - Mobile Number of the Customer
     * @return the new loan details
     */
    private Loans createNewLoan(String mobileNumber) {
        Loans newLoan = new Loans();
        String loanNumber;
        do {
            loanNumber = Long.toString(100000000000L + RANDOM.nextLong(900000000000L));
        } while (loansRepository.existsByLoanNumber(loanNumber));
        newLoan.setLoanNumber(loanNumber);
        newLoan.setMobileNumber(mobileNumber);
        newLoan.setLoanType(LoansConstants.HOME_LOAN);
        newLoan.setTotalLoan(LoansConstants.NEW_LOAN_LIMIT);
        newLoan.setAmountPaid(0);
        newLoan.setOutstandingAmount(LoansConstants.NEW_LOAN_LIMIT);
        return newLoan;
    }

    /**
     *
     * @param mobileNumber - Input mobile Number
     * @return Loan Details based on a given mobileNumber
     */
    @Override
    public LoansDto fetchLoan(String mobileNumber) {
        log.info("Fetching loan for mobile number {}", mobileNumber);
        Loans loans = loansRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("Loan", "mobileNumber", mobileNumber)
        );
        return LoansMapper.mapToLoansDto(loans, new LoansDto());
    }

    /**
     *
     * @param loansDto - LoansDto Object
     * @return boolean indicating if the update of loan details is successful or not
     */
    @Override
    @Transactional
    public boolean updateLoan(LoansDto loansDto) {
        log.info("Updating loan {}", loansDto.getLoanNumber());
        Loans loans = loansRepository.findByLoanNumber(loansDto.getLoanNumber()).orElseThrow(
                () -> new ResourceNotFoundException("Loan", "LoanNumber", loansDto.getLoanNumber()));
        validateBalances(loansDto);
        LoansMapper.mapToLoans(loansDto, loans);
        loansRepository.save(loans);
        log.info("Updated loan {}", loans.getLoanNumber());
        return  true;
    }

    /**
     * @param mobileNumber - Input MobileNumber
     * @return boolean indicating if the delete of loan details is successful or not
     */
    @Override
    @Transactional
    public boolean deleteLoan(String mobileNumber) {
        log.info("Deleting loan for mobile number {}", mobileNumber);
        Loans loans = loansRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("Loan", "mobileNumber", mobileNumber)
        );
        loansRepository.deleteById(loans.getLoanId());
        log.info("Deleted loan {} for mobile number {}", loans.getLoanNumber(), mobileNumber);
        return true;
    }

    private void validateBalances(LoansDto loansDto) {
        // Outstanding balance is derived from the original loan and paid amount.
        if (loansDto.getAmountPaid() > loansDto.getTotalLoan()
                || loansDto.getOutstandingAmount() != loansDto.getTotalLoan() - loansDto.getAmountPaid()) {
            throw new IllegalArgumentException("Loan balances must satisfy outstandingAmount = totalLoan - amountPaid");
        }
    }

}
