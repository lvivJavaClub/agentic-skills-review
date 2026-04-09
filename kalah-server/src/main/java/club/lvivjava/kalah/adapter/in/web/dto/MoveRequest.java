package club.lvivjava.kalah.adapter.in.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record MoveRequest(
        @NotNull @Min(0) @Max(5) Integer pit_index
) {}
