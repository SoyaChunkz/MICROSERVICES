package com.sammo.accounts.service.impl;

import com.sammo.accounts.constants.AccountsConstants;
import com.sammo.accounts.dto.AccountsDto;
import com.sammo.accounts.dto.AccountsMsgDto;
import com.sammo.accounts.dto.CommunicationStatusDto;
import com.sammo.accounts.dto.CustomerDto;
import com.sammo.accounts.entity.Accounts;
import com.sammo.accounts.entity.CommunicationStatus;
import com.sammo.accounts.entity.Customer;
import com.sammo.accounts.exception.CustomerAlreadyExistsException;
import com.sammo.accounts.exception.ResourceNotFoundException;
import com.sammo.accounts.mapper.AccountsMapper;
import com.sammo.accounts.mapper.CustomerMapper;
import com.sammo.accounts.repository.AccountsRepository;
import com.sammo.accounts.repository.CustomerRepository;
import com.sammo.accounts.service.IAccountsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.security.SecureRandom;

@Service
@AllArgsConstructor
@Slf4j
public class AccountsServiceImpl implements IAccountsService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private final AccountsRepository accountsRepository;
    private final CustomerRepository customerRepository;
    private final StreamBridge streamBridge;

    /**
     * @param customerDto - CustomerDto Object
     */
    @Override
    @Transactional
    public void createAccount(CustomerDto customerDto) {
        log.info("Creating account for mobile number {}", customerDto.getMobileNumber());
        Customer customer = CustomerMapper.mapToCustomer(customerDto, new Customer());
        Optional<Customer> optionalCustomer = customerRepository.findByMobileNumber(customerDto.getMobileNumber());
        if(optionalCustomer.isPresent()) {
            throw new CustomerAlreadyExistsException("Customer already registered with given mobileNumber "
                    +customerDto.getMobileNumber());
        }
        Customer savedCustomer = customerRepository.save(customer);
        Accounts savedAccount = accountsRepository.save(createNewAccount(savedCustomer));
        sendCommunication(savedAccount, savedCustomer);
        log.info("Created account {} for mobile number {}", savedAccount.getAccountNumber(), customerDto.getMobileNumber());
    }

    private void sendCommunication(Accounts account, Customer customer) {
        var accountsMsgDto = new AccountsMsgDto(account.getAccountNumber(), customer.getName(), customer.getEmail(), customer.getMobileNumber());
        log.info("Sending Communication request for the details: {}", accountsMsgDto);
        var result = streamBridge.send("sendCommunication-out-0", accountsMsgDto);
        log.info("Is the Communication request successfully triggered ? : {}", result);
    }

    /**
     * @param customer - Customer Object
     * @return the new account details
     */
    private Accounts createNewAccount(Customer customer) {
        Accounts newAccount = new Accounts();
        newAccount.setCustomerId(customer.getCustomerId());
        long randomAccNumber;
        do {
            randomAccNumber = 1000000000L + RANDOM.nextInt(900000000);
        } while (accountsRepository.existsByAccountNumber(randomAccNumber));
        newAccount.setAccountNumber(randomAccNumber);
        newAccount.setAccountType(AccountsConstants.SAVINGS);
        newAccount.setBranchAddress(AccountsConstants.ADDRESS);
        return newAccount;
    }

    /**
     * @param mobileNumber - Input Mobile Number
     * @return Accounts Details based on a given mobileNumber
     */
    @Override
    public CustomerDto fetchAccount(String mobileNumber) {
        log.info("Fetching account for mobile number {}", mobileNumber);
        Customer customer = customerRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("Customer", "mobileNumber", mobileNumber)
        );
        Accounts accounts = accountsRepository.findByCustomerId(customer.getCustomerId()).orElseThrow(
                () -> new ResourceNotFoundException("Account", "customerId", customer.getCustomerId().toString())
        );
        CustomerDto customerDto = CustomerMapper.mapToCustomerDto(customer, new CustomerDto());
        customerDto.setAccountsDto(AccountsMapper.mapToAccountsDto(accounts, new AccountsDto()));
        return customerDto;
    }

    /**
     * @param customerDto - CustomerDto Object
     * @return boolean indicating if the update of Account details is successful or not
     */
    @Override
    @Transactional
    public boolean updateAccount(CustomerDto customerDto) {
        log.info("Updating account for mobile number {}", customerDto.getMobileNumber());
        Customer customer = customerRepository.findByMobileNumber(customerDto.getMobileNumber()).orElseThrow(
                () -> new ResourceNotFoundException("Customer", "mobileNumber", customerDto.getMobileNumber()));
        CustomerMapper.mapToCustomer(customerDto, customer);
        customerRepository.save(customer);

        AccountsDto accountsDto = customerDto.getAccountsDto();
        if (accountsDto != null) {
            Accounts accounts = accountsRepository.findById(accountsDto.getAccountNumber()).orElseThrow(
                    () -> new ResourceNotFoundException("Account", "AccountNumber", accountsDto.getAccountNumber().toString())
            );
            if (!customer.getCustomerId().equals(accounts.getCustomerId())) {
                throw new IllegalArgumentException("Account does not belong to the supplied customer");
            }
            AccountsMapper.mapToAccounts(accountsDto, accounts);
            accounts = accountsRepository.save(accounts);
            log.info("Updated account {} for customer {}", accounts.getAccountNumber(), customer.getCustomerId());
        }
        return true;
    }

    /**
     * @param mobileNumber - Input Mobile Number
     * @return boolean indicating if the delete of Account details is successful or not
     */
    @Override
    @Transactional
    public boolean deleteAccount(String mobileNumber) {
        log.info("Deleting account for mobile number {}", mobileNumber);
        Customer customer = customerRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("Customer", "mobileNumber", mobileNumber)
        );
        accountsRepository.deleteByCustomerId(customer.getCustomerId());
        customerRepository.deleteById(customer.getCustomerId());
        log.info("Deleted account and customer for mobile number {}", mobileNumber);
        return true;
    }

    /**
     *
     * @param communicationStatus - Event received
     * @return boolean indicating if the update of communication status is successful or not
     */
    @Override
    public void updateCommunicationStatus(CommunicationStatusDto communicationStatus) {
        log.info("Updating communication status for account {}: {} / {}",
                communicationStatus.accountNumber(), communicationStatus.type(), communicationStatus.status());

        Accounts accounts = accountsRepository.findById(communicationStatus.accountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Account", "AccountNumber", communicationStatus.accountNumber().toString()));

        CommunicationStatus currentStatus = accounts.getCommunicationStatus();

        if ("EMAIL".equals(communicationStatus.type()) && "EMAIL_SENT".equals(communicationStatus.status())) {

            if (currentStatus == CommunicationStatus.SMS_SENT) accounts.setCommunicationStatus(CommunicationStatus.BOTH_SENT);
            else accounts.setCommunicationStatus(CommunicationStatus.EMAIL_SENT);

        } else if ("SMS".equals(communicationStatus.type()) && "SMS_SENT".equals(communicationStatus.status())) {

            if (currentStatus == CommunicationStatus.EMAIL_SENT) accounts.setCommunicationStatus(CommunicationStatus.BOTH_SENT);
            else accounts.setCommunicationStatus(CommunicationStatus.SMS_SENT);
        }

        accountsRepository.save(accounts);
    }
}
