package club.lvivjava.kalah.domain;

public sealed class DomainException extends RuntimeException
        permits GameNotFoundException, ForbiddenException, ConflictException {

    private final String code;

    protected DomainException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
