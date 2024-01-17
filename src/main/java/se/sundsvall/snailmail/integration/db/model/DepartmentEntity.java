package se.sundsvall.snailmail.integration.db.model;


import java.util.List;

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
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
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

}
