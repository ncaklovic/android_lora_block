package com.crobridge.lorablock

import android.app.Application
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.*

import com.crobridge.lorablock.db.Board
import com.crobridge.lorablock.db.Game
import com.crobridge.lorablock.db.LoraDao

//import androidx.lifecycle.Transformations
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoraViewModel(val db: LoraDao, app: Application) : AndroidViewModel(app) {

    private val current_game_id = MutableLiveData<Long>(0)

    private suspend fun insert(b: Board) {
        db.insert(b)
    }

    fun add_board(b: Board) {
        if (current_game_id.value != 0L) {
            b.game = current_game_id.value!!
            viewModelScope.launch {
                insert(b)
            }
        }
    }

    // switchMap -> refresh for new id
    val boards = current_game_id.switchMap { game_id ->
        db.get_all_boards(game_id)
    }

    private suspend fun insert(g: Game) {
        current_game_id.value = db.insert(g)
    }

    fun add_game(g: Game) {
        viewModelScope.launch {
            insert(g)
        }
    }

    val games = db.get_all_games()

    fun set_game_id(game_id: Long) {
        current_game_id.value = game_id
    }

    val current_game = current_game_id.switchMap { game_id ->
        db.get_current_game(game_id)
    }

    val total = current_game_id.switchMap { game_id ->
        db.get_total(game_id)
    }

    private suspend fun delete_last() = withContext(Dispatchers.Default) {
        db.delete_last_game(current_game_id.value!!)
    }

    fun delete_last_board() {
        if (current_game_id.value != 0L) {
            viewModelScope.launch {
                delete_last()
            }
        }
    }

    fun player_name(index: Int): String {
        return when (index) {
            0 -> current_game.value!!.player1
            1 -> current_game.value!!.player2
            2 -> current_game.value!!.player3
            3 -> current_game.value!!.player4
            else -> ""
        }
    }

}

@BindingAdapter("playerString")
fun TextView.bindPlayer(b: Board?) {
    b?.let {
        val activity = context as MainActivity
        text = activity.player_name(b.player).substring(0, 1)
    }
}

@BindingAdapter("gameString")
fun TextView.bindGame(b: Board?) {
    b?.let {
        text = context.resources.getStringArray(R.array.games_short)[b.type]
    }
}

