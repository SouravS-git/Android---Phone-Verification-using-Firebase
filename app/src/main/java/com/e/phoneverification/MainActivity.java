package com.e.phoneverification;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private Button nextButton;
    private EditText phoneInput;
    private Spinner countryCodeSpinner;

    private String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nextButton = findViewById(R.id.signInButton);
        phoneInput = findViewById(R.id.phoneInput);
        countryCodeSpinner = findViewById(R.id.codeSelector);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String countryCode = countryCodeSpinner.getSelectedItem().toString();
                String number = phoneInput.getText().toString();

                if(number.isEmpty()) {
                    phoneInput.setError("Phone number can't be left as blank.");
                    phoneInput.requestFocus();

                    return;
                }

                else if(number.length() != 10) {
                    phoneInput.setError("Please enter a valid phone number.");
                    phoneInput.requestFocus();

                    return;
                }

                else {
                    phoneNumber = countryCode+number;
                    Intent intent = new Intent(MainActivity.this, VerificationActivity.class);
                    intent.putExtra("phoneNumber", phoneNumber);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                    return;
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }
}
