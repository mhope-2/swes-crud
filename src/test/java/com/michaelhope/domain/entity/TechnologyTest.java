package com.michaelhope.domain.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TechnologyTest {

    @Test
    void technologiesWithTheSameNormalizedNameRepresentTheSameValue() {
        Technology java = new Technology(1, "Java", "java");
        Technology duplicate = new Technology(2, "JAVA", "java");

        assertThat(java).isEqualTo(duplicate);
        assertThat(java).hasSameHashCodeAs(duplicate);
    }
}
