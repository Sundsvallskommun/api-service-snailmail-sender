package se.sundsvall.snailmail.integration.db.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEqualsExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCodeExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import se.sundsvall.snailmail.api.model.EnvelopeType;

class AttachmentEntityTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(AttachmentEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCodeExcluding("requestEntity"),
			hasValidBeanEqualsExcluding("requestEntity"),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		var id = 12L;
		var request = RequestEntity.builder().build();
		var contentType = "contentType";
		var envelopeType = EnvelopeType.PLAIN;
		var name = "name";
		var content = "content";

		var attachment = AttachmentEntity.builder()
			.withId(id)
			.withRequestEntity(request)
			.withContentType(contentType)
			.withEnvelopeType(envelopeType)
			.withName(name)
			.withContent(content)
			.build();

		assertThat(attachment).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(attachment.getId()).isEqualTo(id);
		assertThat(attachment.getRequestEntity()).isNotNull();
		assertThat(attachment.getContentType()).isEqualTo(contentType);
		assertThat(attachment.getEnvelopeType()).isEqualTo(envelopeType);
		assertThat(attachment.getName()).isEqualTo(name);
		assertThat(attachment.getContent()).isEqualTo(content);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(AttachmentEntity.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new AttachmentEntity()).hasAllNullFieldsOrProperties();
	}

}
