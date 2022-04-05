package com.example.winenotes

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.winenotes.databinding.ActivityNoteBinding

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
        super.onBackPressed()
    }
}