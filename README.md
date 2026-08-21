# PlaytimeRewards

A Minecraft plugin for Paper 1.21+ (Java 21) that rewards players for time
spent on the server through a 16-candle (4x4) GUI, where each candle
represents a playtime milestone (30 minutes up to 120 hours).

## Features

- GUI opened with `/playtimerewards` (alias `/pt`)
- 16 fully configurable rewards in `config.yml`
- Playtime tracking with AFK detection (no movement = time doesn't count)
- Automatic data saving every 5 minutes (configurable), plus an instant
  save the moment a reward is claimed (protects against duplicate claims
  even after a hard server crash)
- Player data stored in YAML (`/plugins/PlaytimeRewards/playerdata/`)
- Multi-language messages via `messages.yml`
- Admin commands: `reload`, `time`, `add`, `reset`

## Build

Requires Maven and JDK 21:

```bash
mvn clean package
```

The resulting `.jar` will be in `target/PlaytimeRewards.jar` — drop it into
the `plugins/` folder on a Paper 1.21+ server.

## Commands

| Command | Description |
|---|---|
| `/pt` | Opens the rewards GUI |
| `/pt reload` | Reloads the configuration |
| `/pt time [player]` | Shows playtime |
| `/pt add <player> <minutes>` | Adds playtime (debug) |
| `/pt reset <player>` | Resets a player's data |

## Permissions

- `playtimerewards.use`
- `playtimerewards.reload`
- `playtimerewards.time.others`
- `playtimerewards.add`
- `playtimerewards.claim.<name>`
- `playtimerewards.admin`
