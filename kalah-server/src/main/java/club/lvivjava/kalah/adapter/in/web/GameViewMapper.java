package club.lvivjava.kalah.adapter.in.web;

import club.lvivjava.kalah.adapter.in.web.dto.GameView;
import club.lvivjava.kalah.application.GameService;
import club.lvivjava.kalah.domain.GameStatus;
import club.lvivjava.kalah.domain.KalahGame;
import club.lvivjava.kalah.domain.PlayerSide;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class GameViewMapper {

    private final GameService gameService;

    public GameViewMapper(GameService gameService) {
        this.gameService = gameService;
    }

    public GameView toView(KalahGame g, UUID viewerId) {
        int[] b = g.getBoardSnapshot();
        int[] south = new int[KalahGame.PITS_PER_SIDE];
        int[] north = new int[KalahGame.PITS_PER_SIDE];
        System.arraycopy(b, 0, south, 0, 6);
        System.arraycopy(b, 7, north, 0, 6);

        PlayerSide yours = g.sideForUser(viewerId);
        String yourSide = yours == null ? null : yours.name().toLowerCase();

        return new GameView(
                g.getId(),
                mapStatus(g.getStatus()),
                yourSide,
                g.getInviteCode(),
                new GameView.BoardView(south, north, b[KalahGame.SOUTH_STORE], b[KalahGame.NORTH_STORE]),
                g.getCurrentTurn() == null ? null : g.getCurrentTurn().name().toLowerCase(),
                gameService.legalPits(g, viewerId),
                new GameView.PlayersView(
                        g.getSouthUserId().toString(),
                        g.getNorthUserId() == null ? null : g.getNorthUserId().toString()
                ),
                g.getWinner() == null ? null : g.getWinner().name().toLowerCase(),
                g.getUpdatedAt().toString()
        );
    }

    private static String mapStatus(GameStatus s) {
        return switch (s) {
            case WAITING_OPPONENT -> "waiting_opponent";
            case ACTIVE -> "active";
            case FINISHED -> "finished";
        };
    }
}
