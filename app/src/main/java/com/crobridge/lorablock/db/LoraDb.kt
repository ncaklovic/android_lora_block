package com.crobridge.lorablock.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Board::class, Game::class], version = 1, exportSchema = false)
abstract class LoraDb : RoomDatabase() {

    abstract val loraDbDao: LoraDao

    companion object {
        @Volatile
        private var INSTANCE: LoraDb? = null

        fun getInstance(context: Context): LoraDb {
            synchronized(this) {
                var instance = INSTANCE
                // If instance is `null` make a new database instance.
                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.applicationContext,
                        LoraDb::class.java,
                            "lora_block_db"
                    )
                            .fallbackToDestructiveMigration()
                            //.allowMainThreadQueries()
                            .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}
