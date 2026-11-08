package sg.edu.ntu.simple_crm_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sg.edu.ntu.simple_crm_api.model.Customer;
import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    List<Customer> findByFirstName(String firstName);

    @Query("SELECT c FROM Customer c WHERE c.jobTitle = :jobTitle")
    List<Customer> findByJobTitleJPQL(@Param("jobTitle") String jobTitle);
}
