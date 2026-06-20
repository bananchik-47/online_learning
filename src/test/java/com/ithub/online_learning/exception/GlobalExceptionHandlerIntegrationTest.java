package com.ithub.online_learning.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ithub.online_learning.repository.RoleRepository;
import com.ithub.online_learning.repository.UserRepository;
import com.ithub.online_learning.support.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GlobalExceptionHandlerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        IntegrationTestSupport.ensureRole(roleRepository, "ROLE_STUDENT");
    }

    @Test
    void resourceNotFound_returnsProblemDetail404() throws Exception {
        String studentUsername = "student_ex_" + IntegrationTestSupport.uniqueId();
        IntegrationTestSupport.createUser(
                userRepository, roleRepository, passwordEncoder, studentUsername, "ROLE_STUDENT");

        mockMvc.perform(get("/users/{id}", 999999L).with(httpBasic(studentUsername, "password")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"))
                .andExpect(jsonPath("$.detail").value("User not found: 999999"));
    }

    @Test
    void validationError_returnsProblemDetail400() throws Exception {
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "",
                                  "email": "invalid-email",
                                  "password": "123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    void badRequest_returnsProblemDetail400() throws Exception {
        String username = "student_dup_" + IntegrationTestSupport.uniqueId();

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                com.ithub.online_learning.dto.request.RegisterRequest.builder()
                                        .username(username)
                                        .email("first_" + IntegrationTestSupport.uniqueId() + "@test.com")
                                        .password("password123")
                                        .build())))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                com.ithub.online_learning.dto.request.RegisterRequest.builder()
                                        .username(username)
                                        .email("second_" + IntegrationTestSupport.uniqueId() + "@test.com")
                                        .password("password123")
                                        .build())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad request"))
                .andExpect(jsonPath("$.detail").value("Username already exists: " + username));
    }
}
