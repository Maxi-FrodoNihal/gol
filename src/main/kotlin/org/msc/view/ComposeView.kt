package org.msc.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isCtrlPressed
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import org.msc.model.BMatrix
import org.msc.model.LMatrix

class ComposeView {

    // Visual Constants
    private val greyPanelColor = 160
    private val baseGridSize = 200
    private val baseCellSize = 15.dp
    private val borderWidth = 1f
    private val updateDelayMs = 300L

    // Zoom Constraints
    private val minZoom = 0.3f
    private val maxZoom = 5f
    private val zoomFactor = 1.1f

    private val darkColorScheme = darkColors(
        background = Color(0xFF1E1E1E),
        surface = Color(0xFF2D2D2D),
        primary = Color(0xFF4A4A4A)
    )

    /**
     * Main entry point for the Game of Life UI
     */
    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun GameOfLife() {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val gridDimensions = calculateGridDimensions(maxWidth, maxHeight)

            var grid by remember {
                mutableStateOf(createRandomGrid(gridDimensions.width, gridDimensions.height))
            }
            var zoomLevel by remember { mutableStateOf(1f) }

            GameUpdateLoop(
                onUpdate = { grid = updateGrid(grid) }
            )

            MaterialTheme(colors = darkColorScheme) {
                GameCanvas(
                    grid = grid,
                    gridDimensions = gridDimensions,
                    zoomLevel = zoomLevel,
                    onZoomChange = { newZoom -> zoomLevel = newZoom }
                )
            }
        }
    }

    /**
     * Calculates grid dimensions based on available space
     */
    private fun calculateGridDimensions(maxWidth: Dp, maxHeight: Dp): GridDimensions {
        val aspectRatio = maxWidth / maxHeight
        return GridDimensions(
            width = (baseGridSize * aspectRatio).toInt(),
            height = baseGridSize
        )
    }

    /**
     * Launches a coroutine that continuously updates the game grid
     */
    @Composable
    private fun GameUpdateLoop(onUpdate: () -> Unit) {
        LaunchedEffect(Unit) {
            while (true) {
                delay(updateDelayMs)
                onUpdate()
            }
        }
    }

    /**
     * Main game canvas with scrolling, zooming, and grid rendering
     */
    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    private fun GameCanvas(
        grid: List<List<Boolean>>,
        gridDimensions: GridDimensions,
        zoomLevel: Float,
        onZoomChange: (Float) -> Unit
    ) {
        val currentCellSize = baseCellSize * zoomLevel
        val canvasWidth = gridDimensions.width * currentCellSize
        val canvasHeight = gridDimensions.height * currentCellSize

        val verticalScrollState = rememberScrollState()
        val horizontalScrollState = rememberScrollState()
        val coroutineScope = rememberCoroutineScope()

        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
        ) {
            ScrollableGridContainer(
                verticalScrollState = verticalScrollState,
                horizontalScrollState = horizontalScrollState,
                coroutineScope = coroutineScope,
                zoomLevel = zoomLevel,
                onZoomChange = onZoomChange,
                canvasWidth = canvasWidth,
                canvasHeight = canvasHeight,
                grid = grid
            )

            VerticalScrollbar(verticalScrollState)
            HorizontalScrollbar(horizontalScrollState)
        }
    }

    /**
     * Scrollable container with zoom and drag support
     */
    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    private fun ScrollableGridContainer(
        verticalScrollState: androidx.compose.foundation.ScrollState,
        horizontalScrollState: androidx.compose.foundation.ScrollState,
        coroutineScope: CoroutineScope,
        zoomLevel: Float,
        onZoomChange: (Float) -> Unit,
        canvasWidth: Dp,
        canvasHeight: Dp,
        grid: List<List<Boolean>>
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .verticalScroll(verticalScrollState)
                .horizontalScroll(horizontalScrollState)
                .applyZoomHandler(zoomLevel, onZoomChange)
                .applyDragHandler(horizontalScrollState, verticalScrollState, coroutineScope)
        ) {
            GridCanvas(
                grid = grid,
                canvasWidth = canvasWidth,
                canvasHeight = canvasHeight
            )
        }
    }

    /**
     * Applies zoom functionality via Ctrl+Scroll
     */
    @OptIn(ExperimentalComposeUiApi::class)
    private fun Modifier.applyZoomHandler(
        zoomLevel: Float,
        onZoomChange: (Float) -> Unit
    ): Modifier = this.onPointerEvent(PointerEventType.Scroll) { event ->
        handleZoomEvent(event, zoomLevel, onZoomChange)
    }

    /**
     * Handles zoom event logic
     */
    private fun handleZoomEvent(
        event: PointerEvent,
        currentZoom: Float,
        onZoomChange: (Float) -> Unit
    ) {
        if (!event.keyboardModifiers.isCtrlPressed) return

        val scrollDelta = event.changes.first().scrollDelta.y
        event.changes.first().consume()

        val factor = if (scrollDelta < 0) zoomFactor else 1f / zoomFactor
        val newZoom = (currentZoom * factor).coerceIn(minZoom, maxZoom)
        onZoomChange(newZoom)
    }

    /**
     * Applies drag-to-scroll functionality
     */
    private fun Modifier.applyDragHandler(
        horizontalScrollState: ScrollableState,
        verticalScrollState: ScrollableState,
        coroutineScope: CoroutineScope
    ): Modifier = this.pointerInput(Unit) {
        detectDragGestures { change, dragAmount ->
            change.consume()
            coroutineScope.launch {
                horizontalScrollState.scrollBy(-dragAmount.x)
                verticalScrollState.scrollBy(-dragAmount.y)
            }
        }
    }

    /**
     * Canvas that renders the game grid
     */
    @Composable
    private fun GridCanvas(
        grid: List<List<Boolean>>,
        canvasWidth: Dp,
        canvasHeight: Dp
    ) {
        Canvas(
            Modifier.size(width = canvasWidth, height = canvasHeight)
        ) {
            drawGrid(grid)
        }
    }

    /**
     * Draws the entire grid on the canvas
     */
    private fun DrawScope.drawGrid(grid: List<List<Boolean>>) {
        if (grid.isEmpty() || grid[0].isEmpty()) return

        val cellWidth = size.width / grid[0].size
        val cellHeight = size.height / grid.size

        grid.forEachIndexed { y, row ->
            row.forEachIndexed { x, alive ->
                drawGridCell(x, y, cellWidth, cellHeight, alive)
            }
        }
    }

    /**
     * Draws a single cell with border and optional fill
     */
    private fun DrawScope.drawGridCell(
        x: Int,
        y: Int,
        cellWidth: Float,
        cellHeight: Float,
        alive: Boolean
    ) {
        val topLeft = Offset(x * cellWidth, y * cellHeight)

        // Draw cell border
        drawRect(
            color = Color.Black,
            topLeft = topLeft,
            size = Size(cellWidth, cellHeight),
            style = Stroke(width = borderWidth)
        )

        // Draw cell fill if alive
        if (alive) {
            drawRect(
                color = Color(greyPanelColor, greyPanelColor, greyPanelColor),
                topLeft = Offset(
                    x * cellWidth + borderWidth,
                    y * cellHeight + borderWidth
                ),
                size = Size(
                    cellWidth - borderWidth * 2,
                    cellHeight - borderWidth * 2
                )
            )
        }
    }

    /**
     * Vertical scrollbar component
     */
    @Composable
    private fun BoxScope.VerticalScrollbar(
        verticalScrollState: androidx.compose.foundation.ScrollState
    ) {
        androidx.compose.foundation.VerticalScrollbar(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight(),
            adapter = rememberScrollbarAdapter(verticalScrollState)
        )
    }

    /**
     * Horizontal scrollbar component
     */
    @Composable
    private fun BoxScope.HorizontalScrollbar(
        horizontalScrollState: androidx.compose.foundation.ScrollState
    ) {
        androidx.compose.foundation.HorizontalScrollbar(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(end = 12.dp),
            adapter = rememberScrollbarAdapter(horizontalScrollState)
        )
    }

    /**
     * Data class to hold grid dimensions
     */
    private data class GridDimensions(
        val width: Int,
        val height: Int
    )

    /**
     * Creates a random grid with given dimensions
     * Each cell has 50% chance of being alive
     */
    private fun createRandomGrid(width: Int, height: Int): List<List<Boolean>> =
        List(height) { List(width) { Math.random() < 0.5 } }

    /**
     * Updates the grid using Game of Life rules via backend computation
     * Leverages LMatrix and BMatrix for efficient coroutine-based updates
     */
    private fun updateGrid(grid: List<List<Boolean>>): List<List<Boolean>> {
        val width = grid.maxOf { it.size }
        val height = grid.size
        return LMatrix(width, height).update(BMatrix(grid)).mainMatrix
    }
}