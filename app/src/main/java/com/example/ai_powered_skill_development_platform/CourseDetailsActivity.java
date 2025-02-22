package com.example.ai_powered_skill_development_platform;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

public class CourseDetailsActivity extends AppCompatActivity {

    private ImageView ivThumbnail;
    private TextView tvTitle, tvDescription;
    private ProgressBar progressBar;
    private Button btnAction;
    private Course course; // This will hold course details

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_details);

        ivThumbnail = findViewById(R.id.iv_thumbnail);
        tvTitle = findViewById(R.id.tv_course_title);
        tvDescription = findViewById(R.id.tv_course_description);
        progressBar = findViewById(R.id.course_progress);
        btnAction = findViewById(R.id.btn_action);

        // Get course name from intent extra
        String courseName = getIntent().getStringExtra("courseName");
        // For demonstration, create a dummy course. In a real app, fetch details from Firestore.
        course = new Course(courseName, "This is a description for " + courseName,
                50, R.drawable.ic_placeholder, "https://www.example.com", false);

        // Populate UI with course details
        tvTitle.setText(course.getTitle());
        tvDescription.setText(course.getDescription());
        progressBar.setProgress(course.getProgress());
        Glide.with(this)
                .load(course.getThumbnailUrl()) // Assuming this returns an image URL
                .placeholder(R.drawable.ic_placeholder)
                .into(ivThumbnail);

        // Set button text based on whether the course is already undertaken
        btnAction.setText(course.isUndertaken() ? "Remove" : "Start");

        btnAction.setOnClickListener(v -> {
            if (course.isUndertaken()) {
                // Confirm removal
                new AlertDialog.Builder(this)
                        .setTitle("Delete course?")
                        .setMessage("This will delete all the course progress.")
                        .setPositiveButton("Delete", (dialog, which) -> removeCourse())
                        .setNegativeButton("Cancel", null)
                        .show();
            } else {
                // Start the course (add to user’s courses in Firestore)
                addCourseToUser();
            }
        });

        // Clicking on thumbnail opens course website
        ivThumbnail.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(course.getWebsiteUrl()));
            startActivity(browserIntent);
        });
    }

    private void addCourseToUser() {
        // TODO: Implement Firestore call to add course to user's list.
        Toast.makeText(this, "Course started!", Toast.LENGTH_SHORT).show();
        course.setUndertaken(true);
        btnAction.setText("Remove");
    }

    private void removeCourse() {
        // TODO: Implement Firestore call to remove course data for the user.
        Toast.makeText(this, "Course removed!", Toast.LENGTH_SHORT).show();
        course.setUndertaken(false);
        btnAction.setText("Start");
    }
}
