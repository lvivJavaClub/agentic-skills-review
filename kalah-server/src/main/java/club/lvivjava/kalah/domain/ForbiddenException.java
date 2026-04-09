package club.lvivjava.kalah.domain;

public final class ForbiddenException extends DomainException {
    public ForbiddenException(String message) {
        super("forbidden", message);
    }
}
