package org.aibles.intellihireresume.entity.enums;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JobStatusTest {

    @Test
    void isCompleted_ShouldReturnTrue_ForTerminalStatuses() {
        assertThat(JobStatus.SUCCEEDED.isCompleted()).isTrue();
        assertThat(JobStatus.FAILED.isCompleted()).isTrue();
        assertThat(JobStatus.CANCELED.isCompleted()).isTrue();
    }

    @Test
    void isCompleted_ShouldReturnFalse_ForActiveStatuses() {
        assertThat(JobStatus.QUEUED.isCompleted()).isFalse();
        assertThat(JobStatus.RUNNING.isCompleted()).isFalse();
    }

    @Test
    void isInProgress_ShouldReturnTrue_ForActiveStatuses() {
        assertThat(JobStatus.QUEUED.isInProgress()).isTrue();
        assertThat(JobStatus.RUNNING.isInProgress()).isTrue();
    }

    @Test
    void isInProgress_ShouldReturnFalse_ForTerminalStatuses() {
        assertThat(JobStatus.SUCCEEDED.isInProgress()).isFalse();
        assertThat(JobStatus.FAILED.isInProgress()).isFalse();
        assertThat(JobStatus.CANCELED.isInProgress()).isFalse();
    }

    @Test
    void isSuccessful_ShouldReturnTrue_OnlyForSucceeded() {
        assertThat(JobStatus.SUCCEEDED.isSuccessful()).isTrue();
        assertThat(JobStatus.FAILED.isSuccessful()).isFalse();
        assertThat(JobStatus.QUEUED.isSuccessful()).isFalse();
    }

    @Test
    void isFailed_ShouldReturnTrue_OnlyForFailed() {
        assertThat(JobStatus.FAILED.isFailed()).isTrue();
        assertThat(JobStatus.SUCCEEDED.isFailed()).isFalse();
        assertThat(JobStatus.QUEUED.isFailed()).isFalse();
    }

    @Test
    void isCanceled_ShouldReturnTrue_OnlyForCanceled() {
        assertThat(JobStatus.CANCELED.isCanceled()).isTrue();
        assertThat(JobStatus.FAILED.isCanceled()).isFalse();
        assertThat(JobStatus.RUNNING.isCanceled()).isFalse();
    }

    @Test
    void canRetry_ShouldReturnTrue_OnlyForFailed() {
        assertThat(JobStatus.FAILED.canRetry()).isTrue();
        assertThat(JobStatus.SUCCEEDED.canRetry()).isFalse();
        assertThat(JobStatus.QUEUED.canRetry()).isFalse();
        assertThat(JobStatus.RUNNING.canRetry()).isFalse();
        assertThat(JobStatus.CANCELED.canRetry()).isFalse();
    }

    @Test
    void canCancel_ShouldReturnTrue_ForQueuedAndRunning() {
        assertThat(JobStatus.QUEUED.canCancel()).isTrue();
        assertThat(JobStatus.RUNNING.canCancel()).isTrue();
    }

    @Test
    void canCancel_ShouldReturnFalse_ForTerminalStatuses() {
        assertThat(JobStatus.SUCCEEDED.canCancel()).isFalse();
        assertThat(JobStatus.FAILED.canCancel()).isFalse();
        assertThat(JobStatus.CANCELED.canCancel()).isFalse();
    }

    @Test
    void getActiveStatuses_ShouldReturnQueuedAndRunning() {
        List<JobStatus> active = JobStatus.getActiveStatuses();
        assertThat(active).containsExactlyInAnyOrder(JobStatus.QUEUED, JobStatus.RUNNING);
    }

    @Test
    void getCompletedStatuses_ShouldReturnTerminalStatuses() {
        List<JobStatus> completed = JobStatus.getCompletedStatuses();
        assertThat(completed).containsExactlyInAnyOrder(
                JobStatus.SUCCEEDED, JobStatus.FAILED, JobStatus.CANCELED);
    }

    @Test
    void getRetriableStatuses_ShouldReturnOnlyFailed() {
        List<JobStatus> retriable = JobStatus.getRetriableStatuses();
        assertThat(retriable).containsExactly(JobStatus.FAILED);
    }

    @Test
    void getDisplayName_ShouldReturnHumanReadableName() {
        assertThat(JobStatus.QUEUED.getDisplayName()).isEqualTo("Queued");
        assertThat(JobStatus.RUNNING.getDisplayName()).isEqualTo("Running");
        assertThat(JobStatus.SUCCEEDED.getDisplayName()).isEqualTo("Succeeded");
        assertThat(JobStatus.FAILED.getDisplayName()).isEqualTo("Failed");
        assertThat(JobStatus.CANCELED.getDisplayName()).isEqualTo("Canceled");
    }
}
