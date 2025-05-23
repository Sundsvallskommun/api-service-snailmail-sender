package se.sundsvall.snailmail.integration.db.model;

import static lombok.AccessLevel.PACKAGE;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.With;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = PACKAGE)
@Builder(setterPrefix = "with")
@With(PACKAGE)
@Entity
@Table(name = "recipient")
public class RecipientEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "given_name")
	private String givenName;

	@Column(name = "last_name")
	private String lastName;

	@Column(name = "care_of")
	private String careOf;

	@Column(name = "address")
	private String address;

	@Column(name = "apartment_number")
	private String apartmentNumber;

	@Column(name = "postal_code")
	private String postalCode;

	@Column(name = "city")
	private String city;

	@OneToOne(mappedBy = "recipientEntity")
	private RequestEntity requestEntity;

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o instanceof RecipientEntity other) {
			return id != null && id.equals(other.id);
		}

		return false;
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}
}
