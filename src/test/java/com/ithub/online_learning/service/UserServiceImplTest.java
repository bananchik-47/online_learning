package com.ithub.online_learning.service;

import com.ithub.online_learning.dto.request.RegisterRequest;
import com.ithub.online_learning.dto.response.UserResponse;
import com.ithub.online_learning.entity.Role;
import com.ithub.online_learning.entity.User;
import com.ithub.online_learning.exception.BadRequestException;
import com.ithub.online_learning.exception.ResourceNotFoundException;
import com.ithub.online_learning.mapper.UserMapper;
import com.ithub.online_learning.repository.RoleRepository;
import com.ithub.online_learning.repository.UserRepository;
import com.ithub.online_learning.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void register_createsStudentWithEncodedPassword() {
        RegisterRequest request = RegisterRequest.builder()
                .username("student1")
                .email("student1@test.com")
                .password("password123")
                .firstName("Test")
                .lastName("User")
                .build();

        Role studentRole = Role.builder().id(2L).name("ROLE_STUDENT").build();
        User user = User.builder().username("student1").email("student1@test.com").build();
        User savedUser = User.builder().id(1L).username("student1").role(studentRole).enabled(true).build();
        UserResponse response = UserResponse.builder().id(1L).username("student1").role("ROLE_STUDENT").build();

        when(userRepository.existsByUsername("student1")).thenReturn(false);
        when(userRepository.existsByEmail("student1@test.com")).thenReturn(false);
        when(roleRepository.findByName("ROLE_STUDENT")).thenReturn(Optional.of(studentRole));
        when(userMapper.toEntity(request)).thenReturn(user);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.save(user)).thenReturn(savedUser);
        when(userMapper.toResponse(savedUser)).thenReturn(response);

        UserResponse result = userService.register(request);

        assertThat(result.getUsername()).isEqualTo("student1");
        assertThat(result.getRole()).isEqualTo("ROLE_STUDENT");
        assertThat(user.getPassword()).isEqualTo("encoded-password");
        assertThat(user.getRole()).isEqualTo(studentRole);
        assertThat(user.getEnabled()).isTrue();
    }

    @Test
    void register_duplicateUsername_throwsBadRequest() {
        RegisterRequest request = RegisterRequest.builder()
                .username("existing")
                .email("new@test.com")
                .password("password123")
                .build();

        when(userRepository.existsByUsername("existing")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Username already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_duplicateEmail_throwsBadRequest() {
        RegisterRequest request = RegisterRequest.builder()
                .username("newuser")
                .email("existing@test.com")
                .password("password123")
                .build();

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test
    void register_missingStudentRole_throwsNotFound() {
        RegisterRequest request = RegisterRequest.builder()
                .username("student1")
                .email("student1@test.com")
                .password("password123")
                .build();

        when(userRepository.existsByUsername("student1")).thenReturn(false);
        when(userRepository.existsByEmail("student1@test.com")).thenReturn(false);
        when(roleRepository.findByName("ROLE_STUDENT")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Role not found");
    }

    @Test
    void findByUsername_existingUser_returnsResponse() {
        Role role = Role.builder().name("ROLE_STUDENT").build();
        User user = User.builder().id(1L).username("student1").role(role).build();
        UserResponse response = UserResponse.builder().id(1L).username("student1").build();

        when(userRepository.findByUsername("student1")).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(response);

        UserResponse result = userService.findByUsername("student1");

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void findByUsername_missingUser_throwsNotFound() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findByUsername("missing"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }
}
