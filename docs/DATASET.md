# Pokémon Priority Battle Arena Dataset

## 1. Dataset Overview

This is a small, curated dataset derived from PokéAPI for Pokémon Priority Battle Arena. It contains eight Pokémon and 24 unique moves. The limited scope keeps the data understandable and appropriate for a three-week academic Data Structures and Algorithms project.

The files are stored locally, so the game does not require an internet connection.

## 2. Files Included

- `data/pokemon.csv` contains the eight playable Pokémon, their simplified battle statistics, and four move references each.
- `data/moves.csv` contains every unique move referenced by `pokemon.csv`.
- `docs/DATASET.md` documents the source, columns, transformations, intended use, and limitations.

## 3. Data Source

The data was retrieved from PokéAPI v2, a read-only Pokémon data API.

The following endpoint patterns were used:

- Pokémon: `https://pokeapi.co/api/v2/pokemon/{name-or-id}`
- Moves: `https://pokeapi.co/api/v2/move/{name-or-id}`

For each Pokémon, the API response supplied base statistics, types, and the available move list. For each move, the response supplied its English display name, type, power, accuracy, priority, and PP.

## 4. Date Accessed

PokéAPI data was accessed on September 28, 2026.

## 5. Pokémon CSV Columns

| Column | Description |
|---|---|
| `id` | Unique lowercase, hyphenated Pokémon identifier. |
| `name` | Human-readable Pokémon name. |
| `type` | One supported project type: `NORMAL`, `FIRE`, `WATER`, `GRASS`, or `ELECTRIC`. |
| `hp` | PokéAPI HP base stat. |
| `attack` | PokéAPI Attack base stat. |
| `defense` | PokéAPI Defense base stat. |
| `speed` | PokéAPI Speed base stat. |
| `move_1`–`move_4` | Move IDs referencing rows in `moves.csv`. |

## 6. Move CSV Columns

| Column | Description |
|---|---|
| `id` | Unique lowercase, hyphenated move identifier. |
| `name` | English human-readable move name from PokéAPI. |
| `type` | Uppercase move type matching `PokemonType`. |
| `power` | PokéAPI base power, or `0` when PokéAPI has no fixed numeric power. |
| `accuracy` | PokéAPI accuracy from 1 through 100. |
| `priority` | PokéAPI move priority; higher values act sooner. |
| `max_pp` | PokéAPI PP value used as the move's maximum PP. |

## 7. Data Transformations and Simplifications

- PokéAPI identifiers remain lowercase and hyphenated so CSV references match reliably.
- English move names were selected from each move resource's localized names.
- API type identifiers were converted to uppercase to match the `PokemonType` enum.
- Only HP, Attack, Defense, and Speed are stored. Special Attack and Special Defense are omitted because the current battle model is simplified.
- Only one supported type is stored for each Pokémon. Bulbasaur and Oddish use their primary `GRASS` type, while Magnemite uses its primary `ELECTRIC` type.
- Exactly four verified moves are assigned to each Pokémon.
- Discharge has a fixed base power, so its PokéAPI `power` value is stored directly without a sentinel or gameplay adjustment.
- No statistics were increased, decreased, or rebalanced.

## 8. How the Dataset Is Used

`PokemonDataLoader` reads both local CSV files, validates their records and move references, and creates the `Pokemon` and `Move` objects used by the game. Each Pokémon receives its own independent `Move` instances so that PP changes during one Pokémon's battle do not affect another Pokémon.

During each turn, the selected moves are represented by `BattleAction` objects. The custom max-heap ranks those actions by move priority, then actor Pokémon Speed, then a generated sequence number as the deterministic final tie-breaker. The sequence number is runtime state and is not stored in the dataset.

Current PP is also runtime state and is not stored. Each loaded move begins with `currentPp = maxPp`, and valid attempted moves consume PP during the integrated battle.

## 9. Limitations

- The dataset contains only eight Pokémon rather than the full Pokédex.
- Each Pokémon has exactly four curated moves rather than its complete learnset.
- Dual types are simplified to one supported primary type.
- Special Attack and Special Defense are not represented.
- Damage classes, status effects, secondary effects, and type effectiveness are not represented.
- Data reflects PokéAPI responses on the access date and may differ from later API revisions.
- This project is for educational use and is not affiliated with or endorsed by The Pokémon Company.

## 10. References

- https://pokeapi.co/
- https://pokeapi.co/docs/v2
