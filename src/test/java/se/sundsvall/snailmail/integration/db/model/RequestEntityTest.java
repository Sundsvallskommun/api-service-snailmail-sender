package se.sundsvall.snailmail.integration.db.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

import java.util.List;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

class RequestEntityTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(RequestEntity.class, allOf(
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
		final var department = DepartmentEntity.builder().build();
		final var recipient = RecipientEntity.builder().build();
		final var attachment = AttachmentEntity.builder().build();
		final var deviation = "deviation";
		final var partyId = "partyId";


		final var request = RequestEntity.builder()
			.withId(id)
			.withDepartmentEntity(department)
			.withRecipientEntity(recipient)
			.withAttachmentEntities(List.of(attachment))
			.withDeviation(deviation)
			.withPartyId(partyId)
			.build();

		assertThat(request).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(request.getId()).isEqualTo(id);
		assertThat(request.getDepartmentEntity()).isNotNull();
		assertThat(request.getRecipientEntity()).isNotNull();
		assertThat(request.getAttachmentEntities()).hasSize(1);
		assertThat(request.getDeviation()).isEqualTo(deviation);
		assertThat(request.getPartyId()).isEqualTo(partyId);


	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(RequestEntity.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new RequestEntity()).hasAllNullFieldsOrProperties();
	}


}
