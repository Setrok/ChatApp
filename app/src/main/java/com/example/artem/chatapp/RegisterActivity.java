package com.example.artem.chatapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout mDisplayName,mEmail,mPassword;
    private Button mCreateBtn;
    private ProgressBar progressBar;
    private android.support.v7.widget.Toolbar mToolbar;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    private static final String TAG = "Info";
    private static final String IMAGE_URI = "https://firebasestorage.googleapis.com/v0/b/chatapp-e013b.appspot.com/o/Profile_images%2Fdefault_user.png?alt=media&toke" +
            "n=dde1bf96-58f7-4769-9e81-c6f83699d5ff";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mToolbar = findViewById(R.id.reg_tool_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();

        progressBar = findViewById(R.id.reg_ProgressBar);
        progressBar.setVisibility(View.GONE);
        mDisplayName = findViewById(R.id.reg_displayName);
        mEmail = findViewById(R.id.reg_email);
        mPassword = findViewById(R.id.reg_password);
        mCreateBtn = findViewById(R.id.reg_createAccBtn);
        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                createUser();

            }
        });

    }


    private void createUser() {

        final String name = mDisplayName.getEditText().getText().toString().trim();
        String email = mEmail.getEditText().getText().toString().trim();
        String password = mPassword.getEditText().getText().toString();

        if(!TextUtils.isEmpty(name) || !TextUtils.isEmpty(email) || !TextUtils.isEmpty(password)){

            showProgressBar(true);

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information

                                Log.d(TAG, "createUserWithEmail:success");

                                FirebaseUser currennt_user = FirebaseAuth.getInstance().getCurrentUser();
                                if(currennt_user != null) {

                                    String uid = currennt_user.getUid();

                                    userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                                    String deviceToken = FirebaseInstanceId.getInstance().getToken();

                                    HashMap<String,String> userMap = new HashMap<>();

                                    userMap.put("device_token",deviceToken);
                                    userMap.put("name",name);
                                    userMap.put("image","default");
                                    userMap.put("status","Hi there");
                                    userMap.put("thumb_image","default");

                                    userRef.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                showProgressBar(false);
                                                Intent mainIntent = new Intent(RegisterActivity.this,MainActivity.class);
                                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(mainIntent);
                                                finish();

                                            }

                                        }
                                    });

                                }



//                                updateUI(user);
                            } else {
                                // If sign in fails, display a message to the user.
                                String error ="";
                                try{
                                    throw task.getException();
                                }
                                catch (FirebaseAuthWeakPasswordException e){

                                    error = "Weak Password";

                                }
                                catch (FirebaseAuthInvalidCredentialsException e){

                                    error = "Invalid email";

                                }
                                catch (FirebaseAuthUserCollisionException e){

                                    error = "User exists";

                                }
                                catch (Exception e) {
                                    e.printStackTrace();
                                }
                                Toast.makeText(getApplicationContext(),error,Toast.LENGTH_LONG).show();
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
//                                updateUI(null);
                                showProgressBar(false);
                            }

                            // ...
                        }
                    });

        }else {

            Toast.makeText(RegisterActivity.this, "Empty Fields!",
                    Toast.LENGTH_SHORT).show();

        }

    }

    private void showProgressBar(boolean b) {

        mCreateBtn.setClickable(!b);

        Log.i("Info","PB is" + b);

        if(!b){

            progressBar.setVisibility(View.INVISIBLE);
            mCreateBtn.setVisibility(View.VISIBLE);

        }else{

            progressBar.setVisibility(View.VISIBLE);
            mCreateBtn.setVisibility(View.INVISIBLE);

        }


    }
}
