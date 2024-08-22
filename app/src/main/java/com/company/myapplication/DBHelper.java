package com.company.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
public class DBHelper extends SQLiteOpenHelper{
    private static final String DATABASE_NAME = "projects.db";
    private static final int DATABASE_VERSION = 1;

    // Table and column names
    private static final String TABLE_PROJECTS = "projects";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_LOCATION = "location";
    private static final String COLUMN_MANAGER = "manager";
    private static final String COLUMN_CHECKLIST = "checklist"; // Serialized checklist data

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
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
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
}
