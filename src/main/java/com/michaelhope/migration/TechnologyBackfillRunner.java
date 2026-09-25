package com.michaelhope.migration;

import com.michaelhope.model.Technology;
import com.michaelhope.repository.SoftwareEngineerRepository;
import com.michaelhope.service.TechnologyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class TechnologyBackfillRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;
    private final SoftwareEngineerRepository engineerRepository;
    private final TechnologyService technologyService;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!legacyTechStackColumnExists()) {
            log.debug("technology.backfill.skipped reason=legacy_column_not_found");
            return;
        }

        List<LegacyEngineer> legacyEngineers = jdbcTemplate.query(
            "select id, tech_stack from software_engineer where tech_stack is not null and trim(tech_stack) <> ''",
            (resultSet, rowNumber) -> new LegacyEngineer(
                resultSet.getInt("id"),
                resultSet.getString("tech_stack")
            )
        );

        int migrated = 0;
        for (LegacyEngineer legacyEngineer : legacyEngineers) {
            List<String> technologyNames = Arrays.stream(legacyEngineer.techStack().split(","))
                .map(String::trim)
                .filter(name -> !name.isBlank())
                .toList();
            if (technologyNames.isEmpty()) {
                continue;
            }

            Set<Technology> technologies = technologyService.resolve(technologyNames);
            engineerRepository.findById(legacyEngineer.id()).ifPresent(engineer -> {
                engineer.setTechnologies(technologies);
                engineerRepository.save(engineer);
            });
            migrated++;
        }

        log.info("technology.backfill.completed migratedEngineerCount={}", migrated);
    }

    private boolean legacyTechStackColumnExists() {
        Integer columnCount = jdbcTemplate.queryForObject(
            "select count(*) from information_schema.columns "
                + "where table_schema = current_schema() "
                + "and table_name = 'software_engineer' "
                + "and column_name = 'tech_stack'",
            Integer.class
        );
        return columnCount != null && columnCount > 0;
    }

    private record LegacyEngineer(Integer id, String techStack) {
    }
}
