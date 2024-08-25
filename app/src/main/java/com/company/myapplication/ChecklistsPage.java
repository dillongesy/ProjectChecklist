package com.company.myapplication;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class ChecklistsPage extends AppCompatActivity {
    Button newList;
    Button backBtn;
    GridLayout checkGrid;
    private DBHelper dbHelper;
    int[] imageResources = {
            R.drawable.concrete1,
            R.drawable.concrete2,
            R.drawable.concrete3,
            R.drawable.concrete4,
            R.drawable.concrete5,
            R.drawable.concrete6
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checklistspage);

        dbHelper = new DBHelper(this);
        newList = findViewById(R.id.newProjBtn);
        checkGrid = findViewById(R.id.projectGrid);
        backBtn = findViewById(R.id.cancelButton);

        newList.setOnClickListener(v -> {
            //pop asking for name -> make sure it's not empty
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Enter Checklist Name");

            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            builder.setPositiveButton("OK", (dialog, which) -> {
                String checklistName = input.getText().toString().trim();
                if (!checklistName.isEmpty()) {
                    long newchecklistId = insertNewChecklist(checklistName);
                    Toast.makeText(this, "Checklist Created: " + checklistName, Toast.LENGTH_SHORT).show();
                    addChecklistToGrid(checklistName, (int) newchecklistId);
                } else {
                    Toast.makeText(this, "Checklist name cannot be empty", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

            builder.show();
        });

        backBtn.setOnClickListener(v -> {
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshProjectsList();
    }

    private void refreshProjectsList() {
        checkGrid.removeAllViews();
        loadChecklistsFromDB();
    }

    private void addChecklistToGrid(String checklistName, int checklistId) {
        // Get the screen width for calculating half of the width
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenWidth = size.x;

        Random random = new Random();
        int randomImageIndex = random.nextInt(imageResources.length);

        // Create a button for the new project
        Button projectButton = new Button(this);
        projectButton.setText(checklistName);
        projectButton.setTypeface(null, Typeface.BOLD);

        projectButton.setBackgroundResource(imageResources[randomImageIndex]);

        // Set button style
        projectButton.setPadding(20, 20, 20, 20);
        projectButton.setGravity(Gravity.CENTER);
        projectButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = screenWidth / 2 - 90; // Half the screen width minus padding
        params.height = (params.width / 3) * 2; //edit for new height
        params.setMargins(16, 16, 16, 16); // Add some padding around the button

        // Set layout parameters to the button
        projectButton.setLayoutParams(params);

        // Set a click listener for the project button
        projectButton.setOnClickListener(v -> {

            Intent intent = new Intent(ChecklistsPage.this, ChecklistList.class);
            intent.putExtra("defaultId", checklistId);
            intent.putExtra("checklistName", checklistName);
            startActivity(intent);
            finish();
        });

        // Add the button to the grid layout
        checkGrid.addView(projectButton);
    }

    private void loadChecklistsFromDB() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] columns = {"defaultId", "checklistName"};
        Cursor cursor = db.query("defaultChecklists", columns, null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                // Get column indices safely
                int idIndex = cursor.getColumnIndex("defaultId");
                int nameIndex = cursor.getColumnIndex("checklistName");

                // Ensure that column indices are valid
                if (idIndex != -1 && nameIndex != -1) {
                    // Retrieve the data
                    int checkListId = cursor.getInt(idIndex);
                    String projectName = cursor.getString(nameIndex);

                    // Add the project to the grid
                    addChecklistToGrid(projectName, checkListId);
                } else {
                    // Handle the case where one or more columns are missing
                    Toast.makeText(this, "Error loading checklist data", Toast.LENGTH_SHORT).show();
                }
            } while (cursor.moveToNext());

            cursor.close();
        } else {
            Toast.makeText(this, "No checklists found", Toast.LENGTH_SHORT).show();
        }
        db.close();
    }
    private long insertNewChecklist(String name) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long val;
        ContentValues values = new ContentValues();
        values.put("checklistName", name);
        db.beginTransaction();
        try {
            val = db.insert("defaultChecklists", null, values);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        return val;
    }
}
