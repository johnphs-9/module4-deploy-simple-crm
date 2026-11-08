package sg.edu.ntu.simple_crm_api.service;

import sg.edu.ntu.simple_crm_api.model.Customer;
import sg.edu.ntu.simple_crm_api.model.Interaction;

import java.util.List;

/**
 * Interface for customer-related operations in the system.
 * Provides methods for creating, retrieving, updating, and deleting
 * customers.
 */
public interface CustomerService {

    /**
     * Creates a new customer in the system.
     * 
     * @param customer the customer to be created
     * @return the supplied customer stored in the repository
     */
    Customer createCustomer(Customer customer);

    /**
     * Retrieves a customer by its ID.
     * 
     * @param id the ID of the customer to be retrieved
     * @return the customer with the specified ID
     * @throws sg.edu.ntu.simple_crm_api.exceptions.CustomerNotFoundException if no
     *                                                                        customer
     *                                                                        has
     *                                                                        the
     *                                                                        specified
     *                                                                        ID
     */
    Customer getCustomer(Long id);

    /**
     * Retrieves all customers in the system.
     * 
     * @return the repository's backing list of customers, not a copy
     */
    List<Customer> getAllCustomers();

    /**
     * Replaces all editable fields of an existing customer, preserving its ID.
     * 
     * @param id       the ID of the customer to be updated
     * @param customer the replacement field values; its ID is ignored, and omitted
     *                 fields are not preserved
     * @return the existing stored customer after updating its fields
     * @throws sg.edu.ntu.simple_crm_api.exceptions.CustomerNotFoundException if no
     *                                                                        customer
     *                                                                        has
     *                                                                        the
     *                                                                        specified
     *                                                                        ID
     */
    Customer updateCustomer(Long id, Customer customer);

    /**
     * Deletes a customer from the system.
     * 
     * @param id the ID of the customer to be deleted
     * @throws sg.edu.ntu.simple_crm_api.exceptions.CustomerNotFoundException if no
     *                                                                        customer
     *                                                                        has
     *                                                                        the
     *                                                                        specified
     *                                                                        ID
     */
    void deleteCustomer(Long id);

    /**
     * Adds an interaction to a customer.
     * 
     * @param id          the ID of the customer to add the interaction to
     * @param interaction the interaction to be added
     * @return the added interaction
     * @throws sg.edu.ntu.simple_crm_api.exceptions.CustomerNotFoundException if no
     *                                                                        customer
     *                                                                        has
     *                                                                        the
     *                                                                        specified
     *                                                                        ID
     */
    Interaction addInteractionToCustomer(Long id, Interaction interaction); // 👈 new

    /**
     * Searches for customers by their first name.
     * 
     * @param firstName the first name to search for
     * @return a list of customers with the matching first name
     */
    public List<Customer> searchCustomers(String firstName);

    /** Searches for customers with the specified job title. */
    List<Customer> searchCustomersByJobTitle(String jobTitle);

}// Interface CustomerService
