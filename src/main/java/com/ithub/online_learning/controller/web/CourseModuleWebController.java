package com.ithub.online_learning.controller.web;

import com.ithub.online_learning.controller.web.support.LessonNavigationContext;
import com.ithub.online_learning.controller.web.support.WebPaths;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(WebPaths.APP + "/modules")
@RequiredArgsConstructor
public class CourseModuleWebController {

    private final LessonNavigationContext navigationContext;

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        navigationContext.addModuleContext(id, model);
        return "modules/view";
    }
}
