package sg.edu.ntu.simple_crm_api.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import sg.edu.ntu.simple_crm_api.model.Customer;
import sg.edu.ntu.simple_crm_api.repository.CustomerRepository;
import sg.edu.ntu.simple_crm_api.security.JwtService;

@SpringBootTest(properties = {
                "app.auth.username=configured-user",
                "app.auth.password=configured-password",
                "app.auth.manager.username=configured-manager",
                "app.auth.manager.password=configured-manager-password",
                "app.auth.admin.username=configured-admin",
                "app.auth.admin.password=configured-admin-password"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ConfiguredBasicAuthTest {

        private static final String CUSTOMER_JSON = "{\"firstName\":\"Tony\",\"lastName\":\"Stark\","
                        + "\"email\":\"tony@starkindustries.com\",\"contactNo\":\"12345678\","
                        + "\"jobTitle\":\"Engineer\",\"yearOfBirth\":1970}";

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private JwtService jwtService;

        @Autowired
        private CustomerRepository customerRepository;

        @Test
        void configuredAccountsEnforceCustomerRoleMatrix() throws Exception {
                String[] usernames = { "configured-user", "configured-manager", "configured-admin" };
                String[] passwords = { "configured-password", "configured-manager-password",
                                "configured-admin-password" };
                for (int i = 0; i < usernames.length; i++) {
                        long id = customerRepository.save(new Customer("Bruce", "Banner")).getId();
                        var credentials = httpBasic(usernames[i], passwords[i]);
                        mockMvc.perform(get("/customers").with(credentials)).andExpect(status().isOk());
                        mockMvc.perform(get("/customers/{id}", id).with(credentials)).andExpect(status().isOk());
                        mockMvc.perform(post("/customers").with(credentials).contentType("application/json")
                                        .content(CUSTOMER_JSON))
                                        .andExpect(status().is(i == 2 ? 201 : 403));
                        mockMvc.perform(put("/customers/{id}", id).with(credentials).contentType("application/json")
                                        .content(CUSTOMER_JSON))
                                        .andExpect(status().is(i == 0 ? 403 : 200));
                        mockMvc.perform(delete("/customers/{id}", id).with(credentials))
                                        .andExpect(status().is(i == 2 ? 200 : 403));
                }
        }

        @Test
        void managerTokenMapsToManagerRole() throws Exception {
                long id = customerRepository.save(new Customer("Bruce", "Banner")).getId();
                String token = jwtService.generateToken("configured-manager");
                mockMvc.perform(put("/customers/{id}", id).header("Authorization", "Bearer " + token)
                                .contentType("application/json").content(CUSTOMER_JSON))
                                .andExpect(status().isOk());
                mockMvc.perform(delete("/customers/{id}", id).header("Authorization", "Bearer " + token))
                                .andExpect(status().isForbidden());
        }

        @Test
        void configuredUserCanReadButCannotWrite() throws Exception {
                mockMvc.perform(get("/customers").with(httpBasic("configured-user", "configured-password")))
                                .andExpect(status().isOk());
                mockMvc.perform(post("/customers").with(httpBasic("configured-user", "configured-password")))
                                .andExpect(status().isForbidden());
        }

        @Test
        void configuredAdminHasAdminRole() throws Exception {
                mockMvc.perform(post("/customers").with(httpBasic("configured-admin", "configured-admin-password")))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void oldAndIncorrectCredentialsAreRejected() throws Exception {
                mockMvc.perform(get("/customers").with(httpBasic("user", "user123")))
                                .andExpect(status().isUnauthorized());
                mockMvc.perform(get("/customers").with(httpBasic("configured-user", "wrong")))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void configuredAdminTokenMapsToAdminRole() throws Exception {
                String token = jwtService.generateToken("configured-admin");
                mockMvc.perform(post("/customers").header("Authorization", "Bearer " + token))
                                .andExpect(status().isBadRequest());
        }
}
