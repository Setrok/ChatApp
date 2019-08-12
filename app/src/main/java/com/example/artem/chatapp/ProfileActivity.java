package com.example.artem.chatapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private TextView mDisplayName,mDisplayStatus,mDisplayFriends,mDisplayMutualFriends;
    private ImageView profileImage;
    private Button mProfileSendReqBtn,mProfileDeclineBtn;
    private ProgressBar profileProgressBar;
    private ConstraintLayout profileLayout;

    private TableRow profileLoadLayout;

    private DatabaseReference rootRef;
    private DatabaseReference userRef;
    private DatabaseReference mFriendRequestDatabase;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mNotificationDatabase;
    private FirebaseUser mCurrent_user;

    private String current_state;

    private Bitmap image_bitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String uid = getIntent().getStringExtra("uid");

        rootRef = FirebaseDatabase.getInstance().getReference();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        userRef.keepSynced(true);
        mFriendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("friend_request");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mCurrent_user = FirebaseAuth.getInstance().getCurrentUser();
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");

        profileLayout = findViewById(R.id.profile_layout);

        profileLoadLayout = findViewById(R.id.profile_load_layout);

        mDisplayName = findViewById(R.id.profile_name);
        mDisplayStatus = findViewById(R.id.settings_status);
        mDisplayFriends = findViewById(R.id.profile_friends);
        profileImage = findViewById(R.id.profile_image);
        mProfileSendReqBtn = findViewById(R.id.profile_request_btn);
        mProfileDeclineBtn = findViewById(R.id.profile_decline_btn);

        mProfileDeclineBtn.setVisibility(View.INVISIBLE);
        mProfileDeclineBtn.setEnabled(false);

        profileProgressBar = findViewById(R.id.profile_progressBar);
        if(getIntent().hasExtra("img")) {
            image_bitmap = getIntent().getParcelableExtra("img");
        }
        else image_bitmap = BitmapFactory.decodeResource(getResources(),
                R.drawable.default_user);
        profileImage.setImageBitmap(image_bitmap);

        current_state = "not_friends";

        showProgressBar(true);

        mFriendDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                int mutualFriendsCounter = 0;

                Log.i("Data List","STARTED!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

                Log.i("Data List","Mutual Friends counter is: "+mutualFriendsCounter);

                String friend = "Friend";
                String friends = "Friends";

                int childrenCount = (int)dataSnapshot.child(uid).getChildrenCount();

                String friend_counter = childrenCount + " " + friendLine(childrenCount);
                String mutual_friends_counter;

                if(!uid.equals(mCurrent_user.getUid())) {

                    Iterable<DataSnapshot> dataList = dataSnapshot.child(uid).getChildren();

                    for(DataSnapshot d : dataList){
                        if(dataSnapshot.child(mCurrent_user.getUid()).hasChild(d.getKey()))
                            mutualFriendsCounter+=1;
                    }

                    mutual_friends_counter ="  |  " + mutualFriendsCounter + " mutual "+friendLine(mutualFriendsCounter);

                }
                else
                    mutual_friends_counter = "";

                mDisplayFriends.setText(friend_counter+ mutual_friends_counter);

//                if(dataSnapshot.child(uid).hasChild(mCurrent_user.getUid())) {
//
//                    areFriends = true;
//                    mProfileSendReqBtn.setEnabled(true);
//                    mProfileSendReqBtn.setText(R.string.profile_unfriend);
//                    mProfileDeclineBtn.setVisibility(View.INVISIBLE);
//                    mProfileDeclineBtn.setEnabled(false);
//                    mProfileSendReqBtn.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.profile_cancelBtn));
//                    current_state = "friends";
//                    showProgressBar(false);
//                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        mProfileSendReqBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {

                if(!isNetworkAvailable()){
                    Toast.makeText(getApplicationContext(),"No internet connection",Toast.LENGTH_LONG).show();
                    return;
                }

                if( !uid.equals(mCurrent_user.getUid())) {

                    mProfileSendReqBtn.setEnabled(false);
                    mProfileSendReqBtn.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.profile_disabled_btn));

                    switch (current_state) {
                        case "not_friends":

                            DatabaseReference notificationRef = rootRef.child("notifications").child(uid).push();
                            String newNotificationID = notificationRef.getKey();

                            HashMap<String, String> notificationData = new HashMap<>();
                            notificationData.put("from", mCurrent_user.getUid());
                            notificationData.put("type", "request");

                            Map requestMap = new HashMap();
                            requestMap.put("friend_request/" + mCurrent_user.getUid()+"/"+uid + "/request_type","sent");
                            requestMap.put("friend_request/" + uid + "/" + mCurrent_user.getUid()+ "/request_type","received");
                            requestMap.put("notifications/" + uid + "/" + newNotificationID,notificationData);

                            rootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                    if(databaseError != null){

                                        Toast.makeText(getApplicationContext(),"Database Error",Toast.LENGTH_SHORT).show();

                                    } else {

                                        mProfileSendReqBtn.setEnabled(true);
                                        mProfileSendReqBtn.setText(R.string.cancel_request);
                                        mProfileSendReqBtn.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.profile_cancelBtn));
                                        current_state = "req_sent";

                                    }

                                }
                            });

                            break;
                        case "req_sent":

                            mFriendRequestDatabase.child(mCurrent_user.getUid()).child(uid).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    mFriendRequestDatabase.child(uid).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            mProfileSendReqBtn.setEnabled(true);
                                            mProfileSendReqBtn.setText(R.string.send_friend_request);
                                            mProfileSendReqBtn.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.colorAccent));
                                            current_state = "not_friends";

                                        }
                                    });

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    Toast.makeText(ProfileActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();

                                    mProfileSendReqBtn.setEnabled(true);
                                    mProfileSendReqBtn.setText(R.string.cancel_request);
                                    mProfileSendReqBtn.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.colorAccent));
                                    current_state = "req_sent";

                                }
                            });


                            break;
                        case "req_received":

                            final String currentTime2 = DateFormat.getDateTimeInstance().format(new Date());
                            Date date = new Date();
                            DateFormat df = new SimpleDateFormat("MMM d ''yy 'at' HH:mm a", Locale.US);
                            String currentTime = "posted "+ df.format(date);

                            Map friendMap = new HashMap();
                            friendMap.put("Friends/" + mCurrent_user.getUid() + "/" + uid + "/date",currentTime);
                            friendMap.put("Friends/" + uid + "/" + mCurrent_user.getUid() + "/date",currentTime2);
                            friendMap.put("friend_request/" + mCurrent_user.getUid() + "/" + uid,null);
                            friendMap.put("friend_request/" + uid + "/" + mCurrent_user.getUid(),null);

                            rootRef.updateChildren(friendMap, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                    if(databaseError!=null){

                                        Toast.makeText(getApplicationContext(),"Database Error",Toast.LENGTH_SHORT).show();
                                        mProfileSendReqBtn.setEnabled(true);
                                        mProfileSendReqBtn.setText(R.string.accept_frind_request);
                                        mProfileSendReqBtn.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.profile_acceptBtn));
                                        mProfileDeclineBtn.setVisibility(View.VISIBLE);

                                    } else {

                                        mProfileSendReqBtn.setEnabled(true);
                                        mProfileSendReqBtn.setText(R.string.profile_unfriend);
                                        mProfileSendReqBtn.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.profile_cancelBtn));
                                        current_state = "friends";
                                        mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                                        mProfileDeclineBtn.setEnabled(false);

                                    }

                                }
                            });

                            break;
                        case "friends":

                            Map unfriendMap = new HashMap();
                            unfriendMap.put("Friends/" + mCurrent_user.getUid() + "/" + uid + "/date",null);
                            unfriendMap.put("Friends/" + uid + "/" + mCurrent_user.getUid() + "/date",null);

                            rootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                    if(databaseError!=null){

                                        Toast.makeText(getApplicationContext(),"Database Error",Toast.LENGTH_SHORT).show();

                                    } else {

                                        mProfileSendReqBtn.setEnabled(true);
                                        mProfileSendReqBtn.setText(R.string.send_friend_request);
                                        mProfileSendReqBtn.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.colorAccent));
                                        current_state = "not_friends";

                                    }

                                }
                            });

                            break;
                    }

                }
        }
        });

        mProfileDeclineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!isNetworkAvailable()){
                    Toast.makeText(getApplicationContext(),"No internet connection",Toast.LENGTH_LONG).show();
                    return;
                }

                Map friendMap = new HashMap();
                friendMap.put("friend_request/" + mCurrent_user.getUid() + "/" + uid,null);
                friendMap.put("friend_request/" + uid + "/" + mCurrent_user.getUid(),null);

                rootRef.updateChildren(friendMap, new DatabaseReference.CompletionListener() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                        if(databaseError!=null){

                            Toast.makeText(getApplicationContext(),"Database Error",Toast.LENGTH_SHORT).show();
                            mProfileSendReqBtn.setEnabled(true);
                            mProfileSendReqBtn.setText(R.string.accept_frind_request);
                            mProfileSendReqBtn.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.profile_acceptBtn));
                            mProfileDeclineBtn.setVisibility(View.VISIBLE);

                        } else {

                            mProfileSendReqBtn.setEnabled(true);
                            mProfileSendReqBtn.setText(R.string.send_friend_request);
                            mProfileSendReqBtn.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.colorAccent));
                            current_state = "not_friends";
                            mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                            mProfileDeclineBtn.setEnabled(false);

                        }

                    }
                });

            }
        });

    }

    private String friendLine(int childrenCount) {
        if(childrenCount!=1)
            return "Friends";
        else return "Friend";
    }

    @Override
    protected void onStart() {
        super.onStart();

        final String startUid = getIntent().getStringExtra("uid");

//        rootRef.child("Users").child(mCurrent_user.getUid()).child("online").setValue(true);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                mDisplayName.setText(name);
                mDisplayStatus.setText(status);
                if(!image.equals("default"))
                    loadImageInto(getApplicationContext(),image,profileImage);
                else profileImage.setImageResource(R.drawable.default_user);

                //----------------FRIEND LIST REQUEST FEATURE-------------------------------

                mFriendRequestDatabase.child(mCurrent_user.getUid()).addValueEventListener(new ValueEventListener() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(startUid )){

                            String request_type = dataSnapshot.child(startUid).child("request_type").getValue().toString();
                            if(request_type.equals("received")){

                                mProfileSendReqBtn.setEnabled(true);
                                mProfileSendReqBtn.setText(R.string.accept_frind_request);
                                mProfileDeclineBtn.setVisibility(View.VISIBLE);
                                mProfileDeclineBtn.setEnabled(true);
//                                mProfileSendReqBtn.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.profile_acceptBtn));
                                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
                                    mProfileSendReqBtn.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.profile_acceptBtn));
                                } else {
                                    mProfileSendReqBtn.setBackgroundColor(getApplicationContext().getResources().getColor(R.color.profile_acceptBtn));
                                }

                                current_state = "req_received";

                            } else if(request_type.equals("sent")){

                                current_state = "req_sent";
                                mProfileSendReqBtn.setText(R.string.cancel_request);
//                                mProfileSendReqBtn.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.profile_cancelBtn));
                                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
                                    mProfileSendReqBtn.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.profile_cancelBtn));
                                } else {
                                    mProfileSendReqBtn.setBackgroundColor(getApplicationContext().getResources().getColor(R.color.profile_cancelBtn));
                                }

                            }
                            showProgressBar(false);

                        }
                        else {

                            mFriendDatabase.child(mCurrent_user.getUid()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if(dataSnapshot.hasChild(startUid)){

                                        mProfileSendReqBtn.setEnabled(true);
                                        mProfileSendReqBtn.setText(R.string.profile_unfriend);
                                        mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                                        mProfileDeclineBtn.setEnabled(false);
                                        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
                                            mProfileSendReqBtn.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.profile_cancelBtn));
                                        } else {
                                            mProfileSendReqBtn.setBackgroundColor(getApplicationContext().getResources().getColor(R.color.profile_cancelBtn));
                                        }
