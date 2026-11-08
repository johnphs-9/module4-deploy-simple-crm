package sg.edu.ntu.simple_crm_api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import sg.edu.ntu.simple_crm_api.exceptions.CustomerNotFoundException;
import sg.edu.ntu.simple_crm_api.model.Customer;
import sg.edu.ntu.simple_crm_api.repository.CustomerRepository;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    @Test
    public void createCustomer_validCustomer_returnsSavedCustomer() {
        // 1. ARRANGE
        Customer customer = Customer.builder()
                .firstName("Clint").lastName("Barton")
                .email("clint@avengers.com").contactNo("12345678")
                .jobTitle("Special Agent").yearOfBirth(1975)
                .build();

        // Program the mock. The real repository is never called, no database is
        // touched.
        when(customerRepository.save(customer)).thenReturn(customer);

        // 2. ACT
        Customer savedCustomer = customerService.createCustomer(customer);

        // 3. ASSERT
        assertEquals(customer, savedCustomer, "The saved customer should match the new customer");
        verify(customerRepository, times(1)).save(customer);
    }

    @Test
    public void getCustomer_found_returnsCustomer() {
        // 1. ARRANGE
        Long customerId = 1L;
        Customer mockCustomer = Customer.builder()
                .id(1L)
                .firstName("Clint").lastName("Barton")
                .email("clint@avengers.com").contactNo("12345678")
                .jobTitle("Special Agent").yearOfBirth(1975)
                .build();

        // Mock the findById to return an Optional containing the customer
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(mockCustomer));

        // 2. ACT
        Customer foundCustomer = customerService.getCustomer(customerId);

        // 3. ASSERT
        assertEquals(mockCustomer, foundCustomer, "The returned customer should match the mock");
        verify(customerRepository, times(1)).findById(customerId);
    }

    @Test
    public void getCustomer_missingId_throwsCustomerNotFoundException() {
        // 1. ARRANGE
        Long customerId = 1L;

        // Optional.empty() simulates no record found
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        // 2. ACT + 3. ASSERT
        assertThrows(CustomerNotFoundException.class, () -> customerService.getCustomer(customerId));
    }
}