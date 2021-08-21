package com.example.sudoku.database

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromSet(notes: MutableSet<Int>): String {
        return notes.joinToString("|")
    }

    @TypeConverter
    fun stringToSet(notes: String): MutableSet<Int> {
        if (notes.isEmpty()) return mutableSetOf<Int>()
        val notesSet = notes.split("|").map { it.toInt() }
        return notesSet.toMutableSet()
    }
}