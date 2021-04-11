import java.lang.IllegalArgumentException
import java.lang.Integer.max
import java.util.concurrent.RecursiveTask
import kotlin.math.ceil
import kotlin.math.log2

class Matrix(arr: Array<IntArray>) {
    private var data: Array<IntArray> = arr
    var rows: Int
    var columns: Int

    init {
        if (arr.isEmpty()) {
            throw IllegalArgumentException("Matrix shouldn't be empty")
        }
        rows = arr.size
        columns = arr[0].size
        if (columns == 0) {
            throw IllegalArgumentException("Matrix shouldn't be empty")
        }
        for (row in arr) {
            if (row.size != columns) {
                throw IllegalArgumentException("All rows should have the same size")
            }
        }
    }

    constructor(a11: Matrix, a12: Matrix, a21: Matrix, a22: Matrix) : this(arrayOf(intArrayOf(0))) {
        val n: Int = a11.rows
        data = Array(n shl 1) { IntArray(n shl 1) { 0 } }
        for (i in 0 until n) {
            a11[i].copyInto(data[i])
            a12[i].copyInto(data[i], n)
            a21[i].copyInto(data[i + n])
            a22[i].copyInto(data[i + n], n)
        }
        rows = n shl 1
        columns = n shl 1
    }

    operator fun get(i: Int): IntArray = data[i]

    operator fun set(i: Int, j: Int, t: Int) {
        data[i][j] = t
    }

    operator fun plus(other: Matrix): Matrix {
        if (!isSameSize(other)) {
            throw IllegalArgumentException("Matrix should have same size")
        }
        val res = Matrix(Array(rows) { IntArray(columns) { 0 } })
        for (i in data.indices) {
            for (j in data[0].indices) {
                res[i][j] = data[i][j] + other[i][j]
            }
        }
        return res
    }

    operator fun minus(other: Matrix): Matrix {
        if (!isSameSize(other)) {
            throw IllegalArgumentException("Matrix should have same size")
        }
        val res = Matrix(Array(rows) { IntArray(columns) { 0 } })
        for (i in data.indices) {
            for (j in data[0].indices) {
                res[i][j] = data[i][j] - other[i][j]
            }
        }
        return res
    }

    private fun additionToSquare(newDim: Int): Matrix {
        val res = Matrix(Array(newDim) { IntArray(newDim) { 0 } })
        for (i in data.indices) {
            for (j in data[0].indices) {
                res[i][j] = data[i][j]
            }
        }
        return res
    }

    private fun splitIn4Matrix(): Pair<Pair<Matrix, Matrix>, Pair<Matrix, Matrix>> {
        val n: Int = rows shr 1
        val a11 = Matrix(Array(n) { IntArray(n) { 0 } })
        val a12 = Matrix(Array(n) { IntArray(n) { 0 } })
        val a21 = Matrix(Array(n) { IntArray(n) { 0 } })
        val a22 = Matrix(Array(n) { IntArray(n) { 0 } })
        for (i in 0 until n) {
            data[i].copyInto(a11[i], 0, 0, n)
            data[i].copyInto(a12[i], 0, n)
            data[i + n].copyInto(a21[i], 0, 0, n)
            data[i + n].copyInto(a22[i], 0, n)
        }
        return Pair(Pair(a11, a12), Pair(a21, a22))
    }

    fun usualMultiply(other: Matrix): Matrix {
        val res = Matrix(Array(rows) { IntArray(other.columns) { 0 } })
        val column = IntArray(other.rows) { 0 }
        for (j in 0 until other.columns) {
            for (k in 0 until columns) {
                column[k] = other[k][j]
            }

            for (i in 0 until rows) {
                val row: IntArray = data[i]
                var sum = 0
                for (k in 0 until columns) {
                    sum += row[k] * column[k]
                }
                res[i][j] = sum
            }
        }
        return res
    }

    operator fun times(other: Matrix): Matrix {
        if (columns != other.rows) {
            throw IllegalArgumentException("Matrix should be the correct size: a.columns == b.rows")
        }
        val n: Int = newDim(this, other)
        if (n <= 64) {
            return usualMultiply(other)
        }

        val res = Matrix(Array(rows) { IntArray(other.columns) { 0 } })

        val a: Matrix = this.additionToSquare(n)
        val b: Matrix = other.additionToSquare(n)

        val c = RecursiveMultiply(a, b, n).fork().join()

        for (i in 0 until res.rows) {
            c[i].copyInto(res[i], 0, 0, res.columns)
        }
        return res
    }

    private fun isSameSize(other: Matrix): Boolean {
        return rows == other.rows || columns == other.columns
    }

    override fun equals(other: Any?): Boolean {
        if (other is Matrix) {
            var isSame = true
            for (i in 0 until rows) {
                isSame = isSame and data[i].contentEquals(other.data[i])
            }
            return isSame and isSameSize(other)
        }
        return false
    }

    override fun hashCode(): Int {
        var result = data.contentDeepHashCode()
        result = 31 * result + rows
        result = 31 * result + columns
        return result
    }

    private class RecursiveMultiply(val a: Matrix, val b: Matrix, val n: Int) : RecursiveTask<Matrix>() {

        override fun compute(): Matrix {
            if (n <= 64) {
                return a.usualMultiply(b)
            }

            val newN: Int = n shr 1

            val (p11, p12) = a.splitIn4Matrix()
            val (a11, a12) = p11
            val (a21, a22) = p12

            val (p21, p22) = b.splitIn4Matrix()
            val (b11, b12) = p21
            val (b21, b22) = p22

            val task_p1 = RecursiveMultiply((a11 + a22), (b11 + b22), newN)
            val task_p2 = RecursiveMultiply((a21 + a22), b11, newN)
            val task_p3 = RecursiveMultiply(a11, (b12 - b22), newN)
            val task_p4 = RecursiveMultiply(a22, (b21 - b11), newN)
            val task_p5 = RecursiveMultiply((a11 + a12), b22, newN)
            val task_p6 = RecursiveMultiply((a21 - a11), (b11 + b12), newN)
            val task_p7 = RecursiveMultiply((a12 - a22), (b21 + b22), newN)

            task_p1.fork()
            task_p2.fork()
            task_p3.fork()
            task_p4.fork()
            task_p5.fork()
            task_p6.fork()
            task_p7.fork()

            val p1: Matrix = task_p1.join()
            val p2: Matrix = task_p2.join()
            val p3: Matrix = task_p3.join()
            val p4: Matrix = task_p4.join()
            val p5: Matrix = task_p5.join()
            val p6: Matrix = task_p6.join()
            val p7: Matrix = task_p7.join()

            val c11: Matrix = p1 + p4 - p5 + p7
            val c12: Matrix = p3 + p5
            val c21: Matrix = p2 + p4
            val c22: Matrix = p1 - p2 + p3 + p6

            return Matrix(c11, c12, c21, c22)
        }
    }
}

fun ceilLog2(n: Int): Int = (ceil(log2(n.toDouble()))).toInt()

fun newDim(a: Matrix, b: Matrix): Int {
    return 1 shl max(ceilLog2(a.rows), max(ceilLog2(a.columns), ceilLog2(b.columns)))
}

fun main() {
    val a: Array<Array<Int>> = Array(5) { x ->
        Array(x) { 5 }
    }
    for (i in a.indices) {
        for (j in a[i].indices) {
            print(a[i][j])
        }
        println()
    }
}