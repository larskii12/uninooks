package com.comp90018.uninooks.activities;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.transition.Fade;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import com.comp90018.uninooks.R;


public class FocusModeSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_focus_setting);

        LinearLayout pomodoroTimerLayout = findViewById(R.id.LinearLayoutFocusSettingPomodoro);
        final RelativeLayout pomodoroInputsLayout = findViewById(R.id.RelativeLayoutFocusSettingPomodoro);

        // Set the click listener for the Pomodoro Timer LinearLayout
        pomodoroTimerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a transition instance, a fade transition in this case
                Transition transition = new Fade();
                transition.setDuration(600); // Set the duration of the transition
                transition.addTarget(pomodoroInputsLayout); // Set the target view for the transition

                // Begin delayed transition
                TransitionManager.beginDelayedTransition((ViewGroup) v.getParent(), transition);

                // Toggle the visibility of the RelativeLayout
                if (pomodoroInputsLayout.getVisibility() == View.GONE) {
                    pomodoroInputsLayout.setVisibility(View.VISIBLE);
                } else {
                    pomodoroInputsLayout.setVisibility(View.GONE);
                }
            }
        });
    }
}