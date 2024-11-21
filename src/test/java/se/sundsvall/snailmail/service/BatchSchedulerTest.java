package se.sundsvall.snailmail.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;
import static org.junit.platform.commons.util.ReflectionUtils.findMethod;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.annotation.Scheduled;
import se.sundsvall.snailmail.integration.db.BatchRepository;
import se.sundsvall.snailmail.integration.db.model.BatchEntity;

@ExtendWith(MockitoExtension.class)
class BatchSchedulerTest {

	@Mock
	private BatchRepository mockBatchRepository;

	@Mock
	private SnailMailService mockSnailMailService;

	@Mock
	private BatchEntity mockBatchEntity;

	@Mock
	private Duration mockDuration;  // Only used for setting up the BatchScheduler

	@InjectMocks
	private BatchScheduler batchScheduler;

	private static final String MUNICIPALITY_ID = "2281";
	private static final String BATCH_ID = "550e8400-e29b-41d4-a716-446655440000";

	@Test
	void testSendBatch_shouldSendUnhandledBatches() {
		when(mockBatchRepository.findBatchEntityByCreatedIsBefore(any())).thenReturn(List.of(mockBatchEntity));
		when(mockBatchEntity.getMunicipalityId()).thenReturn(MUNICIPALITY_ID);
		when(mockBatchEntity.getId()).thenReturn(BATCH_ID);
		doNothing().when(mockSnailMailService).sendBatch(Mockito.anyString(), Mockito.anyString());

		batchScheduler.sendUnhandledBatches();

		verify(mockBatchRepository).findBatchEntityByCreatedIsBefore(any());
		verify(mockSnailMailService).sendBatch(MUNICIPALITY_ID, BATCH_ID);
		verifyNoMoreInteractions(mockBatchRepository, mockSnailMailService, mockBatchEntity);
	}

	@Test
	void testSendBatch_shouldDoNothing_whenNoUnhandledBatches() {
		when(mockBatchRepository.findBatchEntityByCreatedIsBefore(any())).thenReturn(List.of());

		batchScheduler.sendUnhandledBatches();

		verify(mockBatchRepository).findBatchEntityByCreatedIsBefore(any());
		verifyNoInteractions(mockSnailMailService);
		verifyNoMoreInteractions(mockBatchRepository, mockSnailMailService, mockBatchEntity);
	}

	@Test
	void testScheduleAnnotationContainsFixedRateString() {
		var scheduledAnnotation = findMethod(BatchScheduler.class, "sendUnhandledBatches")
			.flatMap(method1 -> findAnnotation(method1, Scheduled.class))
			.orElseThrow(() -> new IllegalStateException("Unable to find the 'sendUnhandledBatches' method on the " + BatchScheduler.class.getName() + " class"));

		assertThat(scheduledAnnotation.fixedRateString()).isEqualTo("${batch.dangling.check-interval}");
	}

	@Test
	void testSchedulerLockAnnotationContainsCorrectValues() {
		var scheduledAnnotation = findMethod(BatchScheduler.class, "sendUnhandledBatches")
			.flatMap(method1 -> findAnnotation(method1, SchedulerLock.class))
			.orElseThrow(() -> new IllegalStateException("Unable to find the 'sendUnhandledBatches' method on the " + BatchScheduler.class.getName() + " class"));

		assertThat(scheduledAnnotation.name()).isEqualTo("${batch.dangling.name}");
		assertThat(scheduledAnnotation.lockAtMostFor()).isEqualTo("${batch.dangling.lock-at-most-for}");
	}
}
