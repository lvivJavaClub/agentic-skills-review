package club.lvivjava.kalah.domain;

public enum PlayerSide {
    SOUTH,
    NORTH;

    public PlayerSide opposite() {
        return this == SOUTH ? NORTH : SOUTH;
    }
}
