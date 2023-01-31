package se.sundsvall.snailmail.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;
import se.sundsvall.snailmail.api.model.SendSnailMailRequest;
import se.sundsvall.snailmail.service.SnailMailService;

import javax.validation.Valid;

@RestController
@Validated
@RequestMapping("/send/snailmail")
@Tag(name = "SnailMailSender", description = "SnailMailSender")
public class SnailMailResource {
    private final SnailMailService snailMailService;

    public SnailMailResource(final SnailMailService snailMailService) {
        this.snailMailService = snailMailService;
    }

    @PostMapping
    @Operation(summary = "Create snailmail")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Successful Operation"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request",
                    content = @Content(schema = @Schema(implementation = Problem.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content(schema = @Schema(implementation = Problem.class))
            )
    })
    ResponseEntity<?> sendSnailMail(
            @Valid @RequestBody SendSnailMailRequest request) {
        snailMailService.sendSnailMail(request);
        return ResponseEntity.ok().build();

    }
}
