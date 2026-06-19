package com.ithub.online_learning.controller.web.support;

import com.ithub.online_learning.exception.BadRequestException;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.function.Supplier;

@Component
public class WebFormSupport {

    public <T> void initForm(Model model, String attributeName, Supplier<T> supplier) {
        if (!model.containsAttribute(attributeName)) {
            model.addAttribute(attributeName, supplier.get());
        }
    }

    public String validationView(BindingResult bindingResult, String viewName) {
        return bindingResult.hasErrors() ? viewName : null;
    }

    public String execute(Runnable action,
                          RedirectAttributes redirectAttributes,
                          String successMessage,
                          String redirectPath,
                          Model model,
                          String errorView) {
        try {
            action.run();
            redirectAttributes.addFlashAttribute(WebAttributes.SUCCESS, successMessage);
            return "redirect:" + redirectPath;
        } catch (BadRequestException exception) {
            model.addAttribute(WebAttributes.ERROR, exception.getMessage());
            return errorView;
        }
    }
}
