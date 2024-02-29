package com.example.loanexpensemanager;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class literacy extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_literacy);

        TextView article1TitleTextView = findViewById(R.id.article1TitleTextView);
        TextView article1ContentTextView = findViewById(R.id.article1ContentTextView);
        article1TitleTextView.setText("FIDO Micro Credit");
        article1ContentTextView.setText("How can I get Fido app on my iPhone? Is Fido app on Apple app store? In this article I will answer…..");



    }

    public void openExternalLink(View view) {
        // Handle the click event for the external link
        String url = "https://www.loans.info.ke/2023/07/fido-loan-app-download-for-iphone.html"; // Replace with the actual external link
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
    public void openExternalLink2(View view) {
        // Handle the click event for the external link
        String url = "https://www.loans.info.ke/2023/07/faraja-loan-how-to-access-safaricom-new.html"; // Replace with the actual external link
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
    public void openExternalLink3(View view) {
        // Handle the click event for the external link
        String url = "https://www.loans.info.ke/2023/07/care-finance-loan-app.html"; // Replace with the actual external link
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    public void openExternalLink4(View view) {
        // Handle the click event for the external link
        String url = "https://www.loans.info.ke/2020/07/6-best-loan-apps-for-iphone-users.html"; // Replace with the actual external link
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
    public void openExternalLink10(View view) {
        // Handle the click event for the external link
        String url = "https://nomoredebts.org/blog/manage-money-better/steps-for-effective-personal-finance-management-and-planning"; // Replace with the actual external link
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
    public void openExternalLink11(View view) {
        // Handle the click event for the external link
        String url = "https://zedthefinancialist.wordpress.com/2021/10/20/money-problems-heres-how-financial-therapy-might-help/"; // Replace with the actual external link
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

}
