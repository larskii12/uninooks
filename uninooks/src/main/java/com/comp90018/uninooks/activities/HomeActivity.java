package com.comp90018.uninooks.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.comp90018.uninooks.R;
import com.comp90018.uninooks.models.favorite.Favorite;
import com.comp90018.uninooks.models.location.study_space.StudySpace;
import com.comp90018.uninooks.models.review.ReviewType;
import com.comp90018.uninooks.service.busy_rating.BusyRatingServiceImpl;
import com.comp90018.uninooks.service.favorite.FavoriteServiceImpl;
import com.comp90018.uninooks.service.gps.GPSServiceImpl;
import com.comp90018.uninooks.service.image.ImageServiceImpl;
import com.comp90018.uninooks.service.location.LocationService;
import com.comp90018.uninooks.service.location.LocationServiceImpl;
import com.comp90018.uninooks.service.study_space.StudySpaceServiceImpl;
import com.comp90018.uninooks.service.time.TimeServiceImpl;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * Home activity
 */
public class HomeActivity extends AppCompatActivity {
    public boolean neverShowAgain;
    public ArrayList<StudySpace> topRatedStudySpaces = new ArrayList<>();
    HashMap<String, Double> busyRatingsByLocation;
    private LocationService locationAPI;
    private Context context;
    private SensorManager sensorManager;
    private float acceleration = 0f;
    private float currentAcceleration = 0f;
    private float lastAcceleration = 0f;
    private TextView noFavorites;
    private int userId;
    private String userEmail;
    private String userName;
    private StudySpace randomSpace;
    private SharedPreferences sharedPreferences;
    private HashMap<String, Integer> imagesByLocation;
    private static long lastHomeClickTime;


    /**
     * on create method
     *
     * @param savedInstanceState as savedInstanceState
     */
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        context = getApplicationContext();
        sharedPreferences = getSharedPreferences("uninooks", Context.MODE_PRIVATE);
        showDialog();

        noFavorites = findViewById(R.id.textView4);
        noFavorites.setVisibility(View.GONE);

