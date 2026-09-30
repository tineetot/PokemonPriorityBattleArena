# Pokémon Priority Battle Arena

A terminal-based Pokémon battle simulator developed in Java for our Data Structures and Algorithms final project.

The game simulates a simplified Pokémon battle between one player and a computer-controlled opponent. The player selects a team, chooses moves, and battles using a custom max-heap that determines which action is executed first.

## Features

- Game Boy-inspired terminal interface
- Player team selection
- Computer-controlled opponent
- Three Pokémon per team
- Move selection using number input
- HP and PP management
- Move power and accuracy
- Simplified type effectiveness
- Pokémon fainting and automatic replacement
- Victory and defeat conditions
- Turn-order display
- Input validation
- Optional color disabling using `--no-color`

## Data Structures and Algorithms

The main data structure used is a custom array-based max-heap.

During each turn, the player’s and CPU’s selected actions are inserted into the heap. The heap determines which action is executed first based on:

1. Move priority
2. Pokémon Speed
3. Action sequence number as a tie-breaker

The project manually implements:

- Heap insertion
- `heapifyUp`
- `peek`
- `extractMax`
- `heapifyDown`
- Dynamic array resizing

The project does not use Java’s built-in `PriorityQueue` for battle scheduling.

## Simplified Battle Rules

The game includes only the mechanics needed for the project:

- One active Pokémon per side
- One player versus one CPU
- Three Pokémon per team
- One move selected per turn
- Moves consume one PP when attempted
- Missed moves still consume PP
- Damage is based on move power, Attack, Defense, and type effectiveness
- Fainted Pokémon are automatically replaced
- The battle ends when all Pokémon on one team faint

The project intentionally excludes advanced mechanics such as items, status conditions, critical hits, levels, evolution, catching, and manual switching.

## Dataset

The game uses local CSV files stored in the `data/` folder:

- `pokemon.csv`
- `moves.csv`

The dataset contains selected Pokémon and moves used by the simulator. The data was based on information from PokéAPI and simplified to match the project’s supported mechanics.

The program loads the data at runtime, so the game can be played offline after the project files are downloaded.

## Technologies

- Java
- Visual Studio Code
- Git and GitHub
- Custom array-based max-heap
- CSV dataset
- ANSI terminal colors
- ASCII-based terminal interface

## Project Structure

```text
PokemonPriorityBattleArena/
├── data/
│   ├── pokemon.csv
│   └── moves.csv
├── docs/
│   └── DATASET.md
├── src/
│   ├── Main.java
│   ├── TerminalUI.java
│   ├── Pokemon.java
│   ├── Move.java
│   ├── PokemonType.java
│   ├── BattleAction.java
│   ├── BattleActionMaxHeap.java
│   ├── BattleRules.java
│   ├── BattleGame.java
│   ├── PokemonDataLoader.java
│   ├── TeamSelection.java
│   └── test files
└── README.md
```

## How to Run

Open the project folder in the terminal and compile the Java files:

```bash
javac -encoding UTF-8 -d out src/*.java
```

Run the game:

```bash
java -cp out Main
```

To disable terminal colors:

```bash
java -cp out Main --no-color
```

The program should be launched from the project root so the relative paths to the `data/` folder work correctly.

## Basic Controls

From the main menu:

```text
[1] Start Battle
[2] Exit
```

During team selection:

- Enter a number to select a Pokémon
- Enter `0` to return to the main menu

During battle:

- Enter the number of a move
- Enter `0` to leave the battle
- Invalid input and moves with no remaining PP are rejected

## Testing

The project includes test files for the main components, including:

- Pokémon and move models
- Battle-action comparison
- Custom max-heap operations
- Dataset loading
- Team selection
- Battle rules
- Complete battle behavior

The tests can be compiled together with the project:

```bash
javac -encoding UTF-8 -d out src/*.java
```

Each test class can then be run separately using:

```bash
java -cp out ModelTest
java -cp out BattleActionTest
java -cp out BattleActionMaxHeapTest
java -cp out PokemonDataLoaderTest
java -cp out TeamSelectionTest
java -cp out BattleRulesTest
java -cp out BattleGameTest
```

## Project Status

The core battle system and terminal interface are implemented and currently being tested and refined.

The project is still under development while the group completes:

- Final interface improvements
- Additional manual testing
- Documentation and screenshots
- Presentation preparation
- Final code review and packaging

## Learning Objective

This project demonstrates how a custom max-heap can solve a practical scheduling problem inside a turn-based battle system. Instead of executing actions based only on input order, the program organizes all selected actions and extracts the highest-priority action first.

This allows the group to demonstrate the use of arrays, object-oriented programming, heap operations, comparison rules, validation, file loading, and algorithmic complexity in one interactive Java application.
