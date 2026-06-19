package com.ithub.online_learning.controller.web;

import com.ithub.online_learning.controller.web.support.WebPaths;
import com.ithub.online_learning.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(WebPaths.APP_COURSES)
@RequiredArgsConstructor
public class CourseWebController {

    private final CourseService courseService;

    @GetMapping
    public String list(@PageableDefault(size = 9) @ParameterObject Pageable pageable, Model model) {
        model.addAttribute("courses", courseService.findPublished(pageable));
        return "courses/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("course", courseService.findDetailById(id));
        return "courses/detail";
    }
}
