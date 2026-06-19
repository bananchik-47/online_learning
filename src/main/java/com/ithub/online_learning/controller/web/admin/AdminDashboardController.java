package com.ithub.online_learning.controller.web.admin;

import com.ithub.online_learning.controller.web.support.WebPaths;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(WebPaths.ADMIN)
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    @GetMapping
    public String dashboard() {
        return "admin/index";
    }
}
