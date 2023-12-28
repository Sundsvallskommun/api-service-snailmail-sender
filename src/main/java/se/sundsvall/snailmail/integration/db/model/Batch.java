package se.sundsvall.snailmail.integration.db.model;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(setterPrefix = "with")
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Batch {

	@Id
	private String id;

	@OneToMany(mappedBy = "batch", cascade = CascadeType.ALL)
	private List<Department> departments;

}
