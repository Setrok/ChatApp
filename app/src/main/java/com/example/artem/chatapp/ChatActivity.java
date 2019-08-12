package com.example.artem.chatapp;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity implements MessageAdapter.loadImageInterface{

    String chatUserName;
    String chatUserId;
    String chatUserPic;
    private Toolbar mChatToolbar;
    private ProgressBar chatProgressBar;
    private LinearLayout chatInputLayout;

    private TextView nameView,lastSeenView;
    private EditText messageView;
    private CircleImageView profileImage;
    private ImageView onlinePic;
    private ImageButton sendBtn,addBtn;
    private RecyclerView mMessagesList;
    private SwipeRefreshLayout swipeLayout;

    private DatabaseReference rootRef;
    private StorageReference mImageStorage;
    private FirebaseUser currentUser;
    private final List<Messages> messageList = new ArrayList<>();

    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;
    private static final int TOTAL_ITEMS_TO_LOAD = 15;

    private int mCurrentPage = 0;
    //private static String chatName,currentName;

    private int itemPos = 0;
    private boolean loaded = false;

    int messageIncrement = 5;
    private String lastKey = "",previousKey="prev";

    private static final int GALLERY_PICK = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);

        if(savedInstanceState!=null){
            mCurrentPage = savedInstanceState.getInt("messages");
        }
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if(currentUser == null)
            setToStart();

        mImageStorage = FirebaseStorage.getInstance().getReference();

        mChatToolbar = findViewById(R.id.chat_app_bar);

        setSupportActionBar(mChatToolbar);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        rootRef = FirebaseDatabase.getInstance().getReference();

        if(getIntent().hasExtra("user_id")) {
            chatUserId = getIntent().getStringExtra("user_id");
            chatUserName = getIntent().getStringExtra("user_name");
            chatUserPic = getIntent().getStringExtra("user_pic");
        }

        //actionBar.setTitle(chatUserName);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View acttion_bar_view = inflater.inflate(R.layout.chat_custom_bar,null);

        actionBar.setCustomView(acttion_bar_view);

        nameView = findViewById(R.id.custom_bar_name);
        lastSeenView = findViewById(R.id.custom_bar_lastSeen);
        profileImage = findViewById(R.id.chat_profile_Pic);
        onlinePic = findViewById(R.id.custom_onlinPic);
        onlinePic.setVisibility(View.INVISIBLE);

        messageView = findViewById(R.id.chat_messageView);
        sendBtn = findViewById(R.id.chat_sendBtn);
        addBtn = findViewById(R.id.chat_addBtn);

        mMessagesList = findViewById(R.id.chat_messagesList);
        swipeLayout = findViewById(R.id.chat_swipe_layout);

        chatInputLayout = findViewById(R.id.chat_input_layout);

        chatProgressBar = findViewById(R.id.chat_progressBar);
        chatProgressBar.setVisibility(View.GONE);

        mLinearLayout = new LinearLayoutManager(this);

        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);



        nameView.setText(chatUserName);

        rootRef.child("Users").child(chatUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String online = dataSnapshot.child("online").getValue().toString();

                String data[] = new String[2];
                data[0] = chatUserPic;
                data[1] = chatUserName;

                mAdapter = new MessageAdapter(messageList,ChatActivity.this,data);
                mMessagesList.setAdapter(mAdapter);
                //loadMessages();
                loadMessagesLast(true);

                lastSeenView.setText(online);
                if(online.equals("online")) {
                    onlinePic.setVisibility(View.VISIBLE);
                    lastSeenView.setText(online);
                } else {
                    onlinePic.setVisibility(View.INVISIBLE);
                    try {
                        String timeAgo = GetTimeAgo.getTimeAgo(Long.parseLong(online), getApplicationContext());
                        if(timeAgo!=null) {
                            String last_seen = "Last seen: " + timeAgo;
                            lastSeenView.setText(last_seen);
                        } else lastSeenView.setText(R.string.seen_recently);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

                loadImageInto(chatUserPic,profileImage);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if(currentUser!=null) {

            rootRef.child("Chat").child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if(!dataSnapshot.hasChild(chatUserId)){

                        Map chatAddMap = new HashMap();
                        chatAddMap.put("seen",false);
                        chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                        Map chatUserMap = new HashMap();
                        chatUserMap.put("Chat/" + currentUser.getUid() + "/" + chatUserId, chatAddMap);
                        chatUserMap.put("Chat/" + chatUserId + "/" + currentUser.getUid(),chatAddMap);
                        rootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                if(databaseError != null){

                                    Log.d("InfoApp","damn" + databaseError.getMessage().toString());

                                }

                            }
                        });

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendMessage();

            }
        });

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent,"SELECT IMAGE"),GALLERY_PICK);

            }
        });

        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                itemPos = 0;
                //messageList.clear();

                //loadMoreMessages();
                loadMessagesLast(false);

            }
        });

