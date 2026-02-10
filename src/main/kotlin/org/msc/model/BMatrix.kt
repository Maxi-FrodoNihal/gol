package org.msc.model

import org.msc.model.abstrakt.Lol
import kotlin.random.Random

class BMatrix : Lol<Boolean> {

    constructor(maxX: Int, maxY: Int) : super(maxX, maxY) {
        prefill { x, y -> false }
    }

    constructor(externalMatrix:List<List<Boolean>>):super(externalMatrix) {}

    constructor(bMatrix: BMatrix): super(bMatrix.mainMatrix.map { row -> row.toList() }) {}

    companion object {

        fun getRandom(maxX: Int, maxY: Int): BMatrix {
            val random = Random.Default
            val bMatrix = BMatrix(maxX, maxY)
            bMatrix.setEach { x, y -> random.nextBoolean() }

            return bMatrix
        }
    }
}
