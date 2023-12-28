package se.sundsvall.snailmail.integration.db.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "request", uniqueConstraints = {
	@UniqueConstraint(name = "uq_request_recipient", columnNames = "recipient_id")
})
public class Recipient {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;

	String givenName;

	String lastName;

	String co;

	String adress;

	String postalCode;

	String city;

	@OneToOne(mappedBy = "recipient")
	private Request request;

}
