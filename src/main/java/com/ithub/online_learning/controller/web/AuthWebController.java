package com.ithub.online_learning.controller.web;

import com.ithub.online_learning.dto.request.RegisterRequest;
import com.ithub.online_learning.controller.web.support.WebAttributes;
import com.ithub.online_learning.controller.web.support.WebFormSupport;
import com.ithub.online_learning.controller.web.support.WebPaths;
import com.ithub.online_learning.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthWebController {

    private static final String VIEW_REGISTER = "register";

    private final UserService userService;
    private final WebFormSupport formSupport;

    @GetMapping(WebPaths.LOGIN)
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        formSupport.initForm(model, "registerRequest", RegisterRequest::new);
        return VIEW_REGISTER;
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String register(@Valid @ModelAttribute("registerRequest") RegisterRequest registerRequest,
                           BindingResult bindingResult,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        String validationView = formSupport.validationView(bindingResult, VIEW_REGISTER);
        if (validationView != null) {
            return validationView;
        }

        return formSupport.execute(
                () -> userService.register(registerRequest),
                redirectAttributes,
                "Registration successful. Please log in.",
                WebPaths.LOGIN,
                model,
                VIEW_REGISTER
        );
    }
}
