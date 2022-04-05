package com.example.winenotes.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface NoteDao {

    @Query("SELECT * FROM note")
    fun getAllNotes() : List<Note>

    @Insert
    fun addNote(note : Note) : Long
}