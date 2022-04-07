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
import kotlinx.coroutines.withContext

class NoteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNoteBinding
    private var purpose: String? = ""
    private var noteId : Long = -1

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

//        CoroutineScope(Dispatchers.IO).launch {
//            val noteDao = AppDatabase.getDatabase(applicationContext).noteDao()
//            var noteId : Long
//
//            when {
//                purpose.equals(getString(R.string.intent_purpose_add_note)) -> {
//                    val note = Note(0, title, content, "today")
//                    noteId = noteDao.addNote(note)
//                    Log.i("STATUS_NAME", "inserted new note: $note")
//                }
//                else -> { TODO("Not implemented") }
//            }
//
//            val intent = Intent()
//
//            intent.putExtra(
//                getString(R.string.intent_key_note_id),
//                noteId
//            )
//
//            withContext(Dispatchers.Main) {
//                setResult(RESULT_OK, intent)
//                super.onBackPressed()
//            }
//        }

        CoroutineScope(Dispatchers.IO).launch {
            val noteDao = AppDatabase.getDatabase(applicationContext)
                .noteDao()

            if (purpose.equals(getString(R.string.intent_purpose_add_note))) {
                val note = Note(0, title, content, "today")
                noteId = noteDao.addNote(note)
                Log.i("STATUS_NAME", "inserted new note: note")
            } else {
                // update a current person in the database
//                val note = Note(noteId, title, content, "today")
//                noteDao.updateNote(note)
//                Log.i("STATUS_NAME", "updated existing note: $note")
            }

            Log.i("STATUS_NAME", "result_id: $noteId")

            val intent = Intent()

            intent.putExtra(
                getString(R.string.intent_key_note_id),
                noteId
            )

            withContext(Dispatchers.Main) {
                setResult(RESULT_OK, intent)
                super.onBackPressed()
            }
        }
    }
}