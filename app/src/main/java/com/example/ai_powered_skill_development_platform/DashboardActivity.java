package com.example.ai_powered_skill_development_platform;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
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

    // Navigation drawer components
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ListView listViewCourses; // For courses undertaken in the drawer
    private ArrayAdapter<String> listAdapter;
    private List<String> coursesUndertakenList;

    // Main screen RecyclerViews for courses
    private RecyclerView rvRecommended, rvOngoing, rvTrending;
    private CourseDetailsAdapter recommendedAdapter, ongoingAdapter, trendingAdapter;
    private List<Course> recommendedCourses, ongoingCourses, trendingCourses;

    // Other UI components in main content
    private EditText searchDashboard;
    private ImageView profileImage;
    private TextView profileName, tvWelcome, tvSubtitle;

    // Firebase Auth
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Force Light Mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // ------------------------------
        // Setup Main Content Components
        // ------------------------------
        tvWelcome = findViewById(R.id.tv_welcome);
        tvSubtitle = findViewById(R.id.tv_subtitle);
        searchDashboard = findViewById(R.id.search_dashboard);
        rvRecommended = findViewById(R.id.rv_recommended);
        rvOngoing = findViewById(R.id.rv_ongoing);
        rvTrending = findViewById(R.id.rv_trending);

        // ------------------------------
        // Setup Navigation Drawer Components
        // ------------------------------
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        // The ListView for undertaken courses is inside the NavigationView.
        listViewCourses = findViewById(R.id.listViewCourses);

        // Setup Toolbar and Drawer Toggle
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // ------------------------------
        // Initialize Firebase Auth and User Info
        // ------------------------------
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        // Set welcome message using user's display name if available, else default to "Ambitious Soul!"
        String displayName = "Ambitious Soul!";
        if (user != null && user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
            displayName = user.getDisplayName();
        }
        tvWelcome.setText("Welcome, " + displayName + "!");
        // (tv_subtitle remains as defined in XML)

        // Set profile info in the navigation drawer (profile image and name)
        profileImage = findViewById(R.id.profile_image);
        profileName = findViewById(R.id.profile_name);
        View profileSection = findViewById(R.id.profile_section); // Entire profile section in the drawer

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

        // Set click listener on profile section to open SettingsActivity
        if (profileSection != null) {
            profileSection.setOnClickListener(v -> {
                Intent intent = new Intent(DashboardActivity.this, SettingsActivity.class);
                startActivity(intent);
            });
        }

        // ------------------------------
        // Setup ListView for Undertaken Courses in Drawer
        // ------------------------------
        coursesUndertakenList = getLastViewedCourses(); // Using the same dummy list for now
        listAdapter = new ArrayAdapter<>(this, R.layout.nav_list_item, R.id.tv_course_title, coursesUndertakenList);
        listViewCourses.setAdapter(listAdapter);
        listViewCourses.setDivider(null);
        listViewCourses.setDividerHeight(0);
        listViewCourses.setOnItemClickListener((parent, view, position, id) -> {
            String selectedCourse = coursesUndertakenList.get(position);
            Toast.makeText(DashboardActivity.this, "Selected: " + selectedCourse, Toast.LENGTH_SHORT).show();
        });

        // ------------------------------
        // Setup Main Screen RecyclerViews
        // ------------------------------
        setupMainScreenCourses();

        // ------------------------------
        // Setup Search Filter on Main Dashboard Search Bar
        // ------------------------------
        searchDashboard.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (recommendedAdapter != null) {
                    // Assuming CourseDetailsAdapter has a filter method; implement it if needed.
                    recommendedAdapter.filter(s.toString());
                }
            }
            @Override
            public void afterTextChanged(Editable s) { }
        });

        // ------------------------------
        // Setup Explore Courses Click Listener in Navigation Drawer
        // ------------------------------
        LinearLayout exploreCoursesLayout = findViewById(R.id.ll_explore_courses);
        if (exploreCoursesLayout != null) {
            exploreCoursesLayout.setOnClickListener(v -> {
                Toast.makeText(DashboardActivity.this, "Explore Courses Clicked", Toast.LENGTH_SHORT).show();
            });
        }
    }

    /**
     * Returns a dummy list of courses (undertaken) for the navigation drawer.
     */
    private List<String> getLastViewedCourses() {
        List<String> courses = new ArrayList<>();
        courses.add("Java Development");
        courses.add("Machine Learning Basics");
        courses.add("Web Development with React");
        courses.add("Android App Development");
        return courses;
    }

    /**
     * Sets up the main screen RecyclerViews for Recommended, Ongoing, and Trending courses.
     */
    private void setupMainScreenCourses() {
        // Initialize dummy data lists
        recommendedCourses = new ArrayList<>();
        ongoingCourses = new ArrayList<>();
        trendingCourses = new ArrayList<>();

        // Dummy Data for Recommended Courses
        recommendedCourses.add(new Course("AI for Beginners", "Learn the fundamentals of AI", R.drawable.ic_placeholder, 70));
        recommendedCourses.add(new Course("Java Masterclass", "Comprehensive Java training", R.drawable.ic_placeholder, 50));

        // Dummy Data for Ongoing Courses
        ongoingCourses.add(new Course("Full Stack Web Dev", "Master frontend & backend", R.drawable.ic_placeholder, 40));
        ongoingCourses.add(new Course("Data Science Bootcamp", "Learn data science skills", R.drawable.ic_placeholder, 60));

        // Dummy Data for Trending Courses
        trendingCourses.add(new Course("Cyber Security", "Protect systems and networks", R.drawable.ic_placeholder, 30));
        trendingCourses.add(new Course("Cloud Computing", "Understand cloud platforms", R.drawable.ic_placeholder, 55));

        // Setup RecyclerView for Recommended Courses (Horizontal)
        rvRecommended.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recommendedAdapter = new CourseDetailsAdapter(recommendedCourses);
        rvRecommended.setAdapter(recommendedAdapter);

        // Setup RecyclerView for Ongoing Courses (Horizontal)
        rvOngoing.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        ongoingAdapter = new CourseDetailsAdapter(ongoingCourses);
        rvOngoing.setAdapter(ongoingAdapter);

        // Setup RecyclerView for Trending Courses (Vertical)
        rvTrending.setLayoutManager(new LinearLayoutManager(this));
        trendingAdapter = new CourseDetailsAdapter(trendingCourses);
        rvTrending.setAdapter(trendingAdapter);
    }
}
