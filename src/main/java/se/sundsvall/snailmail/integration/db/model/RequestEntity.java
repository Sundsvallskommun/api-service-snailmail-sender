package se.sundsvall.snailmail.integration.db.model;

import java.util.List;
import java.util.Objects;

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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder(setterPrefix = "with")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "request", uniqueConstraints = {
	@UniqueConstraint(name = "uq_request_recipient", columnNames = "recipient_id")
})
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
	public String toString() {
		return "RequestEntity{" +
			"id=" + id +
			", departmentEntity=" + departmentEntity +
			", deviation='" + deviation + '\'' +
			", attachmentEntities=" + attachmentEntities +
			", recipientEntity=" + recipientEntity +
			", partyId='" + partyId + '\'' +
			'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof RequestEntity that)) return false;
		return Objects.equals(id, that.id) && Objects.equals(deviation, that.deviation) && Objects.equals(partyId, that.partyId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, deviation, partyId);
	}
}
