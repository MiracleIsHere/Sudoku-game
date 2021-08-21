package com.example.sudoku.game

import kotlin.math.sqrt

data class Board(val size: Int, val cells: List<Cell>, var isEditing: Boolean = false) {

    private val sqrSize = sqrt(size.toDouble()).toInt()

    fun getCell(row: Int, col: Int) = cells[row * size + col]

    fun setConflicts(target: Cell) {
        val rowStart = target.row * size
        var currCol = target.col
        var sqrRowStart =
            (((target.row / sqrSize) * (size * sqrSize)) + ((target.col / sqrSize) * (sqrSize)))

        for (x in rowStart until rowStart + size) {
            manageConflicts(target, cells[x])
            manageConflicts(target, cells[currCol])
            manageConflicts(target, cells[sqrRowStart])
            currCol += size
            sqrRowStart += if ((sqrRowStart + 1) % sqrSize != 0) 1 else (size - sqrSize) + 1
        }
    }

    // if value is the same go next
        //if isStartingCell is same -> add conflicts to both
        //else add confilct only who is not starting cell
    //else remove comflicts

    private fun manageConflicts(target: Cell, cell: Cell) {
        if (target.value != 0 && target !== cell &&
            cell.value == target.value
        ) {
            cell.conflicts.add(Pair(target.row, target.col))
            target.conflicts.add(Pair(cell.row, cell.col))
            if (target.isStartingCell != cell.isStartingCell) {
                if (target.isStartingCell) target.conflicts.remove(Pair(cell.row, cell.col))
                else cell.conflicts.remove(Pair(target.row, target.col))
            }
        } else {
            cell.conflicts.remove(Pair(target.row, target.col))
            target.conflicts.remove(Pair(cell.row, cell.col))
        }
    }

    //remove conflicts
    fun makeCorrections() {
        cells.forEach { cell ->
            if (cell.isStartingCell && cell.conflicts.isNotEmpty()) {
                cell.conflicts.forEach { coord ->
                    getCell(coord.first, coord.second).apply {
                        value = 0
                        notes.clear()
                        isStartingCell = false
                        conflicts.remove(Pair(cell.row, cell.col))
                    }
                }
                cell.conflicts.clear()
            } else if (!cell.isStartingCell && cell.conflicts.isNotEmpty()) {
                cell.conflicts.forEach { coord ->
                    getCell(coord.first, coord.second).apply {
                        conflicts.remove(Pair(cell.row, cell.col))
                    }
                }
                cell.value = 0
                cell.conflicts.clear()
                cell.notes.clear()
            }
        }
    }

    fun isGuessed(): Boolean {
        for (index in cells.indices) {
            if (cells[index].value == 0 || cells[index].conflicts.isNotEmpty()) return false
        }
        return true
    }
}