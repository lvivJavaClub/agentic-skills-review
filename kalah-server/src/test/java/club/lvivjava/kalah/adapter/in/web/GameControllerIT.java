package club.lvivjava.kalah.adapter.in.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GameControllerIT {

    @Autowired
    MockMvc mvc;

    @Test
    void createJoinAndGet() throws Exception {
        UUID alice = UUID.randomUUID();
        UUID bob = UUID.randomUUID();

        MvcResult created = mvc.perform(post("/api/v1/games").header("Authorization", bearer(alice)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.your_side").value("south"))
                .andExpect(jsonPath("$.data.status").value("waiting_opponent"))
                .andReturn();

        ObjectMapper om = new ObjectMapper();
        JsonNode root = om.readTree(created.getResponse().getContentAsString());
        String gameId = root.get("data").get("id").asText();
        String invite = root.get("data").get("invite_code").asText();

        mvc.perform(post("/api/v1/games/join")
                        .header("Authorization", bearer(bob))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"invite_code\":\"" + invite + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("active"));

        mvc.perform(get("/api/v1/games/" + gameId).header("Authorization", bearer(alice)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.current_turn").value("south"))
                .andExpect(jsonPath("$.data.legal_pits.length()").value(6));
    }

    private static String bearer(UUID id) {
        return "Bearer " + id;
    }
}
