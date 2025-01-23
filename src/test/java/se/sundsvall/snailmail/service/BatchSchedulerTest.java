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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.dept44.scheduling.Dept44Scheduled;
import se.sundsvall.snailmail.config.BatchProperties;
import se.sundsvall.snailmail.integration.db.model.BatchEntity;

@ExtendWith(MockitoExtension.class)
class BatchSchedulerTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String BATCH_ID = "550e8400-e29b-41d4-a716-446655440000";
	@Mock
	private SnailMailService mockSnailMailService;
	@Mock
	private BatchProperties mockBatchProperties;
	@InjectMocks
	private BatchScheduler batchScheduler;

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
	void testDept44ScheduledAnnotationContainsCorrectValues() {
		final var dept44ScheduledAnnotation = findMethod(BatchScheduler.class, "sendUnhandledBatches")
			.flatMap(sendUnhandledBatches -> findAnnotation(sendUnhandledBatches, Dept44Scheduled.class))
			.orElseThrow(() -> new IllegalStateException("Unable to find the 'sendUnhandledBatches' method on the " + BatchScheduler.class.getName() + " class"));

		assertThat(dept44ScheduledAnnotation.cron()).isEqualTo("${batch.unhandled.cron}");
		assertThat(dept44ScheduledAnnotation.name()).isEqualTo("${batch.unhandled.name}");
		assertThat(dept44ScheduledAnnotation.lockAtMostFor()).isEqualTo("${batch.unhandled.lock-at-most-for}");
		assertThat(dept44ScheduledAnnotation.maximumExecutionTime()).isEqualTo("${batch.unhandled.maximum-execution-time}");
	}

	private BatchEntity createBatchEntity() {
		return BatchEntity.builder()
			.withMunicipalityId(MUNICIPALITY_ID)
			.withId(BATCH_ID)
			.build();
	}
}
