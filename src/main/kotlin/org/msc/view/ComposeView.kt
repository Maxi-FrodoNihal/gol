package org.msc.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.runtime.*
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
import kotlinx.coroutines.delay
import org.msc.model.BMatrix
import org.msc.model.LMatrix
import kotlin.math.floor

class ComposeView {

    // Visual Constants
    private val greyPanelColor = 160
    private val baseCellSizePx = 15f
    private val borderWidth = 1f
    private val updateDelayMs = 300L
    private val bufferZone = 50

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
        var cellCache by remember { mutableStateOf<Map<Pair<Int, Int>, Boolean>>(emptyMap()) }
        var offsetX by remember { mutableStateOf(0f) }
        var offsetY by remember { mutableStateOf(0f) }
        var zoomLevel by remember { mutableStateOf(1f) }
        var viewportWidth by remember { mutableStateOf(1920f) }
        var viewportHeight by remember { mutableStateOf(1080f) }

        GameUpdateLoop(
            onUpdate = {
                cellCache = updateInfiniteGrid(cellCache, offsetX, offsetY, zoomLevel, viewportWidth, viewportHeight)
            }
        )

        MaterialTheme(colors = darkColorScheme) {
            BoxWithConstraints(Modifier.fillMaxSize()) {
                // Update viewport dimensions when constraints change
                LaunchedEffect(constraints.maxWidth, constraints.maxHeight) {
                    viewportWidth = constraints.maxWidth.toFloat()
                    viewportHeight = constraints.maxHeight.toFloat()
                }

                InfiniteGameCanvas(
                    cellCache = cellCache,
                    offsetX = offsetX,
                    offsetY = offsetY,
                    zoomLevel = zoomLevel,
                    onOffsetChange = { dx, dy ->
                        offsetX += dx
                        offsetY += dy
                    },
                    onZoomChange = { newZoom -> zoomLevel = newZoom }
                )
            }
        }
    }

    /**
     * Launches a coroutine that continuously updates the game grid
     */
    @Composable
    private fun GameUpdateLoop(onUpdate: suspend () -> Unit) {
        LaunchedEffect(Unit) {
            while (true) {
                delay(updateDelayMs)
                onUpdate()
            }
        }
    }

    /**
     * Infinite game canvas with panning and zooming
     */
    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    private fun InfiniteGameCanvas(
        cellCache: Map<Pair<Int, Int>, Boolean>,
        offsetX: Float,
        offsetY: Float,
        zoomLevel: Float,
        onOffsetChange: (Float, Float) -> Unit,
        onZoomChange: (Float) -> Unit
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
                .applyZoomHandler(zoomLevel, onZoomChange)
                .applyPanHandler(onOffsetChange)
        ) {
            Canvas(Modifier.fillMaxSize()) {
                drawInfiniteGrid(cellCache, offsetX, offsetY, zoomLevel)
            }
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
     * Applies pan/drag functionality for infinite grid
     */
    private fun Modifier.applyPanHandler(
        onOffsetChange: (Float, Float) -> Unit
    ): Modifier = this.pointerInput(Unit) {
        detectDragGestures { change, dragAmount ->
            change.consume()
            onOffsetChange(dragAmount.x, dragAmount.y)
        }
    }

    /**
     * Draws the infinite grid based on viewport and cell cache
     */
    private fun DrawScope.drawInfiniteGrid(
        cellCache: Map<Pair<Int, Int>, Boolean>,
        offsetX: Float,
        offsetY: Float,
        zoomLevel: Float
    ) {
        val cellSize = baseCellSizePx * zoomLevel
        val viewportBounds = calculateViewportBounds(size.width, size.height, offsetX, offsetY, cellSize)

        for (gridY in viewportBounds.minY..viewportBounds.maxY) {
            for (gridX in viewportBounds.minX..viewportBounds.maxX) {
                val alive = cellCache[Pair(gridX, gridY)] ?: false
                drawInfiniteGridCell(gridX, gridY, cellSize, offsetX, offsetY, alive)
            }
        }
    }

    /**
     * Calculates which grid cells are visible in the current viewport
     */
    private fun calculateViewportBounds(
        canvasWidth: Float,
        canvasHeight: Float,
        offsetX: Float,
        offsetY: Float,
        cellSize: Float
    ): ViewportBounds {
        val minX = floor(-offsetX / cellSize).toInt()
        val minY = floor(-offsetY / cellSize).toInt()
        val maxX = floor((canvasWidth - offsetX) / cellSize).toInt()
        val maxY = floor((canvasHeight - offsetY) / cellSize).toInt()

        return ViewportBounds(minX, minY, maxX, maxY)
    }

    /**
     * Draws a single cell in the infinite grid with border and optional fill
     */
    private fun DrawScope.drawInfiniteGridCell(
        gridX: Int,
        gridY: Int,
        cellSize: Float,
        offsetX: Float,
        offsetY: Float,
        alive: Boolean
    ) {
        val screenX = gridX * cellSize + offsetX
        val screenY = gridY * cellSize + offsetY
        val topLeft = Offset(screenX, screenY)

        // Draw cell border
        drawRect(
            color = Color.Black,
            topLeft = topLeft,
            size = Size(cellSize, cellSize),
            style = Stroke(width = borderWidth)
        )

        // Draw cell fill if alive
        if (alive) {
            drawRect(
                color = Color(greyPanelColor, greyPanelColor, greyPanelColor),
                topLeft = Offset(screenX + borderWidth, screenY + borderWidth),
                size = Size(cellSize - borderWidth * 2, cellSize - borderWidth * 2)
            )
        }
    }

    /**
     * Data class to hold viewport bounds in grid coordinates
     */
    private data class ViewportBounds(
        val minX: Int,
        val minY: Int,
        val maxX: Int,
        val maxY: Int
    )

    /**
     * Updates the infinite grid using Game of Life rules
     * Only processes cells in viewport + buffer zone
     * New cells are randomly initialized on first access
     */
    private suspend fun updateInfiniteGrid(
        currentCache: Map<Pair<Int, Int>, Boolean>,
        offsetX: Float,
        offsetY: Float,
        zoomLevel: Float,
        viewportWidth: Float,
        viewportHeight: Float
    ): Map<Pair<Int, Int>, Boolean> {
        // Calculate viewport bounds with buffer
        val cellSize = baseCellSizePx * zoomLevel

        val minX = floor(-offsetX / cellSize).toInt() - bufferZone
        val minY = floor(-offsetY / cellSize).toInt() - bufferZone
        val maxX = floor((viewportWidth - offsetX) / cellSize).toInt() + bufferZone
        val maxY = floor((viewportHeight - offsetY) / cellSize).toInt() + bufferZone

        // Initialize new cells with random values
        val mutableCache = currentCache.toMutableMap()
        val newEntries = (minY..maxY).asSequence()
            .flatMap { y -> (minX..maxX).asSequence().map { x -> Pair(x, y) } }
            .filter { it !in mutableCache }
            .associateWith { Math.random() < 0.5 }

        mutableCache.putAll(newEntries)

        // Convert to 2D array for LMatrix processing
        val width = maxX - minX + 1
        val height = maxY - minY + 1

        val readMatrix = BMatrix(width, height)
        readMatrix.updateEach { x, y, tmpVal -> mutableCache[Pair(minX + x, minY + y)] ?: false }

        // Apply Game of Life rules using backend
        val updatedBMatrix = LMatrix(width, height).update(readMatrix)

        // Update cache with new values
        updatedBMatrix.getEach { x, y, tmpVal ->
            mutableCache[Pair(minX + x, minY + y)] = tmpVal
        }

        return mutableCache
    }
}