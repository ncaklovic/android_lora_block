
package com.crobridge.lorablock.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "board")
data class Board(
        @PrimaryKey(autoGenerate = true)
        var id: Long = 0L,

        @ColumnInfo(name = "game")
        var game: Long = 0L,

        @ColumnInfo(name = "type") // 1..7
        var type: Int = 1,

        @ColumnInfo(name = "player") // 1..4
        var player: Int = 1,

        @ColumnInfo(name = "score1")
        var score1: Int = 0,

        @ColumnInfo(name = "score2")
        var score2: Int = 0,

        @ColumnInfo(name = "score3")
        var score3: Int = 0,

        @ColumnInfo(name = "score4")
        var score4: Int = 0,
)
