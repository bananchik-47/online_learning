package com.ithub.online_learning.mapper;

import com.ithub.online_learning.dto.request.CourseModuleCreateRequest;
import com.ithub.online_learning.dto.request.CourseModuleUpdateRequest;
import com.ithub.online_learning.dto.response.CourseModuleResponse;
import com.ithub.online_learning.entity.CourseModule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", uses = LessonMapper.class)
public interface CourseModuleMapper {

    @Mapping(source = "course.id", target = "courseId")
    @Mapping(source = "lessons", target = "lessons")
    CourseModuleResponse toResponse(CourseModule courseModule);

    List<CourseModuleResponse> toResponseList(List<CourseModule> courseModules);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "lessons", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    CourseModule toEntity(CourseModuleCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "lessons", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(CourseModuleUpdateRequest request, @MappingTarget CourseModule courseModule);
}
