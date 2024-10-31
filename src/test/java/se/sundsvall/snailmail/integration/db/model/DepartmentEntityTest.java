package se.sundsvall.snailmail.integration.db.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEqualsExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCodeExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

import java.util.List;

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
			hasValidBeanToString()));
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

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(DepartmentEntity.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new DepartmentEntity()).hasAllNullFieldsOrProperties();
	}

}
