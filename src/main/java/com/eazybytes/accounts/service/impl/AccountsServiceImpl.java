package com.eazybytes.accounts.service.impl;

import com.eazybytes.accounts.constants.AccountsConstants;
import com.eazybytes.accounts.dto.AccountsDto;
import com.eazybytes.accounts.dto.CustomerDto;
import com.eazybytes.accounts.entity.Accounts;
import com.eazybytes.accounts.entity.Customer;
import com.eazybytes.accounts.exception.CustomerAlreadyExistsException;
import com.eazybytes.accounts.exception.ResourceNotFoundException;
import com.eazybytes.accounts.mapper.AccountsMapper;
import com.eazybytes.accounts.mapper.CustomerMapper;
import com.eazybytes.accounts.repository.AccountsRepository;
import com.eazybytes.accounts.repository.CustomerRepository;
import com.eazybytes.accounts.service.IAccountsService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@AllArgsConstructor
public class AccountsServiceImpl implements IAccountsService {

    private AccountsRepository accountsRepository;

    private CustomerRepository customerRepository;

    /**
     * @param customerDto
     */
    @Override
    public void createAccount(CustomerDto customerDto) {
        Optional<Customer> optionCustomer = customerRepository.findByMobileNumber(customerDto.getMobileNumber());
        if(optionCustomer.isPresent()){
            throw new CustomerAlreadyExistsException(
                    "Customer already registered with the given mobile number " +customerDto.getMobileNumber()
            );
        }
        Customer customer = CustomerMapper.mapToCustomer(customerDto, new Customer());
        customer.setCreatedBy("Anonymous");
        customer.setCreatedAt(LocalDateTime.now());
        Customer savedCustomer = customerRepository.save(customer);
        accountsRepository.save(createNewAccount(savedCustomer));

    }

    public Accounts createNewAccount(Customer customer){
        Accounts newAccounts = new Accounts();

        newAccounts.setCustomerId(customer.getCustomerId());

        Long randomAccountNumber = 1000000000L + new Random().nextInt(900000000);

        newAccounts.setAccountNumber(randomAccountNumber);
        newAccounts.setAccountType(AccountsConstants.SAVINGS);
        newAccounts.setBranchAddress(AccountsConstants.ADDRESS);
        newAccounts.setCreatedBy("Anonymous");
        newAccounts.setCreatedAt(LocalDateTime.now());
        return newAccounts;
    }

    @Override
    public CustomerDto fetchAccount(String mobileNumber) {
        Customer customer
                = customerRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("Customer", "mobileNumber", mobileNumber)
         );

        Accounts accounts
                = accountsRepository.findByCustomerId(customer.getCustomerId()).orElseThrow(
                () -> new ResourceNotFoundException("Accounts", "customerId", customer.getCustomerId().toString())
        );

       CustomerDto customerDto = CustomerMapper.mapToCustomerDto(customer,new CustomerDto());
       customerDto.setAccountsDto(AccountsMapper.mapToAccountsDto(accounts,new AccountsDto()));
       return customerDto;
    }

    @Override
    public boolean updateAccount(CustomerDto customerDto) {
        boolean isUpdate = false;
        AccountsDto accountsDto = customerDto.getAccountsDto();
        if(accountsDto != null){
            Accounts accounts  = accountsRepository.findById(accountsDto.getAccountNumber()).orElseThrow(
                    () -> new ResourceNotFoundException("Accounts","AccountNumber",accountsDto.getAccountNumber().toString())
            );

        AccountsMapper.mapToAccounts(accountsDto,accounts);
        accounts = accountsRepository.save(accounts);

        Long customerId = accounts.getCustomerId();
        Customer customer
                    = customerRepository.findById(customerId).orElseThrow(
                    () -> new ResourceNotFoundException("Customer", "customerId", customerId.toString())
            );
        CustomerMapper.mapToCustomer(customerDto,customer);
        customerRepository.save(customer);
        isUpdate = true;
        }
        return isUpdate;
    }

    @Override
    public boolean deleteAccount(String mobileNumber) {
        boolean isDeleted = false;

        Customer customer
                = customerRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("Customer", "mobileNumber", mobileNumber)
        );
        accountsRepository.deleteByCustomerId(customer.getCustomerId());
        customerRepository.deleteById(customer.getCustomerId());
        isDeleted = true;

        return isDeleted;
    }


}
