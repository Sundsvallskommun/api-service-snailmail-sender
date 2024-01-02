package se.sundsvall.snailmail.api.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

class SendSnailMailRequestAttachmentTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(SendSnailMailRequest.Attachment.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {

		final var attachment = SendSnailMailRequest.Attachment.builder()
			.withName("someName")
			.withContentType("someContentType")
			.withContent("someContent")
			.withEnvelopeType(EnvelopeType.PLAIN)
			.build();

		assertThat(attachment).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(attachment.getName()).isEqualTo("someName");
		assertThat(attachment.getContent()).isEqualTo("someContent");
		assertThat(attachment.getContentType()).isEqualTo("someContentType");
		assertThat(attachment.getEnvelopeType()).isEqualByComparingTo(EnvelopeType.PLAIN);

	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(SendSnailMailRequest.Attachment.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new SendSnailMailRequest.Attachment()).hasAllNullFieldsOrProperties();
	}

}
