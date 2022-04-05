package com.example.winenotes

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.winenotes.database.AppDatabase
import com.example.winenotes.database.Note
import com.example.winenotes.databinding.ActivityNoteBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NoteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNoteBinding
    private var purpose : String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = getIntent()
        purpose = intent.getStringExtra(getString(R.string.intent_purpose_key))

        setTitle("$purpose Note")
    }

    override fun onBackPressed() {
        val title = binding.titleEditText.text.toString().trim()
        if (title.isBlank()) {
            Toast.makeText(applicationContext, "Title cannot be empty", Toast.LENGTH_LONG).show()
            return
        }

        var content = binding.contentsEditText.text.toString().trim()
        if (content.isNullOrBlank()) { content = "" }

        CoroutineScope(Dispatchers.IO).launch {
            val noteDao = AppDatabase.getDatabase(applicationContext).noteDao()
            var resultId : Long

            when {
                purpose.equals(getString(R.string.intent_purpose_add_note)) -> {
                    val note = Note(0, title, content, "today")
                    resultId = noteDao.addNote(note)
                    Log.i("STATUS_NAME", "inserted new note: $note")
                }
                else -> { TODO("Not implemented") }
            }
        }

        super.onBackPressed()
    }
}