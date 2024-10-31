package se.sundsvall.snailmail.integration.db.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import se.sundsvall.snailmail.api.model.EnvelopeType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
@Entity
@Table(name = "attachment")
public class AttachmentEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@ManyToOne
	@JoinColumn(name = "request_id", foreignKey = @ForeignKey(name = "fk_attachment_request"))
	private RequestEntity requestEntity;

	@Lob
	@Column(name = "content", columnDefinition = "longtext")
	private String content;

	@Column(name = "name")
	private String name;

	@Column(name = "content_type")
	private String contentType;

	@Column(name = "envelope_type")
	@Enumerated(EnumType.STRING)
	private EnvelopeType envelopeType;

}
