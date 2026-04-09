# Kalah server

Spring Boot REST API for two-player Kalah. See [../docs/kalah/ARCHITECTURE.md](../docs/kalah/ARCHITECTURE.md).

## Run

```bash
cd kalah-server
mvn spring-boot:run
```

## Try (two UUIDs)

```bash
export A=00000000-0000-0000-0000-000000000001
export B=00000000-0000-0000-0000-000000000002

curl -s -D - -o /tmp/g.json -X POST http://localhost:8080/api/v1/games \
  -H "Authorization: Bearer $A" -H "Content-Type: application/json"
GAME_ID=$(jq -r .data.id /tmp/g.json)
CODE=$(jq -r .data.invite_code /tmp/g.json)

curl -s -X POST http://localhost:8080/api/v1/games/join \
  -H "Authorization: Bearer $B" -H "Content-Type: application/json" \
  -d "{\"invite_code\":\"$CODE\"}"

curl -s -X POST "http://localhost:8080/api/v1/games/$GAME_ID/moves" \
  -H "Authorization: Bearer $A" -H "Content-Type: application/json" \
  -d '{"pit_index":0}'
```

Requires `jq` for the snippet; or copy `id` and `invite_code` from the first response manually.
