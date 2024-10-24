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

class BatchEntityTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(BatchEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCodeExcluding("departmentEntities"),
			hasValidBeanEqualsExcluding("departmentEntities"),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		// Set values as variables
		final String id = "batchId";
		final String issuer = "issuer";
		final String municipalityId = "municipalityId";
		final var department = DepartmentEntity.builder().build();

		final var batch = BatchEntity.builder()
			.withId(id)
			.withIssuer(issuer)
			.withMunicipalityId(municipalityId)
			.withDepartmentEntities(List.of(department))
			.build();

		assertThat(batch).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(batch.getIssuer()).isEqualTo(issuer);
		assertThat(batch.getId()).isEqualTo(id);
		assertThat(batch.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(batch.getDepartmentEntities()).hasSize(1);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(BatchEntity.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new BatchEntity()).hasAllNullFieldsOrProperties();
	}

}
