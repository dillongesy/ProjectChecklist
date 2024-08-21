package com.company.myapplication;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity{
    Button newProj;
    GridLayout projectGrid;

    private ActivityResultLauncher<Intent> newChecklistLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

                            // Handle the received data (e.g., add a new project to the grid)
                            Toast.makeText(this, "Project Created: " + projectName, Toast.LENGTH_SHORT).show();
                            addProjectToGrid(projectName, projectLocation, projectManager);
                            // Add your code to display the project on the main screen
                        }
                    }
                }
        );

        newProj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, newChecklist.class);
                newChecklistLauncher.launch(intent);
            }
        });

        //Get projects, display them, make them clickable
    }
    private void addProjectToGrid(String projectName, String projectLocation, String projectManager) {
        // Dynamically create a new Button (or custom view) for the project
        Button projectButton = new Button(this);
        projectButton.setText(projectName);

        // Optionally, you can set additional properties for the button (e.g., padding, background color)
        projectButton.setPadding(20, 20, 20, 20);
        projectButton.setBackgroundColor(getResources().getColor(R.color.teal_200));

        // Set a click listener for the project button
        projectButton.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Opening Project: " + projectName, Toast.LENGTH_SHORT).show();
            // You can start a new activity or manage the project here
            Intent intent = new Intent(MainActivity.this, ChecklistProj.class);
            intent.putExtra("projectName", projectName);
            intent.putExtra("projectLocation", projectLocation);
            intent.putExtra("projectManager", projectManager);

            startActivity(intent);
        });

        // Add the button to the grid layout
        projectGrid.addView(projectButton);
    }

}
