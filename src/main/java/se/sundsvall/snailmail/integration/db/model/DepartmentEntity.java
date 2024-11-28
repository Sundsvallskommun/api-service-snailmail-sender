package se.sundsvall.snailmail.integration.db.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.With;

import java.util.List;

import static lombok.AccessLevel.PACKAGE;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = PACKAGE)
@Builder(setterPrefix = "with")
@With(PACKAGE)
@Entity
@Table(name = "department", indexes = @Index(name = "idx_department_name", columnList = "name"))
public class DepartmentEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "name")
	private String name;

	@ManyToOne
	@JoinColumn(name = "batch_id", foreignKey = @ForeignKey(name = "fk_department_batch"))
	private BatchEntity batchEntity;

	@OneToMany(mappedBy = "departmentEntity", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private List<RequestEntity> requestEntities;

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o instanceof DepartmentEntity other) {
			return id != null && id.equals(other.id);
		}

		return false;
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}

}
