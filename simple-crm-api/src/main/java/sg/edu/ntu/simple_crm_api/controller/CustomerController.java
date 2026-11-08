package sg.edu.ntu.simple_crm_api.controller;

import sg.edu.ntu.simple_crm_api.model.Customer;
import sg.edu.ntu.simple_crm_api.model.Interaction;
import sg.edu.ntu.simple_crm_api.service.CustomerService;

// import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

// import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

/**
 * Controller for managing customer-related operations in the system.
 * Provides endpoints for creating, retrieving, updating, and deleting
 * customers under {@code /customers}.
 */
@RestController
@RequestMapping("/customers")
public class CustomerController {

    private static final Logger logger = Logger.getLogger(CustomerController.class.getName());

    private final CustomerService customerService;

    /*
     * private ArrayList<Customer> customers = new ArrayList<>();
     * public CustomerController() {
     * customers.add(new Customer("Bruce", "Banner"));
     * customers.add(new Customer("Peter", "Parker"));
     * customers.add(new Customer("Stephen", "Strange"));
     * customers.add(new Customer("Steve", "Rogers"));
     * }
     */

    /**
     * Constructs a new CustomerController with the specified customer service.
     * 
     * @param customerService the logging service selected by the
     *                        {@code customerServiceWithLoggingImpl} qualifier
     *
     *                        public
     *                        CustomerController(@Qualifier("customerServiceWithLoggingImpl")
     *                        CustomerService customerService) {
     *                        this.customerService = customerService;
     *                        }
     */
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * Creates a new customer and adds it to the system.
     * 
     * @param customer the customer to be created
     * @return the stored customer as the response body with HTTP 201 CREATED
     */
    @PostMapping
    public ResponseEntity<Customer> createCustomer(@Valid @RequestBody Customer customer) {
        Customer newCustomer = customerService.createCustomer(customer);
        logger.info("Customer added with ID: " + newCustomer.getId());
        return new ResponseEntity<>(newCustomer, HttpStatus.CREATED);
    }

    /**
     * Retrieves all customers in the system.
     * 
     * @return all customers as the response body with HTTP 200 OK
     */
    @GetMapping
    public ResponseEntity<List<Customer>> getAllCustomers() {
        List<Customer> allCustomers = customerService.getAllCustomers();
        logger.info("Retrieving all customers. Total count: " + allCustomers.size());
        return new ResponseEntity<>(allCustomers, HttpStatus.OK);
    }

    /**
     * Retrieves a customer by their unique ID.
     * 
     * @param id the unique ID of the customer to be retrieved
     * @return the matching customer with HTTP 200 OK; missing customers are handled
     *         globally
     */
    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Long id) {
        Customer customer = customerService.getCustomer(id);
        logger.info("Customer found with ID: " + id);
        return new ResponseEntity<>(customer, HttpStatus.OK);
    }

    /**
     * Updates all editable fields of the customer with the specified ID,
     * preserving the stored customer's ID.
     * 
     * @param id       the unique ID of the customer to be updated
     * @param customer the replacement values for all editable fields; its ID is
     *                 ignored, and omitted fields are not preserved
     * @return the updated customer with HTTP 200 OK; missing customers are handled
     *         globally
     */
    @PutMapping("/{id}")
    public ResponseEntity<Customer> updateCustomer(@PathVariable Long id, @Valid @RequestBody Customer customer) {
        Customer updatedCustomer = customerService.updateCustomer(id, customer);
        logger.info("Customer updated with ID: " + id);
        return new ResponseEntity<>(updatedCustomer, HttpStatus.OK);
    }

    /**
     * Deletes an existing customer with the specified ID.
     * 
     * @param id the unique ID of the customer to be deleted
     * @return HTTP 200 OK with no body; missing customers are handled globally
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        logger.info("Customer deleted with ID: " + id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Adds a new interaction to an existing customer with the specified ID.
     * 
     * @param id          the unique ID of the customer to which the interaction is
     *                    added
     * @param interaction the interaction to be added
     * @return the created interaction with HTTP 201 CREATED
     */
    @PostMapping("/{id}/interactions")
    public ResponseEntity<Interaction> addInteractionToCustomer(
            @PathVariable Long id,
            @RequestBody Interaction interaction) {
        logger.info("Adding interaction to customer with ID: " + id);
        Interaction newInteraction = customerService.addInteractionToCustomer(id, interaction);
        if (newInteraction == null) {
            logger.warning("Failed to add interaction to customer with ID: " + id);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(newInteraction, HttpStatus.CREATED);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Customer>> searchCustomers(@RequestParam String firstName) {
        List<Customer> foundCustomers = customerService.searchCustomers(firstName);
        return new ResponseEntity<>(foundCustomers, HttpStatus.OK);
    }

    @GetMapping("/search/job")
    public ResponseEntity<List<Customer>> searchCustomersByJobTitle(@RequestParam String jobTitle) {
        List<Customer> foundCustomers = customerService.searchCustomersByJobTitle(jobTitle);
        return new ResponseEntity<>(foundCustomers, HttpStatus.OK);
    }

    /*
     * private int getCustomerIndex(String id) {
     * for (Customer customer : customers) {
     * if (customer.getId().equals(id)) {
     * return customers.indexOf(customer);
     * }
     * }
     * 
     * // Not found
     * throw new CustomerNotFoundException(id);
     * }
     */

}
