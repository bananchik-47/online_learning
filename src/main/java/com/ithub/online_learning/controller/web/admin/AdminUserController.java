package com.ithub.online_learning.controller.web.admin;

import com.ithub.online_learning.dto.request.RegisterRequest;
import com.ithub.online_learning.controller.web.support.WebAttributes;
import com.ithub.online_learning.controller.web.support.WebFormSupport;
import com.ithub.online_learning.controller.web.support.WebPaths;
import com.ithub.online_learning.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
@RequestMapping(WebPaths.ADMIN_USERS)
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminUserController {

    private static final String VIEW_LIST = "admin/users/list";
    private static final String VIEW_FORM = "admin/users/form";
    private static final String VIEW_DETAIL = "admin/users/view";

    private final UserService userService;
    private final WebFormSupport formSupport;

    @GetMapping
    public String list(@PageableDefault(size = 20) @ParameterObject Pageable pageable, Model model) {
        model.addAttribute("users", userService.findAll(pageable));
        return VIEW_LIST;
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        formSupport.initForm(model, "registerRequest", RegisterRequest::new);
        return VIEW_FORM;
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("registerRequest") RegisterRequest registerRequest,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        String validationView = formSupport.validationView(bindingResult, VIEW_FORM);
        if (validationView != null) {
            return validationView;
        }

        return formSupport.execute(
                () -> userService.register(registerRequest),
                redirectAttributes,
                "User created successfully.",
                WebPaths.ADMIN_USERS,
                model,
                VIEW_FORM
        );
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        model.addAttribute("user", userService.findById(id));
        return VIEW_DETAIL;
    }
}
