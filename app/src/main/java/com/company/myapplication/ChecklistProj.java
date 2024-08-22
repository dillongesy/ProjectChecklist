package com.company.myapplication;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChecklistProj extends AppCompatActivity{
    private SQLiteDatabase database;
    private int projectId;
    LinearLayout checklistContainer;
    Button backButton;
    Button closeButton;
    Button addCheckboxButton;
    Button deleteCheckboxButton;
    TextView projectName;
    TextView projectLocation;
    TextView projectManager;
    private boolean isDeleting = false;
    private ArrayList<CheckBox> checkBoxList = new ArrayList<>();
    private ArrayList<View> deleteButtons = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checklist);

        checklistContainer = findViewById(R.id.checklistContainer);
        backButton = findViewById(R.id.backButton);
        closeButton = findViewById(R.id.closeButton);
        addCheckboxButton = findViewById(R.id.addCheckboxButton);
        deleteCheckboxButton = findViewById(R.id.deleteCheckboxButton);
        projectName = findViewById(R.id.projectName);
        projectLocation = findViewById(R.id.projectLocation);
        projectManager = findViewById(R.id.projectManager);

        Intent intent = getIntent();
        projectName.setText(intent.getStringExtra("projectName"));
        projectLocation.setText(intent.getStringExtra("projectLocation"));
        projectManager.setText(intent.getStringExtra("projectManager"));

        DBHelper dbHelper = new DBHelper(this);
        database = dbHelper.getWritableDatabase();

        int projectId = getIntent().getIntExtra("projectId", -1);
        if (projectId != -1) {
            loadChecklistFromDB();
        }

        addCheckboxButton.setOnClickListener(v -> addTaskDialog());
        deleteCheckboxButton.setOnClickListener(v -> toggleDeleteMode());
        backButton.setOnClickListener(v -> finish());
        closeButton.setOnClickListener(v -> confirmCloseProject());

        for (View deleteButton : deleteButtons) {
            deleteButton.setVisibility(isDeleting ? View.VISIBLE : View.GONE);
        }

        updateDeleteButtonState();
    }

    private void loadChecklistFromDB() {
        checklistContainer.removeAllViews();
        checkBoxList.clear();
        deleteButtons.clear();
        // Query the tasks for this project
        Cursor cursor = database.query("tasks", null, "project_id = ?", new String[]{String.valueOf(projectId)}, null, null, null);

        // Get the column indices
        int idColumnIndex = cursor.getColumnIndex("id");
        int taskNameColumnIndex = cursor.getColumnIndex("task_name");
        int isCheckedColumnIndex = cursor.getColumnIndex("is_checked");

        // Check that the columns exist
        if (idColumnIndex == -1 || taskNameColumnIndex == -1 || isCheckedColumnIndex == -1) {
            // Print an error message or log
            Toast.makeText(this, "Column names do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Iterate through the cursor to load tasks
        while (cursor.moveToNext()) {
            int id = cursor.getInt(idColumnIndex);
            String taskName = cursor.getString(taskNameColumnIndex);
            boolean isChecked = cursor.getInt(isCheckedColumnIndex) == 1;

            // Create a new checkbox and set its properties
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(taskName);
            checkBox.setChecked(isChecked);
            checkBox.setOnCheckedChangeListener((buttonView, isChecked1) -> updateTaskInDB(taskName, isChecked1));
            checkBoxList.add(checkBox);

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);

            Button deleteButton = new Button(this);
            deleteButton.setText("-");
            deleteButton.setLayoutParams(new LinearLayout.LayoutParams(125, LinearLayout.LayoutParams.WRAP_CONTENT));
            deleteButton.setOnClickListener(v -> deleteTask(checkBox, row));
            deleteButtons.add(deleteButton);

            // Add delete button and checkbox to the row
            row.addView(deleteButton);
            row.addView(checkBox);

            // Add the row to the checklist container
            checklistContainer.addView(row);
        }

        cursor.close();
    }

    private void addTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Task");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Dialog buttons
        builder.setPositiveButton("Submit", (dialog, which) -> {
            String taskName = input.getText().toString();
            if (!taskName.isEmpty()) {
                addTaskToDB(taskName);
            } else {
                Toast.makeText(this, "Task cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void addTaskToDB(String taskName) {
        ContentValues values = new ContentValues();
        values.put("task_name", taskName);
        values.put("is_checked", 0);
        values.put("project_id", projectId);

        long taskId = database.insert("tasks", null, values);
        if (taskId != -1) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(taskName);
            checkBox.setChecked(false);
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> updateTaskInDB(taskName, isChecked));
            checkBoxList.add(checkBox);

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);

            Button deleteButton = new Button(this);
            deleteButton.setText("-");
            deleteButton.setVisibility(View.GONE);
            deleteButton.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            deleteButton.setOnClickListener(v -> deleteTask(checkBox, row));
            deleteButtons.add(deleteButton);

            row.addView(deleteButton);
            row.addView(checkBox);

            checklistContainer.addView(row);

            updateDeleteButtonState(); // Update delete button availability
        }
    }

    private void updateTaskInDB(String taskName, boolean isChecked) {
        ContentValues values = new ContentValues();
        values.put("is_checked", isChecked ? 1 : 0);
        database.update("tasks", values, "task_name = ? AND project_id = ?", new String[]{taskName, String.valueOf(projectId)});
    }

    private void toggleDeleteMode() {
        isDeleting = !isDeleting;
        deleteCheckboxButton.setText(isDeleting ? "Stop Deleting" : "Delete Task");

        for (View deleteButton : deleteButtons) {
            deleteButton.setVisibility(isDeleting ? View.VISIBLE : View.GONE);
        }
    }

    private void deleteTask(CheckBox checkBox, LinearLayout row) {
        String taskName = checkBox.getText().toString();
        database.delete("tasks", "task_name = ? AND project_id = ?", new String[]{taskName, String.valueOf(projectId)});
        checklistContainer.removeView(row);
        checkBoxList.remove(checkBox);

        updateDeleteButtonState(); // Update delete button availability
    }

    private void updateDeleteButtonState() {
        deleteCheckboxButton.setEnabled(!checkBoxList.isEmpty());
        if (checkBoxList.isEmpty()) {
            toggleDeleteMode();
        }
    }

    private void confirmCloseProject() {
        new AlertDialog.Builder(this)
                .setTitle("Close Project")
                .setMessage("Are you sure you want to close and delete this project?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Delete the project and its tasks from the database
                    database.delete("projects", "id = ?", new String[]{String.valueOf(projectId)});
                    database.delete("tasks", "project_id = ?", new String[]{String.valueOf(projectId)});
                    Toast.makeText(this, "Project deleted", Toast.LENGTH_SHORT).show();
                    finish(); // Go back to the previous screen
                })
                .setNegativeButton("No", null)
                .show();
    }
}
