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
import java.text.SimpleDateFormat
import java.util.*

class NoteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNoteBinding
    private var purpose: String? = ""
    private var noteId : Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = getIntent()
        purpose = intent.getStringExtra(
            getString(R.string.intent_purpose_key)
        )

        if (purpose.equals(getString(R.string.intent_purpose_update_note))) {
            noteId = intent.getLongExtra(
                getString(R.string.intent_key_note_id),
                -1
            )

            CoroutineScope(Dispatchers.IO).launch {
                val note = AppDatabase.getDatabase(applicationContext)
                    .noteDao()
                    .getNote(noteId)

                withContext(Dispatchers.Main) {
                    binding.titleEditText.setText(note.title)
                    binding.contentsEditText.setText(note.notes)
                }
            }
        }

        setTitle("${purpose} Note")
    }

    override fun onBackPressed() {
        val title = binding.titleEditText.text.toString().trim()
        if (title.isBlank()) {
            Toast.makeText(applicationContext, getString(R.string.blank_title_msg), Toast.LENGTH_LONG).show()
            return
        }

        var content = binding.contentsEditText.text.toString().trim()
        if (content.isNullOrBlank()) { content = "" }

        val modified = getDate()

        CoroutineScope(Dispatchers.IO).launch {
            val noteDao = AppDatabase.getDatabase(applicationContext)
                .noteDao()

            if (purpose.equals(getString(R.string.intent_purpose_add_note))) {
                val note = Note(0, title, content, modified)
                noteId = noteDao.addNote(note)
            } else {
                val note = Note(noteId, title, content, modified)
                noteDao.updateNote(note)
            }

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

    private fun getDate(): String {
        // get the current date and time as a timestamp
        val now : Date = Date()
        // Set up a date formatter to support ISO 8601 format and UTC time zone
        val databaseDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        databaseDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"))
        // Convert timestamp to ISO 8601 format
        var dateString : String = databaseDateFormat.format(now)
        return dateString
    }
}