        Intent intent = getIntent();
        userId = intent.getIntExtra("USER_ID_EXTRA", 0);
        userEmail = intent.getStringExtra("USER_EMAIL_EXTRA");
        userName = intent.getStringExtra("USER_NAME_EXTRA");
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.homeNav);

        busyRatingsByLocation = new HashMap<>();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        Objects.requireNonNull(sensorManager).registerListener(sensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);

        acceleration = 10f;
        currentAcceleration = SensorManager.GRAVITY_EARTH;
        lastAcceleration = SensorManager.GRAVITY_EARTH;

        ImageView infoButton = (ImageView) findViewById(R.id.infoButton);
        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInfoDialog();
            }
        });

        //navigate to the different pages
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.homeNav) {
                    long currentTime = System.currentTimeMillis();
                    long elapsedTimeSinceLastClick = currentTime - lastHomeClickTime;

                    if (elapsedTimeSinceLastClick > 1000 * 5) {
                        reloadActivity();
                        lastHomeClickTime = currentTime; // Update the last click time
                    }
                } else if (id == R.id.searchNav) {
                    Intent intent = new Intent(HomeActivity.this, MapsActivity.class);

                    // Pass the user to next page
                    intent.putExtra("USER_ID_EXTRA", userId);
                    intent.putExtra("USER_EMAIL_EXTRA", userEmail);
                    intent.putExtra("USER_NAME_EXTRA", userName);

                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();

                } else if (id == R.id.focusNav) {
                    Intent intent = new Intent(HomeActivity.this, FocusModeSplashActivity.class);

                    // Pass the user to next page
                    intent.putExtra("USER_ID_EXTRA", userId);
                    intent.putExtra("USER_EMAIL_EXTRA", userEmail);
                    intent.putExtra("USER_NAME_EXTRA", userName);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(HomeActivity.this, AccountActivity.class);

                    // Pass the user to next page
                    intent.putExtra("USER_ID_EXTRA", userId);
                    intent.putExtra("USER_EMAIL_EXTRA", userEmail);
                    intent.putExtra("USER_NAME_EXTRA", userName);
                    startActivity(intent);
                }

                return false;
            }
        });


        new Thread() {
            ArrayList<StudySpace> closestStudySpaces = new ArrayList<>();

            @Override
            public void run() {
                try {
                    locationAPI = new LocationServiceImpl();

                    ArrayList<StudySpace> allStudySpaces = new StudySpaceServiceImpl().getAllStudySpaces();
                    ArrayList<StudySpace> openingStudySpaces = new ArrayList<>();
                    ArrayList<StudySpace> closingStudySpaces = new ArrayList<>();

                    for (StudySpace studySpace : allStudySpaces) {
                        if (studySpace.isOpeningNow()) {
                            openingStudySpaces.add(studySpace);
                        } else {
                            closingStudySpaces.add(studySpace);
                        }
                    }

                    if (openingStudySpaces.size() >= 5) {

                        closestStudySpaces = new ArrayList<>(openingStudySpaces);
                        topRatedStudySpaces = new ArrayList<>(openingStudySpaces);
                        closestStudySpaces.sort(Comparator.comparingDouble(StudySpace::getDistanceFromCurrentPosition));
                        topRatedStudySpaces.sort(Comparator.comparingDouble(StudySpace::getAverage_rating));
                        Collections.reverse(topRatedStudySpaces);
                    } else {
                        closestStudySpaces = new ArrayList<>(openingStudySpaces);
                        topRatedStudySpaces = new ArrayList<>(openingStudySpaces);
                        closestStudySpaces.sort(Comparator.comparingDouble(StudySpace::getDistanceFromCurrentPosition));
                        topRatedStudySpaces.sort(Comparator.comparingDouble(StudySpace::getAverage_rating));
                        Collections.reverse(topRatedStudySpaces);

                        // closest closed study spaces
                        ArrayList<StudySpace> closedClosetStudySpaces = new ArrayList<>(closingStudySpaces);
                        ArrayList<StudySpace> closedTopRatedSpaces = new ArrayList<>(closingStudySpaces);
                        closedClosetStudySpaces.sort(Comparator.comparingDouble(StudySpace::getDistanceFromCurrentPosition));
                        closedTopRatedSpaces.sort(Comparator.comparingDouble(StudySpace::getAverage_rating));
                        Collections.reverse(closedTopRatedSpaces);

                        closestStudySpaces.addAll(closedClosetStudySpaces);
                        topRatedStudySpaces.addAll(closedTopRatedSpaces);
                    }

                    List<Favorite> favouriteSpaces = new FavoriteServiceImpl().getFavoritesByUser(userId, ReviewType.valueOf("STUDY_SPACE"));

                    ArrayList<StudySpace> favorites = new ArrayList<StudySpace>();
                    for (Favorite favorite : favouriteSpaces) {
                        StudySpace space = new LocationServiceImpl().findStudySpaceById(favorite.getStudySpaceId());
                        favorites.add(space);
                    }


                    getAllBusyRatings(closestStudySpaces);
                    getAllImages();

                    LinearLayout nearbyLayout = findViewById(R.id.nearbyLayout);
                    LinearLayout topRatedLayout = findViewById(R.id.topRatedLayout);
                    LinearLayout favoritesLayout = findViewById(R.id.favoritesLayout);


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Nearby Section
                            if (closestStudySpaces.size() == 0) {
                                ProgressBar loadingGIF = findViewById(R.id.loadingGIF);
                                loadingGIF.setVisibility(View.VISIBLE);
                                loadingGIF.setVisibility(View.GONE);
                            } else {
                                for (StudySpace space : closestStudySpaces.subList(0, 5)) {
                                    CardView card = (CardView) LayoutInflater.from(getApplicationContext()).inflate(R.layout.small_card_layout, nearbyLayout, false);
                                    ProgressBar loadingGIF = findViewById(R.id.loadingGIF);
                                    loadingGIF.setVisibility(View.VISIBLE);
                                    CardView newCard = createNewSmallCard(card, space, "distance");
                                    for (StudySpace favorite : favorites) {
                                        if (favorite.getName().equals(space.getName())) {
                                            ImageView favouriteIcon = (ImageView) newCard.findViewById(R.id.favouriteIcon);
                                            favouriteIcon.setBackgroundResource(R.drawable.baseline_favorite_24);
                                            favouriteIcon.getBackground().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.red), android.graphics.PorterDuff.Mode.SRC_IN);
                                        }
                                    }
                                    loadingGIF.setVisibility(View.GONE);
                                    nearbyLayout.addView(newCard);
                                    //opens the location page
                                    newCard.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            new Thread() {
                                                public void run() {
                                                    Intent intent = new Intent(HomeActivity.this, LocationActivity.class);
                                                    intent.putExtra("USER_ID_EXTRA", userId);
                                                    intent.putExtra("USER_EMAIL_EXTRA", userEmail);
                                                    intent.putExtra("USER_NAME_EXTRA", userName);
                                                    intent.putExtra("LOCATION_TYPE", space.getType());
                                                    intent.putExtra("LOCATION", space);
                                                    startActivity(intent);
                                                }
                                            }.start();
                                        }
                                    });
                                }
                            }
                            // Top Rated Section
                            if (topRatedStudySpaces.size() == 0) {
                                ProgressBar loadingGIF2 = findViewById(R.id.loadingGIF_2);
                                loadingGIF2.setVisibility(View.VISIBLE);
                                loadingGIF2.setVisibility(View.GONE);
                            } else {
                                for (StudySpace space : topRatedStudySpaces.subList(0, 5)) {
                                    CardView card = (CardView) LayoutInflater.from(getApplicationContext()).inflate(R.layout.top_layout_card, topRatedLayout, false);
                                    ProgressBar loadingGIF2 = findViewById(R.id.loadingGIF_2);
                                    loadingGIF2.setVisibility(View.VISIBLE);
                                    CardView newCard = createNewSmallCard(card, space, "rating");
                                    for (StudySpace favorite : favorites) {
                                        if (favorite.getName().equals(space.getName())) {
                                            ImageView favouriteIcon = (ImageView) newCard.findViewById(R.id.favouriteIcon);
                                            favouriteIcon.setBackgroundResource(R.drawable.baseline_favorite_24);
                                            favouriteIcon.getBackground().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.red), android.graphics.PorterDuff.Mode.SRC_IN);
                                        }
                                    }
                                    loadingGIF2.setVisibility(View.GONE);
                                    topRatedLayout.addView(newCard);

                                    //opens the location page
                                    card.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            new Thread() {
                                                public void run() {
                                                    Intent intent = new Intent(HomeActivity.this, LocationActivity.class);
                                                    intent.putExtra("USER_ID_EXTRA", userId);
                                                    intent.putExtra("USER_EMAIL_EXTRA", userEmail);
                                                    intent.putExtra("USER_NAME_EXTRA", userName);
                                                    intent.putExtra("LOCATION_TYPE", space.getType());
                                                    intent.putExtra("LOCATION", space);
                                                    startActivity(intent);
                                                }
                                            }.start();
                                        }
                                    });
                                }
                            }
                            // Favorites Section
                            if (favorites.size() == 0) {
                                ProgressBar loadingGIF3 = findViewById(R.id.loadingGIF_3);
                                loadingGIF3.setVisibility(View.VISIBLE);
                                loadingGIF3.setVisibility(View.GONE);

                                noFavorites.setVisibility(View.VISIBLE);
                            } else {
                                for (StudySpace space : favorites) {
                                    CardView card = (CardView) LayoutInflater.from(getApplicationContext()).inflate(R.layout.small_card_layout, favoritesLayout, false);
                                    ProgressBar loadingGIF3 = findViewById(R.id.loadingGIF_3);
                                    loadingGIF3.setVisibility(View.VISIBLE);
                                    CardView newCard = createNewSmallCard(card, space, "favorite");
                                    ImageView favouriteIcon = (ImageView) newCard.findViewById(R.id.favouriteIcon);
                                    favouriteIcon.setBackgroundResource(R.drawable.baseline_favorite_24);
                                    favouriteIcon.getBackground().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.red), android.graphics.PorterDuff.Mode.SRC_IN);
                                    loadingGIF3.setVisibility(View.GONE);
                                    favoritesLayout.addView(newCard);
                                    //opens the location page
                                    card.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            new Thread() {
                                                public void run() {
                                                    Intent intent = new Intent(HomeActivity.this, LocationActivity.class);
                                                    intent.putExtra("USER_ID_EXTRA", userId);
                                                    intent.putExtra("USER_EMAIL_EXTRA", userEmail);
                                                    intent.putExtra("USER_NAME_EXTRA", userName);
                                                    //intent.putExtra("LOCATION_ID", space.getId());
                                                    intent.putExtra("LOCATION_TYPE", space.getType());
                                                    intent.putExtra("LOCATION", space);
                                                    startActivity(intent);
                                                }
                                            }.start();
                                        }
                                    });
                                }
                            }
                        }
                    });
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }.start();

    }

    /**
     * Get all busy ratings
     *
     * @param spaces as the study spaces need to query
     */
    private void getAllBusyRatings(ArrayList<StudySpace> spaces) {
        for (StudySpace space : spaces) {
            String spaceName = space.getName();
            try {
                int locationID = space.getId();
                ReviewType type = ReviewType.valueOf(space.getType());
                Double busyScore = new BusyRatingServiceImpl().getAverageScoreFromEntity(locationID, type);
                busyRatingsByLocation.put(spaceName, busyScore);
            } catch (Exception e) {
                throw new RuntimeException();
            }
        }
    }

    /**
     * This creates a new card for the location
     *
     * @param card  as card
     * @param space as locations
     * @param type  as location type
     * @return
     */
    @SuppressLint("SetTextI18n")
    private CardView createNewSmallCard(CardView card, StudySpace space, String type) {
        ImageView banner = (ImageView) card.findViewById(R.id.banner);
        banner.setBackgroundResource(imagesByLocation.get(space.getName()));
        ProgressBar progress = (ProgressBar) card.findViewById(R.id.progressBar);
        Double score = busyRatingsByLocation.get(space.getName());
        Integer busyScore = score != null && space.isOpeningNow() ? (int) (score * 20) : 0;
        progress.setProgress(busyScore);
        if (busyScore >= 0 && busyScore <= 40) {
            progress.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.green)));
        } else if (busyScore > 40 && busyScore <= 75) {
            progress.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.yellow)));
        } else {
            progress.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.red)));
        }
        TextView locationName = (TextView) card.findViewById(R.id.location);
        locationName.setText(space.getName());
        TextView locationHours = (TextView) card.findViewById(R.id.hours);
        TextView distanceLabel = (TextView) card.findViewById(R.id.timeLabel);
        ImageView hoursIcon = (ImageView) card.findViewById(R.id.clockIcon);
        hoursIcon.setBackgroundResource(R.drawable.baseline_access_time_24);
        if (!space.isOpenToday()) {
            locationHours.setText("Close Today");
            hoursIcon.getBackground().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.red), android.graphics.PorterDuff.Mode.SRC_IN);
        } else {
            double hoursToClose = getTimeToClose(space.getCloseTime());
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

            locationHours.setText(sdf.format(space.getOpenTime()) + " - " + ("23:59".equals(sdf.format(space.getCloseTime())) ? "00:00" : sdf.format(space.getCloseTime())));
            hoursIcon.getBackground().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.deepBlue), android.graphics.PorterDuff.Mode.SRC_IN);

            if (hoursToClose < 1 || !space.isOpeningNow()) {
                hoursIcon.getBackground().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.red), android.graphics.PorterDuff.Mode.SRC_IN);
            } else {
                hoursIcon.getBackground().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.deepBlue), android.graphics.PorterDuff.Mode.SRC_IN);
            }
        }

        if (type.equals("rating")) {
            distanceLabel.setText(String.valueOf(space.getAverage_rating()));
        } else {
            if (space.getDistanceFromCurrentPosition() == -1 || !GPSServiceImpl.getGPSPermission()) {
                distanceLabel.setText("N/A");
            } else {
                distanceLabel.setText(space.getDistanceFromCurrentPosition() + " m");
            }
        }

        ImageView favouriteIcon = (ImageView) card.findViewById(R.id.favouriteIcon);

        favouriteIcon.setBackgroundResource(R.drawable.baseline_favorite_border_24);
        favouriteIcon.getBackground().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.red), android.graphics.PorterDuff.Mode.SRC_IN);
        //check if the favourites list includes this user and favourite
        if (type.equals("rating")) {
            ImageView ratingIcon = card.findViewById(R.id.starRating);
            ratingIcon.setBackgroundResource(R.drawable.star_solid);
        }

        return card;
    }

    /**
     * Calculates the time to the closing of a location
     *
     * @param closeTime as close hours
     * @return as the hours to close
     */
    private double getTimeToClose(Time closeTime) {
        Date today = new Date();
        Time currentTime = new TimeServiceImpl().getAEDTTime();
        int closeHour = closeTime.getHours();
        if (closeHour == 0) {
            closeHour = 24;
        }
        long timeDifference = (closeHour - currentTime.getHours()) * 3600L + (closeTime.getMinutes() - currentTime.getMinutes()) * 60L + (closeTime.getSeconds() - currentTime.getSeconds());
        double hours = (double) timeDifference / 3600;
        return hours;
    }

    /**
     * This restarts the accelerometer sensor
     */
    @Override
    protected void onResume() {
        sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        super.onResume();
    }

    /**
     * This disables the accelerometer sensor
     */
    @Override
    protected void onPause() {
        sensorManager.unregisterListener(sensorListener);
        super.onPause();
    }

    /**
     * This reloads the activity
     */
    private void reloadActivity() {
        Intent intent = getIntent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();
        startActivity(intent);
    }

    /**
     * This opens the dialog informing the user of the shake suggestions feature
     */
    private void showDialog() {
        neverShowAgain = sharedPreferences.getBoolean(getString(R.string.never_show_welcome), false);
        if (!neverShowAgain) {
            Dialog dialog = createDialog();
            dialog.show();
        }
    }

    /**
     * This opens a dialog informing the user of the shake suggestions feature
     *
     * @return builder
     */
    private Dialog createDialog() {
        final boolean[] neverShowAgainDialog = {false};

        // Use the Builder class for convenient dialog construction.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.shake_dialog, null))
                // This button confirms that the user understands and closes the dialog
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        neverShowAgainDialog[0] = true;
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean(getString(R.string.never_show_welcome), neverShowAgainDialog[0]);
                        editor.apply();
                    }
                });
        return builder.create();
    }

    /**
     * Gets all the images for each location
     */
    private void getAllImages() {
        try {
            imagesByLocation = new ImageServiceImpl().fetchAllImages();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Show info dialog
     */
    private void showInfoDialog() {

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.information_dialog);
        Button closeButton = dialog.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss(); // Close the dialog
            }
        });
        dialog.show();
    }    private final SensorEventListener sensorListener = new SensorEventListener() {
        //this opens a new suggested study space
        @Override
        public void onSensorChanged(SensorEvent event) {
            boolean shakeEnabled = sharedPreferences.getBoolean(getString(R.string.shaking_enabled), true);

            if (shakeEnabled) {
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];
                lastAcceleration = currentAcceleration;
                currentAcceleration = (float) Math.sqrt((double) (x * x + y * y + z * z));
                float delta = currentAcceleration - lastAcceleration;
                acceleration = acceleration * 0.9f + delta;
                if (acceleration > 12) {
                    sensorManager.unregisterListener(sensorListener);
                    Toast.makeText(getApplicationContext(), "Opening a random Space", Toast.LENGTH_SHORT).show();
                    int randomRange = topRatedStudySpaces.size();
                    int rand = (int) (Math.random() * randomRange);
                    randomSpace = topRatedStudySpaces.get(rand);
                    new Thread() {
                        public void run() {
                            Intent intent = new Intent(HomeActivity.this, LocationActivity.class);
                            intent.putExtra("USER_ID_EXTRA", userId);
                            intent.putExtra("USER_EMAIL_EXTRA", userEmail);
                            intent.putExtra("USER_NAME_EXTRA", userName);
                            intent.putExtra("LOCATION_TYPE", randomSpace.getType());
                            intent.putExtra("LOCATION", randomSpace);
                            startActivity(intent);
                        }
                    }.start();
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };




}
