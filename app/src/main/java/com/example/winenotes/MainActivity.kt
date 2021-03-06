package com.example.winenotes

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.winenotes.database.AppDatabase
import com.example.winenotes.database.Note
import com.example.winenotes.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: MyAdapter
    private val notes = mutableListOf<Note>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val layoutManager = LinearLayoutManager(this)
        binding.mainRecyclerview.layoutManager = layoutManager

        val dividerItemDecoration = DividerItemDecoration(applicationContext, layoutManager.orientation)
        binding.mainRecyclerview.addItemDecoration(dividerItemDecoration)

        adapter = MyAdapter()
        binding.mainRecyclerview.adapter = adapter

        loadAllNotes("")
    }

    private fun loadAllNotes(sort: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(applicationContext)
            val dao = db.noteDao()

            val results = when (sort) {
                getString(R.string.sort_title) -> { dao.getAllNotesByTitle() }
                getString(R.string.sort_modified) -> { dao.getAllNotesByLastModified() }
                else -> { dao.getAllNotes() }
            }

            withContext(Dispatchers.Main) {
                notes.clear()
                notes.addAll(results)
                adapter.notifyDataSetChanged()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_item_add -> {
                addNewNote()
                return true
            }
            R.id.title_sort_menu_item -> {
                loadAllNotes(getString(R.string.sort_title))
                return true
            }
            R.id.modified_sort_menu_item -> {
                loadAllNotes(getString(R.string.sort_modified))
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private val startForAddResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) { loadAllNotes("") }
        }

    private val startForUpdateResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result : ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) { loadAllNotes("") }
        }

    private fun addNewNote() {
        val intent = Intent(applicationContext, NoteActivity::class.java)
        intent.putExtra(
            getString(R.string.intent_purpose_key),
            getString(R.string.intent_purpose_add_note)
        )
        startForAddResult.launch(intent)
    }


    inner class MyViewHolder(val view: TextView) :
        RecyclerView.ViewHolder(view),
        View.OnClickListener, View.OnLongClickListener {

        init {
            view.setOnClickListener(this)
            view.setOnLongClickListener(this)
        }

        override fun onClick(view: View?) {
            val intent = Intent(applicationContext, NoteActivity::class.java)

            intent.putExtra(
                getString(R.string.intent_purpose_key),
                getString(R.string.intent_purpose_update_note)
            )

            val note = notes[adapterPosition]
            intent.putExtra(
                getString(R.string.intent_key_note_id),
                note.id
            )

            startForUpdateResult.launch(intent)
        }

        override fun onLongClick(view: View?): Boolean {
            val note = notes[adapterPosition]

            val builder = AlertDialog.Builder(view!!.context)
                .setTitle(getString(R.string.delete_dialog_title))

                .setMessage(getString(R.string.delete_dialog_msg) + "${note.title}\"?")
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok) {
                        dialogInterface, whichButton ->

                    CoroutineScope(Dispatchers.IO).launch {
                        AppDatabase.getDatabase(applicationContext)
                            .noteDao()
                            .deleteNote(note)
                        loadAllNotes("")
                    }
                }
            builder.show()
            return true
        }
    }

    inner class MyAdapter :
        RecyclerView.Adapter<MyViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_view, parent, false) as TextView
            return MyViewHolder(view)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val note = notes[position]
            val date = getDate(note)
            holder.view.setText("${note.title} - $date")
        }

        override fun getItemCount(): Int {
            return notes.size
        }
    }

    fun getDate(note: Note): String {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        parser.timeZone = TimeZone.getTimeZone("UTC")
        val dateInDatabase: Date = parser.parse(note.lastModified)
        val displayFormat = SimpleDateFormat("hh:mm a  MM/dd/yyyy ")
        return displayFormat.format(dateInDatabase)
    }
}