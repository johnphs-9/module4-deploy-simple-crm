package sg.edu.ntu.simple_crm_api.service;

// import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import sg.edu.ntu.simple_crm_api.model.Customer;
import sg.edu.ntu.simple_crm_api.repository.CustomerRepository;

@Component
public class DataLoader {
    private CustomerRepository customerRepository;

    // @Autowired
    public DataLoader(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @PostConstruct
    public void loadData() {
        if (customerRepository.count() == 0) {
            customerRepository.save(new Customer("Tony", "Stark"));
            customerRepository.save(new Customer("Bruce", "Banner"));
            customerRepository.save(new Customer("Peter", "Parker"));
            customerRepository.save(new Customer("Stephen", "Strange"));
        }
    }
}
