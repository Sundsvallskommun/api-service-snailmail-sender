package se.sundsvall.snailmail.integration.db.model;

import java.util.Objects;

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

@Getter
@Setter
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

	@Column(name = "apartment_number")
	private String apartmentNumber;

	@Column(name = "postal_code")
	private String postalCode;

	@Column(name = "city")
	private String city;

	@OneToOne(mappedBy = "recipientEntity")
	private RequestEntity requestEntity;

	@Override
	public String toString() {
		return "RecipientEntity{" +
			"id=" + id +
			", givenName='" + givenName + '\'' +
			", lastName='" + lastName + '\'' +
			", careOf='" + careOf + '\'' +
			", address='" + address + '\'' +
			", apartmentNumber='" + apartmentNumber + '\'' +
			", postalCode='" + postalCode + '\'' +
			", city='" + city + '\'' +
			", requestEntity=" + requestEntity +
			'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof RecipientEntity that))
			return false;
		return Objects.equals(id, that.id) && Objects.equals(givenName, that.givenName) && Objects.equals(lastName, that.lastName) && Objects.equals(careOf, that.careOf) && Objects.equals(address, that.address) && Objects.equals(apartmentNumber,
			that.apartmentNumber) && Objects.equals(postalCode, that.postalCode) && Objects.equals(city, that.city);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, givenName, lastName, careOf, address, apartmentNumber, postalCode, city);
	}
}
