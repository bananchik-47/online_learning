package com.ithub.online_learning.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseModuleResponse {

    private Long id;
    private Long courseId;
    private String title;
    private String description;
    private Integer orderIndex;
    private List<LessonSummaryResponse> lessons;
}
