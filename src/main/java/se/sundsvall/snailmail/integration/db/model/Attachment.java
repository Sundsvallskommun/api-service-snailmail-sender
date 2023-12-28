package se.sundsvall.snailmail.integration.db.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import se.sundsvall.snailmail.api.model.EnvelopeType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(setterPrefix = "with")
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Attachment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "request_id")
	private Request request;

	private String content;

	private String name;

	private String contentType;

	private EnvelopeType envelopeType;

}
