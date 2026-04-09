package club.lvivjava.kalah.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record JoinGameRequest(
        @NotBlank String invite_code
) {}
