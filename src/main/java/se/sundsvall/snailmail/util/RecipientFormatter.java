package se.sundsvall.snailmail.util;

import org.apache.commons.lang3.StringUtils;
import se.sundsvall.snailmail.integration.db.model.RecipientEntity;

public final class RecipientFormatter {

	private RecipientFormatter() {}

	/**
	 * Formats the recipient name for output on a physical letter address line.
	 * <ul>
	 * <li>Organization name and person name → {@code "{organizationName} (att: {firstName} {lastName})"}</li>
	 * <li>Organization name only → {@code "{organizationName}"}</li>
	 * <li>Otherwise → {@code "{givenName} {lastName}"}</li>
	 * </ul>
	 */
	public static String formatName(final RecipientEntity recipient) {
		final var givenName = recipient.getGivenName();
		final var lastName = recipient.getLastName();
		final var organizationName = recipient.getOrganizationName();
		final var hasPersonName = StringUtils.isNotBlank(givenName) && StringUtils.isNotBlank(lastName);
		final var hasOrganizationName = StringUtils.isNotBlank(organizationName);

		if (hasOrganizationName && hasPersonName) {
			return "%s (att: %s %s)".formatted(organizationName, givenName, lastName);
		}
		if (hasOrganizationName) {
			return organizationName;
		}
		return givenName + " " + lastName;
	}
}
