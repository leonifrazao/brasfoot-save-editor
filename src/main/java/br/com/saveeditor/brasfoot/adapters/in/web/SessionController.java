package br.com.saveeditor.brasfoot.adapters.in.web;

import br.com.saveeditor.brasfoot.application.ports.in.DownloadSaveUseCase;
import br.com.saveeditor.brasfoot.application.ports.in.UploadSaveUseCase;
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

    @PostMapping
    public ResponseEntity<SessionResponse> uploadSave(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }
        String sessionId = uploadSaveUseCase.upload(file.getBytes());
        return ResponseEntity.ok(new SessionResponse(sessionId));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadSave(@PathVariable("id") String id) {
        byte[] payload = downloadSaveUseCase.download(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"save.sav\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(payload);
    }
}