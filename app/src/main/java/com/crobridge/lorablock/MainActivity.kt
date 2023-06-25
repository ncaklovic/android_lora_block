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
    private var used_games: MutableList<Int> = mutableListOf()

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
            it?.let {
                adapter.submitList(it)
                if (it.isEmpty()) {
                    last_player_index = null
                    used_games.clear()
                } else {
                    last_player_index = it.last().player
                    val next_player = next_player_index()
                    it.forEach() {
                        if (it.player == next_player)
                            used_games.add(it.type)
                    }
                }
            }
        })

        view_model.games.observe(this, Observer {
            games = it
        })

        val fab: FloatingActionButton = binding.fab

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
                view_model.delete_last_board()
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
    }

    private fun newGame() {
        val dialog_binding = NewGameBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.new_game)
        builder.setView(dialog_binding.root)
        builder.setNegativeButton(R.string.cancel, null)
        builder.setPositiveButton(R.string.ok,
            DialogInterface.OnClickListener { _, _ ->
                newGame(
                    dialog_binding.player1.text.toString(),
                    dialog_binding.player2.text.toString(),
                    dialog_binding.player3.text.toString(),
                    dialog_binding.player4.text.toString()
                )
            })
        val dialog = builder.create()
        dialog.show()
    }

    private fun newGame(player1: String, player2: String, player3: String, player4: String) {
        val g = Game()
        g.player1 = player1
        g.player2 = player2
        g.player3 = player3
        g.player4 = player4
        view_model.add_game(g)
    }

    private fun newBoard() {
        val dialog_binding = NewBoardBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.new_board)
        builder.setView(dialog_binding.root)
        builder.setNegativeButton(R.string.cancel, null)
        builder.setPositiveButton(R.string.ok,
            DialogInterface.OnClickListener { _, _ ->
                newBoard(
                    dialog_binding.type.selectedItemPosition,
                    dialog_binding.score1.text.toString().toInt(),
                    dialog_binding.score2.text.toString().toInt(),
                    dialog_binding.score3.text.toString().toInt(),
                    dialog_binding.score4.text.toString().toInt()
                )
            })
        val dialog = builder.create()
        dialog_binding.player.text = next_player()
        val games = ArrayList<String>(Arrays.asList(*resources.getStringArray(R.array.games)))
        used_games.forEach {
            games.removeAt(it)
        }
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item, games
        )
        dialog_binding.type.adapter = adapter
        dialog.show()
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