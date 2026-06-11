package com.ithub.online_learning.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProgressResponse {

    private Long id;
    private Long userId;
    private Long lessonId;
    private String lessonTitle;
    private Boolean completed;
    private LocalDateTime completedAt;
    private LocalDateTime updatedAt;
}
