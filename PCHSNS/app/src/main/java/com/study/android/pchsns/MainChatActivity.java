package com.study.android.pchsns;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.iid.FirebaseInstanceId;
import com.study.android.pchsns.chat.SelectUserActivity;
import com.study.android.pchsns.chat_fragment.ChatRoomFragment;
import com.study.android.pchsns.chat_fragment.UserFragment;
import com.study.android.pchsns.chat_fragment.UserListFragment;

import java.util.HashMap;
import java.util.Map;

public class MainChatActivity extends AppCompatActivity {



    private SectionsPagerAdapter mSectionsPagerAdapter;


    private ViewPager mViewPager;

    private FloatingActionButton makeRoomBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatmain);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        try {
            String token = FirebaseInstanceId.getInstance().getToken();
            Log.d("IDService","device token : "+token);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition()==1) {     // char room
                    makeRoomBtn.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                makeRoomBtn.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        sendRegistrationToServer();


        makeRoomBtn = findViewById(R.id.makeRoomBtn);
        makeRoomBtn.setVisibility(View.INVISIBLE);
        makeRoomBtn.setOnClickListener( new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                startActivity(new Intent(v.getContext(), SelectUserActivity.class));
            }
        });
    }

    void sendRegistrationToServer() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String token = FirebaseInstanceId.getInstance().getToken();
        Map<String, Object> map  = new HashMap<>();
        map.put("token", token);
        FirebaseFirestore.getInstance().collection("users").document(uid).set(map, SetOptions.merge());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            this.startActivity(intent);
            this.finish();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: return new UserListFragment();
                case 1: return new ChatRoomFragment();
                default: return new UserFragment();
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    }
}
