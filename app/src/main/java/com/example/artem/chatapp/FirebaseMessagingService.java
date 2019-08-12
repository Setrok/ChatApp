package com.example.artem.chatapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);


        if(!checkIfUserLoggedIn()) return;

//-------------------------Data message------------------------------
        Map<String, String> data = remoteMessage.getData();
        String notification_title = data.get("title");
        String notification_body = data.get("body");
        String from_user_id = data.get("from_user_id");

        //String click_action = remoteMessage.getNotification().getClickAction();


        Log.i("Info","click_action is"+from_user_id);

        NotificationCompat.Builder builder = null; //added

        String id = getString(R.string.default_notification_channel_id);

        Bitmap bmp = BitmapFactory.decodeResource(getResources(),
                R.drawable.default_user);


        Intent resultIntent = new Intent(this,ProfileActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        resultIntent.putExtra("uid",from_user_id);

        int notificationID = (int) System.currentTimeMillis();

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        notificationID,
                        resultIntent,
                        //PendingIntent.FLAG_UPDATE_CURRENT
                        PendingIntent.FLAG_CANCEL_CURRENT
                );

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        builder = new NotificationCompat.Builder(this, id)
                .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                .setSmallIcon(R.drawable.notification)
                .setContentTitle(notification_title)
                .setContentText(notification_body)
                .setAutoCancel(true)
                .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setSound(defaultSoundUri)
                .setContentIntent(resultPendingIntent);



        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(notificationID, builder.build());

        //}
// Configure the notification channel.


    }

    private boolean checkIfUserLoggedIn() {

        FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();

        Log.i("InfoApp",""+current_user);

        if(current_user == null )return false;
        else return  true;

    }
}
