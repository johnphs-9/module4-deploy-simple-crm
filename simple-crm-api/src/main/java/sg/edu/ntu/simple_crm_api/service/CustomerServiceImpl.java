package sg.edu.ntu.simple_crm_api.service;

// import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import sg.edu.ntu.simple_crm_api.model.Customer;
import sg.edu.ntu.simple_crm_api.model.Interaction;
import sg.edu.ntu.simple_crm_api.repository.CustomerRepository;
import sg.edu.ntu.simple_crm_api.repository.InteractionRepository;
import sg.edu.ntu.simple_crm_api.exceptions.CustomerNotFoundException;

import java.util.List;

/**
 * Primary implementation of the CustomerService interface.
 * Provides methods for creating, retrieving, updating, and deleting customers.
 */
@Primary
@Service
public class CustomerServiceImpl implements CustomerService {

    /** The repository used for customer-related operations. */
    private final CustomerRepository customerRepository;

    /** The repository used for interaction-related operations. */
    private final InteractionRepository interactionRepository;

    /**
     * Constructs a new CustomerServiceImpl with the specified customer repository.
     * 
     * @param customerRepository the repository used for customer-related operations
     */
    public CustomerServiceImpl(CustomerRepository customerRepository, InteractionRepository interactionRepository) {
        this.customerRepository = customerRepository;
        this.interactionRepository = interactionRepository;
    }

    /**
     * Creates a new customer in the system.
     * 
     * @param customer the customer to be created
     * @return the supplied customer stored in the repository
     */
    @Override
    public Customer createCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    /**
     * Retrieves a customer by its ID.
     * 
     * @param id the ID of the customer to be retrieved
     * @return the customer with the specified ID
     * @throws CustomerNotFoundException if no customer has the specified ID
     */
    @Override
    public Customer getCustomer(Long id) {
        return customerRepository.findById(id).orElseThrow(() -> new CustomerNotFoundException(id));
    }

    /**
     * Retrieves all customers in the system.
     * 
     * @return the repository's backing list of customers, not a copy
     */
    @Override
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    /**
     * Replaces all editable fields of an existing customer, preserving its ID.
     * 
     * @param id       the ID of the customer to be updated
     * @param customer the replacement field values; its ID is ignored, and omitted
     *                 fields are not preserved
     * @return the existing stored customer after updating its fields
     * @throws CustomerNotFoundException if no customer has the specified ID
     */
    @Override
    public Customer updateCustomer(Long id, Customer customer) {
        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
        existingCustomer.setFirstName(customer.getFirstName());
        existingCustomer.setLastName(customer.getLastName());
        existingCustomer.setEmail(customer.getEmail());
        existingCustomer.setContactNo(customer.getContactNo());
        existingCustomer.setJobTitle(customer.getJobTitle());
        existingCustomer.setYearOfBirth(customer.getYearOfBirth());
        return customerRepository.save(existingCustomer);
    }

    /**
     * Deletes a customer from the system.
     * 
     * @param id the ID of the customer to be deleted
     * @throws CustomerNotFoundException if no customer has the specified ID
     */
    @Override
    public void deleteCustomer(Long id) {
        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
        customerRepository.delete(existingCustomer);
    }

    @Override
    public Interaction addInteractionToCustomer(Long id, Interaction interaction) {
        // Step 1: Find the customer — throw an error if not found
        Customer selectedCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
        // Step 2: Link the customer to the interaction
        interaction.setCustomer(selectedCustomer);
        // Step 3: Save and return the interaction
        return interactionRepository.save(interaction);
    }

    @Override
    public List<Customer> searchCustomers(String firstName) {
        return customerRepository.findByFirstName(firstName);
    }

    @Override
    public List<Customer> searchCustomersByJobTitle(String jobTitle) {
        return customerRepository.findByJobTitleJPQL(jobTitle);
    }

}