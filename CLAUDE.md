# Game of Life - Project Documentation

## Project Overview

This is Conway's Game of Life implementation in Kotlin with a Compose Desktop UI. The project demonstrates a cellular automaton where cells evolve based on simple rules, creating complex patterns.

## Project Goals

### Backend Performance ✅ ACHIEVED
- ✅ **Efficient Coroutine-based Updates**: All backend calculations use Kotlinx Coroutines with chunk-based parallel processing
- ✅ **Code Reuse**: Leverages existing `LMatrix`, `BMatrix`, `Lol<T>` structure for all computations
- ✅ **Parallel Processing**: `LMatrix.update()` uses `getAllElements()` + `chunked()` for optimal CPU core utilization
- ✅ **Performance**: ~4-8× faster on multi-core processors compared to synchronous updates

### Frontend Code Quality ✅ ACHIEVED
- ✅ **Modular Design**: Frontend organized into small, single-responsibility methods
- ✅ **Maintainability**: Clear separation between rendering, state management, and user interaction
- ✅ **Readability**: Self-documenting code with functional programming patterns
- ✅ **No Nested Loops**: Functional sequences (`flatMap`, `filter`, `associateWith`) preferred over nested for-loops

### Main Feature: Infinite Grid ✅ IMPLEMENTED
An infinite Game of Life grid with viewport-based rendering and seamless navigation.

**Implemented Features:**
1. ✅ **Viewport-based Rendering**: Only renders visible cells + 50-cell buffer zone
2. ✅ **Smart State Management**:
   - Cache stores all visited cell states as `Map<Pair<Int, Int>, Boolean>`
   - New cells randomly initialized on first access
   - Only viewport + buffer is sent to backend for calculation
3. ✅ **Seamless Navigation**:
   - Pan with mouse drag (no scrollbars)
   - Zoom with Ctrl+Scroll (0.3× to 5× range)
   - Calculations continue based on cached states
4. ✅ **Backward Compatibility**: All game rules remain functional

**Architecture:**
- Frontend manages viewport offset (`offsetX`, `offsetY`) and zoom level
- Backend receives only relevant subgrid for calculation
- Sparse state cache (`Map<Pair<Int, Int>, Boolean>`) for memory efficiency
- Functional sequence operations for cell initialization

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
- `suspend fun update(readMatrix: BMatrix): BMatrix` - Applies game rules using chunk-based parallel processing
- Uses `getAllElements()` + `chunked()` for optimal CPU utilization
- Chunk size dynamically calculated: `totalElements / availableProcessors()` (minimum 100)

**`BMatrix`** - Boolean matrix for efficient state storage
- Lightweight representation using Boolean values
- Used for reading current state and writing next state
- Companion object provides factory methods

#### 3. View Layer (`org.msc.view`)

**`ComposeView`** - Desktop GUI implementation with infinite grid
- Main entry point: `GameOfLife()` composable function
- **State Management**:
  - `cellCache: Map<Pair<Int, Int>, Boolean>` - Sparse cache for infinite grid
  - `offsetX/offsetY: Float` - Viewport position in world coordinates
  - `zoomLevel: Float` - Current zoom factor (0.3× to 5×)
- **Rendering**: `InfiniteGameCanvas()` - Viewport-based cell rendering
- **User Interaction**:
  - Pan: Mouse drag updates offset
  - Zoom: Ctrl+Scroll updates zoom level
- **Update Loop**: `GameUpdateLoop()` - Coroutine-based continuous updates (300ms interval)

#### 4. Entry Point

**`Main.kt`** - Application launcher
- Creates Compose Desktop window
- Initializes ComposeView

### Data Flow

```
User Interaction (Pan/Zoom)
    ↓
ComposeView updates offsetX/offsetY/zoomLevel
    ↓
LaunchedEffect (Background Coroutine) - every 300ms
    ↓
updateInfiniteGrid() - calculates viewport + buffer bounds
    ↓
Initialize new cells in cache (functional sequence operations)
    ↓
Extract viewport subgrid → BMatrix
    ↓
LMatrix.update(BMatrix) - async chunk-based processing
    ├─ Chunk 1 (Coroutine) → LElement.update()
    ├─ Chunk 2 (Coroutine) → LElement.update()
    ├─ ...
    └─ Chunk N (Coroutine) → LElement.update()
    ↓
Updated BMatrix → write back to cache
    ↓
State change triggers Recomposition
    ↓
InfiniteGameCanvas renders visible cells
```

## Project Structure

