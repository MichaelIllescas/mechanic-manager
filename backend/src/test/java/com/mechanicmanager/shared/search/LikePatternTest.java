package com.mechanicmanager.shared.search;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LikePatternTest {

    @Test
    void wrapsTheTextInWildcardsAndLowercasesIt() {
        assertThat(LikePattern.contains("Pérez")).isEqualTo("%pérez%");
    }

    @Test
    void escapesPercentSoItIsSearchedLiterally() {
        assertThat(LikePattern.contains("50%")).isEqualTo("%50!%%");
    }

    @Test
    void escapesUnderscoreSoItIsSearchedLiterally() {
        assertThat(LikePattern.contains("a_c")).isEqualTo("%a!_c%");
    }

    @Test
    void escapesTheEscapeCharacterItself() {
        assertThat(LikePattern.contains("¡hola!")).isEqualTo("%¡hola!!%");
    }

    @Test
    void aSearchOfOnlyWildcardsNoLongerMatchesEverything() {
        assertThat(LikePattern.contains("%")).isEqualTo("%!%%");
        assertThat(LikePattern.contains("_")).isEqualTo("%!_%");
    }
}
