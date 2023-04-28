package se.sundsvall.snailmail.api.model;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder(setterPrefix = "with")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SendSnailMailRequest {
    
    @NotBlank
    @Schema(description = "Department and unit that should be billed", example = "SBK(Gatuavdelningen, Trafiksektionen)")
    private String department;
    
    @Schema(description = "If the letter to send deviates from the standard", example = "A3 Ritning")
    private String deviation;
    
    @Schema(description = "Attachments")
    private List<@Valid Attachment> attachments;
    
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PACKAGE)
    @Builder(setterPrefix = "with")
    public static class Attachment {
        @Schema(description = "The attachment (file) content as a BASE64-encoded string", example = "aGVsbG8gd29ybGQK")
        @NotBlank
        private String content;
        
        @Schema(description = "The attachment filename", example = "test.txt")
        @NotBlank
        private String name;
        
        @Schema(description = "The attachment content type", example = "text/plain")
        @NotBlank
        private String contentType;
    }
    
    
}
