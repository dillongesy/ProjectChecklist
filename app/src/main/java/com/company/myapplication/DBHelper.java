package com.company.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper{
    private static final String DATABASE_NAME = "projects.db";
    private static final int DATABASE_VERSION = 6;

    // Table and column names for projects
    private static final String TABLE_PROJECTS = "projects";
    private static final String COLUMN_PROJECT_ID = "projectId";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_LOCATION = "location";
    private static final String COLUMN_MANAGER = "manager";

    // Table and column names for checklists (tasks)
    private static final String TABLE_CHECKLISTS = "checklists";
    private static final String COLUMN_TASK_ID = "taskId";
    private static final String COLUMN_TASK_NAME = "task_name";
    private static final String COLUMN_IS_CHECKED = "is_checked";
    private static final String COLUMN_PROJECT_ID_FK = "project_id";

    // Table and column names for DEFAULT CHECK LISTS
    private static final String TABLE_DEFAULT_CHECKLISTS = "defaultChecklists";
    private static final String COLUMN_DEFAULT_ID = "defaultId";
    private static final String COLUMN_DEFAULT_NAME = "checklistName";

    // Table and column names for DEFAULT CHECK LISTS checks
    private static final String TABLE_DEFAULT_CHECKLISTS_CHECKS = "defaultChecklistsCheck";
    private static final String COLUMN_CHECK_ID = "checkId";
    private static final String COLUMN_CHECK_NAME = "checkName";
    private static final String COLUMN_DEFAULT_ID_FK = "default_id";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_PROJECTS_TABLE = "CREATE TABLE " + TABLE_PROJECTS + "("
                + COLUMN_PROJECT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_LOCATION + " TEXT,"
                + COLUMN_MANAGER + " TEXT" + ")";
        db.execSQL(CREATE_PROJECTS_TABLE);

        String CREATE_CHECKLISTS_TABLE = "CREATE TABLE " + TABLE_CHECKLISTS + "("
                + COLUMN_TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TASK_NAME + " TEXT,"
                + COLUMN_IS_CHECKED + " INTEGER,"
                + COLUMN_PROJECT_ID_FK + " INTEGER,"
                + "FOREIGN KEY(" + COLUMN_PROJECT_ID_FK + ") REFERENCES " + TABLE_PROJECTS + "(" + COLUMN_PROJECT_ID + ") ON DELETE CASCADE)";
        db.execSQL(CREATE_CHECKLISTS_TABLE);

        String CREATE_DEFAULTCHECKLISTS_TABLE = "CREATE TABLE " + TABLE_DEFAULT_CHECKLISTS + "("
                + COLUMN_DEFAULT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_DEFAULT_NAME + " TEXT" + ")";
        db.execSQL(CREATE_DEFAULTCHECKLISTS_TABLE);

        String CREATE_DEFAULTLISTS_TABLE = "CREATE TABLE " + TABLE_DEFAULT_CHECKLISTS_CHECKS + "("
                + COLUMN_CHECK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_CHECK_NAME + " TEXT,"
                + COLUMN_DEFAULT_ID_FK + " INTEGER,"
                + "FOREIGN KEY(" + COLUMN_DEFAULT_ID_FK + ") REFERENCES " + TABLE_DEFAULT_CHECKLISTS + "(" + COLUMN_DEFAULT_ID + ") ON DELETE CASCADE)";
        db.execSQL(CREATE_DEFAULTLISTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHECKLISTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROJECTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DEFAULT_CHECKLISTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DEFAULT_CHECKLISTS_CHECKS);
        onCreate(db);
    }

    public boolean insertProject(String name, String location, String manager) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_LOCATION, location);
        values.put(COLUMN_MANAGER, manager);
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
        db.beginTransaction();
        try {
            db.delete(TABLE_PROJECTS, COLUMN_PROJECT_ID + "=?", new String[]{String.valueOf(projectId)});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    // Insert a check into the checklists table
    public long insertChecklist(String taskName, int isChecked, int projectId) {
        SQLiteDatabase db = this.getWritableDatabase();
        long val;
        ContentValues values = new ContentValues();
        values.put(COLUMN_TASK_NAME, taskName);
        values.put(COLUMN_IS_CHECKED, isChecked);
        values.put(COLUMN_PROJECT_ID_FK, projectId);
        db.beginTransaction();
        try {
            val = db.insert(TABLE_CHECKLISTS, null, values);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        return val;
    }

    // Delete a set of tasks from the tasks table when the project is closed
    public void deleteWholeChecklist(int projectId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_CHECKLISTS,  COLUMN_PROJECT_ID_FK + "=?", new String[]{String.valueOf(projectId)});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    //Delete a task from one of the sets of checklists
    public void deleteCheck(String taskName, int projectId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_CHECKLISTS, COLUMN_PROJECT_ID_FK + "=? AND " + COLUMN_TASK_NAME + "=?", new String[]{String.valueOf(projectId), taskName});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    // Update a project's checklist
    public void updateChecklist(String taskName, int isChecked, String projectId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_IS_CHECKED, isChecked);
        db.update(TABLE_CHECKLISTS, values, COLUMN_TASK_NAME + " = ?", new String[]{String.valueOf(projectId)});
        db.close();
    }

    public long insertChecklistIntoList(String taskName, int checkId) {
        SQLiteDatabase db = this.getWritableDatabase();
        long val;
        ContentValues values = new ContentValues();

        values.put(COLUMN_CHECK_NAME, taskName);
        values.put(COLUMN_DEFAULT_ID_FK, checkId);
        db.beginTransaction();
        try {
            val = db.insert(TABLE_DEFAULT_CHECKLISTS_CHECKS, null, values);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        return val;
    }

    public void deleteChecklist(int checkId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_DEFAULT_CHECKLISTS, COLUMN_DEFAULT_ID + "=?", new String[]{String.valueOf(checkId)});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void deleteCheckFromChecklist(String taskName, int checkId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_DEFAULT_CHECKLISTS_CHECKS, COLUMN_DEFAULT_ID_FK + "=? AND " + COLUMN_CHECK_NAME + "=?", new String[]{String.valueOf(checkId), taskName});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
    public void deleteWholeChecklistList(int checkId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            //TODO
            db.delete(TABLE_DEFAULT_CHECKLISTS_CHECKS,  COLUMN_DEFAULT_ID_FK + "=?", new String[]{String.valueOf(checkId)});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
}