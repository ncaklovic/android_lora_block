package com.crobridge.lorablock

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.crobridge.lorablock.databinding.ActivityMainBinding
import com.crobridge.lorablock.db.LoraDb
import com.crobridge.lorablock.db.Game
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.crobridge.lorablock.databinding.NewBoardBinding
import com.crobridge.lorablock.databinding.NewGameBinding
import com.crobridge.lorablock.db.Board
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.Arrays

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var view_model: LoraViewModel
    private lateinit var games: List<Game>
    private var last_player_index: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.appToolbar.setTitle(R.string.app_name)
        setSupportActionBar(binding.appToolbar)

        val dataSource = LoraDb.getInstance(application).loraDbDao
        val viewModelFactory = ViewModelFactory(dataSource, application)
        view_model = ViewModelProvider(this, viewModelFactory).get(LoraViewModel::class.java)

        binding.viewModel = view_model
        binding.setLifecycleOwner(this)

        val manager = LinearLayoutManager(this)
        binding.boardList.layoutManager = manager

        val adapter = LoraAdapter()
        binding.boardList.adapter = adapter

        view_model.boards.observe(this, Observer {
            binding.fab.show()
            last_player_index = null
            it?.let {
                adapter.submitList(it)
                if (!it.isEmpty()) {
                    if (it.size == 28) // R.array.games.size * 4
                        binding.fab.hide()
                    last_player_index = it.last().player
                }
            }
        })

        view_model.games.observe(this, Observer {
            games = it
        })

        val fab: FloatingActionButton = binding.fab
        fab.hide()
        fab.setOnClickListener { _ ->
            newBoard()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.overflow_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.new_game -> {
                newGame()
                true
            }

            R.id.load_game -> {
                loadGame()
                true
            }

            R.id.delete_last -> {
                deleteLastBoard()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadGame() {
        val fmt =
            SimpleDateFormat.getDateInstance()   //  SimpleDateFormat.getDateTimeInstance(), SimpleDateFormat("dd.MM.yyyy hh:mm")
        val items = games.map {
            "${fmt.format(it.startTimeMilliSec)} - ${it.player1},${it.player2},${it.player3},${it.player4} "
        }.toTypedArray()
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.load_game)
        builder.setItems(items) { _, index ->
            loadGame(games[index].id)
        }
        val dlg = builder.create()
        dlg.show()
    }

    private fun loadGame(gameId: Long) {
        view_model.set_game_id(gameId)
        binding.fab.show()
    }

    private fun newGame() {
        val dialog_binding = NewGameBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.new_game)
        builder.setView(dialog_binding.root)
        builder.setNegativeButton(R.string.cancel, null)
        builder.setPositiveButton(R.string.ok, null)
        val dialog = builder.create()
        dialog.show()
        // validate input
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
            val player1 = dialog_binding.player1.text.toString()
            val player2 = dialog_binding.player2.text.toString()
            val player3 = dialog_binding.player3.text.toString()
            val player4 = dialog_binding.player4.text.toString()
            if (player1.isNotEmpty() && player2.isNotEmpty() && player3.isNotEmpty() && player4.isNotEmpty()) {
                newGame(player1, player2, player3, player4)
                dialog.dismiss()
            }
        }
    }

    private fun newGame(player1: String, player2: String, player3: String, player4: String) {
        val g = Game()
        g.player1 = player1
        g.player2 = player2
        g.player3 = player3
        g.player4 = player4
        view_model.add_game(g)
        binding.fab.show()
    }

    private fun deleteLastBoard() {
        view_model.delete_last_board()
        binding.fab.show()
    }

    private fun newBoard() {

        val dialog_binding = NewBoardBinding.inflate(layoutInflater)
        dialog_binding.player.text = next_player()
        dialog_binding.score1.hint = player_name(0)
        dialog_binding.score2.hint = player_name(1)
        dialog_binding.score3.hint = player_name(2)
        dialog_binding.score4.hint = player_name(3)
        val all_games = resources.getStringArray(R.array.games)
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            unused_games(ArrayList<String>(Arrays.asList(*all_games)))
        )
        dialog_binding.type.adapter = adapter

        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.new_board)
        builder.setView(dialog_binding.root)
        builder.setNegativeButton(R.string.cancel, null)
        builder.setPositiveButton(R.string.ok, null)
        val dialog = builder.create()
        dialog.show()
        // validate input
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
            val score1 = dialog_binding.score1.text.toString().toIntOrNull()
            val score2 = dialog_binding.score2.text.toString().toIntOrNull()
            val score3 = dialog_binding.score3.text.toString().toIntOrNull()
            val score4 = dialog_binding.score4.text.toString().toIntOrNull()
            if (score1 != null && score2 != null && score3 != null && score4 != null) {
                val type = all_games.indexOf(dialog_binding.type.selectedItem.toString())
                newBoard(type, score1, score2, score3, score4)
                dialog.dismiss()
            }
        }
    }

    private fun unused_games(all_games: List<String>): List<String> {
        val next_player_index = next_player_index()
        val used_games = view_model.boards.value!!.filter {
            it.player == next_player_index
        }.map { it -> it.type }
        return all_games.filterIndexed { game, _ ->
            game !in used_games
        }
    }

    private fun newBoard(type: Int, score1: Int, score2: Int, score3: Int, score4: Int) {
        val b = Board()
        b.player = next_player_index()
        b.type = type
        b.score1 = score1
        b.score2 = score2
        b.score3 = score3
        b.score4 = score4
        view_model.add_board(b)
    }

    fun next_player_index(): Int {
        if (last_player_index == null) {
            return 0
        }
        return (last_player_index!! + 1) % 4
    }

    fun player_name(index: Int): String {
        return view_model.player_name(index)
    }

    fun next_player(): String {
        return player_name(next_player_index())
    }

}