
//AddTransactionActivity
package com.example.loanexpensemanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.loanexpensemanager.databinding.ActivityAddTransactionBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class AddTransactionActivity extends AppCompatActivity {
    ActivityAddTransactionBinding binding;
    FirebaseFirestore fStore;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    String type="";
    private int totalExpense = 0;
    double budget = 0;

    FirebaseFirestore firebaseFirestore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityAddTransactionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
//firebase
        fStore=FirebaseFirestore.getInstance();
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseUser=firebaseAuth.getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();
        TextView totalBudgetTextView = binding.userBudget;

        String userId = firebaseAuth.getCurrentUser().getUid();
        DocumentReference userRef = firebaseFirestore.collection("users").document(userId);

        userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    budget = documentSnapshot.getDouble("budget");
                    totalBudgetTextView.setText(String.valueOf((int) budget));

                }
            }
        });
        binding.ImageCal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivity(new Intent(AddTransactionActivity.this, CalendarActivity.class));
                } catch (Exception e) {
                    return;
                }
            }
        });
        binding.expenseCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type="Expense";
                binding.expenseCheck.setChecked(true);
                binding.incomeCheck.setChecked(false);
            }
        });
        binding.incomeCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type="Loan";
                binding.expenseCheck.setChecked(false);
                binding.incomeCheck.setChecked(true);
            }
        });
        binding.btnAddTransaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String amount=binding.userAmountAdd.getText().toString().trim();
                String note=binding.userNoteAdd.getText().toString().trim();
                if (amount.length()<=0){
                    return;
                }
                int expenseAmount=Integer.parseInt(amount);
                int newTotalExpense=totalExpense+expenseAmount;
                totalExpense+=expenseAmount;
                if(newTotalExpense >budget){
                    Toast.makeText(AddTransactionActivity.this, "You have Exceeded your Limit spend", Toast.LENGTH_SHORT).show();
                    return;
                }
                totalExpense=newTotalExpense;


                if (type.length()<=0){
                    Toast.makeText(AddTransactionActivity.this, "Select transaction type", Toast.LENGTH_SHORT).show();
                }
                SimpleDateFormat sdf=new SimpleDateFormat("dd MM yyyy", Locale.getDefault());
                String currentDate=sdf.format(new Date());

                String id= UUID.randomUUID().toString();
                Map<String,Object>transaction=new HashMap<>();
                transaction.put("id",id);
                transaction.put("amount",amount);
                transaction.put("note",note);
                transaction.put("type",type);
                transaction.put("date",currentDate);

                fStore.collection("Expenses").document(firebaseAuth.getUid()).collection("Notes").document(id).set(transaction)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(AddTransactionActivity.this, "Added", Toast.LENGTH_SHORT).show();
                                binding.userNoteAdd.setText("");
                                binding.userAmountAdd.setText("");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(AddTransactionActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

            }
        });
    }
}





