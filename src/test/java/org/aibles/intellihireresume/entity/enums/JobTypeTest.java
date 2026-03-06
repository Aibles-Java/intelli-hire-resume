package org.aibles.intellihireresume.entity.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JobTypeTest {

    @Test
    void isParseJob_ShouldReturnTrue_ForParseAndReparse() {
        assertThat(JobType.PARSE.isParseJob()).isTrue();
        assertThat(JobType.REPARSE.isParseJob()).isTrue();
    }

    @Test
    void isParseJob_ShouldReturnFalse_ForAnalyze() {
        assertThat(JobType.ANALYZE.isParseJob()).isFalse();
    }

    @Test
    void isAnalyzeJob_ShouldReturnTrue_OnlyForAnalyze() {
        assertThat(JobType.ANALYZE.isAnalyzeJob()).isTrue();
        assertThat(JobType.PARSE.isAnalyzeJob()).isFalse();
        assertThat(JobType.REPARSE.isAnalyzeJob()).isFalse();
    }

    @Test
    void isInitialParse_ShouldReturnTrue_OnlyForParse() {
        assertThat(JobType.PARSE.isInitialParse()).isTrue();
        assertThat(JobType.REPARSE.isInitialParse()).isFalse();
        assertThat(JobType.ANALYZE.isInitialParse()).isFalse();
    }

    @Test
    void isReparse_ShouldReturnTrue_OnlyForReparse() {
        assertThat(JobType.REPARSE.isReparse()).isTrue();
        assertThat(JobType.PARSE.isReparse()).isFalse();
        assertThat(JobType.ANALYZE.isReparse()).isFalse();
    }

    @Test
    void getPriority_ShouldReturnCorrectPriority() {
        assertThat(JobType.PARSE.getPriority()).isEqualTo(1);
        assertThat(JobType.REPARSE.getPriority()).isEqualTo(2);
        assertThat(JobType.ANALYZE.getPriority()).isEqualTo(3);
    }

    @Test
    void getPriority_ShouldHaveParseAsHighestPriority() {
        assertThat(JobType.PARSE.getPriority())
                .isLessThan(JobType.REPARSE.getPriority());
        assertThat(JobType.REPARSE.getPriority())
                .isLessThan(JobType.ANALYZE.getPriority());
    }

    @Test
    void getEstimatedDurationMinutes_ShouldReturnCorrectDuration() {
        assertThat(JobType.PARSE.getEstimatedDurationMinutes()).isEqualTo(2L);
        assertThat(JobType.REPARSE.getEstimatedDurationMinutes()).isEqualTo(2L);
        assertThat(JobType.ANALYZE.getEstimatedDurationMinutes()).isEqualTo(5L);
    }

    @Test
    void getDisplayName_ShouldReturnHumanReadableName() {
        assertThat(JobType.PARSE.getDisplayName()).isEqualTo("Parse");
        assertThat(JobType.REPARSE.getDisplayName()).isEqualTo("Reparse");
        assertThat(JobType.ANALYZE.getDisplayName()).isEqualTo("Analyze");
    }
}
