package sg.edu.ntu.simple_crm_api.exceptions;

/**
 * Indicates that a customer lookup by ID found no matching customer.
 */
public class CustomerNotFoundException extends RuntimeException {
    /**
     * Creates an exception whose message identifies the missing customer.
     *
     * @param id the customer ID that could not be found
     */
    public CustomerNotFoundException(Long id) {
        super("Exception: Could not find customer with id: " + id);
    }
}
