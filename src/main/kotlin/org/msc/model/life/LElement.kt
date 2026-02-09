package org.msc.model.life

import org.msc.model.BMatrix

class LElement(val x: Int, val y: Int) {

    var life: Boolean = false

    companion object {

        val neighborCoordinates = initNeighborCoordinates()

        fun defaultValue(x: Int, y: Int): LElement {
            return LElement(x, y)
        }

        private fun initNeighborCoordinates(): List<Pair<Int, Int>> {

            val coordinateList = mutableListOf<Pair<Int, Int>>()

            for (x in -1..1) {
                for (y in -1..1) {
                    coordinateList.add(x to y)
                }
            }

            coordinateList.remove(0 to 0)

            return coordinateList
        }
    }

    fun update(readMatrix: BMatrix): LElement {

        life = readMatrix.get(x,y)?:false
        val aliveNeighbors = getNeighbors(readMatrix).count { it }

        life = when {
            !life && aliveNeighbors == 3 -> true           // Regel: Geburt
            life && aliveNeighbors in 2..3 -> true   // Regel: Überleben
            else -> false                                  // Regel: Tod
        }

        return this
    }

    private fun getNeighbors(readMatrix: BMatrix): List<Boolean> {
        return neighborCoordinates.mapNotNull { readMatrix.get(x + it.first, y + it.second) }.toList()
    }
}