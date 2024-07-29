package com.example.loanexpensemanager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;
import com.example.loanexpensemanager.databinding.ActivitySignUpBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {
    private ActivitySignUpBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        binding.goToLoginScreen.setOnClickListener(v -> navigateToLogin());
        binding.btnSignup.setOnClickListener(v -> attemptSignUp());

        setupTextChangeListeners();
    }

    private void setupTextChangeListeners() {
        binding.emailForSignUp.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.emailInputLayout.setError(null);
                updateSignUpButtonState();
            }
        });

        binding.passwordForSignup.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.passwordInputLayout.setError(null);
                updateSignUpButtonState();
            }
        });

        binding.userBudget.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.budgetInputLayout.setError(null);
                updateSignUpButtonState();
            }
        });
    }

    private void updateSignUpButtonState() {
        boolean isValid = !TextUtils.isEmpty(binding.emailForSignUp.getText()) &&
                !TextUtils.isEmpty(binding.passwordForSignup.getText()) &&
                !TextUtils.isEmpty(binding.userBudget.getText());
        binding.btnSignup.setEnabled(isValid);
        binding.btnSignup.setBackgroundTintList(ContextCompat.getColorStateList(this,
                isValid ? R.color.teal_200 : R.color.red2));
    }

    private void navigateToLogin() {
        startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
        finish();
    }

    private void attemptSignUp() {
        String email = binding.emailForSignUp.getText().toString().trim();
        String password = binding.passwordForSignup.getText().toString().trim();
        String budgetString = binding.userBudget.getText().toString().trim();

        if (!validateInputs(email, password, budgetString)) {
            return;
        }

        double budget = Double.parseDouble(budgetString);
        showLoading(true);
        createUser(email, password, budget);
    }

    private boolean validateInputs(String email, String password, String budget) {
        boolean isValid = true;

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailInputLayout.setError("Please enter a valid email address");
            isValid = false;
        }

        if (password.length() < 6) {
            binding.passwordInputLayout.setError("Password must be at least 6 characters long");
            isValid = false;
        }

        try {
            double budgetValue = Double.parseDouble(budget);
            if (budgetValue <= 0) {
                binding.budgetInputLayout.setError("Budget must be greater than 0");
                isValid = false;
            }
        } catch (NumberFormatException e) {
            binding.budgetInputLayout.setError("Invalid budget format");
            isValid = false;
        }

        return isValid;
    }

    private void createUser(String email, String password, double budget) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> saveUserData(email, budget))
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(SignUpActivity.this, "Failed to create user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveUserData(String email, double budget) {
        String userId = firebaseAuth.getCurrentUser().getUid();
        Map<String, Object> userData = new HashMap<>();
        userData.put("email", email);
        userData.put("budget", budget);

        firebaseFirestore.collection("users").document(userId).set(userData)
                .addOnSuccessListener(aVoid -> {
                    showLoading(false);
                    Toast.makeText(SignUpActivity.this, "User created successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SignUpActivity.this, DashboardActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(SignUpActivity.this, "Failed to save user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnSignup.setVisibility(isLoading ? View.INVISIBLE : View.VISIBLE);
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