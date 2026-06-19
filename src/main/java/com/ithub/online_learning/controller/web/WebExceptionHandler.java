package com.ithub.online_learning.controller.web;

import com.ithub.online_learning.exception.BadRequestException;
import com.ithub.online_learning.exception.ResourceNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.ui.Model;

@ControllerAdvice(basePackages = "com.ithub.online_learning.controller.web")
public class WebExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleNotFound(ResourceNotFoundException exception, Model model) {
        model.addAttribute("error", exception.getMessage());
        return "error/not-found";
    }

    @ExceptionHandler(BadRequestException.class)
    public String handleBadRequest(BadRequestException exception, Model model) {
        model.addAttribute("error", exception.getMessage());
        return "error/bad-request";
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public String handleValidation(MethodArgumentNotValidException exception, Model model) {
        model.addAttribute("error", "Validation failed. Please check the form and try again.");
        return "error/bad-request";
    }
}
