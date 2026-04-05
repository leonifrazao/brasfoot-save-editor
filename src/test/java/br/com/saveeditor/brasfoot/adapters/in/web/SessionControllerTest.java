package br.com.saveeditor.brasfoot.adapters.in.web;

import br.com.saveeditor.brasfoot.application.ports.in.DownloadSaveUseCase;
import br.com.saveeditor.brasfoot.application.ports.in.UploadSaveUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SessionController.class)
public class SessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UploadSaveUseCase uploadSaveUseCase;

    @MockBean
    private DownloadSaveUseCase downloadSaveUseCase;

    @Test
    public void testUploadValidFile() throws Exception {
        when(uploadSaveUseCase.upload(any())).thenReturn("session-id");
        
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "save.s22",
                "application/octet-stream",
                "test data".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/sessions").file(file))
                .andExpect(status().isOk());
    }

    @Test
    public void testUploadInvalidFileExtension() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "save.txt",
                "text/plain",
                "test data".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/sessions").file(file))
                .andExpect(status().isBadRequest());
    }
}