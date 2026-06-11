package com.ithub.online_learning.mapper;

import com.ithub.online_learning.dto.response.UploadedFileResponse;
import com.ithub.online_learning.entity.UploadedFile;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UploadedFileMapper {

    UploadedFileResponse toResponse(UploadedFile uploadedFile);

    List<UploadedFileResponse> toResponseList(List<UploadedFile> uploadedFiles);
}
