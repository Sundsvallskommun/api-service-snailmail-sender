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

@Getter
@Setter
@Builder(setterPrefix = "with")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "department", indexes = {
	@Index(name = "idx_department_name", columnList = "name")
})
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

	@OneToMany(mappedBy = "departmentEntity", cascade = CascadeType.ALL)
	private List<RequestEntity> requestEntities;

	@Override
	public String toString() {
		return "DepartmentEntity{" +
			"id=" + id +
			", name='" + name + '\'' +
			", batchEntity=" + batchEntity +
			", requestEntities=" + requestEntities +
			'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof DepartmentEntity that))
			return false;
		return Objects.equals(id, that.id) && Objects.equals(name, that.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name);
	}
}
