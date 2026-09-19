package com.zamtrust.exception;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies oversized uploads return 413 Payload Too Large (finding #11).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MaxUploadSizeHandlerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    private String token;

    @BeforeEach
    void setup() throws Exception {
        // Register (idempotent — ignore error if user exists)
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"username":"uploadtest","email":"uploadtest@example.com",
                     "password":"StrongPass1!","fullName":"Upload Test",
                     "organization":"ZamTrust Test"}
                    """));

        // Login
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"username":"uploadtest","password":"StrongPass1!"}
                            """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(body);
        this.token = json.get("token").asText();
    }

    @Test
    @DisplayName("Oversized upload returns 413 with a human-readable message")
    void oversizedUploadReturns413() throws Exception {
        byte[] content = new byte[30 * 1024 * 1024]; // 30 MB
        MockMultipartFile file = new MockMultipartFile(
                "file", "big.bin", "application/octet-stream", content);

        mockMvc.perform(multipart("/api/documents")
                        .file(file)
                        .param("title", "Oversized")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(jsonPath("$.status").value(413))
                .andExpect(jsonPath("$.message").value(
                        "File exceeds the 25 MB upload limit. Please reduce the file size and try again."));
    }
}
