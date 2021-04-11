import org.junit.jupiter.api.*
import java.io.PrintStream
import java.lang.AssertionError
import kotlin.random.Random
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

class MatrixMultiplicationTest {

    @Test
    fun usualMultiplicationTest() {
        val a = Matrix(arrayOf(intArrayOf(4, 1, 4, 5), intArrayOf(6, 5, 4, 2), intArrayOf(5, 1, 1, 1), intArrayOf(6, 6, 1, 4)))
        val b = Matrix(arrayOf(intArrayOf(2, 4, 2, 6), intArrayOf(5, 4, 1, 1), intArrayOf(1, 2, 1, 5), intArrayOf(2, 1, 2, 1)))
        val c = Matrix(arrayOf(intArrayOf(27, 33, 23, 50), intArrayOf(45, 54, 25, 63), intArrayOf(18, 27, 14, 37), intArrayOf(51, 54, 27, 51)))
        Assertions.assertEquals(c, a.usualMultiply(b))
    }

    @Test
    fun strassenMultiplicationTest() {
        for (i in 65..300) {
            val a: Matrix = genRandomSquareMatrix(i)
            val b: Matrix = genRandomSquareMatrix(i)
            Assertions.assertEquals(a.usualMultiply(b), a * b)
        }
    }

    @ExperimentalTime
    @Test
    fun measureTimeTest() {
        val a: Matrix = genRandomSquareMatrix(2000)
        val b: Matrix = genRandomSquareMatrix(2000)

        val usualDuration = measureTime {
            a.usualMultiply(b)
        }
        println("Time: ${usualDuration.inSeconds} s")

        val strassenDuration = measureTime {
            a * b
        }
        Assertions.assertEquals(a.usualMultiply(b), a * b)
        println("Time: ${strassenDuration.inSeconds} s")
    }

    @Test
    fun stupidTest() {
        val a = Matrix(Array(1000) { j -> IntArray(1000) { i -> if (i == j) 1 else 0 } })
        val b = Matrix(Array(1000) { j -> IntArray(1000) { i -> if (i == j) 1 else 0 } })

        Assertions.assertEquals(a, a * b)
    }

    private fun genRandomSquareMatrix(n: Int): Matrix {
        val m = Matrix(Array(n) { IntArray(n) { 0 } })
        for (i in 0 until m.rows) {
            for (j in 0 until m.columns) {
                m[i][j] = Random.nextInt(-1000, 1000)
            }
        }
        return m
    }
}