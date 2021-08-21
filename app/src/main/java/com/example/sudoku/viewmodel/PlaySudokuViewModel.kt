package com.example.sudoku.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.sudoku.database.BoardDatabaseDao
import com.example.sudoku.game.SudokuGame

class PlaySudokuViewModel(
    val database: BoardDatabaseDao,
    application: Application
) : AndroidViewModel(application) {

    val sudokuGame = SudokuGame(database)

    override fun onCleared() {
        super.onCleared()
        sudokuGame.job.cancel()
    }
}
