package com.michaelhope.service;

import com.michaelhope.exception.InvalidTechnologyException;
import com.michaelhope.model.Technology;
import com.michaelhope.repository.TechnologyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TechnologyService {

    private final TechnologyRepository repository;

    @Transactional
    public Set<Technology> resolve(Collection<String> requestedNames) {
        if (requestedNames == null || requestedNames.isEmpty()) {
            throw new InvalidTechnologyException("At least one technology is required");
        }

        Map<String, String> requestedByNormalizedName = new LinkedHashMap<>();
        for (String requestedName : requestedNames) {
            String displayName = canonicalName(requestedName);
            String normalizedName = displayName.toLowerCase(Locale.ROOT);
            requestedByNormalizedName.putIfAbsent(normalizedName, displayName);
        }

        Map<String, Technology> technologiesByNormalizedName = new LinkedHashMap<>();
        repository.findAllByNormalizedNameIn(requestedByNormalizedName.keySet())
            .forEach(technology -> technologiesByNormalizedName.put(technology.getNormalizedName(), technology));

        for (Map.Entry<String, String> requested : requestedByNormalizedName.entrySet()) {
            technologiesByNormalizedName.computeIfAbsent(requested.getKey(), normalizedName ->
                repository.save(new Technology(requested.getValue(), normalizedName)));
        }

        return requestedByNormalizedName.keySet().stream()
            .map(technologiesByNormalizedName::get)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    String canonicalName(String requestedName) {
        if (requestedName == null) {
            throw new InvalidTechnologyException("Technology names cannot be null");
        }

        String collapsedWhitespace = requestedName.trim().replaceAll("\\s+", " ");
        if (collapsedWhitespace.isBlank()) {
            throw new InvalidTechnologyException("Technology names cannot be blank");
        }
        if (collapsedWhitespace.length() > Technology.MAX_NAME_LENGTH) {
            throw new InvalidTechnologyException("Technology names cannot exceed "
                + Technology.MAX_NAME_LENGTH + " characters");
        }

        String lowerCaseName = collapsedWhitespace.toLowerCase(Locale.ROOT);
        return Character.toUpperCase(lowerCaseName.charAt(0)) + lowerCaseName.substring(1);
    }
}
