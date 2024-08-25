package com.company.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class newChecklist  extends AppCompatActivity{
    EditText projectNameEditText;
    EditText projectLocationEditText;
    EditText projectManagerEditText;
    Button createProjectConfirm;
    Button cancelButton;
    EditText listTextView;
    Button newListBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newchecklist);

        projectNameEditText = findViewById(R.id.projectName);
        projectLocationEditText = findViewById(R.id.projectLocation);
        projectManagerEditText = findViewById(R.id.projectManager);
        createProjectConfirm = findViewById(R.id.createProjectConfirm);
        cancelButton = findViewById(R.id.cancelButton);
        newListBtn = findViewById(R.id.newList);
        listTextView = findViewById(R.id.listTextView);

        createProjectConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String projectName = projectNameEditText.getText().toString().trim();
                String projectLocation = projectLocationEditText.getText().toString().trim();
                String projectManager = projectManagerEditText.getText().toString().trim();

                if (projectName.isEmpty()) {
                    Toast.makeText(newChecklist.this, "Please fill in the name field", Toast.LENGTH_SHORT).show();
                } else {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("projectName", projectName);
                    resultIntent.putExtra("projectLocation", projectLocation);
                    resultIntent.putExtra("projectManager", projectManager);
                    ScreenHelper.getInstance().setProjectName(projectName);
                    ScreenHelper.getInstance().setLocation(projectLocation);
                    ScreenHelper.getInstance().setManager(projectManager);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        newListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String projectName = projectNameEditText.getText().toString().trim();
                String projectLocation = projectLocationEditText.getText().toString().trim();
                String projectManager = projectManagerEditText.getText().toString().trim();


                Intent intent = new Intent(newChecklist.this, ChecklistsPage.class);
                ScreenHelper.getInstance().setProjectName(projectName);
                ScreenHelper.getInstance().setLocation(projectLocation);
                ScreenHelper.getInstance().setManager(projectManager);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        String checkItemsName = ScreenHelper.getInstance().getCheckItemsName();
        if (checkItemsName != null) {
            listTextView.setHint("Loaded list: " + checkItemsName);
        }
    }
}