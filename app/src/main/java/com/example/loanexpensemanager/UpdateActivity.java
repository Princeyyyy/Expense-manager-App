package com.example.loanexpensemanager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.loanexpensemanager.databinding.ActivityUpdateBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class UpdateActivity extends AppCompatActivity {
    ActivityUpdateBinding binding;
    String newType;
    String id;
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUpdateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        id = getIntent().getStringExtra("id");
        String amount = getIntent().getStringExtra("amount");
        String note = getIntent().getStringExtra("note");
        String type = getIntent().getStringExtra("type");

        binding.userAmountAdd.setText(amount);
        binding.userNoteAdd.setText(note);

        newType = type; // Initialize newType with the current type

        switch (type) {
            case "Income":
                binding.incomeCheck.setChecked(true);
                break;
            case "Expense":
                binding.expenseCheck.setChecked(true);
                break;
        }

        binding.incomeCheck.setOnClickListener(v -> {
            newType = "Income";
            binding.incomeCheck.setChecked(true);
            binding.expenseCheck.setChecked(false);
        });

        binding.expenseCheck.setOnClickListener(v -> {
            newType = "Expense";
            binding.incomeCheck.setChecked(false);
            binding.expenseCheck.setChecked(true);
        });

        binding.btnUpdateTransaction.setOnClickListener(v -> updateTransaction());
        binding.btnDeleteTransaction.setOnClickListener(v -> deleteTransaction());
    }

    private void updateTransaction() {
        String amount = binding.userAmountAdd.getText().toString().trim();
        String note = binding.userNoteAdd.getText().toString().trim();

        if (amount.isEmpty() || note.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseFirestore.collection("Expenses").document(firebaseAuth.getUid())
                .collection("Notes").document(id)
                .update("amount", amount, "note", note, "type", newType)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(UpdateActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                    navigateToDashboard();
                })
                .addOnFailureListener(e -> Toast.makeText(UpdateActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void deleteTransaction() {
        firebaseFirestore.collection("Expenses").document(firebaseAuth.getUid())
                .collection("Notes")
                .document(id).delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(UpdateActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                    navigateToDashboard();
                })
                .addOnFailureListener(e -> Toast.makeText(UpdateActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(UpdateActivity.this, DashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}