//                                        mProfileSendReqBtn.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.profile_cancelBtn));
//                                        mProfileSendReqBtn.setBackgroundColor(getApplicationContext().getResources().getColor(R.color.profile_cancelBtn));
                                        current_state = "friends";
                                        showProgressBar(false);

                                    } else {

                                        mProfileSendReqBtn.setEnabled(true);
                                        mProfileSendReqBtn.setText(R.string.send_friend_request);
                                        mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                                        mProfileDeclineBtn.setEnabled(false);
                                        //mProfileSendReqBtn.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.colorAccent));
                                        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
                                            mProfileSendReqBtn.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.colorAccent));
                                        } else {
                                            mProfileSendReqBtn.setBackgroundColor(getApplicationContext().getResources().getColor(R.color.colorAccent));
                                        }
                                        current_state = "not_friends";
                                        showProgressBar(false);

                                    }

                                }
//
                                @Override
                                public void onCancelled(DatabaseError databaseError) {



                                }
                            });

                        }



                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                        showProgressBar(false);

                    }
                });


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                showProgressBar(false);

            }
        });

    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//
//        userRef.child("online").setValue(false);
//
//    }

    private void showProgressBar(boolean b) {

        profileLayout.setClickable(!b);

        if(!b){

            profileLoadLayout.setVisibility(View.GONE);
            profileLayout.setVisibility(View.VISIBLE);

        }else{

            profileLoadLayout.setVisibility(View.VISIBLE);
            profileLayout.setVisibility(View.INVISIBLE);

        }


    }

    public void loadImageInto(Context context, String image, ImageView imageView){

        Drawable d = new BitmapDrawable(getResources(), image_bitmap);
        //Picasso.with(context).load(image).placeholder(d).into(imageView);

        Glide.with(context)
                .load(image)
                .apply(new RequestOptions()
                        .placeholder(d))
                        //.placeholder(R.drawable.default_user))
                .into(imageView);

    }

    public boolean isNetworkAvailable() {
        try {
            ConnectivityManager connectivityManager
                    = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }catch (Exception e){
            return  false;
        }
    }

}
