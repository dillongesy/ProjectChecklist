package com.company.myapplication;

public class ChecklistItem {
    private String task;
    private boolean isChecked;

    public ChecklistItem(String task) {
        this.task = task;
        this.isChecked = false;
    }

    public String getTask() {
        return task;
    }
    public boolean isChecked() {
        return isChecked;
    }
    public void toggleChecked() {
        this.isChecked = !this.isChecked;
    }
}