package com.ithub.online_learning.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ithub.online_learning.dto.request.RegisterRequest;
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
class UserRegistrationIntegrationTest {

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

    private String suffix;

    @BeforeEach
    void setUp() {
        suffix = IntegrationTestSupport.uniqueId();
        IntegrationTestSupport.ensureRole(roleRepository, "ROLE_STUDENT");
        IntegrationTestSupport.ensureRole(roleRepository, "ROLE_ADMIN");
    }

    @Test
    void register_validRequest_returnsCreatedStudent() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("student_" + suffix)
                .email("student_" + suffix + "@test.com")
                .password("password123")
                .firstName("Test")
                .lastName("Student")
                .build();

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("student_" + suffix))
                .andExpect(jsonPath("$.role").value("ROLE_STUDENT"))
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    void register_duplicateUsername_returnsBadRequest() throws Exception {
        String username = "dup_user_" + suffix;
        RegisterRequest request = RegisterRequest.builder()
                .username(username)
                .email("first_" + suffix + "@test.com")
                .password("password123")
                .build();

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        RegisterRequest duplicate = RegisterRequest.builder()
                .username(username)
                .email("second_" + suffix + "@test.com")
                .password("password123")
                .build();

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad request"));
    }

    @Test
    void register_invalidPayload_returnsValidationError() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("")
                .email("not-an-email")
                .password("123")
                .build();

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"));
    }

    @Test
    void getCurrentUser_authenticatedStudent_returnsProfile() throws Exception {
        String username = "auth_student_" + suffix;
        IntegrationTestSupport.createUser(
                userRepository, roleRepository, passwordEncoder, username, "ROLE_STUDENT");

        mockMvc.perform(get("/users/me").with(httpBasic(username, "password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.role").value("ROLE_STUDENT"));
    }
}
