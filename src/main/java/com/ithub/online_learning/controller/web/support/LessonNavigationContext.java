package com.ithub.online_learning.controller.web.support;

import com.ithub.online_learning.dto.response.LessonResponse;
import com.ithub.online_learning.service.CourseModuleService;
import com.ithub.online_learning.service.CourseService;
import com.ithub.online_learning.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

@Component
@RequiredArgsConstructor
public class LessonNavigationContext {

    private final LessonService lessonService;
    private final CourseModuleService courseModuleService;
    private final CourseService courseService;

    public void addModuleContext(Long moduleId, Model model) {
        var module = courseModuleService.findById(moduleId);
        model.addAttribute("module", module);
        model.addAttribute("course", courseService.findById(module.getCourseId()));
    }

    public void addLessonContext(Long lessonId, Model model) {
        addLessonContext(lessonService.findById(lessonId), model);
    }

    public void addLessonContext(LessonResponse lesson, Model model) {
        var module = courseModuleService.findById(lesson.getModuleId());
        model.addAttribute("lesson", lesson);
        model.addAttribute("module", module);
        model.addAttribute("course", courseService.findById(module.getCourseId()));
    }
}
