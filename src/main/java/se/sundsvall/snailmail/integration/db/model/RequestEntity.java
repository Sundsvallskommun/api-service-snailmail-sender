package se.sundsvall.snailmail.integration.db.model;

import static lombok.AccessLevel.PACKAGE;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.List;
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
@Table(name = "request", uniqueConstraints = @UniqueConstraint(name = "uq_request_recipient", columnNames = "recipient_id"))
public class RequestEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@ManyToOne
	@JoinColumn(name = "department_id", foreignKey = @ForeignKey(name = "fk_request_department"))
	private DepartmentEntity departmentEntity;

	@Column(name = "deviation")
	private String deviation;

	@OneToMany(mappedBy = "requestEntity", cascade = CascadeType.ALL)
	private List<AttachmentEntity> attachmentEntities;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "recipient_id", foreignKey = @ForeignKey(name = "fk_request_recipient"))
	private RecipientEntity recipientEntity;

	@Column(name = "party_id")
	private String partyId;

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o instanceof RequestEntity other) {
			return id != null && id.equals(other.id);
		}

		return false;
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}
}
