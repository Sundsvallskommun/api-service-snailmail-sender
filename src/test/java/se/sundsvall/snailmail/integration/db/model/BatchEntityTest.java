package se.sundsvall.snailmail.integration.db.model;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import static org.assertj.core.api.Assertions.assertThat;

class BatchEntityTest {

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(BatchEntity.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new BatchEntity()).hasAllNullFieldsOrProperties();
	}

	@Test
	void testBuilderMethods() {
		var id = "batchId";
		var sentBy = "jeo01doe";
		var municipalityId = "municipalityId";
		var department = DepartmentEntity.builder().build();
		var created = OffsetDateTime.now();

		var batch = BatchEntity.builder()
			.withId(id)
			.withSentBy(sentBy)
			.withMunicipalityId(municipalityId)
			.withDepartmentEntities(List.of(department))
			.withCreated(created)
			.build();

		assertThat(batch).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(batch.getSentBy()).isEqualTo(sentBy);
		assertThat(batch.getId()).isEqualTo(id);
		assertThat(batch.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(batch.getDepartmentEntities()).hasSize(1);
		assertThat(batch.getCreated()).isEqualTo(created);
	}

	@ParameterizedTest
	@ArgumentsSource(EqualsArgumentsProvider.class)
	void testEquals(final Object first, final Object second, final boolean shouldEqual) {
		if (shouldEqual) {
			assertThat(first).isEqualTo(second);
		} else {
			assertThat(first).isNotEqualTo(second);
		}
	}

	@Test
	void testHashCode() {
		assertThat(AttachmentEntity.builder().build().hashCode()).isEqualTo(AttachmentEntity.class.hashCode());
	}

	private static class EqualsArgumentsProvider implements ArgumentsProvider {

		@Override
		public Stream<? extends Arguments> provideArguments(final ExtensionContext context) {
			var first = BatchEntity.builder().build();
			var second = BatchEntity.builder().build();

			return Stream.of(
				Arguments.of(first, second, false),
				Arguments.of(first, first, true),
				Arguments.of(first, "someString", false),
				Arguments.of(first.withId("id"), second.withId("id"), true),
				Arguments.of(first.withId("id").withSentBy("joe01doe"), second.withId("id").withSentBy("joe02doe"), true));
		}
	}
}
