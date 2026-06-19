package com.ithub.online_learning.service;

import com.ithub.online_learning.dto.request.RegisterRequest;
import com.ithub.online_learning.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserResponse register(RegisterRequest request);

    UserResponse findById(Long id);

    UserResponse findByUsername(String username);

    Page<UserResponse> findAll(Pageable pageable);
}
