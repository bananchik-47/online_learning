package com.ithub.online_learning.controller.web.admin;

import com.ithub.online_learning.dto.request.LessonCreateRequest;
import com.ithub.online_learning.dto.request.LessonUpdateRequest;
import com.ithub.online_learning.controller.web.support.AdminFormMapper;
import com.ithub.online_learning.controller.web.support.LessonNavigationContext;
import com.ithub.online_learning.controller.web.support.WebAttributes;
import com.ithub.online_learning.controller.web.support.WebFormSupport;
import com.ithub.online_learning.controller.web.support.WebPaths;
import com.ithub.online_learning.service.LessonService;
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
@RequestMapping(WebPaths.APP + "/admin/modules/{moduleId}/lessons")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminLessonController {

    private static final String VIEW_LIST = "admin/lessons/list";
    private static final String VIEW_FORM = "admin/lessons/form";

    private final LessonService lessonService;
    private final LessonNavigationContext navigationContext;
    private final WebFormSupport formSupport;
    private final AdminFormMapper formMapper;

    @GetMapping
    public String list(@PathVariable Long moduleId, Model model) {
        navigationContext.addModuleContext(moduleId, model);
        model.addAttribute("lessons", lessonService.findByModuleId(moduleId));
        return VIEW_LIST;
    }

    @GetMapping("/new")
    public String createForm(@PathVariable Long moduleId, Model model) {
        navigationContext.addModuleContext(moduleId, model);
        formSupport.initForm(model, "lessonForm", LessonCreateRequest::new);
        model.addAttribute(WebAttributes.EDIT_MODE, false);
        return VIEW_FORM;
    }

    @PostMapping
    public String create(@PathVariable Long moduleId,
                         @Valid @ModelAttribute("lessonForm") LessonCreateRequest lessonForm,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        navigationContext.addModuleContext(moduleId, model);
        model.addAttribute(WebAttributes.EDIT_MODE, false);

        String validationView = formSupport.validationView(bindingResult, VIEW_FORM);
        if (validationView != null) {
            return validationView;
        }

        return formSupport.execute(
                () -> lessonService.create(moduleId, lessonForm),
                redirectAttributes,
                "Lesson created successfully.",
                WebPaths.adminModuleLessons(moduleId),
                model,
                VIEW_FORM
        );
    }

    @GetMapping("/{lessonId}/edit")
    public String editForm(@PathVariable Long moduleId, @PathVariable Long lessonId, Model model) {
        navigationContext.addModuleContext(moduleId, model);
        model.addAttribute("lessonId", lessonId);
        model.addAttribute("lessonForm", formMapper.toLessonUpdateRequest(lessonService.findById(lessonId)));
        model.addAttribute(WebAttributes.EDIT_MODE, true);
        return VIEW_FORM;
    }

    @PostMapping("/{lessonId}/edit")
    public String update(@PathVariable Long moduleId,
                         @PathVariable Long lessonId,
                         @Valid @ModelAttribute("lessonForm") LessonUpdateRequest lessonForm,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        navigationContext.addModuleContext(moduleId, model);
        model.addAttribute("lessonId", lessonId);
        model.addAttribute(WebAttributes.EDIT_MODE, true);

        String validationView = formSupport.validationView(bindingResult, VIEW_FORM);
        if (validationView != null) {
            return validationView;
        }

        return formSupport.execute(
                () -> lessonService.update(lessonId, lessonForm),
                redirectAttributes,
                "Lesson updated successfully.",
                WebPaths.adminModuleLessons(moduleId),
                model,
                VIEW_FORM
        );
    }

    @PostMapping("/{lessonId}/delete")
    public String delete(@PathVariable Long moduleId,
                         @PathVariable Long lessonId,
                         RedirectAttributes redirectAttributes) {
        lessonService.delete(lessonId);
        redirectAttributes.addFlashAttribute(WebAttributes.SUCCESS, "Lesson deleted successfully.");
        return "redirect:" + WebPaths.adminModuleLessons(moduleId);
    }
}
