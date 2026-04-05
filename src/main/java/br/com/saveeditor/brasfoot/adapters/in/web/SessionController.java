package br.com.saveeditor.brasfoot.adapters.in.web;

import br.com.saveeditor.brasfoot.application.ports.in.DownloadSaveUseCase;
import br.com.saveeditor.brasfoot.application.ports.in.UploadSaveUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/sessions")
public class SessionController {

    private final UploadSaveUseCase uploadSaveUseCase;
    private final DownloadSaveUseCase downloadSaveUseCase;

    public SessionController(UploadSaveUseCase uploadSaveUseCase, DownloadSaveUseCase downloadSaveUseCase) {
        this.uploadSaveUseCase = uploadSaveUseCase;
        this.downloadSaveUseCase = downloadSaveUseCase;
    }

    @Operation(summary = "Upload a save file", description = "Uploads a binary Brasfoot save file to start an editing session.")
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

    @Operation(summary = "Download a save file", description = "Downloads the modified binary save file for the given session.")
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadSave(@PathVariable("id") String id) {
        byte[] payload = downloadSaveUseCase.download(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"save.s22\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(payload);
    }
}