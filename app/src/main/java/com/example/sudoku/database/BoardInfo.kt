package com.example.sudoku.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "boards_table")
data class BoardInfo(
    @PrimaryKey(autoGenerate = true)
    var boardId: Long = 0L,
    var size: Int = 0,
    var isEditing: Boolean = false
)