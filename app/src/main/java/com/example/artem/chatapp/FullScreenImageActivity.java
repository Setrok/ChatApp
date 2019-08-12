package com.example.artem.chatapp;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

public class FullScreenImageActivity extends AppCompatActivity {

    ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        String imageUri;

        if(getIntent().hasExtra("imageUri")) {

            imageUri = getIntent().getStringExtra("imageUri");

            image = findViewById(R.id.fullScreen_imageView);

            loadImageInto(getApplicationContext(),imageUri,image);

        } else {
            onBackPressed();
            finish();
        }

    }

    public void loadImageInto(Context context, String image, ImageView circleImageView){

        //Picasso.with(context).load(image).placeholder(R.drawable.default_user).into(circleImageView);

        Log.i("InfoApp","Image is loading");

        Glide.with(context)
                .load(image)
                .apply(new RequestOptions()
                        .placeholder(R.drawable.default_user))
                .into(circleImageView);

    }

}
