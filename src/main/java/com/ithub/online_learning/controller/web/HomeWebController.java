package com.ithub.online_learning.controller.web;

import com.ithub.online_learning.security.CustomUserDetails;
import com.ithub.online_learning.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeWebController {

    private final CourseService courseService;

    @GetMapping("/")
    public String home(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails != null) {
            model.addAttribute("featuredCourses", courseService.findPublished(PageRequest.of(0, 3)));
        }
        return "index";
    }
}
