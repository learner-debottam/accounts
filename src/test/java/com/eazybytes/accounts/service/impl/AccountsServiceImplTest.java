package com.eazybytes.accounts.service.impl;

import com.eazybytes.accounts.dto.CustomerDto;
import com.eazybytes.accounts.entity.Accounts;
import com.eazybytes.accounts.entity.Customer;
import com.eazybytes.accounts.exception.CustomerAlreadyExistsException;
import com.eazybytes.accounts.repository.AccountsRepository;
import com.eazybytes.accounts.repository.CustomerRepository;
// Mockito imports
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountsServiceImplTest {

    @Mock
    private AccountsRepository accountsRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private AccountsServiceImpl accountsService;

    // ---------- createAccount tests ----------

    @Test
    void createAccount_WhenCustomerAlreadyExists_ShouldThrowException() {
        CustomerDto dto = new CustomerDto();
        dto.setMobileNumber("12345");

        when(customerRepository.findByMobileNumber("12345"))
                .thenReturn(Optional.of(new Customer()));

        assertThrows(CustomerAlreadyExistsException.class,
                () -> accountsService.createAccount(dto));

        verify(customerRepository, times(1)).findByMobileNumber("12345");
        verifyNoMoreInteractions(customerRepository);
        verifyNoInteractions(accountsRepository);
    }

    @Test
    void createAccount_WhenCustomerDoesNotExist_ShouldSaveCustomerAndAccount() {
        CustomerDto dto = new CustomerDto();
        dto.setMobileNumber("12345");

        Customer saved = new Customer();
        saved.setCustomerId(1L);

        when(customerRepository.findByMobileNumber("12345"))
                .thenReturn(Optional.empty());

        when(customerRepository.save(any(Customer.class)))
                .thenReturn(saved);

        when(accountsRepository.save(any(Accounts.class)))
                .thenReturn(new Accounts());

        accountsService.createAccount(dto);

        verify(customerRepository).save(any(Customer.class));
        verify(accountsRepository).save(any(Accounts.class));
    }
}
