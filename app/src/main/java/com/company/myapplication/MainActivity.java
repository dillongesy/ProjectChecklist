package com.company.myapplication;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity{
    Button newProj;
    GridLayout projectGrid;
    private ActivityResultLauncher<Intent> newChecklistLauncher;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DBHelper(this);
        newProj = findViewById(R.id.newProjBtn);
        projectGrid = findViewById(R.id.projectGrid);

        newChecklistLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            String projectName = data.getStringExtra("projectName");
                            String projectLocation = data.getStringExtra("projectLocation");
                            String projectManager = data.getStringExtra("projectManager");

                            long newProjectId = insertNewProject(projectName, projectLocation, projectManager);

                            Toast.makeText(this, "Project Created: " + projectName, Toast.LENGTH_SHORT).show();
                            addProjectToGrid(projectName, projectLocation, projectManager, (int) newProjectId);
                        }
                    }
                }
        );

        newProj.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, newChecklist.class);
                newChecklistLauncher.launch(intent);
        });

        //Get projects, display them, make them clickable
        loadProjectsFromDB();
    }
    private void addProjectToGrid(String projectName, String projectLocation, String projectManager, int projectId) {
        // Get the screen width for calculating half of the width
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenWidth = size.x;

        // Create a button for the new project
        Button projectButton = new Button(this);
        projectButton.setText(projectName);

        // Set button style
        projectButton.setPadding(20, 20, 20, 20);
        projectButton.setBackgroundColor(getResources().getColor(R.color.teal_200));
        projectButton.setGravity(Gravity.CENTER);
        projectButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = screenWidth / 2 - 90; // Half the screen width minus padding
        params.height = (params.width / 3) * 2; //edit for new height
        params.setMargins(16, 16, 16, 16); // Add some padding around the button

        // Set layout parameters to the button
        projectButton.setLayoutParams(params);

        // Set a click listener for the project button
        projectButton.setOnClickListener(v -> {
            // You can start a new activity or manage the project here
            Intent intent = new Intent(MainActivity.this, ChecklistProj.class);
            intent.putExtra("projectId", projectId);
            intent.putExtra("projectName", projectName);
            intent.putExtra("projectLocation", projectLocation);
            intent.putExtra("projectManager", projectManager);

            startActivity(intent);
        });

        // Add the button to the grid layout
        projectGrid.addView(projectButton);
    }

    private void loadProjectsFromDB() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] columns = {"id", "name", "location", "manager"};
        Cursor cursor = db.query("projects", columns, null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                // Get column indices safely
                int idIndex = cursor.getColumnIndex("id");
                int nameIndex = cursor.getColumnIndex("name");
                int locationIndex = cursor.getColumnIndex("location");
                int managerIndex = cursor.getColumnIndex("manager");

                // Ensure that column indices are valid
                if (idIndex != -1 && nameIndex != -1 && locationIndex != -1 && managerIndex != -1) {
                    // Retrieve the data
                    int projectId = cursor.getInt(idIndex);
                    String projectName = cursor.getString(nameIndex);
                    String projectLocation = cursor.getString(locationIndex);
                    String projectManager = cursor.getString(managerIndex);

                    // Add the project to the grid
                    addProjectToGrid(projectName, projectLocation, projectManager, projectId);
                } else {
                    // Handle the case where one or more columns are missing
                    Toast.makeText(this, "Error loading project data", Toast.LENGTH_SHORT).show();
                }
            } while (cursor.moveToNext());

            cursor.close();
        } else {
            Toast.makeText(this, "No projects found", Toast.LENGTH_SHORT).show();
        }
        db.close();
    }

    private long insertNewProject(String name, String location, String manager) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("location", location);
        values.put("manager", manager);

        return db.insert("projects", null, values);
    }

}
