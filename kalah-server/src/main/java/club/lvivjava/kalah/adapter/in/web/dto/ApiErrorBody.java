package club.lvivjava.kalah.adapter.in.web.dto;

public record ApiErrorBody(ErrorEnvelope error) {

    public record ErrorEnvelope(String code, String message) {}
}
