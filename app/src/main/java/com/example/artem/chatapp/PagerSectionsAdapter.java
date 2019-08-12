package com.example.artem.chatapp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by Artem on 16.01.2018.
 */

class PagerSectionsAdapter extends FragmentPagerAdapter{


    public PagerSectionsAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position){

            case 0:
                RequestFragment requestFragment = new RequestFragment();
                return requestFragment;

            case 1:
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;
            case 2:
                FriendsFragment friendsFragment = new FriendsFragment();
                return friendsFragment;

            default:
                return  null;
        }
    }

    @Override
    public int getCount() {
        return 3; //Num of Tabs
    }

    public CharSequence getPageTitle(int position){

        switch (position){

            case 0:
                return "REQUEST";

            case 1:
                return "Chat";

            case 2:
                return "Friends";

            default:
                return "";
        }

    }

}
