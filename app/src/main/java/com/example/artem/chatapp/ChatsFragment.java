package com.example.artem.chatapp;


import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.google.firebase.database.ChildEventListener;
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
public class ChatsFragment extends Fragment {

    private RecyclerView mConvList;

    private DatabaseReference mConvDatabase;
    private DatabaseReference mMessageDatabase;
    private DatabaseReference mUsersDatabase;
    private FirebaseAuth mAuth;

    private String current_user_id;

    private View mainView;

    private boolean loaded;

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        mainView = inflater.inflate(R.layout.fragment_chats,container,false);

        mConvList = mainView.findViewById(R.id.conversation_list);
        mAuth = FirebaseAuth.getInstance();

        current_user_id = mAuth.getCurrentUser().getUid();

        mConvDatabase = FirebaseDatabase.getInstance().getReference().child("Chat").child(current_user_id);
        mConvDatabase.keepSynced(true);

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mMessageDatabase = FirebaseDatabase.getInstance().getReference().child("messages").child(current_user_id);
        mUsersDatabase.keepSynced(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        mConvList.setHasFixedSize(true);
        mConvList.setLayoutManager(linearLayoutManager);

        return mainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        loaded = false;

        Query conversationQuery = mConvDatabase.orderByChild("timestamp");

        FirebaseRecyclerOptions<Conv> options =
                new FirebaseRecyclerOptions.Builder<Conv>()
                        .setQuery(conversationQuery, Conv.class)
                        .build();

        FirebaseRecyclerAdapter firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Conv,ConvHolder>(options) {
            @Override
            protected void onBindViewHolder(final ConvHolder holder, int position, final Conv conv) {

                final String list_user_id = getRef(position).getKey();

                Query lastMessageQuery = mMessageDatabase.child(list_user_id).limitToLast(1);

                lastMessageQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                        String data = dataSnapshot.child("message").getValue().toString();
                        holder.setMessage(data,conv.isSeen());

                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                mUsersDatabase.child(list_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                            final String userName = dataSnapshot.child("name").getValue().toString();
                            final String userPic = dataSnapshot.child("thumb_image").getValue().toString();
                            holder.setName(userName);

                            if(null!= getContext())
                                loadImageInto(getContext(), userPic, holder.profile_image);

                            holder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                    chatIntent.putExtra("user_id", list_user_id);
                                    chatIntent.putExtra("user_name", userName);
                                    chatIntent.putExtra("user_pic", userPic);
                                    startActivity(chatIntent);

                                }
                            });


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {


                        String online = dataSnapshot.child("online").getValue().toString();

                        if(online.equals("online")){
                            holder.setOnline(true);
                        } else holder.setOnline(false);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public ConvHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.conv_layout, parent, false);
                return new ConvHolder(view);
            }
        };
        mConvList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

    }

    public static class ConvHolder extends RecyclerView.ViewHolder{

        View mView;
        CircleImageView profile_image;
        //TextView name,message;

        public ConvHolder(View itemView) {
            super(itemView);

            mView = itemView;
            profile_image = mView.findViewById(R.id.conv_image);
//            name = mView.findViewById(R.id.conv_name);
//            message = mView.findViewById(R.id.conv_lastMessage);

        }

        public void setName(String name){

            TextView userNameView = mView.findViewById(R.id.conv_name);
            userNameView.setText(name);

        }

        public void setMessage(String message,boolean seen){

            TextView lastMessage = mView.findViewById(R.id.conv_lastMessage);
            lastMessage.setText(message);

            if(!seen){
                lastMessage.setTypeface(null, Typeface.BOLD);
            }

        }

        public void setOnline(boolean online){

            ImageView onlinePic = mView.findViewById(R.id.conv_onlinePic);
            if(online)
                onlinePic.setVisibility(View.VISIBLE);
            else
                onlinePic.setVisibility(View.GONE);

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

}
