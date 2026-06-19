package com.ithub.online_learning.controller.web.admin;

import com.ithub.online_learning.dto.request.CourseModuleCreateRequest;
import com.ithub.online_learning.dto.request.CourseModuleUpdateRequest;
import com.ithub.online_learning.controller.web.support.AdminFormMapper;
import com.ithub.online_learning.controller.web.support.WebAttributes;
import com.ithub.online_learning.controller.web.support.WebFormSupport;
import com.ithub.online_learning.controller.web.support.WebPaths;
import com.ithub.online_learning.service.CourseModuleService;
import com.ithub.online_learning.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping(WebPaths.ADMIN_COURSES + "/{courseId}/modules")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminCourseModuleController {

    private static final String VIEW_LIST = "admin/modules/list";
    private static final String VIEW_FORM = "admin/modules/form";

    private final CourseModuleService courseModuleService;
    private final CourseService courseService;
    private final WebFormSupport formSupport;
    private final AdminFormMapper formMapper;

    @GetMapping
    public String list(@PathVariable Long courseId, Model model) {
        model.addAttribute("course", courseService.findById(courseId));
        model.addAttribute("modules", courseModuleService.findByCourseId(courseId));
        return VIEW_LIST;
    }

    @GetMapping("/new")
    public String createForm(@PathVariable Long courseId, Model model) {
        model.addAttribute("course", courseService.findById(courseId));
        formSupport.initForm(model, "moduleForm", CourseModuleCreateRequest::new);
        model.addAttribute(WebAttributes.EDIT_MODE, false);
        return VIEW_FORM;
    }

    @PostMapping
    public String create(@PathVariable Long courseId,
                         @Valid @ModelAttribute("moduleForm") CourseModuleCreateRequest moduleForm,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        model.addAttribute("course", courseService.findById(courseId));
        model.addAttribute(WebAttributes.EDIT_MODE, false);

        String validationView = formSupport.validationView(bindingResult, VIEW_FORM);
        if (validationView != null) {
            return validationView;
        }

        return formSupport.execute(
                () -> courseModuleService.create(courseId, moduleForm),
                redirectAttributes,
                "Module created successfully.",
                WebPaths.adminCourseModules(courseId),
                model,
                VIEW_FORM
        );
    }

    @GetMapping("/{moduleId}/edit")
    public String editForm(@PathVariable Long courseId, @PathVariable Long moduleId, Model model) {
        model.addAttribute("course", courseService.findById(courseId));
        model.addAttribute("moduleId", moduleId);
        model.addAttribute("moduleForm", formMapper.toCourseModuleUpdateRequest(courseModuleService.findById(moduleId)));
        model.addAttribute(WebAttributes.EDIT_MODE, true);
        return VIEW_FORM;
    }

    @PostMapping("/{moduleId}/edit")
    public String update(@PathVariable Long courseId,
                         @PathVariable Long moduleId,
                         @Valid @ModelAttribute("moduleForm") CourseModuleUpdateRequest moduleForm,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        model.addAttribute("course", courseService.findById(courseId));
        model.addAttribute("moduleId", moduleId);
        model.addAttribute(WebAttributes.EDIT_MODE, true);

        String validationView = formSupport.validationView(bindingResult, VIEW_FORM);
        if (validationView != null) {
            return validationView;
        }

        return formSupport.execute(
                () -> courseModuleService.update(moduleId, moduleForm),
                redirectAttributes,
                "Module updated successfully.",
                WebPaths.adminCourseModules(courseId),
                model,
                VIEW_FORM
        );
    }

    @PostMapping("/{moduleId}/delete")
    public String delete(@PathVariable Long courseId,
                         @PathVariable Long moduleId,
                         RedirectAttributes redirectAttributes) {
        courseModuleService.delete(moduleId);
        redirectAttributes.addFlashAttribute(WebAttributes.SUCCESS, "Module deleted successfully.");
        return "redirect:" + WebPaths.adminCourseModules(courseId);
    }
}
