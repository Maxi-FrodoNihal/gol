package org.msc.model

import org.msc.model.abstrakt.Lol
import org.msc.model.life.LElement

class LMatrix (maxX: Int, maxY: Int) : Lol<LElement>(maxX, maxY) {

    init {
        prefill { x, y -> LElement.defaultValue(x, y) }
    }

    fun update(readMatrix: BMatrix): BMatrix {

        val writeMatrix = BMatrix(readMatrix)

        updateEach { x, y, tmpVal ->
            val updateElement = tmpVal.update(readMatrix)
            writeMatrix.set(x,y,updateElement.life)
            updateElement
        }

        return writeMatrix
    }
}