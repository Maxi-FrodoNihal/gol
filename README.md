# Game of Life

A high-performance implementation of Conway's Game of Life with an infinite, interactive grid.

## Features

- **Infinite Grid**: Seamlessly navigate an unlimited game space with viewport-based rendering
- **Interactive Controls**:
  - Pan the view with mouse drag
  - Zoom in/out with `Ctrl + Scroll` (0.3× to 5×)
  - Toggle cells by clicking
- **High Performance**: Chunk-based parallel processing using Kotlin Coroutines for optimal CPU utilization
- **Smooth Experience**: Non-blocking UI with efficient sparse state storage

## Technology Stack

- **Language**: Kotlin 2.3.0
- **UI Framework**: Jetbrains Compose Desktop 1.10.0
- **Concurrency**: Kotlinx Coroutines 1.10.0
- **Build Tool**: Gradle 9.2.0
- **JVM**: Java 25

## Getting Started

### Prerequisites

- JDK 25 or compatible
- Gradle 9.2.0 (or use the included wrapper)

### Build & Run

```bash
# Build the project
./gradlew build

# Run the application
./gradlew run

# Run tests
./gradlew test
```

## Game Rules

Conway's Game of Life follows simple rules that create complex patterns:

1. **Birth**: A dead cell with exactly 3 alive neighbors becomes alive
2. **Survival**: A live cell with 2-3 alive neighbors stays alive
3. **Death**: All other cells die or stay dead

## Architecture Highlights

The project demonstrates clean architecture principles:

- **Modular Design**: Separation between game logic (`LMatrix`, `LElement`) and UI (`ComposeView`)
- **Efficient State Management**: Sparse cache using `Map<Pair<Int, Int>, Boolean>` for infinite grid
- **Parallel Processing**: Dynamic chunk-based computation utilizing all CPU cores
- **Functional Programming**: Kotlin sequences and coroutines for clean, efficient code

## Project Structure

```
src/main/kotlin/org/msc/
├── Main.kt                    # Application entry point
├── model/
│   ├── abstrakt/
│   │   └── Lol.kt            # Abstract matrix with async operations
│   ├── life/
│   │   └── LElement.kt       # Game of Life cell logic
│   ├── LMatrix.kt            # Game state matrix
│   └── BMatrix.kt            # Boolean state representation
└── view/
    └── ComposeView.kt        # Infinite grid UI with pan/zoom
```

## Performance

- **~4-8× speedup** on multi-core processors through parallel processing
- **Memory efficient**: Only stores visited regions, not the entire infinite grid
- **Responsive UI**: Background coroutines keep the interface smooth during calculations

## Development

For detailed development guidelines, architecture documentation, and contribution instructions, see [CLAUDE.md](CLAUDE.md).

## License

This project is provided as-is for educational and demonstration purposes.
