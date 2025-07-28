package com.example.noteapp.database;

import androidx.room.*;
import com.example.noteapp.model.Note;
import java.util.List;

@Dao
public interface NoteDao {
    // Modified query to show favorites first, then regular notes
    @Query("SELECT * FROM notes ORDER BY isFavorite DESC, dateModified DESC")
    List<Note> getAllNotes();

    @Query("SELECT * FROM notes WHERE isFavorite = 1 ORDER BY dateModified DESC")
    List<Note> getFavoriteNotes();

    @Query("SELECT * FROM notes WHERE id = :id")
    Note getNoteById(int id);

    @Insert
    long insertNote(Note note);

    @Update
    void updateNote(Note note);

    @Delete
    void deleteNote(Note note);

    @Query("DELETE FROM notes WHERE id = :id")
    void deleteNoteById(int id);
}