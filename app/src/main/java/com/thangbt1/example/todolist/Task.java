package com.thangbt1.example.todolist;

import java.io.Serializable;

public class Task implements Serializable {
    private String title;
    private String description;
    private String category;
    private String imageUri;
    private long dueTimeMillis;
    private boolean completed;

    public Task(String title, String description, String category, String imageUri, long dueTimeMillis) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.imageUri = imageUri;
        this.dueTimeMillis = dueTimeMillis;
        this.completed = false;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public String getImageUri() {
        return imageUri;
    }

    public long getDueTimeMillis() {
        return dueTimeMillis;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
