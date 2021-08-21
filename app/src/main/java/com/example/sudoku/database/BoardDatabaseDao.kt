package com.example.sudoku.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.*
import com.example.sudoku.game.Cell

@Dao
interface BoardDatabaseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBoard(board: BoardInfo)

    @Query("SELECT * FROM boards_table ORDER BY boardId DESC LIMIT 1")
    fun getLatestBoard(): BoardInfo?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCell(cell: Cell)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @JvmSuppressWildcards
    fun insertCells(cells: List<Cell>)

    @Query("SELECT * FROM cells_table ORDER BY cell_row,cell_col ASC LIMIT :limit")
    fun getCells(limit: Int): List<Cell>

}