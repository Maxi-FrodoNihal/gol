package org.msc.view

import org.msc.model.BMatrix
import org.msc.view.abstrakt.Viewer

class ConsoleViewer(val clearPrints:Int = 0) : Viewer {

    override fun printMatrix(matrix: BMatrix) {

        repeat(clearPrints) {
            println()
        }

        matrix.mainMatrix.forEach {
            println(it.joinToString(" ", transform = ::signMapper))
        }
    }

    private fun signMapper(value: Boolean): String{
        return if(value){
            "X"
        }else{
            "0"
        }
    }
}