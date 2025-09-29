package se.sundsvall.snailmail.api.model;

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

class SendSnailMailRequestTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(SendSnailMailRequest.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(SendSnailMailRequest.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new SendSnailMailRequest()).hasAllNullFieldsOrProperties();
	}

	@Test
	void testBuilder() {
		var request = SendSnailMailRequest.builder()
			.withAttachments(List.of(
				SendSnailMailRequest.Attachment.builder()
					.withName("someName")
					.withContentType("someContentType")
					.withContent("someContent")
					.withEnvelopeType(EnvelopeType.PLAIN)
					.build()))
			.withDepartment("someDepartment")
			.withFolderName("someFolder")
			.withBatchId("someBatchId")
			.withDeviation("someDeviation")
			.build();

		assertThat(request).isNotNull();
		assertThat(request.getDepartment()).isEqualTo("someDepartment");
		assertThat(request.getFolderName()).isEqualTo("someFolder");
		assertThat(request.getBatchId()).isEqualTo("someBatchId");
		assertThat(request.getDeviation()).isEqualTo("someDeviation");
		assertThat(request.getAttachments()).satisfies(attachments -> {
			assertThat(attachments).hasSize(1);
			assertThat(attachments).hasSize(1);
			assertThat(attachments.getFirst().getName()).isEqualTo("someName");
			assertThat(attachments.getFirst().getContent()).isEqualTo("someContent");
			assertThat(attachments.getFirst().getContentType()).isEqualTo("someContentType");
			assertThat(attachments.getFirst().getEnvelopeType()).isEqualByComparingTo(EnvelopeType.PLAIN);
		});
	}
}
