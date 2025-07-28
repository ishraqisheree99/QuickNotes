package com.example.noteapp.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.Date;

@Entity(tableName = "notes")
public class Note {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String title;
    private String content;
    private boolean isFavorite;
    private boolean isList;
    private Date dateCreated;
    private Date dateModified;

    // Constructors
    public Note() {
        this.dateCreated = new Date();
        this.dateModified = new Date();
    }

    public Note(String title, String content, boolean isList) {
        this.title = title;
        this.content = content;
        this.isList = isList;
        this.isFavorite = false;
        this.dateCreated = new Date();
        this.dateModified = new Date();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }

    public boolean isList() { return isList; }
    public void setList(boolean list) { isList = list; }

    public Date getDateCreated() { return dateCreated; }
    public void setDateCreated(Date dateCreated) { this.dateCreated = dateCreated; }

    public Date getDateModified() { return dateModified; }
    public void setDateModified(Date dateModified) { this.dateModified = dateModified; }
}