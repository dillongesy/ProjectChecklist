package com.company.myapplication;

import java.util.ArrayList;

public class ScreenHelper {
    private static ScreenHelper instance;

    public String name;
    public String location;
    public String manager;
    public ArrayList<String> checkItems;
    public String checkItemsName;

    private ScreenHelper() {
        checkItems = new ArrayList<>();
    }

    public static synchronized ScreenHelper getInstance() {
        if (instance == null) {
            instance = new ScreenHelper();
        }
        return instance;
    }

    public ArrayList<String> getCheckItems() {
        return checkItems;
    }

    public String getLocation() {
        return location;
    }

    public String getManager() {
        return manager;
    }

    public String getProjectName() {
        return name;
    }

    public String getCheckItemsName() {
        return checkItemsName;
    }

    public void setCheckItems(ArrayList<String> checkItems) {
        this.checkItems = checkItems;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setManager(String manager) {
        this.manager = manager;
    }

    public void setProjectName(String name) {
        this.name = name;
    }

    public void setCheckItemsName(String checkItemsName) {
        this.checkItemsName = checkItemsName;
    }

    public void clearData() {
        name = null;
        location = null;
        manager = null;
        checkItems.clear();
        checkItemsName = null;
    }
}
