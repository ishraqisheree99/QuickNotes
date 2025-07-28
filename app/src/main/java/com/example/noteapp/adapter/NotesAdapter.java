package com.example.noteapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.noteapp.R;
import com.example.noteapp.model.Note;
import com.example.noteapp.model.ListItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {
    private Context context;
    private List<Note> notes;
    private OnNoteClickListener listener;

    public interface OnNoteClickListener {
        void onNoteClick(Note note);
        void onNoteLongClick(Note note);
        void onFavoriteClick(Note note);
    }

    public NotesAdapter(Context context, List<Note> notes) {
        this.context = context;
        this.notes = notes;
    }

    public void setOnNoteClickListener(OnNoteClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notes.get(position);

        holder.titleTextView.setText(note.getTitle());

        // Handle content display based on note type
        if (note.isList()) {
            // Format list content for display
            String formattedContent = formatListContent(note.getContent());
            holder.contentTextView.setText(formattedContent);
        } else {
            // Regular text note
            holder.contentTextView.setText(note.getContent());
        }

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        holder.dateTextView.setText(sdf.format(note.getDateModified()));

        holder.favoriteImageView.setImageResource(note.isFavorite() ?
                R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border);

        holder.typeImageView.setImageResource(note.isList() ?
                R.drawable.ic_list : R.drawable.ic_note);

        holder.cardView.setOnClickListener(v -> {
            if (listener != null) listener.onNoteClick(note);
        });

        holder.cardView.setOnLongClickListener(v -> {
            if (listener != null) listener.onNoteLongClick(note);
            return true;
        });

        holder.favoriteImageView.setOnClickListener(v -> {
            if (listener != null) listener.onFavoriteClick(note);
        });
    }

    private String formatListContent(String jsonContent) {
        try {
            if (jsonContent == null || jsonContent.isEmpty()) {
                return "Empty list";
            }

            Gson gson = new Gson();
            Type listType = new TypeToken<List<ListItem>>(){}.getType();
            List<ListItem> items = gson.fromJson(jsonContent, listType);

            if (items == null || items.isEmpty()) {
                return "Empty list";
            }

            StringBuilder preview = new StringBuilder();
            int itemCount = 0;
            int maxItems = 3; // Show max 3 items in preview

            for (ListItem item : items) {
                if (itemCount >= maxItems) break;

                String text = item.getText();
                if (text != null && !text.trim().isEmpty()) {
                    if (itemCount > 0) {
                        preview.append("\n");
                    }

                    // Add checkbox symbol
                    String checkbox = item.isChecked() ? "☑ " : "☐ ";
                    preview.append(checkbox).append(text.trim());
                    itemCount++;
                }
            }

            // Add indicator if there are more items
            if (items.size() > maxItems) {
                preview.append("\n... and ").append(items.size() - maxItems).append(" more");
            }

            return preview.length() > 0 ? preview.toString() : "Empty list";

        } catch (Exception e) {
            e.printStackTrace();
            return "List preview unavailable";
        }
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public void updateNotes(List<Note> newNotes) {
        this.notes = newNotes;
        notifyDataSetChanged();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView titleTextView, contentTextView, dateTextView;
        ImageView favoriteImageView, typeImageView;

        NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            contentTextView = itemView.findViewById(R.id.contentTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            favoriteImageView = itemView.findViewById(R.id.favoriteImageView);
            typeImageView = itemView.findViewById(R.id.typeImageView);
        }
    }
}