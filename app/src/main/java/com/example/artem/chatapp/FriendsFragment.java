package com.example.artem.chatapp;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    private RecyclerView mFriendsList;

    private String mCurrent_user_id;

    private View mMainView;

    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mUsersDatabase;
    private FirebaseAuth mAuth;
    private Query query;

    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;
    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        mMainView = inflater.inflate(R.layout.fragment_friends, container, false);

        mFriendsList = mMainView.findViewById(R.id.friends_List);
        mAuth = FirebaseAuth.getInstance();

        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrent_user_id);
        mFriendsDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mFriendsDatabase.keepSynced(false);

        mFriendsList.setHasFixedSize(true);
        mFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return mMainView;
    }

    @Override
    public void onStart() {

        super.onStart();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null){
            Intent registerIntent = new Intent(getContext(),StatusActivity.class);
            startActivity(registerIntent);
        } else {

            query = mFriendsDatabase
                    .limitToLast(50)
                    .orderByChild("date");

            FirebaseRecyclerOptions<Friends> options =
                    new FirebaseRecyclerOptions.Builder<Friends>()
                            .setQuery(query, Friends.class)
                            .build();

            firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Friends,FriendsViewHolder>(options) {
                @Override
                protected void onBindViewHolder(final FriendsViewHolder holder, final int position, final Friends model) {

                    //holder.setName(model.getDate());

                    final String list_user_id = getRef(position).getKey();

                    mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            try {
                                final String userName = dataSnapshot.child("name").getValue().toString();
                                String userStatus = dataSnapshot.child("status").getValue().toString();
                                final String thumbImage = dataSnapshot.child("thumb_image").getValue().toString();
                                String isOnline = dataSnapshot.child("online").getValue().toString();

                                holder.setName(userName);
                                holder.setStatus(userStatus);
                                loadImageInto(getContext(), thumbImage, holder.profile_image);

                                Log.i("AppInfo",position + " " +isOnline);

                                holder.setOnlinePic(isOnline);

                                holder.profile_image.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        //Toast.makeText(getContext(),"Hi" + v.getId(),Toast.LENGTH_LONG).show();
                                        holder.profile_image.setDrawingCacheEnabled(true);
                                        Bitmap bmp = holder.profile_image.getDrawingCache();

                                        Intent profileIntent = new Intent(getContext(),ProfileActivity.class);
                                        profileIntent.putExtra("uid",list_user_id);
                                        profileIntent.putExtra("img",bmp);
                                        startActivity(profileIntent);

                                    }
                                });

                                holder.mView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        Intent chatIntent = new Intent(getContext(),ChatActivity.class);
                                        chatIntent.putExtra("user_id",list_user_id);
                                        chatIntent.putExtra("user_name",userName);
                                        chatIntent.putExtra("user_pic",thumbImage);
                                        startActivity(chatIntent);

                                    }
                                });

                            } catch (Exception e) {
                                Log.i("AppInfo", position + " Error happened");
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }

                @Override
                public FriendsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.users_list_layout, parent, false);
                    return new FriendsViewHolder(view);
                }
            };

            mFriendsList.setAdapter(firebaseRecyclerAdapter);
            firebaseRecyclerAdapter.startListening();

        }

    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder{

        View mView;
        CircleImageView profile_image;

        public FriendsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
            profile_image = mView.findViewById(R.id.conv_image);

        }

        public void setName(String name){

            TextView userNameView = mView.findViewById(R.id.conv_name);
            userNameView.setText(name);

        }

        public void setStatus(String status){

            TextView userStatusView = mView.findViewById(R.id.user_single_status);
            userStatusView.setText(status);

        }

        public void setOnlinePic(String online_status) {

            ImageView onlinePic = mView.findViewById(R.id.user_single_online_img);
            if(online_status.equals("online"))
                onlinePic.setVisibility(View.VISIBLE);
            else
                onlinePic.setVisibility(View.INVISIBLE);

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
                    = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }catch (Exception e){
            return  false;
        }
    }

}
