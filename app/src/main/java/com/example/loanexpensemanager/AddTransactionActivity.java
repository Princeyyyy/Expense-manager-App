package com.example.loanexpensemanager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.loanexpensemanager.databinding.ActivityAddTransactionBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class AddTransactionActivity extends AppCompatActivity {
    private ActivityAddTransactionBinding binding;
    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;
    private String transactionType = "";
    private double budget = 0;
    private double balance = 0;
    private double totalLoan = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddTransactionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeFirebase();
        fetchUserBudget();
        setupListeners();
    }

    private void initializeFirebase() {
        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
    }

    private void fetchUserBudget() {
        String userId = firebaseAuth.getCurrentUser().getUid();
        firestore.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Double budgetValue = documentSnapshot.getDouble("budget");
                        budget = budgetValue != null ? budgetValue : 0;
                        fetchTransactions();
                    }
                })
                .addOnFailureListener(e -> showErrorMessage("Failed to fetch budget: " + e.getMessage()));
    }

    private void fetchTransactions() {
        String userId = firebaseAuth.getUid();
        firestore.collection("Expenses").document(userId).collection("Notes")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    balance = budget;
                    totalLoan = 0;
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String type = document.getString("type");
                        double amount = Double.parseDouble(document.getString("amount"));
                        if ("Expense".equals(type)) {
                            balance -= amount;
                        } else if ("Loan".equals(type)) {
                            totalLoan += amount;
                        }
                    }
                    updateFinancialSummary();
                })
                .addOnFailureListener(e -> showErrorMessage("Failed to fetch transactions: " + e.getMessage()));
    }

    private void updateFinancialSummary() {
        binding.userBudget.setText(String.format(Locale.getDefault(), "%.2f", budget));
        binding.userBalance.setText(String.format(Locale.getDefault(), "%.2f", balance));
        binding.userTotalLoan.setText(String.format(Locale.getDefault(), "%.2f", totalLoan));
    }

    private void setupListeners() {
        binding.transactionTypeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.expense_radio) {
                transactionType = "Expense";
            } else if (checkedId == R.id.loan_radio) {
                transactionType = "Loan";
            }
        });

        binding.btnAddTransaction.setOnClickListener(v -> addTransaction());
    }

    private void addTransaction() {
        String amount = binding.userAmountAdd.getText().toString().trim();
        String note = binding.userNoteAdd.getText().toString().trim();

        if (amount.isEmpty() || transactionType.isEmpty()) {
            showErrorMessage("Please enter amount and select transaction type");
            return;
        }

        double transactionAmount;
        try {
            transactionAmount = Double.parseDouble(amount);
        } catch (NumberFormatException e) {
            showErrorMessage("Please enter a valid amount");
            return;
        }

        if (!validateTransaction(transactionAmount)) return;

        String selectedDate = getSelectedDate();
        String id = UUID.randomUUID().toString();

        Map<String, Object> transaction = new HashMap<>();
        transaction.put("id", id);
        transaction.put("amount", String.format(Locale.US, "%.2f", transactionAmount));
        transaction.put("note", note);
        transaction.put("type", transactionType);
        transaction.put("date", selectedDate);

        saveTransaction(transaction);
    }

    private boolean validateTransaction(double transactionAmount) {
        if (transactionType.equals("Expense")) {
            if (transactionAmount > balance) {
                showErrorMessage("Insufficient balance for this expense");
                return false;
            }
            balance -= transactionAmount;
        } else if (transactionType.equals("Loan")) {
            totalLoan += transactionAmount;
        }
        return true;
    }

    private String getSelectedDate() {
        DatePicker datePicker = binding.datePicker;
        Calendar calendar = Calendar.getInstance();
        calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
        SimpleDateFormat sdf = new SimpleDateFormat("dd MM yyyy", Locale.getDefault());
        return sdf.format(calendar.getTime());
    }

    private void saveTransaction(Map<String, Object> transaction) {
        String userId = firebaseAuth.getUid();
        firestore.collection("Expenses").document(userId).collection("Notes").document((String) transaction.get("id")).set(transaction)
                .addOnSuccessListener(unused -> {
                    showSuccessMessage("Transaction added successfully");
                    clearInputFields();
                    navigateToDashboard();
                })
                .addOnFailureListener(e -> showErrorMessage("Failed to add transaction: " + e.getMessage()));
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(AddTransactionActivity.this, DashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void clearInputFields() {
        binding.userAmountAdd.setText("");
        binding.userNoteAdd.setText("");
        binding.transactionTypeGroup.clearCheck();
        binding.datePicker.updateDate(Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
    }

    private void showErrorMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showSuccessMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}