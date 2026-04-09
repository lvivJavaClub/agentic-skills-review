package club.lvivjava.kalah.domain;

public final class ConflictException extends DomainException {
    public ConflictException(String message) {
        super("conflict", message);
    }
}
