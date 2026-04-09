package club.lvivjava.kalah.application;

import club.lvivjava.kalah.domain.KalahGame;

import java.util.Optional;
import java.util.UUID;

public interface GameRepository {

    void save(KalahGame game);

    Optional<KalahGame> findById(UUID id);

    Optional<KalahGame> findByInviteCode(String inviteCode);
}
