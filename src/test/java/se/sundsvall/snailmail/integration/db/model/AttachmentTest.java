package se.sundsvall.snailmail.integration.db.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import se.sundsvall.snailmail.api.model.EnvelopeType;

class AttachmentTest {

	@Test
	void testBean() {
		assertThat(Attachment.class, allOf(
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
		final var contentType = "contentType";
		final var envelopeType = EnvelopeType.PLAIN;
		final var name = "name";
		final var content = "content";


		final var attachment = Attachment.builder()
			.withId(id)
			.withRequest(request)
			.withContentType(contentType)
			.withEnvelopeType(envelopeType)
			.withName(name)
			.withContent(content)
			.build();

		Assertions.assertThat(attachment).isNotNull().hasNoNullFieldsOrProperties();
		Assertions.assertThat(attachment.getId()).isEqualTo(id);
		Assertions.assertThat(attachment.getRequest()).isNotNull();
		Assertions.assertThat(attachment.getContentType()).isEqualTo(contentType);
		Assertions.assertThat(attachment.getEnvelopeType()).isEqualTo(envelopeType);
		Assertions.assertThat(attachment.getName()).isEqualTo(name);
		Assertions.assertThat(attachment.getContent()).isEqualTo(content);

	}

	@Test
	void testNoDirtOnCreatedBean() {
		Assertions.assertThat(Attachment.builder().build()).hasAllNullFieldsOrProperties();
		Assertions.assertThat(new Attachment()).hasAllNullFieldsOrProperties();
	}

}
