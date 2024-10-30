package se.sundsvall.snailmail.integration.db.model;

import java.util.Objects;

import se.sundsvall.snailmail.api.model.EnvelopeType;

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

	@Override
	public String toString() {
		return "AttachmentEntity{" +
			"id=" + id +
			", requestEntity=" + requestEntity +
			", content='" + content + '\'' +
			", name='" + name + '\'' +
			", contentType='" + contentType + '\'' +
			", envelopeType=" + envelopeType +
			'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof AttachmentEntity that))
			return false;
		return Objects.equals(id, that.id) && Objects.equals(content, that.content) && Objects.equals(name, that.name) && Objects.equals(contentType, that.contentType) && envelopeType == that.envelopeType;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, content, name, contentType, envelopeType);
	}
}
