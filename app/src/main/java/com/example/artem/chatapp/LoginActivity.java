package com.example.artem.chatapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout mEmail,mPassword;
    private Button mLogBtn;
    private Toolbar mToolbar;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mToolbar = findViewById(R.id.log_tool_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Login");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        progressBar = findViewById(R.id.log_ProgressBar);
        progressBar.setVisibility(View.INVISIBLE);
        mEmail = findViewById(R.id.log_email);
        mPassword = findViewById(R.id.log_password);
        mLogBtn = findViewById(R.id.log_loginBtn);
        mLogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String uEmail = mEmail.getEditText().getText().toString().trim();
                String uPassword = mPassword.getEditText().getText().toString();

                if(!TextUtils.isEmpty(uEmail) || !TextUtils.isEmpty(uPassword)) {

                    showProgressBar(true);
                    loginUser(uEmail,uPassword);
                }

            }
        });

    }

    private void loginUser(String email, String password) {

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if(task.isSuccessful()){

                        showProgressBar(false);

                        String device_token = FirebaseInstanceId.getInstance().getToken();
                        String current_uid = mAuth.getCurrentUser().getUid();

                        mUserDatabase.child(current_uid).child("device_token").setValue(device_token).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                Intent mainIntent  = new Intent(LoginActivity.this,MainActivity.class);
                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(mainIntent);
                                finish();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(),"Failed Token",Toast.LENGTH_LONG).show();
                            }
                        });



                    } else {

                        String error ="";
                        try{
                            throw task.getException();
                        }
                        catch (FirebaseAuthInvalidCredentialsException e){

                            error = "Invalid email or password";

                        }
                        catch (FirebaseAuthInvalidUserException e){

                            error = "Invalid email or address";

                        }
                        catch (Exception e) {
                            e.printStackTrace();
                            error = "Unknown Error";
                        }
                        Toast.makeText(getApplicationContext(),error,Toast.LENGTH_LONG).show();
                        showProgressBar(false);

                    }

                }
            });

    }


    private void showProgressBar(boolean b) {

        mLogBtn.setClickable(!b);

        if(!b){

            progressBar.setVisibility(View.INVISIBLE);
            mLogBtn.setVisibility(View.VISIBLE);

        }else{

            progressBar.setVisibility(View.VISIBLE);
            mLogBtn.setVisibility(View.INVISIBLE);

        }


    }

}
