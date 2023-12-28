package se.sundsvall.snailmail.integration.db.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class BatchTest {

	@Test
	void testBean() {
		assertThat(Batch.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		// Set values as variables
		final String id = "batchId";
		final var department = Department.builder().build();

		final var batch = Batch.builder()
			.withId(id)
			.withDepartments(List.of(department))
			.build();

		Assertions.assertThat(batch).isNotNull().hasNoNullFieldsOrProperties();
		Assertions.assertThat(batch.getId()).isEqualTo(id);
		Assertions.assertThat(batch.getDepartments()).hasSize(1);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		Assertions.assertThat(Batch.builder().build()).hasAllNullFieldsOrProperties();
		Assertions.assertThat(new Batch()).hasAllNullFieldsOrProperties();
	}

}
