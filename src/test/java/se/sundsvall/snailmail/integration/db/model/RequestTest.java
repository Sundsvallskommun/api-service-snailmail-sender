package se.sundsvall.snailmail.integration.db.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class RequestTest {

	@Test
	void testBean() {
		assertThat(Request.class, allOf(
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
		final var department = Department.builder().build();
		final var recipient = Recipient.builder().build();
		final var attachment = Attachment.builder().build();
		final var deviation = "deviation";


		final var request = Request.builder()
			.withId(id)
			.withDepartment(department)
			.withRecipient(recipient)
			.withAttachments(List.of(attachment))
			.withDeviation(deviation)
			.build();

		Assertions.assertThat(request).isNotNull().hasNoNullFieldsOrProperties();
		Assertions.assertThat(request.getId()).isEqualTo(id);
		Assertions.assertThat(request.getDepartment()).isNotNull();
		Assertions.assertThat(request.getRecipient()).isNotNull();
		Assertions.assertThat(request.getAttachments()).hasSize(1);
		Assertions.assertThat(request.getDeviation()).isEqualTo(deviation);


	}

	@Test
	void testNoDirtOnCreatedBean() {
		Assertions.assertThat(Request.builder().build()).hasAllNullFieldsOrProperties();
		Assertions.assertThat(new Request()).hasAllNullFieldsOrProperties();
	}


}
