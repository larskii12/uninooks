package com.comp90018.uninooks.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.VibrationEffect;

import android.util.Log;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Message;
import android.os.Vibrator;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.comp90018.uninooks.R;
import com.comp90018.uninooks.service.background_app.BackgroundAppService;
import com.comp90018.uninooks.views.TimerView;
import com.comp90018.uninooks.worker.FocusModeWorker;
import com.google.android.material.bottomnavigation.BottomNavigationView;


import java.util.List;
import android.os.Vibrator;
import java.util.concurrent.TimeUnit;

public class FocusModeTimerActivity extends AppCompatActivity {

    private int pomodoroTimer;

    private int shortPauseTimer;

    private int longPauseTimer;

    public static boolean isCurrentlyOnApp = false;




    private int seconds;
    private int timer_length = 60;

//    private int seconds = 60;

    private Vibrator v;

    // Setup Timer Buttons
    private Button pomodoroButton;
    private Button shortPauseButton;
    private Button longPauseButton;

    // Timer Views
    private TimerView mTimerView;
    private TextView timerText;

    // Timer Related Buttons
    private Button timerStartButton;
    private Button timerPauseButton;
    private Button timerResetButton;
    private Button settingsButton;

    private ImageView roundOne;
    private ImageView roundTwo;
    private ImageView roundThree;
    private ImageView roundFour;
    private ImageView roundFive;
    private ImageView[] sequenceIndicators;

    private BottomNavigationView bottomNav;

    public static boolean isRunning = false;

    private boolean isPaused = false;
    private boolean wasPaused = false;
    private boolean wasReset = false;

    private boolean isPomodoro = false;
    private boolean isShortPause = false;
    private boolean isLongPause = false;
    private boolean settingsClicked = false;
    private boolean onAutoSequence = false;
    private boolean inSequence = false;

    public boolean neverShowAgain;

    private int pomodoroTime = 1500;
    private int shortPauseTime = 300;
    private int longPauseTime = 900;

    private int overallDefaultTime = 1500;

    private int pomodoroSequenceNum = 0;
    private final int MAX_LOOP = 4;

    private SharedPreferences sharedPreferences;

    SharedPreferences.Editor editor;

    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler() {
        @SuppressLint("SetTextI18n")
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    handler.postDelayed(timerRunnable, 1000);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_focus_mode);
        System.out.println("on create");

        sharedPreferences = getSharedPreferences("uninooks", Context.MODE_PRIVATE);
//        editor = sharedPreferences.edit();
        showDialog();

        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // Initialize UI components and set up timer controls
        mTimerView = findViewById(R.id.timer);
        timerText = findViewById(R.id.timerTextView);
        bottomNav = findViewById(R.id.bottom_navigation);

        roundOne = findViewById(R.id.sequence1);
        roundTwo = findViewById(R.id.sequence2);
        roundThree = findViewById(R.id.sequence3);
        roundFour = findViewById(R.id.sequence4);
        roundFive = findViewById(R.id.sequence5);

        bottomNav.setSelectedItemId(R.id.focusNav);
        sequenceIndicators = new ImageView[]{roundOne, roundTwo, roundThree, roundFour, roundFive};

        // Change to which setup for the timer
        pomodoroButton = findViewById(R.id.btn_pomodoro);
        pomodoroButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                pomodoroButtonActions();
                clickPomodoroButton();
                resetTimer();
                seconds = pomodoroTime;
                isPomodoro = true;
                isShortPause = false;
                isLongPause = false;
                updateTimerText();
            }
        });

        shortPauseButton = findViewById(R.id.btn_short_pause);
        shortPauseButton.setEnabled(true);
        shortPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickShortPauseButton();
                resetTimer();
                seconds = shortPauseTime;
                isPomodoro = false;
                isShortPause = true;
                isLongPause = false;
                updateTimerText();
            }
        });


        longPauseButton = findViewById(R.id.btn_long_pause);
        longPauseButton.setEnabled(true);
        longPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickLongPauseButton();
                resetTimer();
                seconds = longPauseTime;
                isPomodoro = false;
                isShortPause = false;
                isLongPause = true;
                updateTimerText();
            }
        });


        // Start or Resume the timer button
        timerStartButton = findViewById(R.id.btn_start);
        timerStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                System.out.println("timer start button is clicked");
                if (onAutoSequence) {
                    disableModeChangingButtons();
                    startAutoSequence();
                } else {
                    System.out.println("isPaused: " + isPaused);
                    enableModeChangingButtons();
                    mTimerView.start(seconds+1, isPaused);
                    startTimer();
                }
