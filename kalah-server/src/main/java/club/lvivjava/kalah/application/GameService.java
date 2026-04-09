package club.lvivjava.kalah.application;

import club.lvivjava.kalah.domain.GameNotFoundException;
import club.lvivjava.kalah.domain.KalahGame;
import club.lvivjava.kalah.domain.PlayerSide;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class GameService {

    private static final String INVITE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int INVITE_LEN = 6;

    private final GameRepository games;
    private final SecureRandom random = new SecureRandom();

    public GameService(GameRepository games) {
        this.games = games;
    }

    public KalahGame createGame(UUID southUserId) {
        String code = newInviteCode();
        while (games.findByInviteCode(code).isPresent()) {
            code = newInviteCode();
        }
        KalahGame g = KalahGame.createHostSouth(UUID.randomUUID(), code, southUserId);
        games.save(g);
        return g;
    }

    public KalahGame joinByInvite(String inviteCode, UUID northUserId) {
        KalahGame g = games.findByInviteCode(inviteCode.trim().toUpperCase())
                .orElseThrow(GameNotFoundException::new);
        g.joinNorth(northUserId);
        games.save(g);
        return g;
    }

    public KalahGame getGame(UUID gameId, UUID viewerId) {
        KalahGame g = games.findById(gameId).orElseThrow(GameNotFoundException::new);
        if (g.sideForUser(viewerId) == null) {
            throw new club.lvivjava.kalah.domain.ForbiddenException("You are not a player in this game");
        }
        return g;
    }

    public KalahGame playMove(UUID gameId, UUID userId, int pitIndex) {
        KalahGame g = games.findById(gameId).orElseThrow(GameNotFoundException::new);
        g.move(userId, pitIndex);
        games.save(g);
        return g;
    }

    public List<Integer> legalPits(KalahGame g, UUID viewerId) {
        PlayerSide side = g.sideForUser(viewerId);
        if (side == null || g.getCurrentTurn() == null || g.getCurrentTurn() != side) {
            return List.of();
        }
        int[] b = g.getBoardSnapshot();
        List<Integer> legal = new ArrayList<>();
        for (int i = 0; i < KalahGame.PITS_PER_SIDE; i++) {
            int abs = side == PlayerSide.SOUTH ? i : 7 + i;
            if (b[abs] > 0) {
                legal.add(i);
            }
        }
        return legal;
    }

    private String newInviteCode() {
        StringBuilder sb = new StringBuilder(INVITE_LEN);
        for (int i = 0; i < INVITE_LEN; i++) {
            sb.append(INVITE_ALPHABET.charAt(random.nextInt(INVITE_ALPHABET.length())));
        }
        return sb.toString();
    }
}
