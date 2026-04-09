package club.lvivjava.kalah.adapter.in.web.dto;

import java.util.List;
import java.util.UUID;

public record GameView(
        UUID id,
        String status,
        String your_side,
        String invite_code,
        BoardView board,
        String current_turn,
        List<Integer> legal_pits,
        PlayersView players,
        String winner,
        String updated_at
) {

    public record BoardView(
            int[] pits_south,
            int[] pits_north,
            int store_south,
            int store_north
    ) {}

    public record PlayersView(String south_user_id, String north_user_id) {}
}
