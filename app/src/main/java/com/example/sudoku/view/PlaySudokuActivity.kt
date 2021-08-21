package com.example.sudoku.view

import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.sudoku.R
import com.example.sudoku.database.BoardDatabase
import com.example.sudoku.game.Cell
import com.example.sudoku.view.custom.SudokuBoardView
import com.example.sudoku.viewmodel.PlaySudokuViewModel
import com.example.sudoku.viewmodel.PlaySudokuViewModelFactory
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_play_sudoku.*


const val DOUBLE_CLICK_TIME_DELTA = 300

class PlaySudokuActivity : AppCompatActivity(), SudokuBoardView.OnTouchListener {
    private var lastClickTime = 0L
    private lateinit var viewModel: PlaySudokuViewModel
    private lateinit var numberButtons: List<Button>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_sudoku)

        sudokuBoardView.registerListener(this)

        val dataSource = BoardDatabase.getInstance(application).boardDatabaseDao
        val viewModelFactory = PlaySudokuViewModelFactory(dataSource, application)
        viewModel =
            ViewModelProviders.of(this, viewModelFactory).get(PlaySudokuViewModel::class.java)

        viewModel.sudokuGame.cellsLiveData.observe(this, Observer { updateCells(it) })

        viewModel.sudokuGame.selectedCellLiveData.observe(
            this,
            Observer { updateSelectedCellUI(it) })

        viewModel.sudokuGame.isTakingNotesLiveData.observe(
            this,
            Observer { updateNoteTakingUI(it) })

        viewModel.sudokuGame.highlightedKeysLiveData.observe(
            this,
            Observer { updateHighlightedKeys(it) })

        viewModel.sudokuGame.showSnackbarEvent.observe(
            this,
            Observer {
                if (it == true) {
                    (Snackbar.make(
                        sudokuBoardView, R.string.won_message,
                        Snackbar.LENGTH_LONG
                    ).setAction(R.string.new_message) { viewModel.sudokuGame.newBoard() }).show()
                    viewModel.sudokuGame.doneShowingSnackBar()
                }
            })

        numberButtons = listOf(
            oneButton, twoButton, threeButton, fourButton, fiveButton, sixButton,
            sevenButton, eightButton, nineButton
        )
        numberButtons.forEachIndexed { index, button ->
            button.setOnClickListener { viewModel.sudokuGame.handleInput(index + 1) }
        }

        notesButton.setOnClickListener { viewModel.sudokuGame.changeNoteTakingState() }
        deleteButton.setOnClickListener { viewModel.sudokuGame.delete() }
        undoButton.setOnClickListener { viewModel.sudokuGame.undo() }
        clearButton.setOnClickListener { viewModel.sudokuGame.clearBoard() }
        resolveButton.setOnClickListener { viewModel.sudokuGame.solveBoard() }
        refreshButton.setOnClickListener { viewModel.sudokuGame.newBoard() }
    }

    private fun updateCells(cells: List<Cell>?) = cells?.let {
        sudokuBoardView.updateCells(cells)
    }

    private fun updateSelectedCellUI(cell: Triple<Int, Int, Int>?) = cell?.let {
        sudokuBoardView.updateSelectedCellUI(cell.first, cell.second, cell.third)
    }

    private fun updateNoteTakingUI(isNoteTaking: Boolean?) = isNoteTaking?.let {
        val color = if (it) ContextCompat.getColor(this, R.color.colorPrimary) else Color.LTGRAY
        notesButton.background.colorFilter = BlendModeColorFilter(color, BlendMode.MULTIPLY)
    }

    private fun updateHighlightedKeys(set: Set<Int>?) = set?.let {
        numberButtons.forEachIndexed { index, button ->
            val color = if (set.contains(index + 1)) ContextCompat.getColor(
                this,
                R.color.colorPrimary
            ) else Color.LTGRAY
            button.background.colorFilter = BlendModeColorFilter(color, BlendMode.MULTIPLY)
        }
    }

    override fun onCellTouched(row: Int, col: Int) {
        val clickTime = System.currentTimeMillis()
        if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
            viewModel.sudokuGame.updateStartingRow(row, col)
            lastClickTime = 0
        } else {
            viewModel.sudokuGame.updateSelectedCell(row, col)
        }
        lastClickTime = clickTime
    }
}
