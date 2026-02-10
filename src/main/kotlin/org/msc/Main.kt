package org.msc

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.msc.view.ComposeView

fun main() {
    application {
        Window(onCloseRequest = ::exitApplication, title = "Game of Life") {
            ComposeView().GameOfLife()
        }
    }
}