package com.example.artem.chatapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Artem on 20.02.2018.
 */

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private List<Messages> mMessageList;
    private FirebaseAuth mAuth;
    private DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference().child("messages");
    //private Context context;

    private String name="",image="";
    String currentUserId;

    private loadImageInterface imageInterface;

    public MessageAdapter(List<Messages> mMessageList,loadImageInterface ImageInterface,String[] data) {
        //this.context = context;
        imageInterface = ImageInterface;
        this.mMessageList = mMessageList;
        this.image = data[0];
        this.name = data[1];

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
    }


    @Override
    public int getItemViewType(int position) {

        if(!mMessageList.get(position).getFrom().equals(currentUserId))
            return 0;
        else return 1;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

//        mAuth = FirebaseAuth.getInstance();
//        currentUserId = mAuth.getCurrentUser().getUid();
        final Messages c = mMessageList.get(position);

        String fromUser = c.getFrom();
        String messageType = c.getType();

        DateFormat df = new SimpleDateFormat("MMM d ''yy 'at' HH:mm", Locale.US);
        String messageTime = df.format(c.getTime());

        if(holder.getItemViewType() == 0){

            final MessageViewHolder messageViewHolder = (MessageViewHolder) holder;
            messageViewHolder.nameText.setText(name);
            messageViewHolder.timeText.setText(messageTime);
            loadImageInto(messageViewHolder.userPic.getContext(),image, messageViewHolder.userPic);

            if(messageType.equals("text")) {
                messageViewHolder.messageText.setVisibility(View.VISIBLE);
                messageViewHolder.cardView.setVisibility(View.GONE);
                messageViewHolder.messageText.setText(c.getMessage());
            }
            else {
                messageViewHolder.messageText.setVisibility(View.GONE);
                messageViewHolder.cardView.setVisibility(View.VISIBLE);

                messageViewHolder.messageImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        imageInterface.loadActivity(c.getMessage());
//                        Intent fullScreenIntent = new Intent(messageViewHolder.messageImage.getContext(),FullScreenImageActivity.class);
//                        fullScreenIntent.putExtra("imageUri",c.getMessage());
//                        start

                    }
                });

                loadImageInto(messageViewHolder.messageImage.getContext(),c.getMessage(),messageViewHolder.messageImage);
            }

        } else {

            final MessageViewHolderUser messageViewHolderUser = (MessageViewHolderUser) holder;
            messageViewHolderUser.timeText.setText(messageTime);

            if(messageType.equals("text")) {
                messageViewHolderUser.messageText.setVisibility(View.VISIBLE);
                messageViewHolderUser.cardView.setVisibility(View.GONE);
                messageViewHolderUser.messageText.setText(c.getMessage());
            }
            else {
                messageViewHolderUser.messageText.setVisibility(View.GONE);
                messageViewHolderUser.cardView.setVisibility(View.VISIBLE);

                messageViewHolderUser.messageImage.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        imageInterface.loadActivity(c.getMessage());

                    }

                });

                messageViewHolderUser.messageImage.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        CharSequence options[] = new CharSequence[]{"Download image","Close"};

                        final AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolderUser.messageImage.getContext());

                        builder.setTitle("Select option");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                if(i==0){

                                    Toast.makeText(messageViewHolderUser.messageImage.getContext(),"Start loading...",Toast.LENGTH_LONG).show();

                                    BitmapDrawable draw = (BitmapDrawable) messageViewHolderUser.messageImage.getDrawable();
                                    Bitmap bitmap = draw.getBitmap();

                                    FileOutputStream outStream = null;
                                    File storageLoc = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                                    String fileName = String.format(Locale.US,"%d.jpg", System.currentTimeMillis());
                                    File outFile = new File(storageLoc, fileName);
                                    try {

                                        outStream = new FileOutputStream(outFile);
                                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                                        outStream.flush();
                                        outStream.close();

                                        imageInterface.loadGalleryImage(outFile);

                                        Toast.makeText(messageViewHolderUser.messageImage.getContext(),"Loaded",Toast.LENGTH_LONG).show();

                                    } catch (IOException e){
                                        e.printStackTrace();
                                    }


                                    Log.i("InfoApp","Nice");
                                }


                            }
                        });
                        builder.show();

                        return true;
                    }
                });



                loadImageInto(messageViewHolderUser.messageImage.getContext(),c.getMessage(),messageViewHolderUser.messageImage);
            }

        }

//        //loadImageInto(c.get);
//        //holder.timeText.setText(""+c.getTime());

    }


    @Override
    public int getItemCount() {
        return mMessageList.size();
    }



    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){

        if(viewType == 0) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.message_single_layout, parent, false);

            return new MessageViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.message_single_user_layout, parent, false);

            return new MessageViewHolderUser(v);
        }
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder{

        TextView messageText,timeText,nameText;
        CircleImageView userPic;
        ImageView messageImage;
        CardView cardView;

        MessageViewHolder(View view) {
            super(view);

            messageText = view.findViewById(R.id.message_single_user_text);
            userPic = view.findViewById(R.id.message_single_user_image);
            messageImage = view.findViewById(R.id.message_single_image_send);
            nameText = view.findViewById(R.id.message_single_name);
            timeText = view.findViewById(R.id.message_single_time);
            cardView = view.findViewById(R.id.message_single_image_send_View);
        }
    }

    public static class MessageViewHolderUser extends RecyclerView.ViewHolder{

        TextView messageText,timeText;
        ImageView messageImage;
        CardView cardView;

        MessageViewHolderUser(View view) {
            super(view);

            messageText = view.findViewById(R.id.message_single_user_text);
            messageImage = view.findViewById(R.id.message_single_user_image);
            timeText = view.findViewById(R.id.message_single_user_time);
            cardView = view.findViewById(R.id.message_single_user_imageView);
        }
    }

    public interface loadImageInterface {

        void loadActivity(String image);
        void loadGalleryImage(File file);

    }


    public void loadImageInto(Context context,String image, ImageView circleImageView){

        //Picasso.with(context).load(image).placeholder(R.drawable.default_user).into(circleImageView);

        Log.i("InfoApp","Image is loading");

        Glide.with(context)
                .load(image)
                .apply(new RequestOptions()
                        .placeholder(R.drawable.default_user))
                .into(circleImageView);

    }

}
