package com.ithub.online_learning.mapper;

import com.ithub.online_learning.dto.request.AssignmentCreateRequest;
import com.ithub.online_learning.dto.request.AssignmentUpdateRequest;
import com.ithub.online_learning.dto.response.AssignmentResponse;
import com.ithub.online_learning.entity.Assignment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AssignmentMapper {

    @Mapping(source = "lesson.id", target = "lessonId")
    AssignmentResponse toResponse(Assignment assignment);

    List<AssignmentResponse> toResponseList(List<Assignment> assignments);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lesson", ignore = true)
    @Mapping(target = "submissions", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Assignment toEntity(AssignmentCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lesson", ignore = true)
    @Mapping(target = "submissions", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(AssignmentUpdateRequest request, @MappingTarget Assignment assignment);
}
