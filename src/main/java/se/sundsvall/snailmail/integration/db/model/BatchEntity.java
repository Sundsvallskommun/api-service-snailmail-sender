package se.sundsvall.snailmail.integration.db.model;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(setterPrefix = "with")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "batch")
public class BatchEntity {

	@Id
	@Column(name = "id")
	private String id;

	@OneToMany(mappedBy = "batchEntity", cascade = CascadeType.ALL)
	private List<DepartmentEntity> departmentEntities;

}
