package se.sundsvall.snailmail.integration.db.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.hamcrest.CoreMatchers.allOf;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

class DepartmentTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(Department.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		// Set values as variables
		final var id = 12L;
		final var request = Request.builder().build();
		final var batch = Batch.builder().build();
		final var name = "departmentName";

		final var department = Department.builder()
			.withId(id)
			.withName(name)
			.withBatch(batch)
			.withRequests(List.of(request))
			.build();

		Assertions.assertThat(department).isNotNull().hasNoNullFieldsOrProperties();
		Assertions.assertThat(department.getId()).isEqualTo(id);
		Assertions.assertThat(department.getRequests()).hasSize(1);
		Assertions.assertThat(department.getName()).isEqualTo(name);
		Assertions.assertThat(department.getBatch()).isNotNull();
	}

	@Test
	void testNoDirtOnCreatedBean() {
		Assertions.assertThat(Department.builder().build()).hasAllNullFieldsOrProperties();
		Assertions.assertThat(new Department()).hasAllNullFieldsOrProperties();
	}

}
