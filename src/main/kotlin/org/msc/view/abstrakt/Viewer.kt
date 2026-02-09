package org.msc.view.abstrakt

import org.msc.model.BMatrix

interface Viewer {
    fun printMatrix(matrix: BMatrix)
}