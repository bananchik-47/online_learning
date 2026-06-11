package com.ithub.online_learning.mapper;

import com.ithub.online_learning.dto.request.CourseCreateRequest;
import com.ithub.online_learning.dto.request.CourseUpdateRequest;
import com.ithub.online_learning.dto.response.CourseDetailResponse;
import com.ithub.online_learning.dto.response.CourseResponse;
import com.ithub.online_learning.entity.Course;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", uses = {CourseModuleMapper.class, UserMapper.class})
public interface CourseMapper {

    @Mapping(source = "instructor.id", target = "instructorId")
    @Mapping(source = "instructor", target = "instructorName", qualifiedByName = "fullName")
    CourseResponse toResponse(Course course);

    List<CourseResponse> toResponseList(List<Course> courses);

    @Mapping(source = "instructor.id", target = "instructorId")
    @Mapping(source = "instructor", target = "instructorName", qualifiedByName = "fullName")
    @Mapping(source = "modules", target = "modules")
    CourseDetailResponse toDetailResponse(Course course);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "instructor", ignore = true)
    @Mapping(target = "modules", ignore = true)
    @Mapping(target = "enrollments", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Course toEntity(CourseCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "instructor", ignore = true)
    @Mapping(target = "modules", ignore = true)
    @Mapping(target = "enrollments", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(CourseUpdateRequest request, @MappingTarget Course course);
}
