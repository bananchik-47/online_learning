package com.ithub.online_learning.controller;

import com.ithub.online_learning.repository.RoleRepository;
import com.ithub.online_learning.repository.UserRepository;
import com.ithub.online_learning.support.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminUsername;
    private String studentUsername;

    @BeforeEach
    void setUp() {
        String suffix = IntegrationTestSupport.uniqueId();
        adminUsername = "admin_sec_" + suffix;
        studentUsername = "student_sec_" + suffix;

        IntegrationTestSupport.createUser(userRepository, roleRepository, passwordEncoder, adminUsername, "ROLE_ADMIN");
        IntegrationTestSupport.createUser(userRepository, roleRepository, passwordEncoder, studentUsername, "ROLE_STUDENT");
    }

    @Test
    void publicEndpoints_areAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/register"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }

    @Test
    void protectedApi_withoutAuth_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/courses"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminPanel_studentUser_returnsForbidden() throws Exception {
        mockMvc.perform(get("/app/admin").with(httpBasic(studentUsername, "password")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminPanel_adminUser_returnsOk() throws Exception {
        mockMvc.perform(get("/app/admin").with(httpBasic(adminUsername, "password")))
                .andExpect(status().isOk());
    }

    @Test
    void createCourse_studentUser_returnsForbidden() throws Exception {
        mockMvc.perform(post("/courses")
                        .with(httpBasic(studentUsername, "password"))
                        .contentType("application/json")
                        .content("""
                                {"title":"Forbidden","description":"Test","isPublished":true}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void enrollments_studentOnly_adminGetsForbidden() throws Exception {
        mockMvc.perform(get("/enrollments/me").with(httpBasic(adminUsername, "password")))
                .andExpect(status().isForbidden());
    }
}
