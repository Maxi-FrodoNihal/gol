# Game of Life - Project Documentation

## Project Overview

This is Conway's Game of Life implementation in Kotlin with a Compose Desktop UI. The project demonstrates a cellular automaton where cells evolve based on simple rules, creating complex patterns.

## Project Goals

### Backend Performance
- **Efficient Coroutine-based Updates**: All backend calculations (matrix updates) must run efficiently using Kotlinx Coroutines
- **Code Reuse**: Leverage existing code structure (`LMatrix`, `BMatrix`, `Lol<T>`) for all computations
- **Parallel Processing**: Utilize `updateEachAsync()` for concurrent row processing

### Frontend Code Quality
- **Modular Design**: Frontend code should be organized into many small, understandable methods
- **Maintainability**: Each function should have a clear, single responsibility
- **Readability**: Code should be self-documenting with clear naming conventions

### Main Feature: Infinite Grid
The primary goal is to implement an infinite Game of Life grid while maintaining all existing functionality.

**Key Requirements:**
1. **Viewport-based Rendering**: Only render visible cells plus a 50-cell buffer zone around the viewport
2. **Smart State Management**:
   - Pass visible cells + buffer to backend for calculation
   - New grid cells are randomly pre-filled on first access
   - Store all visited cell states in memory
3. **Seamless Navigation**: Users can pan/zoom through the infinite grid, with calculations continuing based on stored states
4. **Backward Compatibility**: All current features must remain functional

**Implementation Strategy:**
- Frontend manages viewport and coordinate translation
- Backend receives only the relevant subgrid for calculation
- State cache stores historical cell states for revisited regions
- Random initialization ensures new regions have interesting patterns

## Technology Stack

- **Language**: Kotlin 2.3.0
- **Build Tool**: Gradle 9.2.0
- **UI Framework**: Jetbrains Compose Desktop 1.10.0
- **JVM Toolchain**: Java 25
- **Concurrency**: Kotlinx Coroutines 1.10.0
- **Testing**: JUnit Platform + AssertJ 3.27.7

## Architecture

### Core Components

#### 1. Abstract Matrix Layer (`org.msc.model.abstrakt`)

**`Lol<T>`** - Generic 2D matrix abstraction
- Base class for all matrix types in the project
- Provides common matrix operations: get, set, setEach, updateEach, getEach
- Supports both synchronous and asynchronous updates via `updateEachAsync()`
- Uses coroutines for parallel row processing

#### 2. Game Logic Layer (`org.msc.model`)

**`LElement`** (Life Element) - Individual cell in the game
- Properties: `x`, `y`, `life` (boolean)
- Implements Conway's Game of Life rules:
  - **Birth**: Dead cell with exactly 3 alive neighbors becomes alive
  - **Survival**: Live cell with 2-3 alive neighbors stays alive
  - **Death**: All other cases result in death
- Pre-computes neighbor coordinates for efficiency

**`LMatrix`** - Game state matrix (extends `Lol<LElement>`)
- Manages the grid of LElement cells
- `update(readMatrix: BMatrix): BMatrix` - Applies game rules to all cells

**`BMatrix`** - Boolean matrix for efficient state storage
- Lightweight representation using Boolean values
- Used for reading current state and writing next state
- Companion object provides factory methods

#### 3. View Layer (`org.msc.view`)

**`Viewer`** - Abstract viewer interface
- Base class for different visualization strategies

**`ComposeView`** - Desktop GUI implementation
- Interactive Compose Desktop UI
- Main entry point: `GameOfLife()` composable function

**`ConsoleViewer`** - Console output implementation
- Text-based visualization for debugging/testing

#### 4. Entry Point

**`Main.kt`** - Application launcher
- Creates Compose Desktop window
- Initializes ComposeView

### Data Flow

```
User Interaction
    ↓
ComposeView (UI Layer)
    ↓
LMatrix.update(BMatrix) ← reads current state
    ↓
LElement.update() for each cell ← applies Game of Life rules
    ↓
BMatrix (new state) ← writes next generation
    ↓
ComposeView renders new state
```

## Project Structure

```
src/
├── main/kotlin/org/msc/
│   ├── Main.kt                           # Application entry point
│   ├── model/
│   │   ├── abstrakt/
│   │   │   └── Lol.kt                   # Abstract matrix class
│   │   ├── life/
│   │   │   └── LElement.kt              # Game of Life cell
│   │   ├── LMatrix.kt                   # Main game matrix
│   │   └── BMatrix.kt                   # Boolean state matrix
│   └── view/
│       ├── abstrakt/
│       │   └── Viewer.kt                # Abstract viewer
│       ├── ComposeView.kt               # Compose Desktop UI
│       └── ConsoleViewer.kt             # Console output
└── test/kotlin/org/msc/
    └── GolRuleTest.kt                   # Game rules tests
```

## Building and Running

### Build the project
```bash
./gradlew build
```

### Run the application
```bash
./gradlew run
```

### Run tests
```bash
./gradlew test
```

## Development Guidelines

### Code Style
- Package naming: `org.msc.<layer>`
- Use Kotlin idiomatic features: data classes, extension functions, lambda expressions
- Prefer immutability where possible
- Use coroutines for concurrent operations

### Adding New Features

#### Adding a new matrix type
1. Extend `Lol<T>` with your element type
2. Implement required initialization in `init` block
3. Add specific operations as needed

#### Adding a new viewer
1. Extend `Viewer` abstract class
2. Implement visualization logic for your target platform
3. Register in Main.kt or make it selectable

#### Modifying Game Rules
- Edit `LElement.update()` method
- Update corresponding tests in `GolRuleTest.kt`

### Testing Strategy
- Unit tests for game rules in `GolRuleTest.kt`
- Test edge cases: boundary conditions, empty grids, stable patterns
- Use AssertJ for fluent assertions

## Known Patterns and Conventions

1. **Matrix Separation**: `LMatrix` (logic) vs `BMatrix` (state) allows clean separation of concerns
2. **Update Pattern**: Read from one matrix, write to another to avoid race conditions
3. **Coroutines**: `updateEachAsync()` enables parallel processing of matrix rows
4. **Companion Objects**: Used for factory methods and shared constants (e.g., `LElement.neighborCoordinates`)

## Common Tasks

### Add a new game pattern preset
1. Create a factory method in `BMatrix.Companion`
2. Add UI button in `ComposeView.GameOfLife()`
3. Initialize pattern with specific cell coordinates

### Optimize performance
- Use `updateEachAsync()` for large grids
- Consider caching neighbor calculations
- Profile before optimizing

### Add export functionality
- Implement new method in `BMatrix` or `LMatrix`
- Add UI controls in `ComposeView`
- Support common formats (RLE, plaintext, etc.)

## Dependencies

Key dependencies and their purposes:
- `kotlinx-coroutines-core` - Async/concurrent matrix updates
- `compose.desktop.currentOs` - Cross-platform UI framework
- `assertj-core` - Fluent test assertions
- `kotlin-test` - Kotlin testing utilities

## Notes

- The name "Lol" for the abstract matrix class appears to be a play on "List of Lists"
- German comments in `LElement.update()`: "Geburt" (Birth), "Überleben" (Survival), "Tod" (Death)
- JVM args include `--enable-native-access=ALL-UNNAMED` for Compose native interop
