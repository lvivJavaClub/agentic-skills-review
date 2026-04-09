package club.lvivjava.kalah.domain;

public final class GameNotFoundException extends DomainException {
    public GameNotFoundException() {
        super("not_found", "Game not found");
    }
}
