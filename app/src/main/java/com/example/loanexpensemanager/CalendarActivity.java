package com.example.loanexpensemanager;


import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CalendarActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private RecyclerView recyclerView;
    private TransactionAdapter transactionAdapter;
    private Button printButton;
    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;
    private List<TransactionModel> transactions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        calendarView = findViewById(R.id.calendarView);
        recyclerView = findViewById(R.id.history_recycler_view);
        printButton=findViewById(R.id.printButton);

        transactions = new ArrayList<>();
        transactionAdapter = new TransactionAdapter(this, (ArrayList<TransactionModel>) transactions);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(transactionAdapter);

        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int dayOfMonth) {
                // Get the selected date in the format you need
                String selectedDate = formatDate(year, month, dayOfMonth);
                fetchTransactions(selectedDate);
            }
        });
        printButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                printSelectedTransaction();
            }
        });
    }

    private void printSelectedTransaction() {
        long selectedDateMillis = calendarView.getDate();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(selectedDateMillis);

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        String selectedDate = formatDate(year, month, dayOfMonth);
        fetchTransactions(selectedDate);

        StringBuilder printData = new StringBuilder("Selected Transactions for " + selectedDate + ":\n\n");
        for (TransactionModel transaction : transactions) {
            printData.append("Note: ").append(transaction.getNote()).append("\n");
            printData.append("Amount: ").append(transaction.getAmount()).append("\n");
            printData.append("Type: ").append(transaction.getType()).append("\n\n");
        }
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Transaction Details")
                .setMessage(printData.toString())
                .setPositiveButton("Back", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveDataToFile(printData.toString());
                        Toast.makeText(CalendarActivity.this, "Data saved", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
        AlertDialog alertDialog=builder.create();
        alertDialog.show();
    }

    private void saveDataToFile(String data){
        try {
            String filename = "transaction_data.txt";
            File directory = new File(getExternalFilesDir(null), "YourAppFolder");
            directory.mkdirs();
            File file = new File(directory, filename);
            FileWriter writer = new FileWriter(file);
            writer.append(data);
                     writer.flush();
            writer.close();


            Uri uri = FileProvider.getUriForFile(this, "com.example.loanexpensemanager.fileprovider", file);


            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);

            startActivity(Intent.createChooser(shareIntent, "Share File"));


        }catch (Exception e){
            e.printStackTrace();
        }
    }

    String formatDate(int year, int month, int dayOfMonth) {
        // Format the selected date in the format "dd MM yyyy_HHmmss"
        String day = String.valueOf(dayOfMonth);
        String mon = String.valueOf(month + 1); // Month starts from 0 (January)
        String yr = String.valueOf(year);
//        String hr = "00"; // Assuming the time is always 00:00:00
//        String min = "00";
//        String sec = "00";

        // Pad single-digit day/month/hour/minute/second with leading zero
        if (dayOfMonth < 10) {
            day = "0" + day;
        }
        if (month + 1 < 10) {
            mon = "0" + mon;
        }

        return day + " " + mon + " " + yr ;
    }
    private void fetchTransactions(String selectedDate) {
        Log.d("CalendarActivity", "Selected date: " + selectedDate);

        String userId = firebaseAuth.getCurrentUser().getUid();
        firestore.collection("Expenses").document(userId).collection("Notes")
                .whereEqualTo("date", selectedDate)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            transactions.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                TransactionModel model = document.toObject(TransactionModel.class);
                                transactions.add(model);
                            }
                            transactionAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(CalendarActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        Log.d("CalendarActivity", "fetchTransactions method called."); // Log that the method is being called
    }


}
