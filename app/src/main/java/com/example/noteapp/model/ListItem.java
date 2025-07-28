package com.example.noteapp.model;

public class ListItem {
    private String text;
    private boolean isChecked;

    public ListItem(String text, boolean isChecked) {
        this.text = text;
        this.isChecked = isChecked;
    }

    // Getters and Setters
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public boolean isChecked() { return isChecked; }
    public void setChecked(boolean checked) { isChecked = checked; }
}