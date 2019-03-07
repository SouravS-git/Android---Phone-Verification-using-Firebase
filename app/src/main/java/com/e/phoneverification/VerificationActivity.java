package com.e.phoneverification;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import java.util.concurrent.TimeUnit;

public class VerificationActivity extends AppCompatActivity {

    EditText verificationCodeInput;
    Button signInBtn;
    ProgressBar progressBar;

    String phoneNumber, codeFromUser, mVerificationId;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    FirebaseAuth mAuth;

    Snackbar mySnackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        verificationCodeInput = findViewById(R.id.verificationCode);
        signInBtn = findViewById(R.id.signInButton);
        progressBar = findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        phoneNumber = intent.getStringExtra("phoneNumber");

        sendVerificationCode(phoneNumber);

        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                codeFromUser = verificationCodeInput.getText().toString();
                verifyCode(codeFromUser);
            }
        });
    }

    public void prepareCallBack(){
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                Log.i("onVerificationCompleted", "onVerificationCompleted:" + credential);

                String code = credential.getSmsCode();
                if (code != null){
                    verificationCodeInput.setText(code);
                    verifyCode(code);
                }
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Log.i("onVerificationFailed", "onVerificationFailed", e);

                progressBar.setVisibility(View.INVISIBLE);

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    prepareSnackbar(R.id.constraintLayout, "Invalid Credentials", true);
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    prepareSnackbar(R.id.constraintLayout, "The SMS quota for the project has been exceeded", true);
                } else if (e instanceof FirebaseNetworkException){
                    prepareSnackbar(R.id.constraintLayout, "Network Error", true);
                }
            }

            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                Log.i("onCodeSent", "onCodeSent:" + verificationId);
                prepareSnackbar(R.id.constraintLayout, "Verification Code Sent", false);
                signInBtn.setEnabled(true);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                //mResendToken = token;
            }
        };
    }

    public void sendVerificationCode(String phoneNumber){
        signInBtn.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        prepareCallBack();

        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber, 60, TimeUnit.SECONDS, this, mCallbacks);
    }

    public void verifyCode(String codeFromUser){
        if(codeFromUser.isEmpty() || codeFromUser.length() != 6){
            verificationCodeInput.setError("Enter valid code");
            verificationCodeInput.requestFocus();
        } else {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, codeFromUser);

            signInWithPhoneAuthCredential(credential);
        }
    }

    public void signInWithPhoneAuthCredential(PhoneAuthCredential phoneAuthCredential){
        mAuth.signInWithCredential(phoneAuthCredential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Intent intent = new Intent(VerificationActivity.this, ProfileActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }else {
                            progressBar.setVisibility(View.INVISIBLE);
                            prepareSnackbar(R.id.constraintLayout, task.getException().getMessage(), true);
                        }
                    }
                });
    }

    public void prepareSnackbar(int view, String message, Boolean action){

        mySnackbar = Snackbar.make(findViewById(view), message, Snackbar.LENGTH_LONG);

        if(action.equals(true)){
            mySnackbar.setDuration(Snackbar.LENGTH_INDEFINITE);
            mySnackbar.setAction("Resend", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendVerificationCode(phoneNumber);
                }
            });
        }

        mySnackbar.show();
    }
}