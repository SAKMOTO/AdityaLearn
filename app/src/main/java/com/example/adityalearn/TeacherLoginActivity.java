package com.example.adityalearn;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class TeacherLoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private TextInputLayout emailInputLayout, passwordInputLayout;
    private MaterialButton btnLogin;
    private TextView tvRegister;

    // Dev fallback (optional)
    private static final String TEACHER_EMAIL = "teacher@aditya.com";
    private static final String TEACHER_PASSWORD = "teacher123";

    // Prefs Keys
    public static final String PREFS_NAME = "TeacherPrefs";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_login);

        // Views
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        emailInputLayout = findViewById(R.id.textInputLayoutEmail);
        passwordInputLayout = findViewById(R.id.textInputLayoutPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // If already logged in, skip login
        if (isAlreadyLoggedIn()) {
            goToHome();
            return;
        }

        // Login
        btnLogin.setOnClickListener(v -> validateAndLogin());

        // Register
        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(TeacherLoginActivity.this, TeacherRegisterActivity.class);
            startActivity(intent);
        });
    }

    private boolean isAlreadyLoggedIn() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    private void setLoggedIn(String emailToSave) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        if (emailToSave != null) editor.putString(KEY_EMAIL, emailToSave);
        editor.apply();
    }

    private void goToHome() {
        Intent intent = new Intent(TeacherLoginActivity.this, MainActivity.class);
        intent.putExtra("isTeacher", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void validateAndLogin() {
        if (emailInputLayout != null) emailInputLayout.setError(null);
        if (passwordInputLayout != null) passwordInputLayout.setError(null);

        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        boolean isValid = true;

        if (TextUtils.isEmpty(email)) {
            if (emailInputLayout != null) emailInputLayout.setError("Email is required");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (emailInputLayout != null) emailInputLayout.setError("Enter a valid email address");
            isValid = false;
        }

        if (TextUtils.isEmpty(password)) {
            if (passwordInputLayout != null) passwordInputLayout.setError("Password is required");
            isValid = false;
        } else if (password.length() < 6) {
            if (passwordInputLayout != null) passwordInputLayout.setError("Password must be at least 6 characters");
            isValid = false;
        }

        if (!isValid) return;

        // Read saved credentials
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedEmail = prefs.getString(KEY_EMAIL, null);
        String savedPassword = prefs.getString(KEY_PASSWORD, null);

        boolean matchesSaved = savedEmail != null && savedPassword != null
                && email.equals(savedEmail) && password.equals(savedPassword);

        boolean matchesFallback = email.equals(TEACHER_EMAIL) && password.equals(TEACHER_PASSWORD);

        if (matchesSaved || matchesFallback) {
            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
            setLoggedIn(email);
            goToHome();
        } else {
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
        }
    }
}
