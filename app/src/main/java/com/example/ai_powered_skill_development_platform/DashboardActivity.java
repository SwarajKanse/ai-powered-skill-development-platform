package com.example.ai_powered_skill_development_platform;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    // Navigation Drawer components
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private RecyclerView rvUndertakenCourses; // RecyclerView for courses undertaken
    private UndertakenCoursesAdapter undertakenCoursesAdapter;
    private List<String> coursesUndertakenList;

    // Main Content components
    private EditText searchDashboard;
    private TextView tvWelcome, tvSubtitle;
    // Only keeping the recommended courses RecyclerView
    private RecyclerView rvRecommended;
    private CourseDetailsAdapter recommendedAdapter;
    private List<Course> recommendedCourses;

    // Profile info (from Navigation Drawer)
    private ImageView profileImage;
    private TextView profileName;

    // Firebase Auth
    private FirebaseAuth mAuth;

    private CourseRepository courseRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Force Light Mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        courseRepository = new CourseRepository(this);

        // --- Main Content Setup ---
        tvWelcome = findViewById(R.id.tv_welcome);
        tvSubtitle = findViewById(R.id.tv_subtitle);
        searchDashboard = findViewById(R.id.search_dashboard);
        rvRecommended = findViewById(R.id.rv_recommended);

        // --- Navigation Drawer Setup ---
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        // Get the undertaken courses RecyclerView from within the NavigationView layout
        rvUndertakenCourses = navigationView.findViewById(R.id.rv_courses);

        // --- Toolbar Setup ---
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // --- Firebase Auth & Profile Setup ---
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        String displayName = "Ambitious Soul!";
        if (user != null && user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
            displayName = user.getDisplayName();
        }
        tvWelcome.setText("Welcome, " + displayName + "!");

        // --- Setup Profile Info in Navigation Drawer ---
        setupProfileInfo(user);

        // --- Setup RecyclerView for Undertaken Courses in Navigation Drawer ---
        setupUndertakenCourses();

        // --- Setup Main Screen RecyclerView for recommended courses ---
        setupRecommendedCourses();

        // Load personalized recommendations after initial setup
        checkQuestionnaireStatus();

        // --- Setup Search Filter on Main Dashboard Search Bar ---
        setupSearchFilter();
    }

    private void setupProfileInfo(FirebaseUser user) {
        profileImage = navigationView.findViewById(R.id.profile_image);
        profileName = navigationView.findViewById(R.id.profile_name);
        View profileSection = navigationView.findViewById(R.id.profile_section);

        if (user != null) {
            String name;
            if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
                name = user.getDisplayName();
            } else if (user.getEmail() != null) {
                name = "@" + user.getEmail().split("@")[0];
            } else {
                name = "User Name";
            }
            profileName.setText(name);
            if (user.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(user.getPhotoUrl())
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile)
                        .circleCrop()
                        .into(profileImage);
            } else {
                profileImage.setImageResource(R.drawable.ic_profile);
            }
        }

        if (profileSection != null) {
            profileSection.setOnClickListener(v -> {
                Intent intent = new Intent(DashboardActivity.this, SettingsActivity.class);
                startActivity(intent);
            });
        }
    }

    private void setupUndertakenCourses() {
        coursesUndertakenList = getLastViewedCourses();
        undertakenCoursesAdapter = new UndertakenCoursesAdapter(coursesUndertakenList);
        rvUndertakenCourses.setLayoutManager(new LinearLayoutManager(this));
        rvUndertakenCourses.setAdapter(undertakenCoursesAdapter);
    }

    private void setupSearchFilter() {
        searchDashboard.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Filter recommended courses
                if (recommendedAdapter != null) {
                    recommendedAdapter.filter(s.toString());
                }
            }
            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    private void checkQuestionnaireStatus() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        boolean hasFilledQuestionnaire = prefs.getBoolean("hasFilledQuestionnaire", false);

        if (hasFilledQuestionnaire) {
            // Load courses based on questionnaire responses
            loadCoursesBasedOnUserInterests();
        } else {
            // User hasn't filled questionnaire yet - show prompt or redirect
            Toast.makeText(this, "Please complete the questionnaire for personalized recommendations", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(DashboardActivity.this, QuestionnaireActivity.class);
            startActivity(intent);
        }
    }

    private List<String> getLastViewedCourses() {
        List<String> courses = new ArrayList<>();
        courses.add("Java Development");
        courses.add("Machine Learning Basics");
        courses.add("Web Development with React");
        courses.add("Android App Development");
        return courses;
    }

    private void setupRecommendedCourses() {
        recommendedCourses = new ArrayList<>();

        // Dummy Data for Recommended Courses
        recommendedCourses.add(new Course("AI for Beginners", "Learn the fundamentals of AI", R.drawable.ic_placeholder, 70));
        recommendedCourses.add(new Course("Java Masterclass", "Comprehensive Java training", R.drawable.ic_placeholder, 50));

        // Setup RecyclerView for Recommended Courses (Horizontal)
        rvRecommended.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recommendedAdapter = new CourseDetailsAdapter(recommendedCourses);
        rvRecommended.setAdapter(recommendedAdapter);
    }

    private void loadCoursesBasedOnUserInterests() {
        // Use the CourseRepository to get Gemini recommended courses
        courseRepository.getGeminiRecommendedCourses(new CourseRepository.CoursesCallback() {
            @Override
            public void onCoursesLoaded(List<Course> courses) {
                runOnUiThread(() -> {
                    if (courses != null && !courses.isEmpty()) {
                        // Update the recommended courses
                        recommendedCourses.clear();
                        recommendedCourses.addAll(courses);
                        recommendedAdapter.notifyDataSetChanged();

                        // Update UI with personalized message
                        updatePersonalizedMessage();
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    Toast.makeText(DashboardActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void updatePersonalizedMessage() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String careerGoal = prefs.getString("careerGoal", "");

        if (!careerGoal.isEmpty()) {
            tvSubtitle.setText("Courses tailored for your " + careerGoal + " journey");
        }
    }
}