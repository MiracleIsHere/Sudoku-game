package com.example.sudoku.game

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.sudoku.database.BoardDatabaseDao
import com.example.sudoku.database.BoardInfo
import kotlinx.coroutines.*

class SudokuGame(val database: BoardDatabaseDao) {

    var selectedCellLiveData = MutableLiveData<Triple<Int, Int, Int>>()
    var cellsLiveData = MutableLiveData<List<Cell>>()
    val isTakingNotesLiveData = MutableLiveData<Boolean>()
    val highlightedKeysLiveData = MutableLiveData<Set<Int>>()

    private val _showSnackbarEvent = MutableLiveData<Boolean>()
    val showSnackbarEvent: LiveData<Boolean>
        get() = _showSnackbarEvent

    private lateinit var board: Board

    var boardSize = 9
    var isEditing = false
    val toGuess = (boardSize * boardSize) - boardSize * 3

    private var selectedRow = -1
    private var selectedCol = -1
    private var selectedValue = 0
    private var isTakingNotes = false

    private val history = mutableListOf<Cell>()

    var job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)

    init {
        initializeBoard()
    }

    fun handleInput(number: Int) {
        if (selectedRow == -1 || selectedCol == -1) return
        val cell = board.getCell(selectedRow, selectedCol)
        if (cell.isStartingCell) return

        if (isTakingNotes) {
            history.add(cell.deepCopy())

            if (cell.notes.contains(number)) {
                cell.notes.remove(number)
            } else {
                cell.notes.add(number)
            }
            highlightedKeysLiveData.postValue(cell.notes)
            cell.value = 0
        } else {
            if (number != cell.value) history.add(cell.deepCopy())
            cell.value = number
            cell.notes = mutableSetOf<Int>()
        }
        insertCell(cell)
        board.setConflicts(cell)
        cellsLiveData.postValue(board.cells)
        selectedCellLiveData.postValue(Triple(selectedRow, selectedCol, cell.value))
        isGuessed()
    }

    fun updateSelectedCell(row: Int, col: Int) {
        val cell = board.getCell(row, col)
        selectedRow = row
        selectedCol = col
        selectedValue = cell.value
        selectedCellLiveData.postValue(Triple(row, col, selectedValue))

        if (isTakingNotes && !cell.isStartingCell) {
            highlightedKeysLiveData.postValue(cell.notes)
        }
    }

    fun changeNoteTakingState() {
        if (selectedRow == -1 || selectedCol == -1) return
        isTakingNotes = !isTakingNotes
        isTakingNotesLiveData.postValue(isTakingNotes)

        val curNotes = if (isTakingNotes) {
            board.getCell(selectedRow, selectedCol).notes
        } else {
            setOf<Int>()
        }
        highlightedKeysLiveData.postValue(curNotes)
    }

    fun delete() {
        if (selectedRow == -1 || selectedCol == -1) return
        val cell = board.getCell(selectedRow, selectedCol)

        history.add(cell.deepCopy())

        if (isTakingNotes) {
            cell.notes.clear()
            highlightedKeysLiveData.postValue(setOf())
        } else if (!cell.isStartingCell) {
            cell.value = 0
            board.setConflicts(cell)
            selectedCellLiveData.postValue(Triple(selectedRow, selectedCol, 0))
        }
        insertCell(cell)
        cellsLiveData.postValue(board.cells)
    }

    fun undo() {
        if (history.isNotEmpty()) {
            val toPush = history.removeAt(history.lastIndex)
            val newCell = board.getCell(toPush.row, toPush.col).apply { update(toPush) }

//            board.setConflicts(newCell) //NOT necessary IF use Cell.deepcopy() WITH copying conflicts

            cellsLiveData.postValue(board.cells)
            if (selectedCol != -1 && selectedRow != -1) updateSelectedCell(selectedRow, selectedCol)
            isGuessed()
            insertCell(newCell)
        }
    }

    fun newBoard() {
        val cells =
            Sudoku.generate<Cell, Any>(boardSize, toGuess) { row: Int, col: Int, value: Int ->
                Cell(row, col, value, true)
            }

        board = Board(boardSize, cells)
        history.clear()

        cellsLiveData.postValue(board.cells)
        if (selectedCol != -1 && selectedRow != -1) updateSelectedCell(selectedRow, selectedCol)

        uiScope.launch {
            insertBoardToDB(BoardInfo().apply {
                size = boardSize
            })
            insertCellsToDB(board.cells)
        }
    }

    fun clearBoard() {
        board.cells.forEach { it.clear() }
        board.isEditing = true
        history.clear()

        cellsLiveData.postValue(board.cells)
        if (selectedCol != -1 && selectedRow != -1) updateSelectedCell(selectedRow, selectedCol)

        uiScope.launch {
            val boardInfo = getBoardFromDB()
            insertBoardToDB(BoardInfo().apply {
                size = boardSize
                isEditing = true
            })
            insertCellsToDB(board.cells)
        }
    }

    fun solveBoard() {
        history.clear()
        board.makeCorrections()

        Sudoku.solve(board.cells)

        cellsLiveData.postValue(board.cells)
        if (selectedCol != -1 && selectedRow != -1) updateSelectedCell(selectedRow, selectedCol)

        isGuessed()

        uiScope.launch {
            insertCellsToDB(board.cells)
        }
    }

    fun updateStartingRow(row: Int, col: Int) {
        if (board.isEditing) {
            val cell = board.getCell(row, col)
            if (cell.value != 0) {
                history.add(cell.deepCopy())
                cell.isStartingCell = !cell.isStartingCell
                board.setConflicts(cell)

                insertCell(cell)
            }
            if (selectedCol != -1 && selectedRow != -1) updateSelectedCell(selectedRow, selectedCol)
        }
    }

    private fun isGuessed() {
        _showSnackbarEvent.postValue(board.isGuessed())
    }

    fun doneShowingSnackBar() {
        _showSnackbarEvent.postValue(false)
    }

    private fun initializeBoard() {
        uiScope.launch {
            val boardInfo = getBoardFromDB()
            val cells: List<Cell>
            if (boardInfo == null) {
                cells =
                    Sudoku.generate<Cell, Any>(
                        boardSize,
                        toGuess
                    ) { row: Int, col: Int, value: Int ->
                        Cell(
                            row, col, value, true
                        )
                    }
                insertBoardToDB(BoardInfo().apply { size = boardSize })
                insertCellsToDB(cells)
                board = Board(boardSize, cells, isEditing)
            } else {
                boardSize = boardInfo.size
                isEditing = boardInfo.isEditing

                cells = getCellsFromDB(boardSize)

                board = Board(boardSize, cells, isEditing)
                board.cells.forEach { board.setConflicts(it) }
            }
            cellsLiveData.postValue(board.cells)
        }
    }

    private suspend fun insertBoardToDB(boardInfo: BoardInfo) {
        withContext(Dispatchers.IO) {
            database.insertBoard(boardInfo)
        }
    }

    private suspend fun insertCellsToDB(cells: List<Cell>) {
        withContext(Dispatchers.IO) {
            database.insertCells(cells)
        }
    }

    private suspend fun getBoardFromDB() = withContext(Dispatchers.IO) {
        database.getLatestBoard()
    }

    private suspend fun getCellsFromDB(size: Int) = withContext(Dispatchers.IO) {
        database.getCells(size * size)
    }

    private fun insertCell(cell: Cell) {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                database.insertCell(cell)
            }
        }
    }
}