package sg.edu.ntu.simple_crm_api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import sg.edu.ntu.simple_crm_api.model.Customer;
import sg.edu.ntu.simple_crm_api.repository.CustomerRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional // roll back each test's data changes so tests stay independent
public class CustomerControllerTest {

    private static final String CUSTOMER_JSON = "{\"firstName\":\"Tony\",\"lastName\":\"Stark\",\"email\":\"tony@starkindustries.com\","
            + "\"contactNo\":\"12345678\",\"jobTitle\":\"Iron Man\",\"yearOfBirth\":1970}";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository customerRepository;

    // --- R7: requests without credentials are blocked (401) ---

    @Test
    public void getCustomers_withoutCredentials_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/customers"))
                .andExpect(status().isUnauthorized());
    }

    // --- R11: /health is public ---

    @Test
    public void health_withoutCredentials_returnsOk() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk());
    }

    // --- R14: GET is allowed for any authenticated user ---

    @Test
    @WithMockUser(roles = "USER")
    public void getAllCustomers_userRole_returnsOk() throws Exception {
        mockMvc.perform(get("/customers"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    // --- R12: POST /customers requires ADMIN ---

    @Test
    @WithMockUser(roles = "ADMIN")
    public void createCustomer_adminRole_returnsCreated() throws Exception {
        mockMvc.perform(post("/customers")
                .content(CUSTOMER_JSON)
                .contentType("application/json"))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "USER")
    public void createCustomer_userRole_returnsForbidden() throws Exception {
        mockMvc.perform(post("/customers")
                .content(CUSTOMER_JSON)
                .contentType("application/json"))
                .andExpect(status().isForbidden());
    }

    // --- PUT /customers/{id} requires ADMIN (assignment silent; defaulted to POST
    // policy) ---

    @Test
    @WithMockUser(roles = "ADMIN")
    public void updateCustomer_adminRole_returnsOk() throws Exception {
        Customer customer = customerRepository.save(new Customer("Bruce", "Banner"));
        mockMvc.perform(put("/customers/" + customer.getId())
                .content(CUSTOMER_JSON)
                .contentType("application/json"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    public void updateCustomer_userRole_returnsForbidden() throws Exception {
        Customer customer = customerRepository.save(new Customer("Bruce", "Banner"));
        mockMvc.perform(put("/customers/" + customer.getId())
                .content(CUSTOMER_JSON)
                .contentType("application/json"))
                .andExpect(status().isForbidden());
    }

    // --- R13: DELETE requires ADMIN ---

    @Test
    @WithMockUser(roles = "ADMIN")
    public void deleteCustomer_adminRole_returnsOk() throws Exception {
        Customer customer = customerRepository.save(new Customer("Bruce", "Banner"));
        mockMvc.perform(delete("/customers/" + customer.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    public void deleteCustomer_userRole_returnsForbidden() throws Exception {
        Customer customer = customerRepository.save(new Customer("Bruce", "Banner"));
        mockMvc.perform(delete("/customers/" + customer.getId()))
                .andExpect(status().isForbidden());
    }
}
