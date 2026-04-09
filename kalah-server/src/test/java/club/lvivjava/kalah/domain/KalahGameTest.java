package club.lvivjava.kalah.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KalahGameTest {

    private static final UUID SOUTH = UUID.randomUUID();
    private static final UUID NORTH = UUID.randomUUID();

    @Test
    void initialBoard_hasFourSeedsPerPit() {
        KalahGame g = KalahGame.createHostSouth(UUID.randomUUID(), "ABC123", SOUTH);
        int[] b = g.getBoardSnapshot();
        for (int i = 0; i < 6; i++) {
            assertThat(b[i]).isEqualTo(4);
            assertThat(b[7 + i]).isEqualTo(4);
        }
        assertThat(b[KalahGame.SOUTH_STORE]).isZero();
        assertThat(b[KalahGame.NORTH_STORE]).isZero();
    }

    @Test
    void joinNorth_startsWithSouthTurn() {
        KalahGame g = KalahGame.createHostSouth(UUID.randomUUID(), "ABC123", SOUTH);
        g.joinNorth(NORTH);
        assertThat(g.getStatus()).isEqualTo(GameStatus.ACTIVE);
        assertThat(g.getCurrentTurn()).isEqualTo(PlayerSide.SOUTH);
    }

    @Test
    void cannotJoinTwice() {
        KalahGame g = KalahGame.createHostSouth(UUID.randomUUID(), "ABC123", SOUTH);
        g.joinNorth(NORTH);
        assertThatThrownBy(() -> g.joinNorth(UUID.randomUUID())).isInstanceOf(ConflictException.class);
    }

    @Test
    void cannotMoveFromEmptyPit() {
        KalahGame g = newActiveGame();
        g.move(SOUTH, 0);
        assertThatThrownBy(() -> g.move(SOUTH, 0)).isInstanceOf(ConflictException.class);
    }

    @Test
    void cannotMoveOnWrongTurn() {
        KalahGame g = newActiveGame();
        assertThatThrownBy(() -> g.move(NORTH, 0)).isInstanceOf(ConflictException.class);
    }

    @Test
    void gameEventuallyFinishes_withWinnerOrTie() {
        KalahGame g = newActiveGame();
        int guard = 0;
        while (g.getStatus() == GameStatus.ACTIVE && guard++ < 500) {
            PlayerSide t = g.getCurrentTurn();
            UUID uid = t == PlayerSide.SOUTH ? SOUTH : NORTH;
            int[] b = g.getBoardSnapshot();
            int start = t == PlayerSide.SOUTH ? 0 : 7;
            int pit = -1;
            for (int i = 0; i < 6; i++) {
                if (b[start + i] > 0) {
                    pit = i;
                    break;
                }
            }
            assertThat(pit).isGreaterThanOrEqualTo(0);
            g.move(uid, pit);
        }
        assertThat(g.getStatus()).isEqualTo(GameStatus.FINISHED);
        int[] end = g.getBoardSnapshot();
        assertThat(end[KalahGame.SOUTH_STORE] + end[KalahGame.NORTH_STORE]).isEqualTo(48);
    }

    private static KalahGame newActiveGame() {
        KalahGame g = KalahGame.createHostSouth(UUID.randomUUID(), "JOIN1", SOUTH);
        g.joinNorth(NORTH);
        return g;
    }
}
