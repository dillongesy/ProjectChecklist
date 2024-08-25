package com.company.myapplication;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
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

public class ChecklistList extends AppCompatActivity {
    private SQLiteDatabase database;
    private int checklistId;
    LinearLayout checklistContainer;
    Button backButton;
    Button closeButton;
    Button addCheckboxButton;
    Button deleteCheckboxButton;
    Button useBtn;
    TextView checklistName;
    private boolean isDeleting = false;
    private ArrayList<CheckBox> checkBoxList = new ArrayList<>();
    private ArrayList<View> deleteButtons = new ArrayList<>();
    private ArrayList<String> checkItems = new ArrayList<>();
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checklistlist);

        checklistContainer = findViewById(R.id.checklistContainer);
        backButton = findViewById(R.id.backButton);
        closeButton = findViewById(R.id.closeButton);
        addCheckboxButton = findViewById(R.id.addCheckboxButton);
        deleteCheckboxButton = findViewById(R.id.deleteCheckboxButton);
        checklistName = findViewById(R.id.projectName);
        useBtn = findViewById(R.id.useBtn);

        Intent intent = getIntent();
        checklistName.setText(intent.getStringExtra("checklistName"));

        dbHelper = new DBHelper(this);
        database = dbHelper.getWritableDatabase();

        checklistId = getIntent().getIntExtra("defaultId", -1);
        if (checklistId != -1) {
            loadChecklistFromDB(checklistId);
        }

        addCheckboxButton.setOnClickListener(v -> addTaskDialog());
        deleteCheckboxButton.setOnClickListener(v -> toggleDeleteMode());
        backButton.setOnClickListener(v -> goBack());
        closeButton.setOnClickListener(v -> confirmDeleteChecklist());
        useBtn.setOnClickListener(V -> useChecklist());

        for (View deleteButton : deleteButtons) {
            deleteButton.setVisibility(isDeleting ? View.VISIBLE : View.GONE);
        }
    }

    private void loadChecklistFromDB(int checklistId) {
        checklistContainer.removeAllViews();
        checkBoxList.clear();
        deleteButtons.clear();
        // Query the tasks for this project
        Cursor cursor = database.rawQuery("SELECT * FROM defaultChecklistsCheck WHERE default_id = ?", new String[]{String.valueOf(checklistId)});
        if (cursor.moveToFirst()) {
            do {
                String checkName = cursor.getString(cursor.getColumnIndexOrThrow("checkName"));

                CheckBox checkBox = new CheckBox(this);
                checkBox.setText(checkName);
                checkBox.setEnabled(false);
                checkBox.setTextSize(20);
                LinearLayout.LayoutParams checkBoxParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 168);
                checkBoxParams.setMargins(0, 0, 0, 8);
                checkBox.setLayoutParams(checkBoxParams);

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
        deleteCheckboxButton.setEnabled(!checkBoxList.isEmpty());
    }

    private void addTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Check");

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
        long taskId = dbHelper.insertChecklistIntoList(taskName, checklistId);
        if (taskId != -1) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(taskName);
            checkBox.setEnabled(false);
            checkBox.setTextSize(20);
            LinearLayout.LayoutParams checkBoxParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 168);
            checkBoxParams.setMargins(0, 0, 0, 8);
            checkBox.setLayoutParams(checkBoxParams);

            checkBoxList.add(checkBox);

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);

            Button deleteButton = new Button(this);
            deleteButton.setText("-");
            deleteButton.setBackgroundColor(Color.RED);
            deleteButton.setTypeface(null, Typeface.BOLD);
            deleteButton.setVisibility(View.GONE);
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

    private void toggleDeleteMode() {
        isDeleting = !isDeleting;
        deleteCheckboxButton.setText(isDeleting ? "Stop Deleting" : "Delete Task");

        for (View deleteButton : deleteButtons) {
            deleteButton.setVisibility(isDeleting ? View.VISIBLE : View.GONE);
        }
    }

    private void deleteTask(CheckBox checkBox, LinearLayout row) {
        String taskName = checkBox.getText().toString();
        dbHelper.deleteCheckFromChecklist(taskName, checklistId);

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

    private void confirmDeleteChecklist() {
        new AlertDialog.Builder(this)
                .setTitle("Close Checklist")
                .setMessage("Are you sure you want to delete this checklist? This will permanently delete the checklist from this device.")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Delete the project and its tasks from the database
                    dbHelper.deleteChecklist(checklistId);
                    dbHelper.deleteWholeChecklistList(checklistId);
                    Toast.makeText(this, "Checklist deleted", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ChecklistList.this, ChecklistsPage.class);
                    startActivity(intent);
                    finish(); // Go back to the previous screen
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void useChecklist() {
        for (CheckBox check : checkBoxList) {
            checkItems.add(check.getText().toString());
        }
        ScreenHelper.getInstance().setCheckItemsName(checklistName.getText().toString());
        ScreenHelper.getInstance().setCheckItems(checkItems);
        finish();
    }

    private void goBack() {
        Intent intent = new Intent(ChecklistList.this, ChecklistsPage.class);
        startActivity(intent);
        finish();
    }
}
