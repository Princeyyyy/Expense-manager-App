package com.example.loanexpensemanager;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.loanexpensemanager.databinding.ActivityForgotPasswordBinding;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {
    private ActivityForgotPasswordBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        binding.btnReset.setOnClickListener(v -> attemptResetPassword());
        binding.btnForgotPasswordBack.setOnClickListener(v -> onBackPressed());

        setupTextChangeListener();
    }

    private void setupTextChangeListener() {
        binding.edtForgotPasswordEmail.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.txtLayoutEmail.setError(null);
                updateResetButtonState();
            }
        });
    }

    private void updateResetButtonState() {
        boolean isValid = !TextUtils.isEmpty(binding.edtForgotPasswordEmail.getText());
        binding.btnReset.setEnabled(isValid);
        binding.btnReset.setBackgroundTintList(getColorStateList(isValid ? R.color.black : R.color.red2));
    }

    private void attemptResetPassword() {
        String email = binding.edtForgotPasswordEmail.getText().toString().trim();
        if (validateEmail(email)) {
            resetPassword(email);
        }
    }

    private boolean validateEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            binding.txtLayoutEmail.setError("Email field can't be empty");
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.txtLayoutEmail.setError("Please enter a valid email address");
            return false;
        }
        return true;
    }

    private void resetPassword(String email) {
        showLoading(true);
        mAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused -> {
                    showLoading(false);
                    Toast.makeText(ForgotPasswordActivity.this, "Reset Password link has been sent to your registered Email", Toast.LENGTH_SHORT).show();
                    navigateToLogin();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(ForgotPasswordActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showLoading(boolean isLoading) {
        binding.forgetPasswordProgressbar.setVisibility(isLoading ? View.VISIBLE : View.INVISIBLE);
        binding.btnReset.setVisibility(isLoading ? View.INVISIBLE : View.VISIBLE);
    }

    private void navigateToLogin() {
        startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
        finish();
    }

    private static class TextWatcherAdapter implements android.text.TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(android.text.Editable s) {
        }
    }
}