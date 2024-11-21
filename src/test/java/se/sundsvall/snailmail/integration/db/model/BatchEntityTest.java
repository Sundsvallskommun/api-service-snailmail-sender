package se.sundsvall.snailmail.integration.db.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEqualsExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCodeExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.hamcrest.CoreMatchers.allOf;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class BatchEntityTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> OffsetDateTime.now().plusDays(new Random().nextInt()), OffsetDateTime.class);
	}

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
		var id = "batchId";
		var issuer = "issuer";
		var municipalityId = "municipalityId";
		var department = DepartmentEntity.builder().build();
		var created = OffsetDateTime.now();

		var batch = BatchEntity.builder()
			.withId(id)
			.withIssuer(issuer)
			.withMunicipalityId(municipalityId)
			.withDepartmentEntities(List.of(department))
			.withCreated(created)
			.build();

		assertThat(batch).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(batch.getIssuer()).isEqualTo(issuer);
		assertThat(batch.getId()).isEqualTo(id);
		assertThat(batch.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(batch.getDepartmentEntities()).hasSize(1);
		assertThat(batch.getCreated()).isEqualTo(created);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(BatchEntity.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new BatchEntity()).hasAllNullFieldsOrProperties();
	}
}
