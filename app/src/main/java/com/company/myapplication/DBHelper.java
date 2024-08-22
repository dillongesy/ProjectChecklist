package com.company.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
public class DBHelper extends SQLiteOpenHelper{
    private static final String DATABASE_NAME = "projects.db";
    private static final int DATABASE_VERSION = 2;

    // Table and column names
    private static final String TABLE_PROJECTS = "projects";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_LOCATION = "location";
    private static final String COLUMN_MANAGER = "manager";
    private static final String COLUMN_CHECKLIST = "checklist";

    private static final String TABLE_TASKS = "tasks";
    private static final String COLUMN_TASK_ID = "id";
    private static final String COLUMN_TASK_NAME = "task_name";
    private static final String COLUMN_IS_CHECKED = "is_checked";
    private static final String COLUMN_PROJECT_ID = "project_id";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_PROJECTS_TABLE = "CREATE TABLE " + TABLE_PROJECTS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_LOCATION + " TEXT,"
                + COLUMN_MANAGER + " TEXT,"
                + COLUMN_CHECKLIST + " TEXT" + ")";
        db.execSQL(CREATE_PROJECTS_TABLE);

        String CREATE_TASKS_TABLE = "CREATE TABLE " + TABLE_TASKS + "("
                + COLUMN_TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TASK_NAME + " TEXT,"
                + COLUMN_IS_CHECKED + " INTEGER,"
                + COLUMN_PROJECT_ID + " INTEGER,"
                + "FOREIGN KEY(" + COLUMN_PROJECT_ID + ") REFERENCES " + TABLE_PROJECTS + "(" + COLUMN_ID + ") ON DELETE CASCADE)";
        db.execSQL(CREATE_TASKS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROJECTS);
        onCreate(db);
    }

    public boolean insertProject(String name, String location, String manager, String checklist) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_LOCATION, location);
        values.put(COLUMN_MANAGER, manager);
        values.put(COLUMN_CHECKLIST, checklist);

        long result = db.insert(TABLE_PROJECTS, null, values);
        return result != -1;
    }

    public Cursor getAllProjects() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_PROJECTS, null);
    }

    // Delete a project by ID
    public void deleteProject(int projectId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PROJECTS, COLUMN_ID + "=?", new String[]{String.valueOf(projectId)});
    }

    // Update a project's checklist
    public void updateChecklist(int projectId, String newChecklist) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CHECKLIST, newChecklist);
        db.update(TABLE_PROJECTS, values, COLUMN_ID + "=?", new String[]{String.valueOf(projectId)});
    }

    // Insert a task into the tasks table
    public long insertTask(String taskName, int isChecked, int projectId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TASK_NAME, taskName);
        values.put(COLUMN_IS_CHECKED, isChecked);
        values.put(COLUMN_PROJECT_ID, projectId);

        return db.insert(TABLE_TASKS, null, values);
    }

    // Delete a task from the tasks table
    public void deleteTask(String taskName, int projectId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TASKS, COLUMN_TASK_NAME + "=? AND " + COLUMN_PROJECT_ID + "=?", new String[]{taskName, String.valueOf(projectId)});
    }
}
