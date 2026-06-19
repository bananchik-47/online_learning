package com.ithub.online_learning.controller.web.support;

public final class WebPaths {

    public static final String LOGIN = "/login";
    public static final String APP = "/app";
    public static final String ADMIN = APP + "/admin";
    public static final String ADMIN_COURSES = ADMIN + "/courses";
    public static final String ADMIN_USERS = ADMIN + "/users";
    public static final String APP_COURSES = APP + "/courses";

    private WebPaths() {
    }

    public static String adminCourseModules(Long courseId) {
        return ADMIN_COURSES + "/" + courseId + "/modules";
    }

    public static String adminModuleLessons(Long moduleId) {
        return APP + "/admin/modules/" + moduleId + "/lessons";
    }

    public static String adminLessonAssignments(Long lessonId) {
        return APP + "/admin/lessons/" + lessonId + "/assignments";
    }
}
