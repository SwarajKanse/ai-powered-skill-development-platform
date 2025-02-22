package com.example.ai_powered_skill_development_platform;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ListView listViewCourses; // Changed to ListView
    private ArrayAdapter<String> courseAdapter;
    private List<String> courseList;
    private FirebaseAuth mAuth;
    private ImageView profileImage;
    private TextView profileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        listViewCourses = findViewById(R.id.listViewCourses);
        EditText searchCourses = findViewById(R.id.search_courses);
        profileImage = findViewById(R.id.profile_image);
        profileName = findViewById(R.id.profile_name);
        View profileSection = findViewById(R.id.profile_section);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        TextView welcomeMessage = findViewById(R.id.tv_welcome);
        String displayName = "Ambitious Soul!";

        if (user != null && user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
            displayName = user.getDisplayName();
        }

        welcomeMessage.setText("Welcome, " + displayName + "!");

        if (user != null) {
            String name;

            if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
                name = user.getDisplayName();
            } else if (user.getEmail() != null) {
                name = "@" + user.getEmail().split("@")[0]; // Extracts part before '@' and prefixes '@'
            } else {
                name = "User Name"; // Default fallback
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

        // Navigation Drawer Toggle
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Click listener for profile section
        profileSection.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        // ✅ Initialize courseList for ListView
        courseList = getLastViewedCourses();

        // ✅ Set up ListView Adapter
        courseAdapter = new ArrayAdapter<>(this, R.layout.nav_list_item, R.id.tv_course_title, courseList);
        listViewCourses.setAdapter(courseAdapter);
        listViewCourses.setDivider(null);
        listViewCourses.setDividerHeight(0);

        // ✅ Handle ListView item clicks
        listViewCourses.setOnItemClickListener((parent, view, position, id) -> {
            String selectedCourse = courseList.get(position);
            Toast.makeText(DashboardActivity.this, "Selected: " + selectedCourse, Toast.LENGTH_SHORT).show();
        });

        // ✅ Search Filter
        searchCourses.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                courseAdapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Explore Courses click listener
        LinearLayout exploreCoursesLayout = findViewById(R.id.ll_explore_courses);
        exploreCoursesLayout.setOnClickListener(v ->
                Toast.makeText(DashboardActivity.this, "Explore Courses Clicked", Toast.LENGTH_SHORT).show()
        );
    }

    private List<String> getLastViewedCourses() {
        List<String> courses = new ArrayList<>();
        courses.add("Java Development");
        courses.add("Machine Learning Basics");
        courses.add("Web Development with React");
        courses.add("Android App Development");
        return courses;
    }
}
