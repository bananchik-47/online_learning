package com.ithub.online_learning.controller;

import com.ithub.online_learning.dto.response.UploadedFileResponse;
import com.ithub.online_learning.security.CustomUserDetails;
import com.ithub.online_learning.service.UploadedFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@Tag(name = "Files", description = "File upload and download operations")
public class UploadedFileController {

    private final UploadedFileService uploadedFileService;

    @PostMapping(value = "/submissions/{submissionId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Upload a file for a submission")
    @ApiResponse(responseCode = "201", description = "File uploaded")
    public UploadedFileResponse uploadForSubmission(
            @Parameter(description = "Submission ID") @PathVariable Long submissionId,
            @Parameter(description = "File to upload") @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails userDetails) throws IOException {
        return uploadedFileService.uploadForSubmission(
                file.getBytes(),
                file.getOriginalFilename(),
                file.getContentType(),
                userDetails.getId(),
                submissionId
        );
    }

    @PostMapping(value = "/lessons/{lessonId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Upload a file for a lesson")
    @ApiResponse(responseCode = "201", description = "File uploaded")
    public UploadedFileResponse uploadForLesson(
            @Parameter(description = "Lesson ID") @PathVariable Long lessonId,
            @Parameter(description = "File to upload") @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails userDetails) throws IOException {
        return uploadedFileService.uploadForLesson(
                file.getBytes(),
                file.getOriginalFilename(),
                file.getContentType(),
                userDetails.getId(),
                lessonId
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get file metadata by ID")
    @ApiResponse(responseCode = "200", description = "File metadata found")
    @ApiResponse(responseCode = "404", description = "File not found")
    public UploadedFileResponse findById(@Parameter(description = "File ID") @PathVariable Long id) {
        return uploadedFileService.findById(id);
    }

    @GetMapping("/{id}/content")
    @Operation(summary = "Download file content")
    @ApiResponse(responseCode = "200", description = "File content returned")
    @ApiResponse(responseCode = "404", description = "File not found")
    public ResponseEntity<byte[]> getFileContent(@Parameter(description = "File ID") @PathVariable Long id) {
        UploadedFileResponse metadata = uploadedFileService.findById(id);
        byte[] content = uploadedFileService.getFileContent(id);

        MediaType mediaType = metadata.getContentType() != null
                ? MediaType.parseMediaType(metadata.getContentType())
                : MediaType.APPLICATION_OCTET_STREAM;

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + metadata.getOriginalFilename() + "\"")
                .body(content);
    }

    @GetMapping("/submissions/{submissionId}")
    @Operation(summary = "Get all files for a submission")
    @ApiResponse(responseCode = "200", description = "List of submission files")
    public List<UploadedFileResponse> findBySubmissionId(
            @Parameter(description = "Submission ID") @PathVariable Long submissionId) {
        return uploadedFileService.findBySubmissionId(submissionId);
    }

    @GetMapping("/lessons/{lessonId}")
    @Operation(summary = "Get all files for a lesson")
    @ApiResponse(responseCode = "200", description = "List of lesson files")
    public List<UploadedFileResponse> findByLessonId(
            @Parameter(description = "Lesson ID") @PathVariable Long lessonId) {
        return uploadedFileService.findByLessonId(lessonId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a file")
    @ApiResponse(responseCode = "204", description = "File deleted")
    @ApiResponse(responseCode = "404", description = "File not found")
    public void delete(@Parameter(description = "File ID") @PathVariable Long id) {
        uploadedFileService.delete(id);
    }
}
