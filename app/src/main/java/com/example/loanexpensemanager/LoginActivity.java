package com.example.loanexpensemanager;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.example.loanexpensemanager.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseAuth;
import androidx.core.content.ContextCompat;
import android.text.TextUtils;
import android.util.Patterns;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        binding.txtForgotPassword.setOnClickListener(v -> navigateToForgotPassword());
        binding.goToSignUpScreen.setOnClickListener(v -> navigateToSignUp());
        binding.btnLogin.setOnClickListener(v -> attemptLogin());

        setupAuthStateListener();
        setupTextChangeListeners();
    }

    private void setupAuthStateListener() {
        firebaseAuth.addAuthStateListener(auth -> {
            if (auth.getCurrentUser() != null) {
                navigateToDashboard();
            }
        });
    }

    private void setupTextChangeListeners() {
        binding.emailLogin.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.emailInputLayout.setError(null);
                updateLoginButtonState();
            }
        });

        binding.passwordLogin.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.passwordInputLayout.setError(null);
                updateLoginButtonState();
            }
        });
    }

    private void updateLoginButtonState() {
        boolean isValid = !TextUtils.isEmpty(binding.emailLogin.getText()) &&
                !TextUtils.isEmpty(binding.passwordLogin.getText());
        binding.btnLogin.setEnabled(isValid);
        binding.btnLogin.setBackgroundTintList(ContextCompat.getColorStateList(this,
                isValid ? R.color.teal_200 : R.color.red2));
    }

    private void navigateToForgotPassword() {
        startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
    }

    private void navigateToSignUp() {
        startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
    }

    private void navigateToDashboard() {
        startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
        finish();
    }

    private void attemptLogin() {
        String email = binding.emailLogin.getText().toString().trim();
        String password = binding.passwordLogin.getText().toString().trim();

        if (!validateInputs(email, password)) {
            return;
        }

        showLoading(true);
        loginUser(email, password);
    }

    private boolean validateInputs(String email, String password) {
        boolean isValid = true;

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailInputLayout.setError("Please enter a valid email address");
            isValid = false;
        }

        if (password.length() < 6) {
            binding.passwordInputLayout.setError("Password must be at least 6 characters long");
            isValid = false;
        }

        return isValid;
    }

    private void loginUser(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    showLoading(false);
                    Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                    navigateToDashboard();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(LoginActivity.this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnLogin.setVisibility(isLoading ? View.INVISIBLE : View.VISIBLE);
    }

    private static class TextWatcherAdapter implements android.text.TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(android.text.Editable s) {}
    }
}