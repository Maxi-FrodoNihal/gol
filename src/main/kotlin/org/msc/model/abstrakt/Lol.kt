package org.msc.model.abstrakt

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

abstract class Lol<T>(val maxX: Int, val maxY: Int) {

    val mainMatrix: MutableList<MutableList<T>> = mutableListOf()

    constructor(externMatrix: List<List<T>>) : this(externMatrix.maxOf { it.size }, externMatrix.size) {
        mainMatrix.addAll(externMatrix.map { it.toMutableList() })
    }

    protected fun prefill(defaultValFunc: (x:Int, y:Int) -> T){
        for (y in 0 until maxY) {
            val rowList = mutableListOf<T>()
            for (x in 0 until maxX) {
                rowList.add(defaultValFunc(x,y))
            }
            mainMatrix.add(rowList)
        }
    }

    fun set(x: Int, y: Int, value: T){
        if(x < 0 || y < 0 || x >= maxX || y >= maxY){
            return
        }
        mainMatrix[y][x] = value
    }

    fun get(x: Int, y: Int):T?{
        if(x < 0 || y < 0 || x >= maxX || y >= maxY){
            return null
        }
        return mainMatrix[y][x]
    }

    fun setEach(setFunction:(x:Int,y:Int)->T){
        for(y in 0 until mainMatrix.size){
            for (x in 0 until mainMatrix[y].size) {
                mainMatrix[y][x] = setFunction(x,y)
            }
        }
    }

    fun updateEach(setFunction:(x:Int, y:Int, tmpVal:T)->T){
        for(y in 0 until mainMatrix.size){
            for (x in 0 until mainMatrix[y].size) {
                mainMatrix[y][x] = setFunction(x,y, mainMatrix[y][x])
            }
        }
    }

    fun getEach(getFunction:(x:Int,y:Int, tmpVal:T)->Unit){
        for(y in 0 until mainMatrix.size){
            for (x in 0 until mainMatrix[y].size) {
                getFunction(x,y, mainMatrix[y][x])
            }
        }
    }

    fun getAllElements():List<T> {
        return mainMatrix.flatten()
    }
}