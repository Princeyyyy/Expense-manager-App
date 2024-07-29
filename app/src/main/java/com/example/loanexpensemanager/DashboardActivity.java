package com.example.loanexpensemanager;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.loanexpensemanager.databinding.ActivityDashboardBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {
    private ActivityDashboardBinding binding;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private double totalLoan = 0;
    private double totalExpense = 0;
    private double budget = 0;
    private ArrayList<TransactionModel> transactionModelArrayList;
    private TransactionAdapter transactionAdapter;
    private String selectedDate = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeFirebase();
        setupRecyclerView();
        setupListeners();
        loadBudget();
        loadData(null);
    }

    private void initializeFirebase() {
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        firebaseAuth.addAuthStateListener(firebaseAuth -> {
            if (firebaseAuth.getCurrentUser() == null) {
                startActivity(new Intent(DashboardActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    private void setupRecyclerView() {
        transactionModelArrayList = new ArrayList<>();
        transactionAdapter = new TransactionAdapter(this, transactionModelArrayList);
        binding.historyRecyclerView.setAdapter(transactionAdapter);
        binding.historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.historyRecyclerView.setHasFixedSize(true);
    }

    private void setupListeners() {
        binding.signOutBtn.setOnClickListener(v -> createSignOutDialog());
        binding.addFloatingBtn.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, AddTransactionActivity.class)));
        binding.addFloatingBtn2.setOnClickListener(v -> showLiteracyFeature());
        binding.calendarBtn.setOnClickListener(v -> showDatePicker());
        binding.allTransactionsBtn.setOnClickListener(v -> {
            loadData(null);
            updateDashboardTitle(null);
        });
    }

    private void createSignOutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Log Out")
                .setMessage("Are you sure you want to Log Out?")
                .setPositiveButton("Yes", (dialog, which) -> firebaseAuth.signOut())
                .setNegativeButton("No", (dialog, which) -> dialog.cancel())
                .show();
    }

    private void showLiteracyFeature() {
        // Implement the literacy feature here
        Snackbar.make(binding.getRoot(), "Literacy feature coming soon!", Snackbar.LENGTH_SHORT).show();
    }

    private void loadBudget() {
        String userId = firebaseAuth.getCurrentUser().getUid();
        DocumentReference userRef = firebaseFirestore.collection("users").document(userId);
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                budget = documentSnapshot.getDouble("budget");
                updateUI();
            }
        });
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    selectedDate = String.format(Locale.getDefault(), "%02d %02d %d", dayOfMonth, monthOfYear + 1, year1);
                    loadData(selectedDate);
                    updateDashboardTitle(selectedDate);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void loadData(String date) {
        firebaseFirestore.collection("Expenses").document(firebaseAuth.getUid()).collection("Notes")
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        transactionModelArrayList.clear();
                        totalLoan = 0;
                        totalExpense = 0;
                        for (DocumentSnapshot ds : task.getResult()) {
                            String amountString = ds.getString("amount");
                            if (amountString == null || amountString.isEmpty()) {
                                continue; // Skip this transaction if amount is null or empty
                            }

                            try {
                                double amount = Double.parseDouble(amountString);

                                TransactionModel model = new TransactionModel(
                                        ds.getString("id"),
                                        ds.getString("note"),
                                        amountString,
                                        ds.getString("type"),
                                        ds.getString("date"));

                                if (date == null || model.getDate().equals(date)) {
                                    if ("Expense".equals(ds.getString("type"))) {
                                        totalExpense += amount;
                                    } else {
                                        totalLoan += amount;
                                    }
                                    transactionModelArrayList.add(model);
                                }
                            } catch (NumberFormatException e) {
                                // Log the error or show a message
                                showErrorMessage("Invalid amount format in transaction: " + ds.getId());
                            }
                        }
                        updateUI();
                    } else {
                        showErrorMessage("Failed to load data. Please try again.");
                    }
                });
    }

    private void updateUI() {
        binding.totalIncome.setText(String.format(Locale.getDefault(), "%.2f", totalLoan));
        binding.totalExpense.setText(String.format(Locale.getDefault(), "%.2f", totalExpense));
        double balance = budget - totalExpense;
        binding.totalBalance.setText(String.format(Locale.getDefault(), "%.2f", balance));
        binding.userBudget.setText(String.format(Locale.getDefault(), "%.2f", budget));
        transactionAdapter.notifyDataSetChanged();

        // Update progress towards budget
        updateBudgetProgress(totalExpense, budget);
    }

    private void updateBudgetProgress(double expense, double budget) {
        int progress = (int) ((expense / budget) * 100);

        if (progress >= 90 && progress < 100) {
            showWarningMessage("You're close to your budget limit!");
        } else if (progress >= 100) {
            showWarningMessage("You've exceeded your budget!");
        }
    }

    private void updateDashboardTitle(String selectedDate) {
        if (selectedDate == null) {
            binding.dashboardTitle.setText("All Transactions");
        } else {
            binding.dashboardTitle.setText("Transactions for " + selectedDate);
        }
    }

    private void showErrorMessage(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
    }

    private void showWarningMessage(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getResources().getColor(R.color.red))
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBudget();
        loadData(selectedDate);
    }
}
