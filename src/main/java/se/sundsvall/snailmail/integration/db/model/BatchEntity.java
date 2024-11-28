package se.sundsvall.snailmail.integration.db.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.With;
import org.hibernate.annotations.TimeZoneStorage;

import java.time.OffsetDateTime;
import java.util.List;

import static lombok.AccessLevel.PACKAGE;
import static org.hibernate.annotations.TimeZoneStorageType.NORMALIZE;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = PACKAGE)
@Builder(setterPrefix = "with")
@With(PACKAGE)
@Entity
@Table(name = "batch", indexes = @Index(name = "idx_batch_municipality_id", columnList = "municipality_id"))
public class BatchEntity {

	@Id
	@Column(name = "id")
	private String id;

	@Column(name = "issuer")
	private String issuer;

	@OneToMany(mappedBy = "batchEntity", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private List<DepartmentEntity> departmentEntities;

	@Column(name = "municipality_id")
	private String municipalityId;

	@Column(name = "created")
	@TimeZoneStorage(NORMALIZE)
	private OffsetDateTime created;

	@PrePersist
	public void prePersist() {
		this.created = OffsetDateTime.now();
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o instanceof BatchEntity other) {
			return id != null && id.equals(other.id);
		}

		return false;
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}
}