//        messageView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View view, boolean b) {
//                if(b)
//                    mMessagesList.smoothScrollToPosition(messageList.size()-1);
//            }
//        });

    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("messages",mCurrentPage);
        super.onSaveInstanceState(outState);
    }

    private void loadMoreMessages(){

        DatabaseReference messageRef = rootRef.child("messages").child(currentUser.getUid()).child(chatUserId);

        Query messageQuery = messageRef.orderByKey().endAt(lastKey).limitToLast(messageIncrement+1);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                if(dataSnapshot!= null) {
                    Log.i("InfoApp","last key is :" + lastKey + "   ----data is: " + dataSnapshot.getKey());
                    if(!lastKey.equals(dataSnapshot.getKey()) && !previousKey.equals(dataSnapshot.getKey()))
                    {
                        Messages message = dataSnapshot.getValue(Messages.class);

                        messageList.add(itemPos++, message);
                        mCurrentPage++;

                        if (itemPos == 1) {
                            previousKey = lastKey;
                            lastKey = dataSnapshot.getKey();
                            Log.i("InfoApp","prev key is :" + previousKey);
                        }

                        mAdapter.notifyDataSetChanged();

                        mLinearLayout.scrollToPositionWithOffset(itemPos, 0);
                        swipeLayout.setRefreshing(false);

                    } else {

                        swipeLayout.setRefreshing(false);

                    }


                } else {

                    swipeLayout.setRefreshing(false);

                }

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

    }

    private void loadMessages() {

        DatabaseReference messageRef = rootRef.child("messages").child(currentUser.getUid()).child(chatUserId);

        Log.i("InfoApp", "current page is: " + mCurrentPage);

        Query messageQuery = messageRef.limitToLast(mCurrentPage + TOTAL_ITEMS_TO_LOAD);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                if(dataSnapshot!= null) {
                    Messages message = dataSnapshot.getValue(Messages.class);

                    itemPos++;

                    if(itemPos == 1 ){
                        lastKey = dataSnapshot.getKey();
                        previousKey = dataSnapshot.getKey();
                    }


                    messageList.add(message);
                    mAdapter.notifyDataSetChanged();

                    mMessagesList.scrollToPosition(messageList.size() -1 + mCurrentPage);

                }

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

    }

    private void loadMessagesLast(final boolean firstLaunch){

        DatabaseReference messageRef = rootRef.child("messages").child(currentUser.getUid()).child(chatUserId);

        Query messageQuery;
        if(lastKey.equals(""))
            messageQuery = messageRef.limitToLast(TOTAL_ITEMS_TO_LOAD);
        else
            messageQuery = messageRef.orderByKey().endAt(lastKey).limitToLast(messageIncrement+1);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                if(dataSnapshot!=null){

                    Log.i("InfoApp", "-------------------------------Launched QUERRY-----------------------------= ");

                    Messages message = dataSnapshot.getValue(Messages.class);

                    if(firstLaunch){

                        messageList.add(message);

                        Log.i("InfoApp","first launch Item Pos = " +itemPos+" ------------message:" + message.getMessage());

                        itemPos++;

                        if(itemPos == TOTAL_ITEMS_TO_LOAD) {
                            Log.i("InfoApp","loaded is true now");
                            loaded=true;
                        }

                    } else {
                        if(itemPos<messageIncrement && !lastKey.equals(dataSnapshot.getKey()) && !previousKey.equals(dataSnapshot.getKey())) {
                            Log.i("InfoApp"," Item Pos = " +itemPos+" ------------message:" + message.getMessage());
                            messageList.add(itemPos++, message);
                        }
                        //else itemPos++;

                    }

                    if(itemPos == 1 ){
                        previousKey = lastKey;
                        lastKey = dataSnapshot.getKey();
                    }

                    mAdapter.notifyDataSetChanged();

                    if(firstLaunch)
                        mMessagesList.scrollToPosition(messageList.size() -1);
                    else
                        mLinearLayout.scrollToPositionWithOffset(itemPos, 0);

                    swipeLayout.setRefreshing(false);

                } else {
                    swipeLayout.setRefreshing(false);
                }

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

    }

