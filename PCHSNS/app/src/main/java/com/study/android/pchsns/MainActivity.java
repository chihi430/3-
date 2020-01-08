/*
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.study.android.pchsns;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.study.android.pchsns.chat.ChatActivity;
import com.study.android.pchsns.fragment.MyPostsFragment;
import com.study.android.pchsns.fragment.MyTopPostsFragment;
import com.study.android.pchsns.fragment.RecentPostsFragment;

import java.util.ArrayList;
import java.util.List;


public class  MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";

    private FragmentPagerAdapter mPagerAdapter;
    private ViewPager mViewPager;
    AdView mAdView;
    //권한체크
    private String[] permissions = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            };

    private static final int MULTIPLE_PERMISSIONS =101;

    @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // 초기화
        String bannerid = getResources().getString(R.string.ad_unit_id_1);
        MobileAds.initialize(getApplicationContext(), bannerid);

        // 테스트 광고 부르기
        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest
                .Builder()
                .addTestDevice("11C87A6EF8240F17AC6F96375010D565")
                .build();
        mAdView.loadAd(adRequest);
        checkPermissions();
        // Create the adapter that will return a fragment for each section
        mPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            private final Fragment[] mFragments = new Fragment[] {
                    new RecentPostsFragment(),
                    new MyPostsFragment(),
                    new MyTopPostsFragment(),
            };
            private final String[] mFragmentNames = new String[] {
                    getString(R.string.heading_recent),
                    getString(R.string.heading_my_posts),
                    getString(R.string.heading_my_top_posts)
            };
            @Override
            public Fragment getItem(int position) {
                return mFragments[position];
            }
            @Override
            public int getCount() {
                return mFragments.length;
            }
            @Override
            public CharSequence getPageTitle(int position) {
                return mFragmentNames[position];
            }
        };
        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mPagerAdapter);
        TabLayout tabLayout = findViewById(R.id.tab);
        tabLayout.setupWithViewPager(mViewPager);

        // Button launches NewPostActivity 글쓰기
        findViewById(R.id.fab_new_post).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NewPostActivity.class);
                //intent.putExtra("username", "Asd");

                startActivity(intent);

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    //툴바 옵션
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.action_logout) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            FirebaseAuth.getInstance().signOut();
            return true;
        } else if(i == R.id.action_chat){
            startActivity(new Intent(this, MainChatActivity.class));

            return false;
        }

        else {
            return super.onOptionsItemSelected(item);
        }
    }


//////////////////////////권한체크
    private boolean checkPermissions() {
        int result;
        List<String> permissionList = new ArrayList<>();
        for(String pm : permissions){
            result = ContextCompat.checkSelfPermission(this,pm);
            if(result != PackageManager.PERMISSION_GRANTED){
                permissionList.add(pm);
            }
        }
        if (!permissionList.isEmpty()){
            ActivityCompat.requestPermissions(this, permissionList.toArray
                    (new String[permissionList.size()]), MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(
            int requestCode, String permissions[], int[] grantResults)
    {
        switch (requestCode){
            case MULTIPLE_PERMISSIONS:{
                if(grantResults.length > 0){
                    for(int i =0; i < permissions.length; i++){
                        if(permissions[i].equals(this.permissions[0])){
                            if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                                showNoPermissionToastAndFinish();
                            }
                        }else if(permissions[i].equals(this.permissions[1])){
                            if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                                showNoPermissionToastAndFinish();
                            }
                        }
                    }
                } else{
                    showNoPermissionToastAndFinish();
                }
                return;
            }
        }
    }

    private void showNoPermissionToastAndFinish(){
        Toast.makeText(this,"권한요청에 동의해 주셔야 이용 가능합니다.",
                Toast.LENGTH_SHORT).show();
        finish();
    }

}
