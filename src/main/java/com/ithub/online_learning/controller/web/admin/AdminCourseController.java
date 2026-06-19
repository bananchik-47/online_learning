package com.ithub.online_learning.controller.web.admin;

import com.ithub.online_learning.dto.request.CourseCreateRequest;
import com.ithub.online_learning.dto.request.CourseUpdateRequest;
import com.ithub.online_learning.controller.web.support.AdminFormMapper;
import com.ithub.online_learning.controller.web.support.WebAttributes;
import com.ithub.online_learning.controller.web.support.WebFormSupport;
import com.ithub.online_learning.controller.web.support.WebPaths;
import com.ithub.online_learning.security.CustomUserDetails;
import com.ithub.online_learning.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping(WebPaths.ADMIN_COURSES)
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminCourseController {

    private static final String VIEW_LIST = "admin/courses/list";
    private static final String VIEW_FORM = "admin/courses/form";

    private final CourseService courseService;
    private final WebFormSupport formSupport;
    private final AdminFormMapper formMapper;

    @GetMapping
    public String list(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("courses", courseService.findByInstructor(userDetails.getId()));
        return VIEW_LIST;
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        formSupport.initForm(model, "courseForm", CourseCreateRequest::new);
        model.addAttribute(WebAttributes.EDIT_MODE, false);
        return VIEW_FORM;
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("courseForm") CourseCreateRequest courseForm,
                         BindingResult bindingResult,
                         @AuthenticationPrincipal CustomUserDetails userDetails,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        model.addAttribute(WebAttributes.EDIT_MODE, false);
        String validationView = formSupport.validationView(bindingResult, VIEW_FORM);
        if (validationView != null) {
            return validationView;
        }

        return formSupport.execute(
                () -> courseService.create(courseForm, userDetails.getId()),
                redirectAttributes,
                "Course created successfully.",
                WebPaths.ADMIN_COURSES,
                model,
                VIEW_FORM
        );
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("courseId", id);
        model.addAttribute("courseForm", formMapper.toCourseUpdateRequest(courseService.findById(id)));
        model.addAttribute(WebAttributes.EDIT_MODE, true);
        return VIEW_FORM;
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("courseForm") CourseUpdateRequest courseForm,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        model.addAttribute("courseId", id);
        model.addAttribute(WebAttributes.EDIT_MODE, true);

        String validationView = formSupport.validationView(bindingResult, VIEW_FORM);
        if (validationView != null) {
            return validationView;
        }

        return formSupport.execute(
                () -> courseService.update(id, courseForm),
                redirectAttributes,
                "Course updated successfully.",
                WebPaths.ADMIN_COURSES,
                model,
                VIEW_FORM
        );
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        courseService.delete(id);
        redirectAttributes.addFlashAttribute(WebAttributes.SUCCESS, "Course deleted successfully.");
        return "redirect:" + WebPaths.ADMIN_COURSES;
    }
}
