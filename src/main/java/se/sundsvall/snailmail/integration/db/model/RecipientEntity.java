package se.sundsvall.snailmail.integration.db.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor
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

	@Column(name = "postal_code")
	private String postalCode;

	@Column(name = "city")
	private String city;

	@OneToOne(mappedBy = "recipientEntity")
	private RequestEntity requestEntity;

}
