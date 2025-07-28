package com.example.noteapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.example.noteapp.adapter.ListItemAdapter;
import com.example.noteapp.database.AppDatabase;
import com.example.noteapp.model.ListItem;
import com.example.noteapp.model.Note;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AddEditNoteActivity extends AppCompatActivity implements ListItemAdapter.OnItemChangeListener {
    private EditText titleEditText, contentEditText;
    private RecyclerView listRecyclerView;
    private FloatingActionButton addItemFab;
    private ListItemAdapter listAdapter;

    private Note currentNote;
    private AppDatabase database;
    private boolean isListNote;
    private boolean isEditMode;
    private List<ListItem> listItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_note);

        initViews();
        setupData();
        setupListRecyclerView();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        titleEditText = findViewById(R.id.titleEditText);
        contentEditText = findViewById(R.id.contentEditText);
        listRecyclerView = findViewById(R.id.listRecyclerView);
        addItemFab = findViewById(R.id.addItemFab);

        database = AppDatabase.getInstance(this);
        listItems = new ArrayList<>();

        addItemFab.setOnClickListener(v -> addNewListItem());
    }

    private void setupData() {
        isListNote = getIntent().getBooleanExtra("is_list", false);
        int noteId = getIntent().getIntExtra("note_id", -1);

        if (noteId != -1) {
            isEditMode = true;
            new LoadNoteTask().execute(noteId);
        } else {
            isEditMode = false;
            currentNote = new Note("", "", isListNote);
        }

        // Show/hide views based on note type
        if (isListNote) {
            contentEditText.setVisibility(View.GONE);
            listRecyclerView.setVisibility(View.VISIBLE);
            addItemFab.setVisibility(View.VISIBLE);
        } else {
            contentEditText.setVisibility(View.VISIBLE);
            listRecyclerView.setVisibility(View.GONE);
            addItemFab.setVisibility(View.GONE);
        }

        setupTextWatchers();
    }

    private void setupTextWatchers() {
        titleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (currentNote != null) {
                    currentNote.setTitle(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        if (!isListNote) {
            contentEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (currentNote != null) {
                        currentNote.setContent(s.toString());
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void setupListRecyclerView() {
        listAdapter = new ListItemAdapter(this, listItems);
        listAdapter.setOnItemChangeListener(this);
        listRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        listRecyclerView.setAdapter(listAdapter);
    }

    private void addNewListItem() {
        listItems.add(new ListItem("", false));
        listAdapter.notifyItemInserted(listItems.size() - 1);
    }

    @Override
    public void onItemChanged() {
        updateNoteContent();
    }

    @Override
    public void onItemDeleted(int position) {
        if (position >= 0 && position < listItems.size()) {
            listItems.remove(position);
            listAdapter.notifyItemRemoved(position);
            updateNoteContent();
        }
    }

    private void updateNoteContent() {
        if (isListNote && currentNote != null) {
            Gson gson = new Gson();
            currentNote.setContent(gson.toJson(listItems));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_edit_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_save) {
            saveNote();
            return true;
        } else if (id == R.id.action_favorite) {
            toggleFavorite();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void toggleFavorite() {
        if (currentNote != null) {
            currentNote.setFavorite(!currentNote.isFavorite());
            invalidateOptionsMenu();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem favoriteItem = menu.findItem(R.id.action_favorite);
        if (currentNote != null && favoriteItem != null) {
            favoriteItem.setIcon(currentNote.isFavorite() ?
                    R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    private void saveNote() {
        if (currentNote == null) return;

        String title = titleEditText.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show();
            return;
        }

        currentNote.setTitle(title);

        if (!isListNote) {
            currentNote.setContent(contentEditText.getText().toString());
        } else {
            updateNoteContent();
        }

        currentNote.setDateModified(new Date());

        if (isEditMode) {
            new UpdateNoteTask().execute(currentNote);
        } else {
            new InsertNoteTask().execute(currentNote);
        }
    }

    // AsyncTasks
    private class LoadNoteTask extends AsyncTask<Integer, Void, Note> {
        @Override
        protected Note doInBackground(Integer... ids) {
            return database.noteDao().getNoteById(ids[0]);
        }

        @Override
        protected void onPostExecute(Note note) {
            if (note != null) {
                currentNote = note;
                titleEditText.setText(note.getTitle());

                if (note.isList()) {
                    loadListItems(note.getContent());
                } else {
                    contentEditText.setText(note.getContent());
                }

                invalidateOptionsMenu();
            }
        }
    }

    private void loadListItems(String content) {
        if (content != null && !content.isEmpty()) {
            try {
                Gson gson = new Gson();
                Type listType = new TypeToken<List<ListItem>>(){}.getType();
                List<ListItem> items = gson.fromJson(content, listType);
                if (items != null) {
                    listItems.clear();
                    listItems.addAll(items);
                    listAdapter.notifyDataSetChanged();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class InsertNoteTask extends AsyncTask<Note, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Note... notes) {
            try {
                database.noteDao().insertNote(notes[0]);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(AddEditNoteActivity.this, "Note saved", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(AddEditNoteActivity.this, "Error saving note", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class UpdateNoteTask extends AsyncTask<Note, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Note... notes) {
            try {
                database.noteDao().updateNote(notes[0]);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(AddEditNoteActivity.this, "Note updated", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(AddEditNoteActivity.this, "Error updating note", Toast.LENGTH_SHORT).show();
            }
        }
    }
}