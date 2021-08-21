package com.example.sudoku.game

import androidx.room.*

abstract class simpleCell(
    open val row: Int,
    open val col: Int,
    open var value: Int,
    open var isStartingCell: Boolean = false
)

@Entity(tableName = "cells_table", primaryKeys = ["cell_row", "cell_col"])
data class Cell(
    @ColumnInfo(name = "cell_row") override var row: Int,
    @ColumnInfo(name = "cell_col") override var col: Int,
    @ColumnInfo(name = "cell_value") override var value: Int,
    @ColumnInfo(name = "cell_start") override var isStartingCell: Boolean = false,
    @ColumnInfo(name = "cell_notes") var notes: MutableSet<Int> = mutableSetOf<Int>(),
    @Ignore var conflicts: MutableSet<Pair<Int, Int>> = mutableSetOf<Pair<Int, Int>>()
) : simpleCell(row, col, value, isStartingCell) {
    constructor(
        row: Int,
        col: Int,
        value: Int,
        isStartingCell: Boolean,
        notes: MutableSet<Int>
    ) : this(
        row,
        col,
        value,
        isStartingCell,
        notes,
        mutableSetOf<Pair<Int, Int>>()
    )

    fun update(from: Cell) {
        value = from.value
        isStartingCell = from.isStartingCell
        notes = from.notes
        conflicts = from.conflicts
    }

    fun clear() {
        value = 0
        isStartingCell = false
        notes = mutableSetOf<Int>()
        conflicts = mutableSetOf<Pair<Int, Int>>()
    }

    fun deepCopy(
        row: Int = this.row,
        col: Int = this.col,
        value: Int = this.value,
        isStartingCell: Boolean = this.isStartingCell,
        notes: MutableSet<Int> = this.notes.toMutableSet(),
        conflicts: MutableSet<Pair<Int, Int>> = this.conflicts.toMutableSet<Pair<Int, Int>>()
    ) = Cell(row, col, value, isStartingCell, notes, conflicts)

}