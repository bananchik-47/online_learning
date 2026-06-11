package com.ithub.online_learning.mapper;

import com.ithub.online_learning.dto.request.SubmissionGradeRequest;
import com.ithub.online_learning.dto.request.SubmissionRequest;
import com.ithub.online_learning.dto.response.SubmissionResponse;
import com.ithub.online_learning.entity.Submission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UploadedFileMapper.class, UserMapper.class})
public interface SubmissionMapper {

    @Mapping(source = "assignment.id", target = "assignmentId")
    @Mapping(source = "assignment.title", target = "assignmentTitle")
    @Mapping(source = "student.id", target = "studentId")
    @Mapping(source = "student", target = "studentName", qualifiedByName = "fullName")
    @Mapping(source = "uploadedFiles", target = "files")
    SubmissionResponse toResponse(Submission submission);

    List<SubmissionResponse> toResponseList(List<Submission> submissions);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "assignment", ignore = true)
    @Mapping(target = "student", ignore = true)
    @Mapping(target = "score", ignore = true)
    @Mapping(target = "feedback", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "submittedAt", ignore = true)
    @Mapping(target = "gradedAt", ignore = true)
    @Mapping(target = "uploadedFiles", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateFromRequest(SubmissionRequest request, @MappingTarget Submission submission);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "assignment", ignore = true)
    @Mapping(target = "student", ignore = true)
    @Mapping(target = "content", ignore = true)
    @Mapping(target = "status", expression = "java(com.ithub.online_learning.entity.enums.SubmissionStatus.GRADED)")
    @Mapping(target = "submittedAt", ignore = true)
    @Mapping(target = "gradedAt", ignore = true)
    @Mapping(target = "uploadedFiles", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateGradeFromRequest(SubmissionGradeRequest request, @MappingTarget Submission submission);
}
