
package com.crobridge.lorablock.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface LoraDao {

    @Insert
    suspend fun insert(b: Board) : Long

    @Query("SELECT * FROM board WHERE game = :game_id")
    fun get_all_boards(game_id: Long): LiveData<List<Board>>

    @Insert
    suspend fun insert(g: Game) : Long

    @Query("SELECT * FROM Game")
    fun get_all_games(): LiveData<List<Game>>

    @Query("SELECT * FROM game WHERE id = :game_id")
    fun get_current_game(game_id: Long): LiveData<Game>

    @Query("SELECT SUM(score1) AS score1, SUM(score2) AS score2, SUM(score3) AS score3, SUM(score4) AS score4 FROM board WHERE game = :game_id")
    fun get_total(game_id: Long): LiveData<Total>

    @Query("DELETE FROM board WHERE id = (SELECT MAX(id) FROM board WHERE game = :game_id)")
    fun delete_last_game(game_id: Long)

}
