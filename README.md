# Pokémon Priority Battle Arena

## Project Overview

Pokémon Priority Battle Arena is a terminal-based Java battle simulator created for a second-year Data Structures and Algorithms project. A human player selects three Pokémon and battles a three-Pokémon CPU team. One Pokémon from each team is active at a time, and fainted Pokémon are replaced automatically.

The project uses a small CSV dataset and a Game Boy-inspired terminal interface. It can run with ANSI colors or in a plain-text mode.

## Main DSA Requirement

The main data structure is a manually implemented, array-based max-heap. The heap stores `BattleAction[]`, not `Move[]`. Each `BattleAction` connects an acting Pokémon, its selected move, its target, and a deterministic sequence number.

The implementation demonstrates insertion, peek, extract-max, heapify-up, heapify-down, size, empty checks, clearing, and array resizing without using Java's `PriorityQueue`.

## Final Game Features

- One human player versus one CPU
- Three Pokémon per team with one active Pokémon on each side
- Four moves per Pokémon
- HP, PP, Attack, Defense, Speed, move power, accuracy, and priority
- Five supported types: Normal, Fire, Water, Grass, and Electric
- Simplified type effectiveness and damage calculation
- Automatic replacement after a Pokémon faints
- Victory and defeat results
- Custom max-heap action scheduling
- Game Boy-inspired terminal interface
- ANSI true-color theme with a no-color option
- CSV-based Pokémon and move loading

## Custom Max-Heap Action Ordering

Each selected turn action is inserted into `BattleActionMaxHeap`. Actions rank from highest to lowest using this order:

1. Higher move priority
2. Higher actor Pokémon Speed
3. Lower sequence number

The sequence number provides a deterministic result when move priority and Speed are equal. The battle engine repeatedly calls `extractMax()` to determine the actual action order, and that extracted order is shown on the turn-result screen.

The main heap operation complexities are:

- `insert()` — O(log n)
- `peek()` — O(1)
- `extractMax()` — O(log n)
- Array resizing — O(n)

## Project Structure

```text
PokemonPriorityBattleArena/
├── data/              Pokémon and move CSV files
├── docs/              Dataset documentation
├── out/               Compiled class files, created locally and ignored by Git
├── src/               Game source code and dependency-free test classes
├── .gitignore
└── README.md
```

All Java classes use the default package. The project does not require Maven, Gradle, JUnit, or external libraries.

## Requirements

- A Java Development Kit containing `javac` and `java`
- A terminal or command prompt
- UTF-8 support for correctly displaying the project text
- A terminal width of about 94 columns for the widest battle screens

Run all commands from the project root directory.

## Compilation Instructions

```text
javac -encoding UTF-8 -d out src/*.java
```

## Running the Game

```text
java -cp out Main
```

Select `Start Battle`, choose three unique Pokémon, and then select moves by entering their displayed numbers.

## Running Without ANSI Colors

```text
java -cp out Main --no-color
```

The game also disables ANSI colors when the `NO_COLOR` environment variable is present.

## Running the Tests

The test classes use small custom check helpers and do not require JUnit.

```text
java -cp out ModelTest
java -cp out BattleActionTest
java -cp out BattleActionMaxHeapTest
java -cp out PokemonDataLoaderTest
java -cp out TeamSelectionTest
java -cp out BattleRulesTest
java -cp out BattleGameTest
```

Compile the project before running these commands.

## Dataset Summary

The local dataset contains eight Pokémon and 24 unique moves. Each Pokémon row stores simplified battle statistics and four move references. Each move row stores its type, power, accuracy, priority, and maximum PP.

`PokemonDataLoader` reads `data/pokemon.csv` and `data/moves.csv`, validates their references, and creates the Pokémon and independent Move objects used during play. More details and PokéAPI attribution are available in [docs/DATASET.md](docs/DATASET.md).

## Simplifications and Limitations

This is a focused academic simulator, not a complete implementation of the official Pokémon games. It intentionally excludes items, status conditions, critical hits, STAB, healing moves, stat changes, manual switching, manual target selection, running away, levels, experience, evolution, catching, saved battles, Struggle, and the full official damage formula.

The CPU selects the first available move with remaining PP. Pokémon have one simplified type, and only eight Pokémon are included. Terminal appearance depends on ANSI support and available width; use `--no-color` if escape codes are displayed incorrectly.

## Final Project Status

The planned simplified game is complete. Team selection, data loading, custom heap scheduling, battle execution, terminal presentation, and the dependency-free test suite are implemented and ready for academic submission.
