package com.company.myapplication;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ChecklistProj extends AppCompatActivity{
    private Project currentProject;
    LinearLayout checklistContainer;
    SharedPreferences preferences;
    private String projectKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checklist);

        Intent intent = getIntent();
        String projectName = intent.getStringExtra("projectName");
        String projectLocation = intent.getStringExtra("projectLocation");
        String projectManager = intent.getStringExtra("projectManager");

        currentProject = new Project(projectName, projectLocation, projectManager);

        TextView projectNameTextView = findViewById(R.id.projectName);
        TextView projectLocationTextView = findViewById(R.id.projectLocation);
        TextView projectManagerTextView = findViewById(R.id.projectManager);

        projectNameTextView.setText("Project Name: " + currentProject.getName());
        projectLocationTextView.setText("Project Location: " + currentProject.getLocation());
        projectManagerTextView.setText("Project Manager: " + currentProject.getManager());

        projectKey = currentProject.getName() + "_checklist";
        preferences = getSharedPreferences("ProjectPrefs", MODE_PRIVATE);

        checklistContainer = findViewById(R.id.checklistContainer);

        // Load and display the checklist
        loadChecklist();
    }

    private Project getProjectFromIntent() {
        // Mock project for simplicity
        Project project = new Project("Sample Project", "New York", "John Doe");
        project.addChecklistItem(new ChecklistItem("Task 1"));
        project.addChecklistItem(new ChecklistItem("Task 2"));
        project.addChecklistItem(new ChecklistItem("Task 3"));
        return project;
    }

    private void loadChecklist() {
        for (int i = 0; i < currentProject.getChecklist().size(); i++) {
            ChecklistItem item = currentProject.getChecklist().get(i);

            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(item.getTask());

            // Restore the saved checked state
            boolean isChecked = preferences.getBoolean(projectKey + "_" + i, false);
            checkBox.setChecked(isChecked);

            // Save the checked state when changed
            int index = i; // Final reference for the listener
            checkBox.setOnCheckedChangeListener((buttonView, isChecked1) -> {
                preferences.edit().putBoolean(projectKey + "_" + index, isChecked1).apply();
            });

            // Add the checkbox to the container
            checklistContainer.addView(checkBox);
        }
    }
}
