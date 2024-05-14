package se.sundsvall.snailmail.integration.db.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEqualsExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCodeExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToStringExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.hamcrest.CoreMatchers.allOf;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

class DepartmentEntityTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(DepartmentEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCodeExcluding("batchEntity", "requestEntities"),
			hasValidBeanEqualsExcluding("batchEntity", "requestEntities"),
			hasValidBeanToStringExcluding("batchEntity", "requestEntities")));
	}

	@Test
	void testBuilderMethods() {
		// Set values as variables
		final var id = 12L;
		final var request = RequestEntity.builder().build();
		final var batch = BatchEntity.builder().build();
		final var name = "departmentName";

		final var department = DepartmentEntity.builder()
			.withId(id)
			.withName(name)
			.withBatchEntity(batch)
			.withRequestEntities(List.of(request))
			.build();

		Assertions.assertThat(department).isNotNull().hasNoNullFieldsOrProperties();
		Assertions.assertThat(department.getId()).isEqualTo(id);
		Assertions.assertThat(department.getRequestEntities()).hasSize(1);
		Assertions.assertThat(department.getName()).isEqualTo(name);
		Assertions.assertThat(department.getBatchEntity()).isNotNull();
	}

	@Test
	void testNoDirtOnCreatedBean() {
		Assertions.assertThat(DepartmentEntity.builder().build()).hasAllNullFieldsOrProperties();
		Assertions.assertThat(new DepartmentEntity()).hasAllNullFieldsOrProperties();
	}

}
