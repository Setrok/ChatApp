package com.example.artem.chatapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    private static final int MAX_LENGTH = 32;
    private TextView profileName,profileStatus;
    private CircleImageView profileImage;
    private Button changeStatusBtn,changeImageBtn;
    private ProgressBar progressBar;

    private DatabaseReference userDatabase;
    private FirebaseUser currentUser;
    private StorageReference mImageStorage;

    private static final int GALLERY_INTENT_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        profileName = findViewById(R.id.settings_name);
        profileStatus = findViewById(R.id.settings_status);
        profileImage = findViewById(R.id.settings_profile_Img);
        progressBar = findViewById(R.id.settings_progressBar);


        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        mImageStorage = FirebaseStorage.getInstance().getReference();

        String uid = currentUser.getUid();

        userDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        userDatabase.keepSynced(true);

        userDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                try {

                    String name = dataSnapshot.child("name").getValue().toString();
                    String status = dataSnapshot.child("status").getValue().toString();
                    String image = dataSnapshot.child("image").getValue().toString();
                    String thumbImage = dataSnapshot.child("thumb_image").getValue().toString();

                    profileName.setText(name);
                    profileStatus.setText(status);
                    if(!image.equals("default")) {

                        //Picasso.with(SettingsActivity.this).load(image).placeholder(R.drawable.default_user).into(profileImage);
                        Glide.with(getApplicationContext())
                                .load(image)
                                .apply(new RequestOptions()
                                        .placeholder(R.drawable.default_user))
                                .into(profileImage);
                                //.preload().onLoadStarted(getResources().getDrawable( R.drawable.default_user))


                    }
                }catch (Exception e){

                    e.printStackTrace();

                }

            }



            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        changeStatusBtn = findViewById(R.id.change_statusBtn);
        changeStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String sendStatus = profileStatus.getText().toString();

                Intent statusIntent  = new Intent(SettingsActivity.this,StatusActivity.class);
                statusIntent.putExtra("Status",sendStatus);
                startActivity(statusIntent);

            }
        });

        changeImageBtn = findViewById(R.id.change_imageBtn);
        showProgressBar(false);
        changeImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent,"SELECT IAMGE"),GALLERY_INTENT_CODE);
                //startActivityForResult(galleryIntent,GALLERY_INTENT_CODE);


            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == GALLERY_INTENT_CODE && resultCode ==RESULT_OK) {

            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setAspectRatio(1,1)
                    .setMinCropWindowSize(400,400)
                    .start(this);
//            Toast.makeText(SettingsActivity.this,imageUri,Toast.LENGTH_LONG).show();
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                showProgressBar(true);

                final Uri resultUri = result.getUri();

                File thumb_filePath = new File(resultUri.getPath());

                String current_user_id = currentUser.getUid();

                StorageReference filePath = mImageStorage.child("Profile_images").child(current_user_id+".jpg");

                try {
                    Bitmap thumb_bitmap = new Compressor(this)
                            .setMaxHeight(200)
                            .setMaxWidth(200)
                            .setQuality(75)
                            .compressToBitmap(thumb_filePath);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    final byte[] thumb_byte = baos.toByteArray();
                    final StorageReference thumb_filePathRef = mImageStorage.child("Profile_images").child("thumbs").child(current_user_id+".jpg");

                    filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                            if(task.isSuccessful()){

                                final String downloadUrl = task.getResult().getDownloadUrl().toString();

                                UploadTask thumbTask = thumb_filePathRef.putBytes(thumb_byte);
                                thumbTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {

                                        String thumb_download_url = thumb_task.getResult().getDownloadUrl().toString();

                                        if(thumb_task.isSuccessful()){

                                            Map updateHashMap = new HashMap();
                                            updateHashMap.put("image",downloadUrl);
                                            updateHashMap.put("thumb_image",thumb_download_url);

                                            userDatabase.updateChildren(updateHashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    //profileImage.setImageURI(resultUri);
                                                    showProgressBar(false);

                                                }
                                            });

                                        } else {

                                            Toast.makeText(SettingsActivity.this,"Failed loading a ThumbNail",Toast.LENGTH_LONG).show();
                                            showProgressBar(false);

                                        }

                                    }
                                });



                            } else {

                                Toast.makeText(SettingsActivity.this,"Failed loading an Image",Toast.LENGTH_LONG).show();
                                showProgressBar(false);

                            }

                        }
                    });


                } catch (IOException e) {
                    e.printStackTrace();
                }
                catch (Exception e){
                    e.printStackTrace();
                }



            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                showProgressBar(false);
            }
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
//            userDatabase.child("online").setValue(true);
//
//        }

    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//
//        userDatabase.child("online").setValue(false);
//
//    }

    private void setToStart() {

        Intent startIntent = new Intent(SettingsActivity.this,StartActivity.class);
        startActivity(startIntent);
        finish();

    }

    private void showProgressBar(boolean b) {

        changeImageBtn.setClickable(!b);

        if(!b){

            progressBar.setVisibility(View.INVISIBLE);
            changeImageBtn.setVisibility(View.VISIBLE);

        }else{

            progressBar.setVisibility(View.VISIBLE);
            changeImageBtn.setVisibility(View.INVISIBLE);

        }


    }



}
