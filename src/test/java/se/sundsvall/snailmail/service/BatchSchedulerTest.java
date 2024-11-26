package se.sundsvall.snailmail.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;
import static org.junit.platform.commons.util.ReflectionUtils.findMethod;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.annotation.Scheduled;
import se.sundsvall.snailmail.config.BatchProperties;
import se.sundsvall.snailmail.integration.db.model.BatchEntity;

@ExtendWith(MockitoExtension.class)
class BatchSchedulerTest {

	@Mock
	private SnailMailService mockSnailMailService;

	@Mock
	private BatchProperties mockBatchProperties;

	@InjectMocks
	private BatchScheduler batchScheduler;

	private static final String MUNICIPALITY_ID = "2281";
	private static final String BATCH_ID = "550e8400-e29b-41d4-a716-446655440000";

	@Test
	void testSendBatch_shouldSendUnhandledBatches() {
		when(mockBatchProperties.getOutdatedAfter()).thenReturn("PT1M");
		when(mockSnailMailService.getUnhandledBatches(any())).thenReturn(List.of(createBatchEntity()));
		doNothing().when(mockSnailMailService).sendBatch(Mockito.anyString(), Mockito.anyString());

		batchScheduler.sendUnhandledBatches();

		verify(mockBatchProperties).getOutdatedAfter();
		verify(mockSnailMailService).getUnhandledBatches(any());
		verify(mockSnailMailService).sendBatch(MUNICIPALITY_ID, BATCH_ID);
		verifyNoMoreInteractions(mockBatchProperties, mockSnailMailService);
	}

	@Test
	void testSendBatch_shouldDoNothing_whenNoUnhandledBatches() {
		when(mockBatchProperties.getOutdatedAfter()).thenReturn("PT1M");
		when(mockSnailMailService.getUnhandledBatches(any())).thenReturn(List.of());

		batchScheduler.sendUnhandledBatches();

		verify(mockBatchProperties).getOutdatedAfter();
		verify(mockSnailMailService).getUnhandledBatches(any());
		verifyNoMoreInteractions(mockBatchProperties, mockSnailMailService);
	}

	@Test
	void testScheduleAnnotationContainsFixedRateString() {
		var scheduledAnnotation = findMethod(BatchScheduler.class, "sendUnhandledBatches")
			.flatMap(sendUnhandledBatches -> findAnnotation(sendUnhandledBatches, Scheduled.class))
			.orElseThrow(() -> new IllegalStateException("Unable to find the 'sendUnhandledBatches' method on the " + BatchScheduler.class.getName() + " class"));

		assertThat(scheduledAnnotation.fixedRateString()).isEqualTo("#{@batchProperties.getCheckInterval()}");
		assertThat(scheduledAnnotation.initialDelayString()).isEqualTo("#{@batchProperties.getInitialDelay()}");
	}

	@Test
	void testSchedulerLockAnnotationContainsCorrectValues() {
		var scheduledAnnotation = findMethod(BatchScheduler.class, "sendUnhandledBatches")
			.flatMap(sendUnhandledBatches -> findAnnotation(sendUnhandledBatches, SchedulerLock.class))
			.orElseThrow(() -> new IllegalStateException("Unable to find the 'sendUnhandledBatches' method on the " + BatchScheduler.class.getName() + " class"));

		assertThat(scheduledAnnotation.name()).isEqualTo("#{@batchProperties.getName()}");
		assertThat(scheduledAnnotation.lockAtMostFor()).isEqualTo("#{@batchProperties.getLockAtMostFor()}");
	}

	private BatchEntity createBatchEntity() {
		return BatchEntity.builder()
			.withMunicipalityId(MUNICIPALITY_ID)
			.withId(BATCH_ID)
			.build();
	}
}
