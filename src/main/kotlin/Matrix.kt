import java.lang.IllegalArgumentException
import java.lang.Integer.max
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

    constructor(a11: Matrix, a12: Matrix, a21: Matrix, a22: Matrix) {
        val n: Int = a11.rows
        this.data = Array(n shl 1) { IntArray(n shl 1) { 0 } }
        for (i in 0 until n) {
            a11[i].copyInto(data[i])
            a12[i].copyInto(data[i], n)
            a21[i].copyInto(data[i + n])
            a22[i].copyInto(data[i + n], n)
        }
        this.rows = n
        this.columns = n
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
            data[i + n].copyInto(a22[i], n, 0)
        }
        return Pair(Pair(a11, a12), Pair(a21, a22))
    }

    private fun usualMultiply(other: Matrix): Matrix {
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

        val res = Matrix(Array(rows) { IntArray(columns) { 0 } })

        this.additionToSquare(n)
        other.additionToSquare(n)

        val (p11, p12) = this.splitIn4Matrix()
        val (a11, a12) = p11
        val (a21, a22) = p12

        val (p21, p22) = other.splitIn4Matrix()
        val (b11, b12) = p21
        val (b21, b22) = p22

        val p1: Matrix = (a11 + a22) * (b11 + b22)
        val p2: Matrix = (a21 + a22) * b11
        val p3: Matrix = a11 * (b12 - b22)
        val p4: Matrix = a22 * (b21 - b11)
        val p5: Matrix = (a11 + a12) * b22
        val p6: Matrix = (a21 - a11) * (b11 + b12)
        val p7: Matrix = (a12 - a22) * (b21 + b22)

        val c11: Matrix = p1 + p4 - p5 + p7
        val c12: Matrix = p3 + p5
        val c21: Matrix = p2 + p4
        val c22: Matrix = p1 - p2 - p3 + p6

        val c = Matrix(c11, c12, c21, c22)

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
            return data.contentEquals(other.data)
        }
        return false
    }

    override fun hashCode(): Int {
        var result = data.contentDeepHashCode()
        result = 31 * result + rows
        result = 31 * result + columns
        return result
    }
}

fun ceilLog2(n: Int): Int = (ceil(log2(n.toDouble()))).toInt()

fun newDim(a: Matrix, b: Matrix): Int {
    return 1 shr max(ceilLog2(a.rows), max(ceilLog2(a.columns), ceilLog2(b.columns)))
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