//                mTimerView.start(timer_length, isPaused);
//                startTimer();
//                mTimerView.start(seconds+1, isPaused);
            }
        });

        // Pause the timer button
        timerPauseButton = findViewById(R.id.btn_pause);
        timerPauseButton.setVisibility(View.GONE);
        timerPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pauseTimer();
            }
        });

        // Reset the timer button
        timerResetButton = findViewById(R.id.btn_restart);
        timerResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wasReset = true;

                resetTimer();

                if (isPomodoro) {
                    seconds = pomodoroTime;
                } else if (isShortPause) {
                    seconds = shortPauseTime;
                } else if (isLongPause) {
                    seconds = longPauseTime;
                } else {
                    seconds = overallDefaultTime;
                }

                updateTimerText();
            }
        });

        // Open the Settings for the Timer
        settingsButton = findViewById(R.id.btn_settings);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetTimer();
                settingsClicked = true;
                saveCurrentSettings();
                Intent Intent = new Intent(FocusModeTimerActivity.this, FocusModeSettingsActivity.class);
                startActivity(Intent);
            }
        });

        clickPomodoroButton();
        isCurrentlyOnApp = true;
    }


    public void onStart(){
        super.onStart();
    }

    public void onRestart(){
        super.onRestart();;
    }

    // When back button pressed
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isRunning) {
            isCurrentlyOnApp = false;
            // Detect the recent 20 seconds used apps

            startServiceViaWorker();
            startService();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("in on resume");

        System.out.println("settings clicked = " + settingsClicked);
        if (settingsClicked) {
            System.out.println("in settings clicked");
            retrieveSettings();
            settingsClicked = false;

            // if settings change, then do this -- have to compare before and after
            if (onAutoSequence) {
                // if settings change (even if its only the duration, everything will reset to the start of the sequence)
                resetTimer();
                seconds = pomodoroTime;
                isPomodoro = true;
                isShortPause = false;
                isLongPause = false;
                updateTimerText();
                pomodoroSequenceNum = 0;
                clickPomodoroButton();
            } else {
                enableModeChangingButtons();
                if (isPomodoro) {
                    clickPomodoroButton();
                } else if (isShortPause) {
                    clickShortPauseButton();
                } else if (isLongPause) {
                    clickLongPauseButton();
                }
            }
        } else {
            if (isRunning) {
                isCurrentlyOnApp = true;
                Log.d("TimerActivity", "Service has stopped");
                stopService();
        }
        }
    }

    public void onStop(){
        super.onStop();
    }

    public void onDestroy(){
        super.onDestroy();
        stopService();
    }


    /**
     * Actions to be executed when pomodoro button is clicked
     */
    private void pomodoroButtonActions() {
        resetTimer();
        seconds = pomodoroTime;
        isPomodoro = true;
        isShortPause = false;
        isLongPause = false;
        updateTimerText();

        clickPomodoroButton();
    }

    /**
     * Actions to be executed when short pause button is clicked
     */
    private void shortPauseButtonActions() {
        resetTimer();
        seconds = shortPauseTime;
        isPomodoro = false;
        isShortPause = true;
        isLongPause = false;
        updateTimerText();

        clickShortPauseButton();
    }

    /**
     * Actions to be executed when long pause button is clicked
     */
    private void longPauseButtonActions() {
        resetTimer();
        seconds = longPauseTime;
        isPomodoro = false;
        isShortPause = false;
        isLongPause = true;
        updateTimerText();

        clickLongPauseButton();
    }

    private void clickPomodoroButton() {
        pomodoroButton.setPressed(true);
        isPaused = false;

        if (onAutoSequence) {
            pomodoroButton.setBackgroundColor(ContextCompat.getColor(this, R.color.grey));
            pomodoroButton.setTextColor(ContextCompat.getColor(this, R.color.white));

        } else {
            pomodoroButton.setBackgroundColor(ContextCompat.getColor(this, R.color.deepBlue));
            pomodoroButton.setTextColor(ContextCompat.getColor(this, R.color.white));
        }
        unclickShortPauseButton();
        unclickLongPauseButton();
        isPomodoro = true;
        isShortPause = false;
        isLongPause = false;
    }

    private void unclickPomodoroButton() {
        pomodoroButton.setPressed(false);
        if (onAutoSequence) {
            pomodoroButton.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
            pomodoroButton.setTextColor(ContextCompat.getColor(this, R.color.grey));
        } else {
            pomodoroButton.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
            pomodoroButton.setTextColor(ContextCompat.getColor(this, R.color.deepBlue));
        }
    }

    private void clickShortPauseButton() {
        shortPauseButton.setPressed(true);
        isPaused = false;
        System.out.println("inSequence when clicking short pause button: " + inSequence);
        unclickPomodoroButton();
        unclickLongPauseButton();

        if (onAutoSequence) {
            shortPauseButton.setBackgroundColor(ContextCompat.getColor(this, R.color.grey));
            shortPauseButton.setTextColor(ContextCompat.getColor(this, R.color.white));

        } else {
            shortPauseButton.setBackgroundColor(ContextCompat.getColor(this, R.color.deepBlue));
            shortPauseButton.setTextColor(ContextCompat.getColor(this, R.color.white));
        }
        isPomodoro = false;
        isShortPause = true;
        isLongPause = false;

    }

    private void unclickShortPauseButton() {
        shortPauseButton.setPressed(false);
        if (onAutoSequence) {
            shortPauseButton.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
            shortPauseButton.setTextColor(ContextCompat.getColor(this, R.color.grey));
        } else {
            shortPauseButton.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
            shortPauseButton.setTextColor(ContextCompat.getColor(this, R.color.deepBlue));
        }
    }

    private void clickLongPauseButton() {
        longPauseButton.setPressed(true);
        isPaused = false;
        unclickPomodoroButton();
        unclickShortPauseButton();

        if (onAutoSequence) {
            longPauseButton.setBackgroundColor(ContextCompat.getColor(this, R.color.grey));
            longPauseButton.setTextColor(ContextCompat.getColor(this, R.color.white));

        } else {
            longPauseButton.setBackgroundColor(ContextCompat.getColor(this, R.color.deepBlue));
            longPauseButton.setTextColor(ContextCompat.getColor(this, R.color.white));
        }

        isPomodoro = false;
        isShortPause = false;
        isLongPause = true;
    }

    private void unclickLongPauseButton() {
        longPauseButton.setPressed(false);
//        longPauseButton.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
//        longPauseButton.setTextColor(ContextCompat.getColor(this, R.color.deepBlue));

        if (onAutoSequence) {
            longPauseButton.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
            longPauseButton.setTextColor(ContextCompat.getColor(this, R.color.grey));
        } else {
            longPauseButton.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
            longPauseButton.setTextColor(ContextCompat.getColor(this, R.color.deepBlue));
        }
    }

    private void startTimer() {
        if (!isRunning) {
            handler.sendEmptyMessage(0);
            isRunning = true;
            isPaused = false;
            timerStartButton.setVisibility(View.GONE);
            timerPauseButton.setVisibility(View.VISIBLE);
        }
    }

    private void pauseTimer() {
        if (isRunning) {
            mTimerView.pause();
            isRunning = false;
            isPaused = true;
            wasPaused = true;
            handler.removeCallbacks(timerRunnable);
            handler.removeCallbacksAndMessages(null);
            timerStartButton.setVisibility(View.VISIBLE);
            timerPauseButton.setVisibility(View.GONE);
        }
    }

    private void resetTimer() {
        mTimerView.stop();

        isRunning = false;
//        seconds = 0;

        handler.removeCallbacks(timerRunnable);
        handler.removeCallbacksAndMessages(null);
        timerStartButton.setVisibility(View.VISIBLE);
        timerPauseButton.setVisibility(View.GONE);
    }

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            System.out.println(seconds);
            seconds--;
            if (seconds >= 0) {
                updateTimerText();
                handler.postDelayed(this, 1000);
            } else {
                System.out.println("timer is done");
                System.out.println("V FOR VIBRATE wave");
                long[] pattern = {0, 500, 200, 500, 200, 500, 200, 500};
                v.vibrate(VibrationEffect.createWaveform(pattern, -1));

                resetTimer();
                isRunning = false;
                inSequence = false;
                timerStartButton.setVisibility(View.VISIBLE);
                timerPauseButton.setVisibility(View.GONE);

                if (pomodoroSequenceNum == 6) {
                    sequenceIndicators[pomodoroSequenceNum - 2].setVisibility(View.VISIBLE);
                    pomodoroSequenceNum = 0;
                }

                if (isPomodoro) {
                    seconds = pomodoroTime;
                } else if (isShortPause) {
                    seconds = shortPauseTime;
                } else if (isLongPause) {
                    seconds = longPauseTime;
                } else {
                    seconds = overallDefaultTime;
                }

                if (!isScreenOn()) {
                    Log.d("FocusModeTimerActivity", "trying to stop service and notify");
                    isCurrentlyOnApp = true;
                    stopService();
                }

                updateTimerText();
            }
        }
    };

        private void updateTimerText() {
            //int hours = seconds / 3600;
            int minutes = (seconds % 3600) / 60;
            int secs = seconds % 60;

            String timeString = String.format("%02d:%02d", minutes, secs);
            timerText.setText(timeString);
        }

        /**
         * Retrieve settings from the setting page
         */
        private void retrieveSettings() {
            System.out.println("within retrieve settings");
            pomodoroTime = sharedPreferences.getInt(getString(R.string.pomodoro_setting), pomodoroTime);
            shortPauseTime = sharedPreferences.getInt(getString(R.string.short_break_setting), shortPauseTime);
            longPauseTime = sharedPreferences.getInt(getString(R.string.long_break_setting), longPauseTime);
            onAutoSequence = sharedPreferences.getBoolean(getString(R.string.auto_pomodoro), false);

            if (isPomodoro) {
                seconds = pomodoroTime;
            } else if (isShortPause) {
                seconds = shortPauseTime;
            } else if (isLongPause) {
                seconds = longPauseTime;
            } else {
                seconds = overallDefaultTime;
            }
            updateTimerText();
        }

        /**
         * To set the default/current values on the next settings page
         */
        private void saveCurrentSettings() {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(getString(R.string.pomodoro_setting), pomodoroTime);
            editor.putInt(getString(R.string.short_break_setting), shortPauseTime);
            editor.putInt(getString(R.string.long_break_setting), longPauseTime);
            editor.putBoolean(getString(R.string.auto_pomodoro), onAutoSequence);
            editor.apply();
        }

        /**
         * Starts auto sequence of the pomodoro timer, ran on a separate thread
         * <p>
         * focus --> short pause --> focus --> short pause --> focus --> short pause --> focus -->
         * short pause --> focus --> long pause --> focus
         * <p>
         * Buttons will be disabled while pomodoro sequence is running!!
         */
        private void startAutoSequence() {
            if (!inSequence) {
                clearSequenceIndicators();
            }
            pomodoroRunnable.run();
        }

        /**
         * Takes care of running the sequence automatically when the setting is chosen
         */
        private Runnable pomodoroRunnable = new Runnable() {
            int timeSpentPaused = 0;

            @Override
            public void run() {
                displaySequenceIndicator();
                switchModes();
                System.out.println("num: " + pomodoroSequenceNum);

                if (inSequence && (isPomodoro || isShortPause || isLongPause) && wasPaused) {
                    // if timer was paused in the middle of the sequence
                    System.out.println("continuing after pausing");
                    continueTimerAfterPause();
                } else if (wasReset) {
                    System.out.println("current mode was reset");
                    if (isShortPause || isLongPause) {
                        pomodoroSequenceNum--;
                    }
                    continueTimerAfterReset();
                } else if (pomodoroSequenceNum < MAX_LOOP) {
                    // pomodoro-short pause pair
                    // if current mode is pomodoro, it will run focus then short break
                    // else, it is in short break mode, and it will run short break only
                    inSequence = true;
                    if (isPomodoro) {
                        pomodoroShortBreak();
                    } else {
                        runShortBreak();
                    }
                } else if (pomodoroSequenceNum == MAX_LOOP) {
                    // pomodoro-long pause pair
                    inSequence = true;
                    if (isPomodoro) {
                        pomodoroLongBreak();
                    } else {
                        runLongBreak();
                    }
                } else if (pomodoroSequenceNum == (MAX_LOOP + 1)) {
                    // last pomodoro run
                    runPomodoro();
                    System.out.println("Sequence is done!");
                    pomodoroSequenceNum++;
                }
            }
        };

        /**
         * Continues timer from where it stopped
         */
        private void continueTimerAfterPause() {
            mTimerView.start(seconds, isPaused);
            startTimer();
            wasPaused = false;
            handler.postDelayed(pomodoroRunnable, (seconds) * 1000);
        }

        /**
         * Replays the current mode
         */
        private void continueTimerAfterReset() {
            wasReset = false;
            if (isPomodoro) {
                if (pomodoroSequenceNum == MAX_LOOP) {
                    pomodoroLongBreak();
                } else {
                    pomodoroShortBreak();
                }
//            runPomodoro();
            } else if (isShortPause) {
                runShortBreak();
            } else if (isLongPause) {
                runLongBreak();
            }
        }

        /**
         * Displays sequence indicator after each focus-break pair is done
         */
        private void displaySequenceIndicator() {
            if (pomodoroSequenceNum > 0 && pomodoroSequenceNum <= MAX_LOOP + 1 && !wasPaused) {
                sequenceIndicators[pomodoroSequenceNum - 1].setVisibility(View.VISIBLE);
            }
        }

        /**
         * Run pomodoro then short break
         */
        private void pomodoroShortBreak() {
            runPomodoro();
            System.out.println("before short break");

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    runShortBreak();
                }
            }, (pomodoroTime) * 1000);
        }

        /**
         * Run pomodoro then long break
         */
        private void pomodoroLongBreak() {
            runPomodoro();
            System.out.println("before long break");

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    runLongBreak();
                }
            }, (pomodoroTime) * 1000);
        }

        /**
         * Run short break
         */
        private void runShortBreak() {
            System.out.println("VIBRATE default"); // default when focus mode ends (At the start of short break)
            v.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
            seconds = shortPauseTime;
            updateTimerText();
            clickShortPauseButton();
            mTimerView.start(shortPauseTime, isPaused);

            startTimer();
            System.out.println("ShortPause timer started");

            isPomodoro = false;
            isShortPause = true;
            isLongPause = false;

            pomodoroSequenceNum++;
            handler.postDelayed(pomodoroRunnable, (shortPauseTime) * 1000);
        }

        /**
         * Run short break
         */
        private void runLongBreak() {
            System.out.println("VIBRATE default before long"); // default when focus mode ends (At the start of short break)
            v.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));

            seconds = longPauseTime;
            updateTimerText();
            mTimerView.start(longPauseTime, isPaused);
            startTimer();
            System.out.println("Long pause timer started");

            isPomodoro = false;
            isShortPause = false;
            isLongPause = true;
            pomodoroSequenceNum++;

            handler.postDelayed(pomodoroRunnable, (longPauseTime) * 1000);
        }

        /**
         * Run pomodoro
         */
        private void runPomodoro() {
            seconds = pomodoroTime;
            updateTimerText();
            clickPomodoroButton();
            mTimerView.start(pomodoroTime, isPaused);
            startTimer();
            System.out.println("Pomodoro");
            isPomodoro = true;
            isShortPause = false;
            isLongPause = false;
        }

        /**
         * Switch modes in the sequence
         */
        private void switchModes() {
            if (seconds <= 2) {
                System.out.println("mode switched");
                if (pomodoroSequenceNum < MAX_LOOP) {
                    if (isPomodoro) {
                        isPomodoro = false;
                        isShortPause = true;
                        isLongPause = false;
                    } else if (isShortPause) {
                        isPomodoro = true;
                        isShortPause = false;
                        isLongPause = false;
                        System.out.println("VIBRATE 90 1");
                        v.vibrate(VibrationEffect.createOneShot(1000, 90));
                    }
                } else {
                    if (isPomodoro) {
                        isPomodoro = false;
                        isShortPause = false;
                        isLongPause = true;
                    } else {
                        isPomodoro = true;
                        isShortPause = false;
                        isLongPause = false;
                        System.out.println("VIBRATE 90 2");
                        v.vibrate(VibrationEffect.createOneShot(1000, 90));
                    }
                }
            }
        }

        private void clearSequenceIndicators() {
            for (ImageView indicator : sequenceIndicators) {
                indicator.setVisibility(View.GONE);
            }
        }


        /**
         * Disable all buttons except for settings
         */
        private void disableModeChangingButtons() {
            pomodoroButton.setClickable(false);
            shortPauseButton.setClickable(false);
            longPauseButton.setClickable(false);
        }

        private void enableModeChangingButtons() {
            pomodoroButton.setClickable(true);
            shortPauseButton.setClickable(true);
            longPauseButton.setClickable(true);
        }

        private void showDialog() {
            neverShowAgain = sharedPreferences.getBoolean(getString(R.string.never_show_again_setting), false);
            if (!neverShowAgain) {
                Dialog dialog = createDialog();
                dialog.show();
            }
        }

        private Dialog createDialog() {
            String[] choices = {"Never show this message again"};
            final boolean[] neverShowAgainDialog = {false};

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.dialogMessage)
                    .setSingleChoiceItems(choices, -1, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            neverShowAgainDialog[0] = true;
                        }
                    })
                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean(getString(R.string.never_show_again_setting), neverShowAgainDialog[0]);
                            editor.apply();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
            return builder.create();
        }



        private boolean isScreenOn() {
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            return powerManager.isInteractive();
        }

        public void startService() {
            Log.d("FocusModeTimerActivity", "startService called");
            if (!BackgroundAppService.isServiceRunning) {
                Intent serviceIntent = new Intent(this, BackgroundAppService.class);
                ContextCompat.startForegroundService(this, serviceIntent);
            }
        }

        public void stopService() {
            Log.d("FocusModeTimerActivity", "stopService called");
            if (BackgroundAppService.isServiceRunning) {
                Intent serviceIntent = new Intent(this, BackgroundAppService.class);
                stopService(serviceIntent);
            }
        }

        public void startServiceViaWorker() {
            String UNIQUE_WORK_NAME = "StartMyServiceViaWorker";
            WorkManager workManager = WorkManager.getInstance(this);

            // As per Documentation: The minimum repeat interval that can be defined is 15 minutes
            // (same as the JobScheduler API), but in practice 15 doesn't work. Using 16 here
            PeriodicWorkRequest request =
                    new PeriodicWorkRequest.Builder(
                            FocusModeWorker.class,
                            16,
                            TimeUnit.MINUTES)
                            .build();

            // to schedule a unique work, no matter how many times app is opened i.e. startServiceViaWorker gets called
            // do check for AutoStart permission
            workManager.enqueueUniquePeriodicWork(UNIQUE_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request);
        }
    }