//    public static String getChatName(){
//
//        return chatName;
//
//    }
//
//    public static String getCurrentUserName(){
//
//        return currentName;
//
//    }

    private void sendMessage() {

        String message = messageView.getText().toString();

        if(!TextUtils.isEmpty(message)){

            String currentUserRef = "messages/" + currentUser.getUid() + "/" + chatUserId;
            String chatUserRef = "messages/" + chatUserId + "/" + currentUser.getUid();

            DatabaseReference user_message_push = rootRef.child("messages").child(currentUser.getUid()).child(chatUserId).push();

            String pushID = user_message_push.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message",message);
            messageMap.put("seen",false);
            messageMap.put("type","text");
            messageMap.put("time",ServerValue.TIMESTAMP);
            messageMap.put("from",currentUser.getUid());

            Map messageUserMap = new HashMap();
            messageUserMap.put(currentUserRef + "/" + pushID,messageMap);
            messageUserMap.put(chatUserRef + "/" + pushID,messageMap);

            messageView.setText("");

            rootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                    if(databaseError != null) {
                        Log.d("InfoApp", "message damn " + databaseError.getMessage().toString());
                    }else {
                        if(messageList.size()!=0) {
//                mMessagesList.smoothScrollToPosition(messageList.size() - 1);
//                mMessagesList.scrollToPosition(messageList.size()-1);
                            mLinearLayout.scrollToPositionWithOffset(messageList.size()-1,0);
                        }
                    }

                }
            });

        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if(currentUser == null)
            setToStart();

    }

    private void setToStart() {

        Intent startIntent = new Intent(ChatActivity.this,StartActivity.class);
        startActivity(startIntent);
        finish();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_PICK && resultCode ==RESULT_OK){

            showPB(true);

            if(data.getData()!=null) {
                Uri imageUri = data.getData();

                loadMessageImage(imageUri);

            } else {

                if(data.getClipData()!=null){
                    ClipData mClipData=data.getClipData();

                    for(int i=0;i<mClipData.getItemCount();i++){

                        ClipData.Item item = mClipData.getItemAt(i);
                        Uri uri = item.getUri();
                        loadMessageImage(uri);

                    }

                }

            }

        }
    }

    private void loadMessageImage(Uri imageUri) {

        final String current_user_ref = "messages/" + currentUser.getUid() + "/" + chatUserId;
        final String chat_user_ref = "messages/" + chatUserId + "/" + currentUser.getUid();

        DatabaseReference userMessagePush = rootRef.child("messages").child(currentUser.getUid()).child(chatUserId).push();

        final String push_id = userMessagePush.getKey();

        StorageReference filePath = mImageStorage.child("message_images").child(push_id + ".jpg");

        filePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                if (task.isSuccessful()) {

                    String downloadUrl = task.getResult().getDownloadUrl().toString();

                    Map messageMap = new HashMap();
                    messageMap.put("message", downloadUrl);
                    messageMap.put("seen", false);
                    messageMap.put("type", "image");
                    messageMap.put("time", ServerValue.TIMESTAMP);
                    messageMap.put("from", currentUser.getUid());

                    Map messageUserMap = new HashMap();
                    messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
                    messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

                    messageView.setText("");

                    rootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError != null) {
                                Log.d("InfoApp", databaseError.getMessage().toString());
                            }

                            showPB(false);

                        }
                    });

                } else{
                    showPB(false);
                }

            }
        });

    }

    private void showPB(boolean show){

        chatInputLayout.setEnabled(!show);

        if(show){

            chatInputLayout.setVisibility(View.INVISIBLE);
            chatProgressBar.setVisibility(View.VISIBLE);

        } else {

            chatInputLayout.setVisibility(View.VISIBLE);
            chatProgressBar.setVisibility(View.INVISIBLE);

        }

    }

    public void loadImageInto(String image, CircleImageView circleImageView){

        //Picasso.with(context).load(image).placeholder(R.drawable.default_user).into(circleImageView);

        Glide.with(getApplicationContext())
                .load(image)
                .apply(new RequestOptions()
                        .placeholder(R.drawable.default_user))
                .into(circleImageView);

    }


    @Override
    public void loadActivity(String image) {

        Log.i("InfoApp", "Interface called");

        Intent fullScreenIntent = new Intent(getApplicationContext(),FullScreenImageActivity.class);
        fullScreenIntent.putExtra("imageUri",image);
        startActivity(fullScreenIntent);

    }

    @Override
    public void loadGalleryImage(File file) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(file));
        sendBroadcast(intent);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }

    @Override
    public void finish() {
        super.finish();
        onLeaveThisActivity();
    }

    protected void onLeaveThisActivity() {
        overridePendingTransition(R.anim.slide_in_main, R.anim.slide_out_main);
    }
}
