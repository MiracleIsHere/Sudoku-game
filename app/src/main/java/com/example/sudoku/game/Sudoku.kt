package com.example.sudoku.game

import kotlin.math.sqrt

class Sudoku {
    companion object {
        @JvmStatic
        fun <T : simpleCell, U> generate(
            size: Int = 9,
            guessed: Int,
            createStraight: (row: Int, col: Int, value: Int) -> T
        ): List<T> {
            val list =
                List<T>(size * size) { index: Int -> createStraight(index / size, index % size, 0) }

            fillDiagonal(list)
            solve(list)
            normalise(list, guessed)
            return list
        }

        private fun <T : simpleCell> fillDiagonal(list: List<T>) {
            val size = sqrt(list.size.toDouble()).toInt()
            val sqrSize = sqrt(size.toDouble()).toInt()
            val valRange = MutableList(9) { it + 1 }
            var sqrRowStart = 0

            for (smallSqr in 0 until sqrSize) {
                valRange.shuffle()
                valRange.forEach { i ->
                    list[sqrRowStart].value = i
                    sqrRowStart += if ((sqrRowStart + 1) % sqrSize == 0) (size - sqrSize) + 1 else 1
                }
                sqrRowStart += sqrSize
            }
        }

        fun <T : simpleCell> solve(list: List<T>): Boolean {
            val empty: T? = findEmpty(list) ?: return true
            val size = sqrt(list.size.toDouble()).toInt()
            for (x in 1..size) {
                empty!!.value = x
                if (!isConflicting(list, empty)) {
                    empty.value = x
                    if (solve(list)) return true
                    empty.value = 0
                }
                empty.value = 0
            }
            return false
        }

        private fun <T : simpleCell> findEmpty(list: List<T>) = list.find { it.value == 0 }

        fun <T : simpleCell> normalise(list: List<T>, guess: Int) {
            if (guess > list.size) return
            var guess = guess
            while (guess >= 0) {
                val toClear = (0 until list.size).random()
                val candidate = list[toClear]
                if (candidate.value != 0) {
                    candidate.apply {
                        value = 0
                        isStartingCell = false
                    }
                    guess -= 1
                    if (guess >= 0) {
                        list[list.size - 1 - toClear].apply {
                            value = 0
                            isStartingCell = false
                        }
                        guess -= 1
                    }
                }
            }
        }

        private fun <T : simpleCell> isConflicting(
            list: List<T>,
            target: simpleCell
        ): Boolean {
            val size = sqrt(list.size.toDouble()).toInt()
            val sqrSize = sqrt(size.toDouble()).toInt()
            val rowStart = target.row * size
            var currCol = target.col
            var sqrRowStart =
                (((target.row / sqrSize) * (size * sqrSize)) + ((target.col / sqrSize) * (sqrSize)))

            for (x in rowStart until rowStart + size) {
                if (target !== list[x] &&
                    list[x].value == target.value
                ) {
                    return true
                }
                if (target !== list[currCol] &&
                    list[currCol].value == target.value
                ) {
                    return true
                }
                if (target !== list[sqrRowStart] &&
                    list[sqrRowStart].value == target.value
                ) {
                    return true
                }
                currCol += size
                sqrRowStart += if ((sqrRowStart + 1) % sqrSize != 0) 1 else (size - sqrSize) + 1
            }
            return false
        }
    }
}