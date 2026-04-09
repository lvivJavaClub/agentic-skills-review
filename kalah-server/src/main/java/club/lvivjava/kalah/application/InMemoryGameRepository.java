package club.lvivjava.kalah.application;

import club.lvivjava.kalah.domain.KalahGame;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryGameRepository implements GameRepository {

    private final Map<UUID, KalahGame> byId = new ConcurrentHashMap<>();
    private final Map<String, KalahGame> byInvite = new ConcurrentHashMap<>();

    @Override
    public void save(KalahGame game) {
        byId.put(game.getId(), game);
        byInvite.put(game.getInviteCode(), game);
    }

    @Override
    public Optional<KalahGame> findById(UUID id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public Optional<KalahGame> findByInviteCode(String inviteCode) {
        return Optional.ofNullable(byInvite.get(inviteCode));
    }
}
