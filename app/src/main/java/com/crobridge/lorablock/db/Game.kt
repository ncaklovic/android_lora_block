
package com.crobridge.lorablock.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game")
data class Game(
        @PrimaryKey(autoGenerate = true)
        var id: Long = 0L,

        @ColumnInfo(name = "start_time")
        val startTimeMilliSec: Long = System.currentTimeMillis(),

        @ColumnInfo(name = "player1")
        var player1: String = "",

        @ColumnInfo(name = "player2")
        var player2: String = "",

        @ColumnInfo(name = "player3")
        var player3: String = "",

        @ColumnInfo(name = "player4")
        var player4: String = ""
)
