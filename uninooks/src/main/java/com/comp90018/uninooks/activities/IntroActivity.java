package com.comp90018.uninooks.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import com.comp90018.uninooks.R;
import com.comp90018.uninooks.activities.HomeActivity;
import com.comp90018.uninooks.activities.IntroViewPagerAdapter;
import com.comp90018.uninooks.activities.ScreenItem;
import com.comp90018.uninooks.activities.SignUpActivity;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class IntroActivity extends AppCompatActivity {
    private ViewPager screenPager;
    IntroViewPagerAdapter introViewPagerAdapter;
    TabLayout tabIndicator;
    Button btnNext, btnGetStarted;
    LinearLayout linearLayoutNext, linearLayoutGetStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("this page was opened");

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (restorePreData()){
            Intent mainActivity = new Intent(getApplicationContext(), SignUpActivity.class);
            startActivity(mainActivity);
            finish();
        }

        setContentView(R.layout.activity_intro);

        btnNext = findViewById(R.id.btn_next);
        btnGetStarted = findViewById(R.id.btn_get_started);
        linearLayoutNext = findViewById(R.id.linear_layout_next);
        linearLayoutGetStarted = findViewById(R.id.linear_layout_get_started);
        tabIndicator = findViewById(R.id.tab_indicator);
        //Data
        final List<ScreenItem> mList = new ArrayList<>();
        mList.add(new ScreenItem("Check your destination", "Check how busy each study area is. \nPick the best place to study quietly", R.drawable.homepage_screenshot));
        mList.add(new ScreenItem("Search study space suits you!", "Search study space suits you! \\nYou can search by location, capacity, and more", R.drawable.search_page_screenshot));
        mList.add(new ScreenItem("Focus on your study", "Focus on your study \\nUse Focus Mode to block distractions and stay focused", R.drawable.focus_mode_screenshot));

        //Setup viewPager
        screenPager = findViewById(R.id.screen_viewpager);
        introViewPagerAdapter = new IntroViewPagerAdapter(this, mList);
        screenPager.setAdapter(introViewPagerAdapter);

        //Setup tab indicator
        tabIndicator.setupWithViewPager(screenPager);

        //Button Next
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                screenPager.setCurrentItem(screenPager.getCurrentItem()+1, true);
            }
        });
        System.out.println("still alive 1");

        tabIndicator.addOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition()==mList.size()-1){
                    loadLastScreen();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        System.out.println("still alive 2");

        //Button Get Started
        btnGetStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mainActivity = new Intent(getApplicationContext(), HomeActivity.class);
                startActivity(mainActivity);
                savePrefsData();
                finish();
            }
        });
        System.out.println("still alive 3");
    }

    private boolean restorePreData(){
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("uninooks", MODE_PRIVATE);
        Boolean isIntroActivityOpenedBefore = preferences.getBoolean("isIntroOpened", false);
        return isIntroActivityOpenedBefore;
    }

    private void savePrefsData(){
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("uninooks", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isIntroOpened", true);
        editor.apply();
    }

    private void loadLastScreen(){
        linearLayoutNext.setVisibility(View.INVISIBLE);
        linearLayoutGetStarted.setVisibility(View.VISIBLE);
    }
}