package se.sundsvall.snailmail.integration.db.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

class DepartmentEntityTest {

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(DepartmentEntity.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new DepartmentEntity()).hasAllNullFieldsOrProperties();
	}

	@Test
	void testBuilderMethods() {
		var id = 12L;
		var request = RequestEntity.builder().build();
		var batch = BatchEntity.builder().build();
		var name = "departmentName";

		var department = DepartmentEntity.builder()
			.withId(id)
			.withName(name)
			.withBatchEntity(batch)
			.withRequestEntities(List.of(request))
			.build();

		assertThat(department).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(department.getId()).isEqualTo(id);
		assertThat(department.getRequestEntities()).hasSize(1);
		assertThat(department.getName()).isEqualTo(name);
		assertThat(department.getBatchEntity()).isNotNull();
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
			var first = DepartmentEntity.builder().build();
			var second = DepartmentEntity.builder().build();

			return Stream.of(
				Arguments.of(first, second, false),
				Arguments.of(first, first, true),
				Arguments.of(first, "someString", false),
				Arguments.of(first.withId(123L), second.withId(123L), true),
				Arguments.of(first.withId(123L).withName("first"), second.withId(123L).withName("second"), true));
		}
	}
}
