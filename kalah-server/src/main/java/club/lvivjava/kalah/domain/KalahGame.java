package club.lvivjava.kalah.domain;

import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

/**
 * Kalah with 6 pits per side, 4 seeds per pit. Board indices:
 * 0–5 south pits, 6 south store, 7–12 north pits, 13 north store.
 */
public final class KalahGame {

    public static final int PITS_PER_SIDE = 6;
    public static final int INITIAL_SEEDS = 4;
    public static final int BOARD_LEN = 14;
    public static final int SOUTH_STORE = 6;
    public static final int NORTH_STORE = 13;

    private final UUID id;
    private final String inviteCode;
    private final UUID southUserId;
    private UUID northUserId;
    private GameStatus status;
    private PlayerSide currentTurn;
    private PlayerSide winner;
    private final int[] board;
    private Instant updatedAt;

    private KalahGame(UUID id, String inviteCode, UUID southUserId) {
        this.id = Objects.requireNonNull(id);
        this.inviteCode = Objects.requireNonNull(inviteCode);
        this.southUserId = Objects.requireNonNull(southUserId);
        this.status = GameStatus.WAITING_OPPONENT;
        this.currentTurn = null;
        this.winner = null;
        this.board = new int[BOARD_LEN];
        for (int i = 0; i < 6; i++) {
            board[i] = INITIAL_SEEDS;
            board[7 + i] = INITIAL_SEEDS;
        }
        this.updatedAt = Instant.now();
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    public static KalahGame createHostSouth(UUID gameId, String inviteCode, UUID southUserId) {
        return new KalahGame(gameId, inviteCode, southUserId);
    }

    public UUID getId() {
        return id;
    }

    public String getInviteCode() {
        return inviteCode;
    }

    public UUID getSouthUserId() {
        return southUserId;
    }

    public UUID getNorthUserId() {
        return northUserId;
    }

    public GameStatus getStatus() {
        return status;
    }

    public PlayerSide getCurrentTurn() {
        return currentTurn;
    }

    public PlayerSide getWinner() {
        return winner;
    }

    public int[] getBoardSnapshot() {
        return Arrays.copyOf(board, board.length);
    }

    public void joinNorth(UUID northUserId) {
        if (status != GameStatus.WAITING_OPPONENT) {
            throw new ConflictException("Game is not accepting players");
        }
        if (this.northUserId != null) {
            throw new ConflictException("Game is full");
        }
        if (southUserId.equals(northUserId)) {
            throw new ConflictException("Cannot play against yourself");
        }
        this.northUserId = northUserId;
        this.status = GameStatus.ACTIVE;
        this.currentTurn = PlayerSide.SOUTH;
        touch();
    }

    public PlayerSide sideForUser(UUID userId) {
        if (southUserId.equals(userId)) {
            return PlayerSide.SOUTH;
        }
        if (northUserId != null && northUserId.equals(userId)) {
            return PlayerSide.NORTH;
        }
        return null;
    }

    public void move(UUID userId, int pitIndexRelative) {
        PlayerSide side = requirePlayer(userId);
        if (status != GameStatus.ACTIVE) {
            throw new ConflictException("Game is not active");
        }
        if (currentTurn != side) {
            throw new ConflictException("Not your turn");
        }
        if (pitIndexRelative < 0 || pitIndexRelative >= PITS_PER_SIDE) {
            throw new ConflictException("pit_index must be between 0 and 5");
        }
        int pit = toAbsolutePit(side, pitIndexRelative);
        if (board[pit] == 0) {
            throw new ConflictException("Cannot sow from an empty pit");
        }

        int last = sow(side, pit);
        boolean extraTurn = grantsExtraTurn(side, last);

        if (!extraTurn && lastCaptures(side, last)) {
            capture(side, last);
        }

        if (isGameOver()) {
            finishGame();
        } else if (!extraTurn) {
            currentTurn = side.opposite();
        }
        touch();
    }

    private PlayerSide requirePlayer(UUID userId) {
        PlayerSide s = sideForUser(userId);
        if (s == null) {
            throw new ForbiddenException("You are not a player in this game");
        }
        return s;
    }

    private static int toAbsolutePit(PlayerSide side, int pitIndexRelative) {
        return side == PlayerSide.SOUTH ? pitIndexRelative : 7 + pitIndexRelative;
    }

    private static boolean isOpponentKalah(PlayerSide mover, int index) {
        return (mover == PlayerSide.SOUTH && index == NORTH_STORE)
                || (mover == PlayerSide.NORTH && index == SOUTH_STORE);
    }

    /**
     * @return index where the last seed was dropped
     */
    private int sow(PlayerSide side, int startPit) {
        int seeds = board[startPit];
        board[startPit] = 0;
        int pos = startPit;
        while (seeds > 0) {
            pos = (pos + 1) % BOARD_LEN;
            if (isOpponentKalah(side, pos)) {
                continue;
            }
            board[pos]++;
            seeds--;
        }
        return pos;
    }

    private static boolean grantsExtraTurn(PlayerSide side, int lastIndex) {
        return (side == PlayerSide.SOUTH && lastIndex == SOUTH_STORE)
                || (side == PlayerSide.NORTH && lastIndex == NORTH_STORE);
    }

    private boolean lastCaptures(PlayerSide side, int lastIndex) {
        if (grantsExtraTurn(side, lastIndex)) {
            return false;
        }
        if (!isOwnPit(side, lastIndex)) {
            return false;
        }
        if (board[lastIndex] != 1) {
            return false;
        }
        int opposite = 12 - lastIndex;
        return board[opposite] > 0;
    }

    private static boolean isOwnPit(PlayerSide side, int index) {
        return (side == PlayerSide.SOUTH && index >= 0 && index <= 5)
                || (side == PlayerSide.NORTH && index >= 7 && index <= 12);
    }

    private void capture(PlayerSide side, int ownPit) {
        int opposite = 12 - ownPit;
        int gained = board[ownPit] + board[opposite];
        board[ownPit] = 0;
        board[opposite] = 0;
        if (side == PlayerSide.SOUTH) {
            board[SOUTH_STORE] += gained;
        } else {
            board[NORTH_STORE] += gained;
        }
    }

    private boolean isGameOver() {
        return sumSouthPits() == 0 || sumNorthPits() == 0;
    }

    private int sumSouthPits() {
        int s = 0;
        for (int i = 0; i < 6; i++) {
            s += board[i];
        }
        return s;
    }

    private int sumNorthPits() {
        int s = 0;
        for (int i = 7; i <= 12; i++) {
            s += board[i];
        }
        return s;
    }

    private void finishGame() {
        status = GameStatus.FINISHED;
        if (sumSouthPits() == 0) {
            for (int i = 7; i <= 12; i++) {
                board[NORTH_STORE] += board[i];
                board[i] = 0;
            }
        } else if (sumNorthPits() == 0) {
            for (int i = 0; i < 6; i++) {
                board[SOUTH_STORE] += board[i];
                board[i] = 0;
            }
        }
        int southScore = board[SOUTH_STORE];
        int northScore = board[NORTH_STORE];
        if (southScore > northScore) {
            winner = PlayerSide.SOUTH;
        } else if (northScore > southScore) {
            winner = PlayerSide.NORTH;
        } else {
            winner = null;
        }
        currentTurn = null;
        touch();
    }
}
