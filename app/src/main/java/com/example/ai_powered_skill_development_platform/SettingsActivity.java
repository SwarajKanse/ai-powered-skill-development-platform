package com.example.ai_powered_skill_development_platform;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SettingsActivity extends AppCompatActivity {

    private ImageView profileImage, backButton;
    private TextView profileName, profileEmail, interests, about, signOut;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        profileImage = findViewById(R.id.profile_image);
        backButton = findViewById(R.id.back_button);
        profileName = findViewById(R.id.profile_name);
        profileEmail = findViewById(R.id.profile_email);
        interests = findViewById(R.id.interests);
        about = findViewById(R.id.about);
        signOut = findViewById(R.id.sign_out);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            String name = (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) ? user.getDisplayName() : "";
            String email = (user.getEmail() != null) ? user.getEmail() : "No Email";

            profileName.setText(name);
            profileEmail.setText(email);

            if (user.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(user.getPhotoUrl())
                        .placeholder(R.drawable.ic_profile)  // Default image while loading
                        .error(R.drawable.ic_profile)        // Default image on error
                        .circleCrop()
                        .into(profileImage);
            } else {
                profileImage.setImageResource(R.drawable.ic_profile);
            }
        }

        backButton.setOnClickListener(v -> finish());

        interests.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, QuestionnaireActivity.class);
            intent.putExtra("editMode", true); // Indicating that we are editing existing data
            startActivity(intent);
        });

        about.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, AboutActivity.class);
            startActivity(intent);
        });

        signOut.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(SettingsActivity.this, AuthActivity.class));
            finish();
        });
    }
}
