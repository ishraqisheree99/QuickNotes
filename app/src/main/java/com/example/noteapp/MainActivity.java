package com.example.noteapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.noteapp.adapter.NotesAdapter;
import com.example.noteapp.database.AppDatabase;
import com.example.noteapp.model.Note;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NotesAdapter.OnNoteClickListener {
    private RecyclerView recyclerView;
    private NotesAdapter adapter;
    private List<Note> notes;
    private AppDatabase database;
    private boolean showFavoritesOnly = false;
    private boolean isGridLayout = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupRecyclerView();
        loadNotes();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recyclerView);
        FloatingActionButton fab = findViewById(R.id.fab);

        database = AppDatabase.getInstance(this);
        notes = new ArrayList<>();

        fab.setOnClickListener(v -> showCreateNoteDialog());
    }

    private void setupRecyclerView() {
        adapter = new NotesAdapter(this, notes);
        adapter.setOnNoteClickListener(this);
        recyclerView.setAdapter(adapter);
        updateLayoutManager();
    }

    private void updateLayoutManager() {
        if (isGridLayout) {
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        }
    }

    private void showCreateNoteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create Note")
                .setItems(new String[]{"Text Note", "List Note"}, (dialog, which) -> {
                    Intent intent = new Intent(this, AddEditNoteActivity.class);
                    intent.putExtra("is_list", which == 1);
                    startActivity(intent);
                });
        builder.show();
    }

    private void loadNotes() {
        new LoadNotesTask().execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotes();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_toggle_favorites) {
            showFavoritesOnly = !showFavoritesOnly;
            item.setTitle(showFavoritesOnly ? "Show All" : "Show Favorites");
            loadNotes();
            return true;
        } else if (id == R.id.action_toggle_layout) {
            isGridLayout = !isGridLayout;
            item.setIcon(isGridLayout ? R.drawable.ic_view_list : R.drawable.ic_view_grid);
            updateLayoutManager();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNoteClick(Note note) {
        Intent intent = new Intent(this, AddEditNoteActivity.class);
        intent.putExtra("note_id", note.getId());
        intent.putExtra("is_list", note.isList());
        startActivity(intent);
    }

    @Override
    public void onNoteLongClick(Note note) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Note")
                .setMessage("Are you sure you want to delete this note?")
                .setPositiveButton("Delete", (dialog, which) -> deleteNote(note))
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onFavoriteClick(Note note) {
        note.setFavorite(!note.isFavorite());
        new UpdateNoteTask().execute(note);
    }

    private void deleteNote(Note note) {
        new DeleteNoteTask().execute(note);
    }

    // AsyncTasks
    private class LoadNotesTask extends AsyncTask<Void, Void, List<Note>> {
        @Override
        protected List<Note> doInBackground(Void... voids) {
            if (showFavoritesOnly) {
                return database.noteDao().getFavoriteNotes();
            } else {
                return database.noteDao().getAllNotes();
            }
        }

        @Override
        protected void onPostExecute(List<Note> noteList) {
            notes.clear();
            notes.addAll(noteList);
            adapter.notifyDataSetChanged();
        }
    }

    private class UpdateNoteTask extends AsyncTask<Note, Void, Void> {
        @Override
        protected Void doInBackground(Note... notes) {
            database.noteDao().updateNote(notes[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            loadNotes();
        }
    }

    private class DeleteNoteTask extends AsyncTask<Note, Void, Void> {
        @Override
        protected Void doInBackground(Note... notes) {
            database.noteDao().deleteNote(notes[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            loadNotes();
        }
    }
}