package com.example.artem.chatapp;

import android.content.Context;

/**
 * Created by Artem on 13.02.2018.
 */

public class GetTimeAgo {

    private final static int SECOND_MILLS = 1000;
    private final static int MINUTE_MILLS = 60*SECOND_MILLS;
    private final static int HOUR_MILLS = 60*MINUTE_MILLS;
    private final static int DAY_MILLS = 24*HOUR_MILLS;

    public static String getTimeAgo(long time, Context ctx){

        if(time < 1000000000000L){
            //convert second to mils
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if(time > now || time <= 0){
            return null;
        }

        //TODO: localize
        final long diff = now - time;

        if(diff < MINUTE_MILLS){
            return "just now";
        } else if (diff<2*MINUTE_MILLS){
            return "few minutes ago";
        } else if (diff<50*MINUTE_MILLS){
            return diff/MINUTE_MILLS + " minutes ago";
        } else if (diff<90*MINUTE_MILLS){
            return diff/MINUTE_MILLS + " an hour ago";
        } else if (diff<24*HOUR_MILLS){
            return diff/HOUR_MILLS + " hours ago";
        } else if (diff<48*HOUR_MILLS){
            return diff/HOUR_MILLS + " yesterday";
        } else {
            return diff/DAY_MILLS + " days ago";
        }

    }

}
