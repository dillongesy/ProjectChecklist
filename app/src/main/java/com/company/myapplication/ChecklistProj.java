package com.company.myapplication;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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
    private boolean isDeleting = false;
    private Map<CheckBox, Integer> checkboxToIdMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checklist);

        int projectId = getIntent().getIntExtra("projectId", -1);

        checklistContainer = findViewById(R.id.checklistContainer);
        backButton = findViewById(R.id.backButton);
        closeButton = findViewById(R.id.closeButton);
        addCheckboxButton = findViewById(R.id.addCheckboxButton);
        deleteCheckboxButton = findViewById(R.id.deleteCheckboxButton);

        //TODO
        /*ProjectDatabaseHelper dbHelper = new ProjectDatabaseHelper(this);
        database = dbHelper.getWritableDatabase();*/

        loadChecklistFromDB();

        addCheckboxButton.setOnClickListener(v -> addTaskDialog());
        deleteCheckboxButton.setOnClickListener(v -> toggleDeleteMode());
        backButton.setOnClickListener(v -> finish());
        closeButton.setOnClickListener(v -> confirmCloseProject());

        Intent intent = getIntent();
        //DELETE below?
        /*String projectName = intent.getStringExtra("projectName");
        String projectLocation = intent.getStringExtra("projectLocation");
        String projectManager = intent.getStringExtra("projectManager");

        currentProject = new Project(projectName, projectLocation, projectManager);

        TextView projectNameTextView = findViewById(R.id.projectName);
        TextView projectLocationTextView = findViewById(R.id.projectLocation);
        TextView projectManagerTextView = findViewById(R.id.projectManager);

        projectNameTextView.setText("Project Name: " + currentProject.getName());
        projectLocationTextView.setText("Project Location: " + currentProject.getLocation());
        projectManagerTextView.setText("Project Manager: " + currentProject.getManager());

        for (String item : checklistItems) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(item);
            checklistContainer.addView(checkBox);
        }

        projectKey = currentProject.getName() + "_checklist";
        preferences = getSharedPreferences("ProjectPrefs", MODE_PRIVATE);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChecklistProj.this, MainActivity.class);
                startActivity(intent);
            }
        });

        closeButton.setOnClickListener(v -> {
            new AlertDialog.Builder(ChecklistProj.this)
                    .setTitle("Close Project")
                    .setMessage("Are you sure you want to close this project?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        dbHelper.deleteProject(projectId);
                        Intent intent2 = new Intent(ChecklistProj.this, MainActivity.class);
                        startActivity(intent2);
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        addCheckboxButton.setOnClickListener(v -> {
            CheckBox newCheckbox = new CheckBox(this);
            newCheckbox.setText("New Task");
            checklistContainer.addView(newCheckbox);
        });

        deleteCheckboxButton.setOnClickListener(v -> {
            if (checklistContainer.getChildCount() > 0) {
                checklistContainer.removeViewAt(checklistContainer.getChildCount() - 1);
            }
        });*/
    }

    //TODO
    private void loadChecklistFromDB() {
        /*Cursor cursor = database.query("checklist", null, "project_id = ?", new String[]{String.valueOf(projectId)}, null, null, null);
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            String taskName = cursor.getString(cursor.getColumnIndex("task_name"));
            boolean isChecked = cursor.getInt(cursor.getColumnIndex("is_checked")) == 1;

            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(taskName);
            checkBox.setChecked(isChecked);
            checklistContainer.addView(checkBox);

            checkboxToIdMap.put(checkBox, id);

            // Listen for changes in the checkbox state
            checkBox.setOnCheckedChangeListener((buttonView, isCheckedState) -> {
                ContentValues values = new ContentValues();
                values.put("is_checked", isCheckedState ? 1 : 0);
                database.update("checklist", values, "id = ?", new String[]{String.valueOf(id)});
            });
        }
        cursor.close();*/
    }

    private void addTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New Task");

        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("Submit", (dialog, which) -> addTask(input.getText().toString()));
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void addTask(String taskName) {
        if (!taskName.isEmpty()) {
            ContentValues values = new ContentValues();
            values.put("task_name", taskName);
            values.put("is_checked", 0);
            values.put("project_id", projectId);
            long taskId = database.insert("checklist", null, values);

            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(taskName);
            checklistContainer.addView(checkBox);

            checkboxToIdMap.put(checkBox, (int) taskId);
            checkBox.setOnCheckedChangeListener((buttonView, isCheckedState) -> {
                ContentValues updateValues = new ContentValues();
                updateValues.put("is_checked", isCheckedState ? 1 : 0);
                database.update("checklist", updateValues, "id = ?", new String[]{String.valueOf(taskId)});
            });
        }
    }

    private void toggleDeleteMode() {
        if (isDeleting) {
            isDeleting = false;
            deleteCheckboxButton.setText("Delete Task");
            for (int i = 0; i < checklistContainer.getChildCount(); i++) {
                View child = checklistContainer.getChildAt(i);
                if (child instanceof Button) {
                    checklistContainer.removeView(child);
                    i--;
                }
            }
        } else {
            isDeleting = true;
            deleteCheckboxButton.setText("Stop Deleting");
            for (CheckBox checkBox : checkboxToIdMap.keySet()) {
                Button deleteButton = new Button(this);
                deleteButton.setText("-");
                deleteButton.setOnClickListener(v -> deleteTask(checkBox));
                checklistContainer.addView(deleteButton, checklistContainer.indexOfChild(checkBox));
            }
        }
    }

    private void deleteTask(CheckBox checkBox) {
        int taskId = checkboxToIdMap.get(checkBox);
        database.delete("checklist", "id = ?", new String[]{String.valueOf(taskId)});
        checklistContainer.removeView(checkBox);
        checkboxToIdMap.remove(checkBox);
    }

    private void confirmCloseProject() {
        new AlertDialog.Builder(this)
                .setTitle("Close Project")
                .setMessage("Are you sure you want to close this project? It will be deleted permanently.")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Delete project from database
                    database.delete("projects", "id = ?", new String[]{String.valueOf(projectId)});
                    database.delete("checklist", "project_id = ?", new String[]{String.valueOf(projectId)});
                    finish();  // Go back to MainActivity
                })
                .setNegativeButton("No", null)
                .show();
    }
}
