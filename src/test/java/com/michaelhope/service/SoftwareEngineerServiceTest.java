package com.michaelhope.service;

import com.michaelhope.dto.SoftwareEngineerRequest;
import com.michaelhope.dto.SoftwareEngineerResponse;
import com.michaelhope.exception.ResourceNotFoundException;
import com.michaelhope.model.SoftwareEngineer;
import com.michaelhope.repository.SoftwareEngineerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SoftwareEngineerServiceTest {

    @Mock
    private SoftwareEngineerRepository repository;

    @InjectMocks
    private SoftwareEngineerService service;

    private SoftwareEngineer engineer;

    @BeforeEach
    void setUp() {
        engineer = new SoftwareEngineer(1, "Alice", "Java");
    }

    @Test
    void getAllSoftwareEngineers_returnsAllMapped() {
        when(repository.findAll()).thenReturn(List.of(engineer));

        List<SoftwareEngineerResponse> result = service.getAllSoftwareEngineers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Alice");
    }

    @Test
    void getSoftwareEngineerById_returnsResponseWhenFound() {
        when(repository.findById(1)).thenReturn(Optional.of(engineer));

        SoftwareEngineerResponse result = service.getSoftwareEngineerById(1);

        assertThat(result.name()).isEqualTo("Alice");
        assertThat(result.techStack()).isEqualTo("Java");
    }

    @Test
    void getSoftwareEngineerById_throwsWhenNotFound() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getSoftwareEngineerById(99))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Software engineer with id 99 not found");
    }

    @Test
    void addSoftwareEngineer_savesAndReturnsResponse() {
        SoftwareEngineerRequest request = new SoftwareEngineerRequest("Bob", "Kotlin");
        SoftwareEngineer saved = new SoftwareEngineer(2, "Bob", "Kotlin");
        when(repository.save(any(SoftwareEngineer.class))).thenReturn(saved);

        SoftwareEngineerResponse result = service.addSoftwareEngineer(request);

        assertThat(result.id()).isEqualTo(2);
        assertThat(result.name()).isEqualTo("Bob");
        verify(repository).save(any(SoftwareEngineer.class));
    }

    @Test
    void updateSoftwareEngineer_updatesFieldsAndReturnsResponse() {
        SoftwareEngineerRequest request = new SoftwareEngineerRequest("Alice Updated", "Go");
        when(repository.findById(1)).thenReturn(Optional.of(engineer));
        when(repository.save(engineer)).thenReturn(new SoftwareEngineer(1, "Alice Updated", "Go"));

        SoftwareEngineerResponse result = service.updateSoftwareEngineer(1, request);

        assertThat(result.name()).isEqualTo("Alice Updated");
        assertThat(result.techStack()).isEqualTo("Go");
        verify(repository).save(engineer);
    }

    @Test
    void updateSoftwareEngineer_throwsWhenNotFound() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateSoftwareEngineer(99, new SoftwareEngineerRequest("X", "Y")))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Software engineer with id 99 not found");
    }

    @Test
    void deleteSoftwareEngineer_deletesWhenFound() {
        when(repository.existsById(1)).thenReturn(true);

        service.deleteSoftwareEngineer(1);

        verify(repository).deleteById(1);
    }

    @Test
    void deleteSoftwareEngineer_throwsWhenNotFound() {
        when(repository.existsById(99)).thenReturn(false);

        assertThatThrownBy(() -> service.deleteSoftwareEngineer(99))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Software engineer with id 99 not found");
    }
}
