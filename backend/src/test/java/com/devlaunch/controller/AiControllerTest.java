package com.devlaunch.controller;

import com.devlaunch.dto.response.TranscribeResponse;
import com.devlaunch.service.interfaces.AiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller tests for the Whisper transcription endpoint.
 * <p>
 * Verifies that {@code POST /api/ai/transcribe} receives the uploaded audio
 * file and the client-tracked duration as multipart form data, forwards
 * them to the AI service, and returns the transcript and duration.
 * </p>
 *
 * @author DevLaunch
 */
@ExtendWith(MockitoExtension.class)
class AiControllerTest {

    @Mock
    private AiService aiService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AiController(aiService)).build();
    }

    @Test
    @DisplayName("transcribe receives the uploaded audio file and duration")
    void transcribeReceivesTheUploadedAudioFile() throws Exception {
        when(aiService.transcribe(any(MultipartFile.class), eq(65)))
                .thenReturn(TranscribeResponse.builder()
                        .transcript("I led a team of five engineers.")
                        .duration(65.0)
                        .build());

        final MockMultipartFile audio = new MockMultipartFile(
                "file", "answer.webm", "audio/webm", "fake audio".getBytes());

        mockMvc.perform(multipart("/api/ai/transcribe")
                        .file(audio)
                        .param("duration", "65"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transcript").value("I led a team of five engineers."))
                .andExpect(jsonPath("$.duration").value(65.0));
    }

    @Test
    @DisplayName("transcribe without an audio file returns 400")
    void transcribeRequiresTheFilePart() throws Exception {
        mockMvc.perform(multipart("/api/ai/transcribe").param("duration", "65"))
                .andExpect(status().isBadRequest());
    }

}
