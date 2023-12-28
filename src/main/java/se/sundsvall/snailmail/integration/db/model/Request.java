package se.sundsvall.snailmail.integration.db.model;

import java.util.List;

import jakarta.persistence.CascadeType;
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
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(setterPrefix = "with")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "request", uniqueConstraints = {
	@UniqueConstraint(name = "uq_request_recipient", columnNames = "recipient_id")
})
public class Request {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "department_id", foreignKey = @ForeignKey(name = "fk_request_department"))
	private Department department;

	private String deviation;

	@OneToMany(mappedBy = "request", cascade = CascadeType.ALL)
	private List<Attachment> attachments;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "recipient_id", foreignKey = @ForeignKey(name = "fk_request_recipient"))
	private Recipient recipient;

}
