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

class RequestEntityTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(RequestEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCodeExcluding("departmentEntity", "recipientEntity", "attachmentEntities"),
			hasValidBeanEqualsExcluding("departmentEntity", "recipientEntity", "attachmentEntities"),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		var id = 12L;
		var department = DepartmentEntity.builder().build();
		var recipient = RecipientEntity.builder().build();
		var attachment = AttachmentEntity.builder().build();
		var deviation = "deviation";
		var partyId = "partyId";

		var request = RequestEntity.builder()
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
