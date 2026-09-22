# Pokémon Priority Battle Arena Development Rules

## Project context

This is a three-week academic Data Structures and Algorithms project developed by four students. It is a terminal-based Java game created in Visual Studio Code.

The primary data structure is a custom array-based max-heap used to schedule Pokémon battle actions.

## Development approach

* Work on only one requested milestone at a time.
* Do not generate or redesign the entire project in one turn.
* Inspect the repository before editing.
* Preserve existing files and user-written code.
* Explain what files were changed and why.
* Compile and test every milestone before finishing.
* Report errors honestly instead of hiding them.
* Do not commit, push, pull, merge, or modify Git history unless explicitly requested.
* Keep the implementation understandable to a second-year IT student.
* Avoid unnecessary design patterns, frameworks, dependencies, Maven, or Gradle.
* Keep Java classes focused and reasonably small.
* Add useful comments for algorithms and non-obvious logic, but do not comment every line.

## DSA requirements

* Implement the max-heap manually using a BattleAction array.
* Do not use Java PriorityQueue for the main action scheduler.
* Do not replace the custom heap with another built-in collection.
* The heap must demonstrate insertion, peek, extract-max, heapify-up, heapify-down, size, and empty checks.
* Battle actions are ranked first by move priority, then Pokémon Speed, then a deterministic tie-breaker.
* Make the heap visible through a demonstration or debug display for the academic presentation.

## Planned game scope

* Six playable Pokémon in the initial roster
* Player selects a team of three
* Three-versus-three arena format so several actions enter the heap each round
* Five simplified types: Fire, Water, Grass, Electric, and Normal
* HP, Attack, Defense, Speed, accuracy, move power, move priority, and PP
* Simplified type effectiveness
* Basic computer-controlled opponent
* Status effects and items are optional until the core battle works

## Terminal design

* Use a clean retro battle-console style inspired by classic monster-battling games.
* Use ASCII or terminal-safe Unicode art without making the interface excessively wide.
* Target approximately 100 terminal columns, with readable fallback output for smaller windows.
* Keep the design energetic and gender-neutral.
* Use dark backgrounds, strong red borders, cream highlights, soft-white main text, and warm-gray secondary text.
* Use rose colors sparingly.
* Store all colors and formatting codes in one theme class.
* Always apply an ANSI reset after colored output.
* Provide a way to disable colors if ANSI output is unsupported.
* Store the working game title in one constant because the final title is not yet decided.

## Color palette

* Coral Red: RGB(242, 85, 99)
* Berry Red: RGB(201, 60, 84)
* Cherry Wine: RGB(187, 58, 77)
* Cream Peach: RGB(251, 210, 176)
* Dusty Rose: RGB(229, 165, 146)
* Warm Rose: RGB(212, 135, 123)
* Soft White: RGB(255, 244, 236)
* Warm Gray: RGB(207, 194, 188)

Use true-color ANSI escape sequences for these RGB values. Keep fallback or no-color behavior separate from the visual content.

## Current structure

Keep the project simple:

* `src/` for Java source files
* `data/` for Pokémon and move data
* `docs/` for documentation materials
* `out/` for ignored compiled files

Do not implement future milestones unless the user explicitly asks.
