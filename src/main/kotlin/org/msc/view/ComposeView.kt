package org.msc.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isCtrlPressed
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import org.msc.model.BMatrix
import org.msc.model.LMatrix

class ComposeView {

    private val greyPanelColor = 160
    private val baseGridSize = 200
    private val baseCellSize = 15.dp

    private val darkColorScheme = darkColors(
        background = Color(0xFF1E1E1E),
        surface = Color(0xFF2D2D2D),
        primary = Color(0xFF4A4A4A)
    )

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun GameOfLife() {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val aspectRatio = maxWidth / maxHeight
            val gridWidth = (baseGridSize * aspectRatio).toInt()
            val gridHeight = baseGridSize

            var grid by remember { mutableStateOf(createRandomGrid(gridWidth, gridHeight)) }
            var zoomLevel by remember { mutableStateOf(1f) }

            LaunchedEffect(Unit) {
                while (true) {
                    delay(300)
                    grid = updateGrid(grid)
                }
            }

            val currentCellSize = baseCellSize * zoomLevel
            val canvasWidth = gridWidth * currentCellSize
            val canvasHeight = gridHeight * currentCellSize

            val verticalScrollState = rememberScrollState()
            val horizontalScrollState = rememberScrollState()
            val coroutineScope = rememberCoroutineScope()

            MaterialTheme(colors = darkColorScheme) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colors.background)
                ) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(verticalScrollState)
                            .horizontalScroll(horizontalScrollState)
                            .onPointerEvent(PointerEventType.Scroll) { event ->
                                val delta = event.changes.first().scrollDelta.y

                                // Prüfe ob Strg gedrückt ist
                                if (event.keyboardModifiers.isCtrlPressed) {
                                    event.changes.first().consume()

                                    // Zoom anpassen
                                    val zoomFactor = if (delta < 0) 1.1f else 0.9f
                                    zoomLevel = (zoomLevel * zoomFactor).coerceIn(0.3f, 5f)
                                }
                            }
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    coroutineScope.launch {
                                        horizontalScrollState.scrollBy(-dragAmount.x)
                                        verticalScrollState.scrollBy(-dragAmount.y)
                                    }
                                }
                            }
                    ) {
                        Canvas(
                            Modifier.size(
                                width = canvasWidth,
                                height = canvasHeight
                            )
                        ) {
                            val cellWidth = size.width / grid[0].size
                            val cellHeight = size.height / grid.size
                            val borderWidth = 1f

                            grid.forEachIndexed { y, row ->
                                row.forEachIndexed { x, alive ->
                                    val topLeft = Offset(x * cellWidth, y * cellHeight)

                                    drawRect(
                                        color = Color.Black,
                                        topLeft = topLeft,
                                        size = Size(cellWidth, cellHeight),
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = borderWidth)
                                    )

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
                            }
                        }
                    }

                    // Vertikale Scrollbar
                    androidx.compose.foundation.VerticalScrollbar(
                        modifier = Modifier
                            .align(androidx.compose.ui.Alignment.CenterEnd)
                            .fillMaxHeight(),
                        adapter = rememberScrollbarAdapter(verticalScrollState)
                    )

                    // Horizontale Scrollbar
                    androidx.compose.foundation.HorizontalScrollbar(
                        modifier = Modifier
                            .align(androidx.compose.ui.Alignment.BottomStart)
                            .fillMaxWidth()
                            .padding(end = 12.dp),
                        adapter = rememberScrollbarAdapter(horizontalScrollState)
                    )
                }
            }
        }
    }

    fun createRandomGrid(width: Int, height: Int) =
        List(height) { List(width) { Math.random() < 0.5 } }

    fun updateGrid(grid: List<List<Boolean>>): List<List<Boolean>> {
        return LMatrix(grid.maxOf { it.size }, grid.size).update(BMatrix(grid)).mainMatrix
    }
}