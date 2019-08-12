package com.example.artem.chatapp;

import android.content.Intent;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {


    private Toolbar mToolbar;

    private FirebaseAuth mAuth;

    private ViewPager mViewPager;
    private PagerSectionsAdapter mPagerSectionsAdapter;
    private DatabaseReference mUserRef;

    private TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        mToolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("ChatApp");

        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users");

        //TABS
        mViewPager = findViewById(R.id.main_pager);
        mPagerSectionsAdapter = new PagerSectionsAdapter(getSupportFragmentManager());

        mViewPager.setAdapter(mPagerSectionsAdapter);

        mTabLayout = findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){

            setToStart();

        }
//        else {
//
//            mUserRef.child("online").setValue(true);
//
//        }

    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//
//        mUserRef.child("online").setValue(false);
//
//    }

    private void setToStart() {

        Intent startIntent = new Intent(MainActivity.this,StartActivity.class);
        startActivity(startIntent);
        finish();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()){

            case R.id.main_page_logout_btn:

                FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();

                Map userMap = new HashMap();
                userMap.put("online",ServerValue.TIMESTAMP);
                userMap.put("decice_token", "");

                mUserRef.child(current_user.getUid()).child("online").setValue(ServerValue.TIMESTAMP).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        FirebaseAuth.getInstance().signOut();
                        setToStart();

                    }
                });

                break;
            case R.id.main_page_settings_btn:
                Intent settingsIntent  = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;
            case R.id.main_page_all_btn:
                Intent allUsersIntent  = new Intent(MainActivity.this, UsersActivity.class);
                startActivity(allUsersIntent);
                break;
            default:
                break;

        }

        return true;
    }
}
