package com.ithub.online_learning.mapper;

import com.ithub.online_learning.dto.request.UserProgressRequest;
import com.ithub.online_learning.dto.response.UserProgressResponse;
import com.ithub.online_learning.entity.UserProgress;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserProgressMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "lesson.id", target = "lessonId")
    @Mapping(source = "lesson.title", target = "lessonTitle")
    UserProgressResponse toResponse(UserProgress userProgress);

    List<UserProgressResponse> toResponseList(List<UserProgress> progressRecords);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "lesson", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateFromRequest(UserProgressRequest request, @MappingTarget UserProgress userProgress);
}
