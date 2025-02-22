package com.example.ai_powered_skill_development_platform;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

public class AuthActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnAuth, btnGoogleSignIn;
    private TextView tvToggleAuth, tvTitle;
    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;
    private ProgressDialog progressDialog;
    private boolean isSignUpMode = false; // Default to Login Mode
    private Handler handler = new Handler();
    private Runnable verificationChecker;
    private boolean isCheckingVerification = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            checkUserState(currentUser);
            return;
        }

        setContentView(R.layout.activity_auth);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnAuth = findViewById(R.id.btnSignUpEmail);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        tvToggleAuth = findViewById(R.id.tvAlreadyHaveAccount);
        tvTitle = findViewById(R.id.tvTitle);
        progressDialog = new ProgressDialog(this);

        btnAuth.setOnClickListener(v -> handleEmailAuth());
        btnGoogleSignIn.setOnClickListener(v -> signInWithGoogle());
        tvToggleAuth.setOnClickListener(v -> toggleAuthMode());

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        updateUIForMode(); // Set the UI for login/signup mode
    }

    private void toggleAuthMode() {
        isSignUpMode = !isSignUpMode;
        updateUIForMode();
    }

    private void updateUIForMode() {
        if (isSignUpMode) {
            btnAuth.setText("Sign Up");
            tvTitle.setText("Create an Account");
            tvToggleAuth.setText("Already have an account? Login here");
        } else {
            btnAuth.setText("Login");
            tvTitle.setText("Welcome Back");
            tvToggleAuth.setText("Don't have an account? Sign up here");
        }
    }

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                showToast("Google Sign-In failed: " + e.getMessage());
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                checkUserState(mAuth.getCurrentUser());
            } else {
                showToast("Google Sign-In failed.");
            }
        });
    }

    private void checkUserState(FirebaseUser user) {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        boolean hasFilledQuestionnaire = prefs.getBoolean("hasFilledQuestionnaire", false);

        if (!hasFilledQuestionnaire) {
            startActivity(new Intent(this, QuestionnaireActivity.class));
        } else {
            startActivity(new Intent(this, DashboardActivity.class));
        }
        finish();
    }

    private void handleEmailAuth() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            showToast("Email and Password are required");
            return;
        }

        progressDialog.setMessage(isSignUpMode ? "Signing Up..." : "Logging In...");
        progressDialog.show();

        if (isSignUpMode) {
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    user.sendEmailVerification().addOnSuccessListener(aVoid -> {
                        showToast("Verification email sent. Please check your email.");
                    }).addOnFailureListener(e -> showToast("Failed to send verification email."));
                } else {
                    showToast("Sign-up failed: " + task.getException().getMessage());
                }
            });
        } else {
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    checkUserState(mAuth.getCurrentUser());
                } else {
                    showToast("Login failed: " + task.getException().getMessage());
                }
            });
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
