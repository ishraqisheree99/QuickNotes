package com.example.noteapp.adapter;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.noteapp.R;
import com.example.noteapp.model.ListItem;
import java.util.List;

public class ListItemAdapter extends RecyclerView.Adapter<ListItemAdapter.ListItemViewHolder> {
    private List<ListItem> items;
    private Context context;
    private OnItemChangeListener listener;

    public interface OnItemChangeListener {
        void onItemChanged();
        void onItemDeleted(int position);
    }

    public ListItemAdapter(Context context, List<ListItem> items) {
        this.context = context;
        this.items = items;
    }

    public void setOnItemChangeListener(OnItemChangeListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ListItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_list, parent, false);
        return new ListItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListItemViewHolder holder, int position) {
        ListItem item = items.get(position);

        holder.checkBox.setChecked(item.isChecked());
        holder.editText.setText(item.getText());

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setChecked(isChecked);
            if (listener != null) listener.onItemChanged();
        });

        holder.editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                item.setText(s.toString());
                if (listener != null) listener.onItemChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) listener.onItemDeleted(position);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ListItemViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        EditText editText;
        ImageButton deleteButton;

        ListItemViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkBox);
            editText = itemView.findViewById(R.id.editText);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}