package se.sundsvall.snailmail.integration.db.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.With;
import org.hibernate.annotations.TimeZoneStorage;
import org.springframework.data.domain.Persistable;

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
public class BatchEntity implements Persistable<String> {

	@Id
	@Column(name = "id")
	private String id;

	@Column(name = "sent_by")
	private String sentBy;

	@OneToMany(mappedBy = "batchEntity", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private List<DepartmentEntity> departmentEntities;

	@Column(name = "municipality_id", length = 4)
	private String municipalityId;

	@Column(name = "created")
	@TimeZoneStorage(NORMALIZE)
	private OffsetDateTime created;

	// The id is assigned (not generated), so Spring Data would treat a non-null id as an existing row and call merge()
	// instead of persist(). A merge re-SELECTs and, if a concurrent request already committed the row, silently turns
	// into an UPDATE that overwrites columns such as created (set only by @PrePersist on insert). Reporting the entity
	// as new forces persist(), so a lost race fails with a primary-key violation that BatchService catches and
	// recovers from by re-fetching. @PostLoad/@PostPersist flip the flag so a loaded entity is correctly merged.
	@Builder.Default
	@Transient
	private boolean isNew = true;

	@PrePersist
	public void prePersist() {
		// Belt-and-suspenders: the mapper already sets created; only default it if the caller left it unset, so
		// neither a persist nor a (merge-triggered) update can leave created null.
		if (created == null) {
			this.created = OffsetDateTime.now(ZoneId.systemDefault());
		}
	}

	@Override
	public boolean isNew() {
		return isNew;
	}

	@PostLoad
	@PostPersist
	void markNotNew() {
		this.isNew = false;
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
