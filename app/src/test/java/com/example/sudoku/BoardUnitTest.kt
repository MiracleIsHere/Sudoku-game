package com.example.sudoku

import com.example.sudoku.game.Board
import com.example.sudoku.game.Cell
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class BoardUnitTest {
    val size = 9
    val cells = List(size * size) { i -> Cell(i / 9, i % 9, i % 9) }
    val board = Board(size, cells)

    @Test
    fun addition_isCorrect() {

        assertEquals(4, 2 + 2)
    }
}