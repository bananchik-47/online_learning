package com.ithub.online_learning.controller.web.admin;

import com.ithub.online_learning.dto.request.AssignmentCreateRequest;
import com.ithub.online_learning.dto.request.AssignmentUpdateRequest;
import com.ithub.online_learning.controller.web.support.AdminFormMapper;
import com.ithub.online_learning.controller.web.support.LessonNavigationContext;
import com.ithub.online_learning.controller.web.support.WebAttributes;
import com.ithub.online_learning.controller.web.support.WebFormSupport;
import com.ithub.online_learning.controller.web.support.WebPaths;
import com.ithub.online_learning.service.AssignmentService;
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
@RequestMapping(WebPaths.APP + "/admin/lessons/{lessonId}/assignments")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminAssignmentController {

    private static final String VIEW_LIST = "admin/assignments/list";
    private static final String VIEW_FORM = "admin/assignments/form";

    private final AssignmentService assignmentService;
    private final LessonNavigationContext navigationContext;
    private final WebFormSupport formSupport;
    private final AdminFormMapper formMapper;

    @GetMapping
    public String list(@PathVariable Long lessonId, Model model) {
        navigationContext.addLessonContext(lessonId, model);
        model.addAttribute("assignments", assignmentService.findByLessonId(lessonId));
        return VIEW_LIST;
    }

    @GetMapping("/new")
    public String createForm(@PathVariable Long lessonId, Model model) {
        navigationContext.addLessonContext(lessonId, model);
        formSupport.initForm(model, "assignmentForm", AssignmentCreateRequest::new);
        model.addAttribute(WebAttributes.EDIT_MODE, false);
        return VIEW_FORM;
    }

    @PostMapping
    public String create(@PathVariable Long lessonId,
                         @Valid @ModelAttribute("assignmentForm") AssignmentCreateRequest assignmentForm,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        navigationContext.addLessonContext(lessonId, model);
        model.addAttribute(WebAttributes.EDIT_MODE, false);

        String validationView = formSupport.validationView(bindingResult, VIEW_FORM);
        if (validationView != null) {
            return validationView;
        }

        return formSupport.execute(
                () -> assignmentService.create(lessonId, assignmentForm),
                redirectAttributes,
                "Assignment created successfully.",
                WebPaths.adminLessonAssignments(lessonId),
                model,
                VIEW_FORM
        );
    }

    @GetMapping("/{assignmentId}/edit")
    public String editForm(@PathVariable Long lessonId, @PathVariable Long assignmentId, Model model) {
        navigationContext.addLessonContext(lessonId, model);
        model.addAttribute("assignmentId", assignmentId);
        model.addAttribute("assignmentForm", formMapper.toAssignmentUpdateRequest(assignmentService.findById(assignmentId)));
        model.addAttribute(WebAttributes.EDIT_MODE, true);
        return VIEW_FORM;
    }

    @PostMapping("/{assignmentId}/edit")
    public String update(@PathVariable Long lessonId,
                         @PathVariable Long assignmentId,
                         @Valid @ModelAttribute("assignmentForm") AssignmentUpdateRequest assignmentForm,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        navigationContext.addLessonContext(lessonId, model);
        model.addAttribute("assignmentId", assignmentId);
        model.addAttribute(WebAttributes.EDIT_MODE, true);

        String validationView = formSupport.validationView(bindingResult, VIEW_FORM);
        if (validationView != null) {
            return validationView;
        }

        return formSupport.execute(
                () -> assignmentService.update(assignmentId, assignmentForm),
                redirectAttributes,
                "Assignment updated successfully.",
                WebPaths.adminLessonAssignments(lessonId),
                model,
                VIEW_FORM
        );
    }

    @PostMapping("/{assignmentId}/delete")
    public String delete(@PathVariable Long lessonId,
                         @PathVariable Long assignmentId,
                         RedirectAttributes redirectAttributes) {
        assignmentService.delete(assignmentId);
        redirectAttributes.addFlashAttribute(WebAttributes.SUCCESS, "Assignment deleted successfully.");
        return "redirect:" + WebPaths.adminLessonAssignments(lessonId);
    }
}
