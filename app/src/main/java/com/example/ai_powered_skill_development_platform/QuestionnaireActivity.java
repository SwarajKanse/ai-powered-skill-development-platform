package com.example.ai_powered_skill_development_platform;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class QuestionnaireActivity extends AppCompatActivity {

    private Spinner spinnerEducation, spinnerFurtherEducation, spinnerWorkType;
    private EditText etFieldOfStudy, etCareerGoal, etSkillsToMaster;
    private Button btnSubmitQuestionnaire;
    private String selectedEducation, selectedFurtherEducation, selectedWorkType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questionnaire);

        // Initialize UI components
        spinnerEducation = findViewById(R.id.spinnerEducation);
        spinnerFurtherEducation = findViewById(R.id.spinnerFurtherEducation);
        spinnerWorkType = findViewById(R.id.spinnerWorkType);
        etFieldOfStudy = findViewById(R.id.etFieldOfStudy);
        etCareerGoal = findViewById(R.id.etCareerGoal);
        etSkillsToMaster = findViewById(R.id.etSkillsToMaster);
        btnSubmitQuestionnaire = findViewById(R.id.btnSubmitQuestionnaire);

        setUpSpinners(); // Ensure spinners are initialized first

        // Check if it's edit mode
        boolean isEditMode = getIntent().getBooleanExtra("editMode", false);
        if (isEditMode) {
            loadPreviousResponses(); // Now it is safe to load previous responses
        }

        btnSubmitQuestionnaire.setOnClickListener(v -> saveResponses());
    }

    private void loadPreviousResponses() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);

        // Load and set spinner selections
        selectedEducation = prefs.getString("educationLevel", "");
        selectedFurtherEducation = prefs.getString("furtherEducation", "");
        selectedWorkType = prefs.getString("workType", "");

        // Load and set EditText values
        etFieldOfStudy.setText(prefs.getString("fieldOfStudy", ""));
        etCareerGoal.setText(prefs.getString("careerGoal", ""));
        etSkillsToMaster.setText(prefs.getString("skillsToMaster", ""));

        // Set spinners to saved values
        setSpinnerSelection(spinnerEducation, selectedEducation);
        setSpinnerSelection(spinnerFurtherEducation, selectedFurtherEducation);
        setSpinnerSelection(spinnerWorkType, selectedWorkType);
    }

    // Helper method to select correct spinner item
    private void setSpinnerSelection(Spinner spinner, String value) {
        if (spinner.getAdapter() == null) {
            return; // Prevent crash if adapter is not set
        }

        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spinner.getAdapter();
        int position = adapter.getPosition(value);

        if (position >= 0) {
            spinner.setSelection(position);
        }
    }


    private void setUpSpinners() {
        ArrayAdapter<CharSequence> educationAdapter = ArrayAdapter.createFromResource(this,
                R.array.education_levels, android.R.layout.simple_spinner_item);
        educationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEducation.setAdapter(educationAdapter);

        ArrayAdapter<CharSequence> furtherEducationAdapter = ArrayAdapter.createFromResource(this,
                R.array.further_education_options, android.R.layout.simple_spinner_item);
        furtherEducationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFurtherEducation.setAdapter(furtherEducationAdapter);

        ArrayAdapter<CharSequence> workTypeAdapter = ArrayAdapter.createFromResource(this,
                R.array.work_types, android.R.layout.simple_spinner_item);
        workTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWorkType.setAdapter(workTypeAdapter);

        // Capture selected values
        spinnerEducation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedEducation = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinnerFurtherEducation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedFurtherEducation = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinnerWorkType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedWorkType = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void saveResponses() {
        String fieldOfStudy = etFieldOfStudy.getText().toString().trim();
        String careerGoal = etCareerGoal.getText().toString().trim();
        String skillsToMaster = etSkillsToMaster.getText().toString().trim();

        // Validation (ensure user enters required details)
        if (fieldOfStudy.isEmpty() || careerGoal.isEmpty() || skillsToMaster.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields before submitting.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save responses in SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("educationLevel", selectedEducation);
        editor.putString("furtherEducation", selectedFurtherEducation);
        editor.putString("fieldOfStudy", fieldOfStudy);
        editor.putString("careerGoal", careerGoal);
        editor.putString("workType", selectedWorkType);
        editor.putString("skillsToMaster", skillsToMaster);
        editor.putBoolean("hasFilledQuestionnaire", true);
        editor.apply();

        Toast.makeText(this, "Responses saved successfully!", Toast.LENGTH_SHORT).show();

        // Navigate to Dashboard
        startActivity(new Intent(this, DashboardActivity.class));
        finish();
    }
}
