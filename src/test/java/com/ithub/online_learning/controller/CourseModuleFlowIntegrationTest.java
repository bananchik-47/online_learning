package com.ithub.online_learning.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ithub.online_learning.dto.request.CourseCreateRequest;
import com.ithub.online_learning.dto.request.CourseModuleCreateRequest;
import com.ithub.online_learning.entity.Role;
import com.ithub.online_learning.entity.User;
import com.ithub.online_learning.repository.RoleRepository;
import com.ithub.online_learning.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CourseModuleFlowIntegrationTest {

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

    private Long courseId;

    @BeforeEach
    void setUp() throws Exception {
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_ADMIN").build()));

        User admin = userRepository.save(User.builder()
                .username("admin_flow")
                .email("admin_flow@test.com")
                .password(passwordEncoder.encode("password"))
                .enabled(true)
                .role(adminRole)
                .build());

        CourseCreateRequest courseRequest = CourseCreateRequest.builder()
                .title("Spring Course")
                .description("Test")
                .isPublished(true)
                .build();

        String courseResponse = mockMvc.perform(post("/courses")
                        .with(httpBasic("admin_flow", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(courseRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        courseId = objectMapper.readTree(courseResponse).get("id").asLong();
    }

    @Test
    void createModule_thenListModulesByCourse_returnsCreatedModule() throws Exception {
        CourseModuleCreateRequest moduleRequest = CourseModuleCreateRequest.builder()
                .title("Module 1")
                .description("Intro")
                .orderIndex(0)
                .build();

        mockMvc.perform(post("/courses/{courseId}/modules", courseId)
                        .with(httpBasic("admin_flow", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(moduleRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.courseId").value(courseId))
                .andExpect(jsonPath("$.title").value("Module 1"));

        mockMvc.perform(get("/courses/{courseId}/modules", courseId)
                        .with(httpBasic("admin_flow", "password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Module 1"))
                .andExpect(jsonPath("$[0].courseId").value(courseId));
    }
}
