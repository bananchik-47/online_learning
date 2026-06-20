package com.ithub.online_learning.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ithub.online_learning.dto.request.SubmissionGradeRequest;
import com.ithub.online_learning.dto.request.SubmissionRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SubmissionFlowIntegrationTest {

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

    private String adminUsername;
    private String studentUsername;
    private Long assignmentId;

    @BeforeEach
    void setUp() throws Exception {
        String suffix = IntegrationTestSupport.uniqueId();
        adminUsername = "admin_submit_" + suffix;
        studentUsername = "student_submit_" + suffix;

        IntegrationTestSupport.createUser(userRepository, roleRepository, passwordEncoder, adminUsername, "ROLE_ADMIN");
        IntegrationTestSupport.createUser(userRepository, roleRepository, passwordEncoder, studentUsername, "ROLE_STUDENT");

        IntegrationTestSupport.CourseHierarchyIds hierarchy = IntegrationTestSupport.createPublishedCourseHierarchy(
                mockMvc, objectMapper, adminUsername);
        assignmentId = hierarchy.assignmentId();
    }

    @Test
    void submitAndGrade_fullFlow_updatesSubmissionStatus() throws Exception {
        SubmissionRequest submitRequest = SubmissionRequest.builder()
                .content("My solution")
                .build();

        String submitResponse = mockMvc.perform(post("/submissions/assignments/{assignmentId}", assignmentId)
                        .with(httpBasic(studentUsername, "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submitRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.content").value("My solution"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long submissionId = objectMapper.readTree(submitResponse).get("id").asLong();

        mockMvc.perform(get("/submissions/pending").with(httpBasic(adminUsername, "password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + submissionId + ")].status").value("SUBMITTED"));

        SubmissionGradeRequest gradeRequest = SubmissionGradeRequest.builder()
                .score(95)
                .feedback("Excellent work")
                .build();

        mockMvc.perform(put("/submissions/{id}/grade", submissionId)
                        .with(httpBasic(adminUsername, "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gradeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("GRADED"))
                .andExpect(jsonPath("$.score").value(95))
                .andExpect(jsonPath("$.feedback").value("Excellent work"));

        mockMvc.perform(get("/submissions/pending").with(httpBasic(adminUsername, "password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + submissionId + ")]").isEmpty());
    }

    @Test
    void submit_duplicateSubmission_returnsBadRequest() throws Exception {
        SubmissionRequest request = SubmissionRequest.builder().content("First attempt").build();

        mockMvc.perform(post("/submissions/assignments/{assignmentId}", assignmentId)
                        .with(httpBasic(studentUsername, "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/submissions/assignments/{assignmentId}", assignmentId)
                        .with(httpBasic(studentUsername, "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad request"));
    }

    @Test
    void updateSubmission_resetsGradeAndResubmits() throws Exception {
        SubmissionRequest initial = SubmissionRequest.builder().content("Draft answer").build();

        String submitResponse = mockMvc.perform(post("/submissions/assignments/{assignmentId}", assignmentId)
                        .with(httpBasic(studentUsername, "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(initial)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long submissionId = objectMapper.readTree(submitResponse).get("id").asLong();

        SubmissionGradeRequest gradeRequest = SubmissionGradeRequest.builder()
                .score(70)
                .feedback("Needs improvement")
                .build();

        mockMvc.perform(put("/submissions/{id}/grade", submissionId)
                        .with(httpBasic(adminUsername, "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gradeRequest)))
                .andExpect(status().isOk());

        SubmissionRequest updated = SubmissionRequest.builder().content("Improved answer").build();

        mockMvc.perform(put("/submissions/assignments/{assignmentId}", assignmentId)
                        .with(httpBasic(studentUsername, "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Improved answer"))
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.score").doesNotExist());
    }
}
