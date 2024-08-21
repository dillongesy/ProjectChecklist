package com.company.myapplication;

import java.util.ArrayList;

public class Project {
    private String name;
    private String location;
    private String manager;

    private ArrayList<ChecklistItem> checklist;

    public Project(String name, String location, String manager) {
        this.name = name;
        this.location = location;
        this.manager = manager;
        this.checklist = new ArrayList<>();
    }

    public String getName() {
        return name;
    }
    public String getLocation() {
        return location;
    }
    public String getManager() {
        return manager;
    }
    public  ArrayList<ChecklistItem> getChecklist() {
        return checklist;
    }
    public void addChecklistItem(ChecklistItem item) {
        checklist.add(item);
    }
    public void deleteChecklistItem(ChecklistItem item) {
        //TODO
    }
}
