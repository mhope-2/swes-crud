package com.michaelhope.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.michaelhope.dto.SoftwareEngineerRequest;
import com.michaelhope.dto.SoftwareEngineerResponse;
import com.michaelhope.exception.GlobalExceptionHandler;
import com.michaelhope.exception.ResourceNotFoundException;
import com.michaelhope.service.SoftwareEngineerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SoftwareEngineerController.class)
@Import(GlobalExceptionHandler.class)
class SoftwareEngineerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SoftwareEngineerService service;

    @Test
    void getEngineers_returns200WithList() throws Exception {
        when(service.getAllSoftwareEngineers()).thenReturn(
            List.of(new SoftwareEngineerResponse(1, "Alice", "Java"))
        );

        mockMvc.perform(get("/api/v1/software-engineer"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("Alice"))
            .andExpect(jsonPath("$[0].techStack").value("Java"));
    }

    @Test
    void getEngineerById_returns200WhenFound() throws Exception {
        when(service.getSoftwareEngineerById(1)).thenReturn(
            new SoftwareEngineerResponse(1, "Alice", "Java")
        );

        mockMvc.perform(get("/api/v1/software-engineer/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Alice"));
    }

    @Test
    void getEngineerById_returns404WhenNotFound() throws Exception {
        when(service.getSoftwareEngineerById(99))
            .thenThrow(new ResourceNotFoundException("Software engineer with id 99 not found"));

        mockMvc.perform(get("/api/v1/software-engineer/99"))
            .andExpect(status().isNotFound());
    }

    @Test
    void addEngineer_returns201WithBody() throws Exception {
        SoftwareEngineerRequest request = new SoftwareEngineerRequest("Bob", "Kotlin");
        when(service.addSoftwareEngineer(any(SoftwareEngineerRequest.class)))
            .thenReturn(new SoftwareEngineerResponse(2, "Bob", "Kotlin"));

        mockMvc.perform(post("/api/v1/software-engineer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(2))
            .andExpect(jsonPath("$.name").value("Bob"));
    }

    @Test
    void addEngineer_returns400WhenNameBlank() throws Exception {
        SoftwareEngineerRequest request = new SoftwareEngineerRequest("", "Kotlin");

        mockMvc.perform(post("/api/v1/software-engineer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void updateEngineer_returns200WithUpdatedBody() throws Exception {
        SoftwareEngineerRequest request = new SoftwareEngineerRequest("Alice Updated", "Go");
        when(service.updateSoftwareEngineer(eq(1), any(SoftwareEngineerRequest.class)))
            .thenReturn(new SoftwareEngineerResponse(1, "Alice Updated", "Go"));

        mockMvc.perform(put("/api/v1/software-engineer/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Alice Updated"));
    }

    @Test
    void updateEngineer_returns404WhenNotFound() throws Exception {
        when(service.updateSoftwareEngineer(eq(99), any(SoftwareEngineerRequest.class)))
            .thenThrow(new ResourceNotFoundException("Software engineer with id 99 not found"));

        mockMvc.perform(put("/api/v1/software-engineer/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new SoftwareEngineerRequest("X", "Y"))))
            .andExpect(status().isNotFound());
    }

    @Test
    void deleteEngineer_returns204WhenDeleted() throws Exception {
        mockMvc.perform(delete("/api/v1/software-engineer/1"))
            .andExpect(status().isNoContent());
    }

    @Test
    void deleteEngineer_returns404WhenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Software engineer with id 99 not found"))
            .when(service).deleteSoftwareEngineer(99);

        mockMvc.perform(delete("/api/v1/software-engineer/99"))
            .andExpect(status().isNotFound());
    }
}
