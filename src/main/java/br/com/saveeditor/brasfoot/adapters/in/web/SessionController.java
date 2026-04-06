package br.com.saveeditor.brasfoot.adapters.in.web;

import br.com.saveeditor.brasfoot.application.ports.in.DownloadSaveUseCase;
import br.com.saveeditor.brasfoot.application.ports.in.UploadSaveUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/sessions")
@Tag(name = "Session Management", description = "Endpoints to start a session by uploading a save file, and finish by downloading the modified save file.")
public class SessionController {

    private final UploadSaveUseCase uploadSaveUseCase;
    private final DownloadSaveUseCase downloadSaveUseCase;

    public SessionController(UploadSaveUseCase uploadSaveUseCase, DownloadSaveUseCase downloadSaveUseCase) {
        this.uploadSaveUseCase = uploadSaveUseCase;
        this.downloadSaveUseCase = downloadSaveUseCase;
    }

    @Operation(summary = "Upload a save file (.s22)", description = "Uploads a binary Brasfoot save file to start an editing session. This must be the first step in the workflow.",
               responses = {
                   @ApiResponse(responseCode = "200", description = "File uploaded successfully, returns the session ID to use in further requests."),
                   @ApiResponse(responseCode = "400", description = "Invalid file or unsupported format. Only .s22 files are supported.")
               })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SessionResponse> uploadSave(
            @Parameter(description = "The binary save file (.s22)", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestPart("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".s22")) {
            throw new IllegalArgumentException("Invalid file format. Only .s22 files are supported.");
        }
        String sessionId = uploadSaveUseCase.upload(file.getBytes());
        return ResponseEntity.ok(new SessionResponse(sessionId));
    }

    @Operation(summary = "Download the modified save file", description = "Generates and downloads the modified binary save file for the given session. Use this after you are done editing.",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Returns the binary save file as an attachment."),
                   @ApiResponse(responseCode = "404", description = "Session not found.")
               })
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadSave(
            @Parameter(description = "The unique session ID received during file upload")
            @PathVariable("id") String id) {
        byte[] payload = downloadSaveUseCase.download(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"save.s22\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(payload);
    }
}