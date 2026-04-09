package club.lvivjava.kalah.adapter.in.web;

import club.lvivjava.kalah.adapter.in.web.dto.ApiDataWrapper;
import club.lvivjava.kalah.adapter.in.web.dto.GameView;
import club.lvivjava.kalah.adapter.in.web.dto.JoinGameRequest;
import club.lvivjava.kalah.adapter.in.web.dto.MoveRequest;
import club.lvivjava.kalah.application.GameService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/games")
public class GameController {

    private final GameService gameService;
    private final GameViewMapper mapper;

    public GameController(GameService gameService, GameViewMapper mapper) {
        this.gameService = gameService;
        this.mapper = mapper;
    }

    private static UUID user(HttpServletRequest req) {
        return (UUID) req.getAttribute(SecurityConstants.USER_ID_ATTRIBUTE);
    }

    @PostMapping
    public ResponseEntity<ApiDataWrapper<GameView>> create(HttpServletRequest request) {
        var g = gameService.createGame(user(request));
        GameView view = mapper.toView(g, user(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.LOCATION, "/api/v1/games/" + g.getId())
                .body(new ApiDataWrapper<>(view));
    }

    @PostMapping("/join")
    public ApiDataWrapper<GameView> join(@Valid @RequestBody JoinGameRequest body, HttpServletRequest request) {
        var g = gameService.joinByInvite(body.invite_code(), user(request));
        return new ApiDataWrapper<>(mapper.toView(g, user(request)));
    }

    @GetMapping("/{id}")
    public ApiDataWrapper<GameView> get(@PathVariable UUID id, HttpServletRequest request) {
        var g = gameService.getGame(id, user(request));
        return new ApiDataWrapper<>(mapper.toView(g, user(request)));
    }

    @PostMapping("/{id}/moves")
    public ApiDataWrapper<GameView> move(@PathVariable UUID id, @Valid @RequestBody MoveRequest body,
            HttpServletRequest request) {
        var g = gameService.playMove(id, user(request), body.pit_index());
        return new ApiDataWrapper<>(mapper.toView(g, user(request)));
    }
}
