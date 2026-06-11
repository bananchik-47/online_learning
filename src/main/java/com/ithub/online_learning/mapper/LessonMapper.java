package com.ithub.online_learning.mapper;

import com.ithub.online_learning.dto.request.LessonCreateRequest;
import com.ithub.online_learning.dto.request.LessonUpdateRequest;
import com.ithub.online_learning.dto.response.LessonResponse;
import com.ithub.online_learning.dto.response.LessonSummaryResponse;
import com.ithub.online_learning.entity.Lesson;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", uses = {AssignmentMapper.class, UploadedFileMapper.class})
public interface LessonMapper {

    @Mapping(source = "module.id", target = "moduleId")
    @Mapping(source = "assignments", target = "assignments")
    @Mapping(source = "uploadedFiles", target = "files")
    LessonResponse toResponse(Lesson lesson);

    LessonSummaryResponse toSummaryResponse(Lesson lesson);

    List<LessonSummaryResponse> toSummaryResponseList(List<Lesson> lessons);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "module", ignore = true)
    @Mapping(target = "assignments", ignore = true)
    @Mapping(target = "progressRecords", ignore = true)
    @Mapping(target = "uploadedFiles", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Lesson toEntity(LessonCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "module", ignore = true)
    @Mapping(target = "assignments", ignore = true)
    @Mapping(target = "progressRecords", ignore = true)
    @Mapping(target = "uploadedFiles", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(LessonUpdateRequest request, @MappingTarget Lesson lesson);
}
