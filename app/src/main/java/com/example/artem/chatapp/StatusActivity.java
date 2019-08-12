package com.example.artem.chatapp;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private android.support.v7.widget.Toolbar mToolbar;
    private TextInputLayout statusView;
    private Button saveBtn;
    private ProgressBar progressBar;

    //Firebase
    private DatabaseReference statusDatabase;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        String statusText = getIntent().getStringExtra("Status");

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = currentUser.getUid();
        statusDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

        progressBar = findViewById(R.id.status_progressBar);

        statusView = findViewById(R.id.status_input);
        statusView.getEditText().setText(statusText);

        mToolbar = findViewById(R.id.status_appBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        saveBtn = findViewById(R.id.status_saveBtn);
        showProgressBar(false);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showProgressBar(true);
                //if (isNetworkAvailable()){

                        String status = statusView.getEditText().getText().toString();
                        statusDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()) {
                                Toast.makeText(StatusActivity.this, "Staus is Updated", Toast.LENGTH_SHORT).show();
                                showProgressBar(false);
                                Intent startIntent = new Intent(StatusActivity.this,SettingsActivity.class);
                                startActivity(startIntent);
                            } else {

                                Toast.makeText(StatusActivity.this, "Update is failed(", Toast.LENGTH_SHORT).show();

                            }

                        }
                    });
                }
//                else {
//
//                    showProgressBar(false);
//                    Toast.makeText(StatusActivity.this, "No internet Connection", Toast.LENGTH_SHORT).show();
//
//                }
//
//            }
        });


    }

    private boolean isNetworkAvailable() {
        try {
            ConnectivityManager connectivityManager
                    = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }catch (Exception e){
            return  false;
        }
    }

    private void showProgressBar(boolean b) {

        saveBtn.setClickable(!b);

        if(!b){

            progressBar.setVisibility(View.INVISIBLE);
            saveBtn.setVisibility(View.VISIBLE);

        }else{

            progressBar.setVisibility(View.VISIBLE);
            saveBtn.setVisibility(View.INVISIBLE);

        }


    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if(currentUser == null){

            setToStart();

        }
//        else {
//
//            statusDatabase.child("online").setValue(true);
//
//        }

    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//
//        statusDatabase.child("online").setValue(false);
//
//    }

    private void setToStart() {

        Intent startIntent = new Intent(StatusActivity.this,StartActivity.class);
        startActivity(startIntent);
        finish();

    }

}
