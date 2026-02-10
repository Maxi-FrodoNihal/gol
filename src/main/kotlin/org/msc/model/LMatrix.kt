package org.msc.model

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import org.msc.model.abstrakt.Lol
import org.msc.model.life.LElement

class LMatrix (maxX: Int, maxY: Int) : Lol<LElement>(maxX, maxY) {

    init {
        prefill { x, y -> LElement.defaultValue(x, y) }
    }

    suspend fun update(readMatrix: BMatrix): BMatrix {
        val writeMatrix = BMatrix(readMatrix)

        val allElements = getAllElements()
        val chunkSize = (allElements.size / Runtime.getRuntime().availableProcessors()).coerceAtLeast(100)

        coroutineScope {
            allElements.chunked(chunkSize).map { chunk ->
                launch {
                    chunk.forEach { element ->
                        val updatedElement = element.update(readMatrix)
                        writeMatrix.set(updatedElement.x, updatedElement.y, updatedElement.life)
                    }
                }
            }.joinAll()
        }

        return writeMatrix
    }
}