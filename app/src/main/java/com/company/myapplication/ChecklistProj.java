package com.company.myapplication;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
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
    private DBHelper dbHelper;

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

        dbHelper = new DBHelper(this);
        database = dbHelper.getWritableDatabase();

        projectId = getIntent().getIntExtra("projectId", -1);
        if (projectId != -1) {
            loadChecklistFromDB(projectId);
        }

        addCheckboxButton.setOnClickListener(v -> addTaskDialog());
        deleteCheckboxButton.setOnClickListener(v -> toggleDeleteMode());
        backButton.setOnClickListener(v -> finish());
        closeButton.setOnClickListener(v -> confirmCloseProject());

        for (View deleteButton : deleteButtons) {
            deleteButton.setVisibility(isDeleting ? View.VISIBLE : View.GONE);
        }

    }

    private void loadChecklistFromDB(int projectId) {
        checklistContainer.removeAllViews();
        checkBoxList.clear();
        deleteButtons.clear();
        // Query the tasks for this project
        Cursor cursor = database.rawQuery("SELECT * FROM checklists WHERE project_id = ?", new String[]{String.valueOf(projectId)});
        if (cursor.moveToFirst()) {
            do {
                String taskName = cursor.getString(cursor.getColumnIndexOrThrow("task_name"));
                int isChecked = cursor.getInt(cursor.getColumnIndexOrThrow("is_checked"));

                CheckBox checkBox = new CheckBox(this);
                checkBox.setText(taskName);
                checkBox.setChecked(isChecked == 1);
                checkBox.setTextSize(20);
                checkBox.setOnCheckedChangeListener((ButtonView, isChecked1) -> updateTaskInDB(taskName, isChecked1));
                //new
                LinearLayout.LayoutParams checkBoxParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 168);
                checkBoxParams.setMargins(0, 0, 0, 8);
                checkBox.setLayoutParams(checkBoxParams);

                //later
                checkBoxList.add(checkBox);

                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);

                Button deleteButton = new Button(this);
                deleteButton.setText("-");
                deleteButton.setBackgroundColor(Color.RED);
                deleteButton.setTypeface(null, Typeface.BOLD);
                LinearLayout.LayoutParams deleteButtonParams = new LinearLayout.LayoutParams(125, LinearLayout.LayoutParams.WRAP_CONTENT);
                deleteButtonParams.setMargins(0, 0, 0, 8);
                deleteButton.setLayoutParams(deleteButtonParams);

                deleteButton.setOnClickListener(v -> deleteTask(checkBox, row));
                deleteButtons.add(deleteButton);

                // Add delete button and checkbox to the row
                row.addView(deleteButton);
                row.addView(checkBox);

                // Add the row to the checklist container
                checklistContainer.addView(row);
            } while (cursor.moveToNext());
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
        if (isDeleting) {
            toggleDeleteMode();
        }
        for (int i =0; i < checkBoxList.size(); i++){
            if (checkBoxList.get(i).getText().toString().equals(taskName)) {
                Toast.makeText(this, "Cannot have duplicate tasks", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        long taskId = dbHelper.insertChecklist(taskName, 0, projectId);
        if (taskId != -1) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(taskName);
            checkBox.setChecked(false);
            checkBox.setTextSize(20);
            checkBox.setOnCheckedChangeListener((ButtonView, isChecked1) -> updateTaskInDB(taskName, isChecked1));
            //new
            LinearLayout.LayoutParams checkBoxParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 168);
            checkBoxParams.setMargins(0, 0, 0, 8);
            checkBox.setLayoutParams(checkBoxParams);

            //later
            checkBoxList.add(checkBox);

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);

            Button deleteButton = new Button(this);
            deleteButton.setText("-");
            deleteButton.setBackgroundColor(Color.RED);
            deleteButton.setTypeface(null, Typeface.BOLD);
            deleteButton.setVisibility(View.GONE);
            //deleteButton.setLayoutParams(new LinearLayout.LayoutParams(125, LinearLayout.LayoutParams.WRAP_CONTENT));
            LinearLayout.LayoutParams deleteButtonParams = new LinearLayout.LayoutParams(125, LinearLayout.LayoutParams.WRAP_CONTENT);
            deleteButtonParams.setMargins(0, 0, 0, 8);
            deleteButton.setLayoutParams(deleteButtonParams);

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
        database.update("checklists", values, "task_name = ? AND project_id = ?", new String[]{taskName, String.valueOf(projectId)});
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
        dbHelper.deleteCheck(taskName, projectId);

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
                .setMessage("Are you sure you want to close and delete this project? This will delete the project from this device.")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Delete the project and its tasks from the database
                    dbHelper.deleteProject(projectId);
                    dbHelper.deleteWholeChecklist(projectId);
                    Toast.makeText(this, "Project deleted", Toast.LENGTH_SHORT).show();
                    finish(); // Go back to the previous screen
                })
                .setNegativeButton("No", null)
                .show();
    }
}