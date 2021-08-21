package com.example.sudoku.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.sudoku.game.Cell

@Database(entities = [Cell::class, BoardInfo::class], version = 10, exportSchema = false)
@TypeConverters(Converters::class)
abstract class BoardDatabase : RoomDatabase() {
    abstract val boardDatabaseDao: BoardDatabaseDao

    companion object {
        @Volatile
        private var INSTANCE: BoardDatabase? = null
        fun getInstance(context: Context): BoardDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        BoardDatabase::class.java,
                        "board_history_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}