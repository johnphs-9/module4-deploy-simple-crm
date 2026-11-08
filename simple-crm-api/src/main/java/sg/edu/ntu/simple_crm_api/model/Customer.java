package sg.edu.ntu.simple_crm_api.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
// import java.util.UUID;
import jakarta.persistence.Id;
// import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.logging.Logger;

@Data
@NoArgsConstructor
@Entity
@Table(name = "customer")
@Builder // fluent object creation in tests
@EqualsAndHashCode // value-based equality, required by assertEquals()
@AllArgsConstructor // required by @Builder when other constructors exist
// @JsonPropertyOrder({ "id", "firstName", "lastName", "email", "contactNo",
// "jobTitle", "yearOfBirth" })
public class Customer {
    private static final Logger logger = Logger.getLogger(Customer.class.getName());

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank(message = "First name is mandatory")
    @Column(name = "first_name")
    private String firstName;
    @NotBlank(message = "Last name is mandatory")
    @Column(name = "last_name")
    private String lastName;
    @Email(message = "Email should be valid")
    @Column(name = "email")
    private String email;
    @Size(min = 8, max = 8, message = "Contact number must be exactly 8 characters")
    @Column(name = "contact_no")
    private String contactNo;
    @Size(min = 2, max = 50, message = "Job title must be between 2 and 50 characters")
    @Column(name = "job_title")
    private String jobTitle;
    @Column(name = "year_of_birth")
    private int yearOfBirth;

    // private final String id;
    // private String firstName;
    // private String lastName;
    // private String email;
    // private String contactNo;
    // private String jobTitle;
    // private int yearOfBirth;

    /**
     * Creates a customer with a randomly generated UUID string as its ID and the
     * supplied names. Email, contact number, and job title remain null, and the
     * birth year defaults to zero.
     *
     * @param firstName the customer's first name
     * @param lastName  the customer's last name
     */
    public Customer(String firstName, String lastName) {
        // this.id = UUID.randomUUID().toString();
        this.firstName = firstName;
        this.lastName = lastName;
        logger.info("Customer created: [" + this.id + "] " + this.firstName + " " + this.lastName);
    }
}
