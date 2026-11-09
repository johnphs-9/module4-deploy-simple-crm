package sg.edu.ntu.simple_crm_api.service;

import java.util.logging.Logger;

// import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
// import sg.edu.ntu.simple_crm_api.controller.CustomerController;
import sg.edu.ntu.simple_crm_api.model.Customer;
import sg.edu.ntu.simple_crm_api.repository.CustomerRepository;

@Component
public class DataLoader {
    private CustomerRepository customerRepository;

    private static final Logger logger = Logger.getLogger(DataLoader.class.getName());

    // @Autowired
    public DataLoader(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @PostConstruct
    public void loadData() {

        Long dataCount = customerRepository.count();
        logger.info("DataLoader - CustomerRepository count: " + dataCount);

        if (dataCount == 0) {
            logger.info("Seeding data to customer repository.");
            customerRepository.save(new Customer("Tony", "Stark"));
            customerRepository.save(new Customer("Bruce", "Banner"));
            customerRepository.save(new Customer("Peter", "Parker"));
            customerRepository.save(new Customer("Stephen", "Strange"));
        } else {
            logger.info("No need to seed data.");
        }
    } // loadData()

} // DataLoader
