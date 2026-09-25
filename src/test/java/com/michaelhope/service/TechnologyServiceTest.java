package com.michaelhope.service;

import com.michaelhope.exception.InvalidTechnologyException;
import com.michaelhope.model.Technology;
import com.michaelhope.repository.TechnologyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TechnologyServiceTest {

    @Mock
    private TechnologyRepository repository;

    @Test
    void resolve_trimsNormalizesAndDeduplicatesTechnologyNames() {
        Technology java = new Technology(1, "Java", "java");
        when(repository.findAllByNormalizedNameIn(anyCollection())).thenReturn(List.of(java));
        when(repository.save(any(Technology.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Set<Technology> result = new TechnologyService(repository)
            .resolve(List.of(" java ", "JAVA", " Spring  Boot "));

        assertThat(result).extracting(Technology::getName)
            .containsExactly("Java", "Spring boot");
        verify(repository).save(any(Technology.class));
    }

    @Test
    void resolve_rejectsBlankTechnologyNames() {
        TechnologyService service = new TechnologyService(repository);

        assertThatThrownBy(() -> service.resolve(List.of("  ")))
            .isInstanceOf(InvalidTechnologyException.class)
            .hasMessage("Technology names cannot be blank");
    }

    @Test
    void resolve_rejectsEmptyCollections() {
        TechnologyService service = new TechnologyService(repository);

        assertThatThrownBy(() -> service.resolve(List.of()))
            .isInstanceOf(InvalidTechnologyException.class)
            .hasMessage("At least one technology is required");
    }
}
