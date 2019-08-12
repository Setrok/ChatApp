package com.example.artem.chatapp;

import android.app.Application;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class ChatApp extends Application {

    private DatabaseReference mUserDatabse;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {

            mUserDatabse = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid());

            mUserDatabse.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot != null) {

                        Date date = new Date();
                        DateFormat df = new SimpleDateFormat("MMM d ''yy 'at' HH:mm a", Locale.US);
                        //String currentTime = "posted "+ df.format(date);

                        mUserDatabse.child("online").onDisconnect().setValue(ServerValue.TIMESTAMP);
                        Log.i("AppInfo","set to online");
                        mUserDatabse.child("online").setValue("online");

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }
}
