package org.msc

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.msc.model.BMatrix
import org.msc.model.LMatrix
import kotlin.test.Test

class GolRuleTest {

    @Test
    fun checkRuleOne(): Unit = runBlocking {

        val dim = 3
        val bMatrix = BMatrix(dim, dim)

        // three neighbors a life means birth
        bMatrix.set(0, 0, true)
        bMatrix.set(2, 0, true)
        bMatrix.set(0, 2, true)

        assertThat(bMatrix.get(1, 1)).isFalse()

        val lMatrix = LMatrix( dim,dim)
        val newBMatrix = lMatrix.update(bMatrix)

        assertThat(newBMatrix.get(1, 1)).isTrue()
        assertThat(newBMatrix.getAllElements().count { it == true }).isEqualTo(1)
        assertThat(newBMatrix.getAllElements().count { it == false }).isEqualTo(8)
    }

    @Test
    fun checkRuleTwoWithTwo(): Unit = runBlocking {

        val dim = 3
        val bMatrix = BMatrix(dim, dim)

        // two means death
        bMatrix.set(0, 0, true)
        bMatrix.set(2, 2, true)

        bMatrix.set(1, 1, true)

        assertThat(bMatrix.get(1, 1)).isTrue()

        val lMatrix = LMatrix( dim,dim)
        val newBMatrix = lMatrix.update(bMatrix)

        assertThat(newBMatrix.get(1, 1)).isTrue()
        assertThat(newBMatrix.getAllElements().count { it == true }).isEqualTo(1)
        assertThat(newBMatrix.getAllElements().count { it == false }).isEqualTo(8)
    }

    @Test
    fun checkRuleTwoWithThree(): Unit = runBlocking {

        val dim = 3
        val bMatrix = BMatrix(dim, dim)

        // two or three neighbors means stay a life
        bMatrix.set(0, 0, true)
        bMatrix.set(2, 2, true)
        bMatrix.set(0, 2, true)

        bMatrix.set(1, 1, true)

        assertThat(bMatrix.get(1, 1)).isTrue()

        val lMatrix = LMatrix( dim,dim)
        val newBMatrix = lMatrix.update(bMatrix)

        assertThat(newBMatrix.get(1, 1)).isTrue()
        assertThat(newBMatrix.getAllElements().count { it == true }).isEqualTo(3)
        assertThat(newBMatrix.getAllElements().count { it == false }).isEqualTo(6)
    }

    @Test
    fun checkRuleThreeWithNoNeighbors(): Unit = runBlocking {

        val dim = 3
        val bMatrix = BMatrix(dim, dim)

        // no neighbors means death
        bMatrix.set(1, 1, true)

        assertThat(bMatrix.get(1, 1)).isTrue()

        val lMatrix = LMatrix( dim,dim)
        val newBMatrix = lMatrix.update(bMatrix)

        assertThat(newBMatrix.get(1, 1)).isFalse()
        assertThat(newBMatrix.getAllElements().count { it == false }).isEqualTo(9)
    }

    @Test
    fun checkRuleThreeWithTooMuchNeighbors(): Unit = runBlocking {

        val dim = 3
        val bMatrix = BMatrix(dim, dim)

        // too much neighbors means death
        bMatrix.set(0, 0, true)
        bMatrix.set(2, 2, true)
        bMatrix.set(0, 2, true)
        bMatrix.set(2, 0, true)

        bMatrix.set(1, 1, true)

        assertThat(bMatrix.get(1, 1)).isTrue()

        val lMatrix = LMatrix( dim,dim)
        val newBMatrix = lMatrix.update(bMatrix)

        assertThat(newBMatrix.get(1, 1)).isFalse()
        assertThat(newBMatrix.getAllElements().count { it == true }).isEqualTo(4)
        assertThat(newBMatrix.getAllElements().count { it == false }).isEqualTo(5)
    }
}