package se.sundsvall.snailmail.integration.db.model;

import java.util.List;
import java.util.Objects;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
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
@Table(name = "batch")
public class BatchEntity {

	@Id
	@Column(name = "id")
	private String id;

	@OneToMany(mappedBy = "batchEntity", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private List<DepartmentEntity> departmentEntities;

	@Override
	public String toString() {
		return "BatchEntity{" +
			"id='" + id + '\'' +
			", departmentEntities=" + departmentEntities +
			'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof BatchEntity that)) return false;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}
}
