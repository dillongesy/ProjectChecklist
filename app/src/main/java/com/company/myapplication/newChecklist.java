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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newchecklist);

        projectNameEditText = findViewById(R.id.projectName);
        projectLocationEditText = findViewById(R.id.projectLocation);
        projectManagerEditText = findViewById(R.id.projectManager);
        createProjectConfirm = findViewById(R.id.createProjectConfirm);
        cancelButton = findViewById(R.id.cancelButton);

        createProjectConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String projectName = projectNameEditText.getText().toString().trim();
                String projectLocation = projectLocationEditText.getText().toString().trim();
                String projectManager = projectManagerEditText.getText().toString().trim();

                if (projectName.isEmpty()) {// || projectLocation.isEmpty() || projectManager.isEmpty()) {
                    Toast.makeText(newChecklist.this, "Please fill in the name field", Toast.LENGTH_SHORT).show();
                } else {
                    // Pass data back to MainActivity (you can use Intent or another method)
                    Intent intent = new Intent();
                    intent.putExtra("projectName", projectName);
                    intent.putExtra("projectLocation", projectLocation);
                    intent.putExtra("projectManager", projectManager);
                    setResult(RESULT_OK, intent);

                    // Finish this activity and go back to the project list
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
    }

}