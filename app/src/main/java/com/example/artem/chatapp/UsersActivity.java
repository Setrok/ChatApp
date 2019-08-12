package com.example.artem.chatapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {

    private Toolbar mToolBar;
    private RecyclerView mUsersList;

    private DatabaseReference rootRef;
    private Query query;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        rootRef = FirebaseDatabase.getInstance().getReference().child("Users");
        rootRef.keepSynced(true);
        mToolBar = findViewById(R.id.allusers_appBar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUsersList = findViewById(R.id.users_recyclerView);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));



    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser == null) {
            setToStart();
            finish();
        }

        //rootRef.child(currentUser.getUid()).child("online").setValue(true);

        query = rootRef
                .limitToLast(50)
                .orderByChild("name");

        FirebaseRecyclerOptions<Users> options =
                new FirebaseRecyclerOptions.Builder<Users>()
                        .setQuery(query, Users.class)
                        .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(options) {
            @Override
            protected void onBindViewHolder(final UsersViewHolder holder, int position, Users model) {

                holder.setName(model.getName());
                holder.setStatus(model.getStatus());
                if(!model.getImage().equals("default"))
                    //if(isNetworkAvailable())
                holder.setProfileImage(model.getThumb_image(),getApplicationContext());
                //else holder.setProfileImage("default",getApplicationContext());

                final String user_id = getRef(position).getKey();


                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        holder.user_pic.setDrawingCacheEnabled(true);
                        Bitmap bmp = holder.user_pic.getDrawingCache();

                        //Toast.makeText(getApplicationContext(),"Opened",Toast.LENGTH_LONG).show();

                        Intent profileIntent = new Intent(UsersActivity.this,ProfileActivity.class);
                        profileIntent.putExtra("uid",user_id);
                        profileIntent.putExtra("img",bmp);
                        startActivity(profileIntent);

                    }
                });

            }

            @Override
            public UsersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_list_layout, parent, false);
                return new UsersViewHolder(view);
            }
        };

        mUsersList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder{

        View mView;
        CircleImageView user_pic;

        public UsersViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
            user_pic = mView.findViewById(R.id.conv_image);
            mView.findViewById(R.id.user_single_online_img).setVisibility(View.INVISIBLE);

        }

        public void setName(String name){

            TextView nameField = mView.findViewById(R.id.conv_name);
            nameField.setText(name);

        }

        public void setStatus(String status){

            TextView statusField = mView.findViewById(R.id.user_single_status);
            statusField.setText(status);

        }

        public void setProfileImage(String image,Context context){

            user_pic = mView.findViewById(R.id.conv_image);
            //if(!image.equals("default"))
            loadImageInto(context,image,user_pic);
            //else profileImageView.setImageResource(R.drawable.default_user);
            //profileImageView.setText(status);

        }

    }

    public static void loadImageInto(Context context, String image, CircleImageView circleImageView){

        //Picasso.with(context).load(image).placeholder(R.drawable.default_user).into(circleImageView);

        Glide.with(context)
                .load(image)
                .apply(new RequestOptions()
                        .placeholder(R.drawable.default_user))
                .into(circleImageView);

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


//    @Override
//    protected void onPause() {
//        super.onPause();
//
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        rootRef.child(user.getUid()).child("online").setValue(false);
//
//    }

    private void setToStart() {

        Intent startIntent = new Intent(UsersActivity.this,StartActivity.class);
        startActivity(startIntent);
        finish();

    }

}