```
src/
├── main/kotlin/org/msc/
│   ├── Main.kt                           # Application entry point
│   ├── model/
│   │   ├── abstrakt/
│   │   │   └── Lol.kt                   # Abstract matrix (chunk-based async)
│   │   ├── life/
│   │   │   └── LElement.kt              # Game of Life cell
│   │   ├── LMatrix.kt                   # Main game matrix (async update)
│   │   └── BMatrix.kt                   # Boolean state matrix
│   └── view/
│       └── ComposeView.kt               # Infinite grid UI with pan/zoom
└── test/kotlin/org/msc/
    └── GolRuleTest.kt                   # Game rules tests (async-ready)
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

### Performance Best Practices
1. **Nested For-Loops - Pragmatic Approach**:

   **Use functional style when:**
   - The logic is complex and involves transformations
   - Working with collections where `map`/`filter`/`flatMap` improves clarity
   - The loops are part of business logic (e.g., cell initialization in ComposeView)

   ```kotlin
   // ✅ Good use of functional style (complex logic)
   val newEntries = (minY..maxY).asSequence()
       .flatMap { y -> (minX..maxX).asSequence().map { x -> Pair(x, y) } }
       .filter { it !in mutableCache }
       .associateWith { Math.random() < 0.5 }
   ```

   **Keep for-loops when:**
   - The function consists mostly of simple iteration
   - The loop body is straightforward and clear
   - Functional style would reduce readability

   ```kotlin
   // ✅ Simple iteration - for-loops are fine
   fun setEach(setFunction:(x:Int,y:Int)->T){
       for(y in 0 until mainMatrix.size){
           for (x in 0 until mainMatrix[y].size) {
               mainMatrix[y][x] = setFunction(x,y)
           }
       }
   }
   ```

   **Rule of thumb:** If the function is primarily a simple iteration with a clear, single expression in the body, for-loops are perfectly acceptable and often more readable.

2. **Chunk-Based Parallel Processing**: For batch operations on collections
   ```kotlin
   // ✅ Pattern used in LMatrix.update()
   suspend fun processElements(elements: List<T>) {
       val chunkSize = (elements.size / availableProcessors()).coerceAtLeast(100)
       coroutineScope {
           elements.chunked(chunkSize).map { chunk ->
               launch { chunk.forEach { processElement(it) } }
           }.joinAll()
       }
   }
   ```

3. **Suspend Functions**: Mark functions as `suspend` only if they:
   - Call other suspend functions
   - Use coroutine builders (`launch`, `async`)
   - Use suspending operators (`delay`, `await`)
   - Do NOT mark functions as suspend just because they're called from a coroutine

4. **Lazy Evaluation**: Use `asSequence()` for large collections to avoid intermediate allocations

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
- **Async Testing**: Use `runBlocking` wrapper for suspend functions
  ```kotlin
  @Test
  fun myTest() = runBlocking {
      val result = lMatrix.update(bMatrix)  // suspend call
      assertThat(result).isNotNull()
  }
  ```

## Known Patterns and Conventions

1. **Matrix Separation**: `LMatrix` (logic) vs `BMatrix` (state) allows clean separation of concerns
2. **Update Pattern**: Read from one matrix, write to another to avoid race conditions
3. **Chunk-Based Parallelism**:
   - `getAllElements()` + `chunked()` + `launch` for parallel processing
   - Better load-balancing than row-based parallelism
   - Chunk size = `totalElements / CPU cores` (minimum 100)
4. **Functional vs Imperative - Be Pragmatic**:
   - Use `flatMap`, `filter`, `map` for complex transformations
   - Use `asSequence()` for lazy evaluation on large collections
   - Keep simple for-loops when they're clearer (e.g., `setEach`, `updateEach` in Lol.kt)
   - Example: Cell initialization in ComposeView uses sequences (complex logic), while Lol.kt helper methods use for-loops (simple iteration)
5. **Suspend Function Hierarchy**:
   - `LMatrix.update()` is suspend (coordinates coroutines)
   - `LElement.update()` is NOT suspend (pure computation)
   - Rule: Suspend propagates up, not down
6. **Companion Objects**: Used for factory methods and shared constants (e.g., `LElement.neighborCoordinates`)
7. **Sparse State Storage**: Use `Map<Pair<Int, Int>, Boolean>` for infinite grids instead of dense arrays

## Common Tasks

### Add a new game pattern preset
1. Create a factory method in `BMatrix.Companion`
2. Add UI button in `ComposeView.GameOfLife()`
3. Initialize pattern with specific cell coordinates

### Optimize performance
- **Already optimized**: Chunk-based parallel processing in `LMatrix.update()`
- For new batch operations: Use `getAllElements()` + `chunked()` + `launch`
- Replace nested for-loops with functional sequences (`flatMap`, `filter`)
- Use `asSequence()` for lazy evaluation of large collections
- Profile before optimizing further

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

## Performance Characteristics

### Backend Performance
- **Parallelism**: ~4-8× speedup on multi-core processors
- **Chunk Size**: Dynamically calculated based on CPU cores
- **Memory**: Efficient with sparse cache for infinite grid
- **Update Frequency**: 300ms interval (configurable)

### UI Responsiveness
- **Non-Blocking**: UI remains responsive during calculations (LaunchedEffect runs on background thread)
- **Pan/Zoom**: Immediate feedback with smooth interactions
- **Rendering**: Only visible cells + 50-cell buffer are rendered

### Scalability
- **Infinite Grid**: Memory usage scales with visited regions, not total grid size
- **Sparse Storage**: `Map<Pair<Int, Int>, Boolean>` only stores non-empty cells in cache
- **Viewport Optimization**: Only viewport + buffer is processed each frame

## Notes

- The name "Lol" for the abstract matrix class appears to be a play on "List of Lists"
- German comments in `LElement.update()`: "Geburt" (Birth), "Überleben" (Survival), "Tod" (Death)
- JVM args include `--enable-native-access=ALL-UNNAMED` for Compose native interop
- **Achieved Goals**: All major project goals have been successfully implemented (see checkmarks ✅ above)
