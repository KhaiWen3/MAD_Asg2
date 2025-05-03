package my.edu.madgroupassignment;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ClockTimer extends AppCompatActivity {

    private TextView hoursText, minutesText, secondsText, dayInfoText, dateText; //Views
    private Button startButton, resetButton;
    private MaterialButtonToggleGroup timerModeToggleGroup;
    private CountDownTimer countDownTimer;
    private boolean timerRunning;
    private long timeLeftInMillis = 0;
    private static final long POMODORO_TIME = 25 * 60 * 1000; // 25 minutes in milliseconds
    private static final long START_FROM_ZERO_TIME = 0;
    private long selectedStartTime = POMODORO_TIME;

    // Handler for clock updates
    private Handler clockHandler = new Handler();
    private Runnable clockUpdater;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clock_timer);


        // Initialize views
        hoursText = findViewById(R.id.hoursText);
        minutesText = findViewById(R.id.minutesText);
        secondsText = findViewById(R.id.secondsText);
        secondsText.setVisibility(View.GONE); // Hide by default
        dayInfoText = findViewById(R.id.dayInfoText);

        startButton = findViewById(R.id.startButton);
        resetButton = findViewById(R.id.resetButton);
        dateText = findViewById(R.id.dateText);
        timerModeToggleGroup = findViewById(R.id.timerModeToggleGroup);

        // Show walkthrough
            showWalkthrough();


        // Initialize bottom navigation
        setupBottomNavigation();

        // Set initial time
        updateCountDownText(POMODORO_TIME);
        updateDayInfo();

        // Start live clock updates
        startClockUpdates();

        // Handle rotation
        checkOrientation();


        // Toggle between Pomodoro and Start from 0
        timerModeToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.clockBtn) {
                    // Handle Clock mode
                    try{
                        timerRunning=false;
                        startClockUpdates();
                        //updateDayAndDate();
                        Log.d("Back to clock button", "rinsideeeee");
                    }catch (Exception ex){
                        Log.d("Back to clock button", ex.getMessage());
                    }
                }
                else if (checkedId == R.id.pomodoroButton) {
                    try{
                        Log.d("test", "inside promot");
                        selectedStartTime = POMODORO_TIME;
                        resetTimer();
                    }
                    catch(Exception ex){
                        Log.d("Error",ex.getMessage());
                    }
                }
                else if (checkedId == R.id.startFromZeroButton) {
                    selectedStartTime = 60 * 1000;
                    resetTimer();
                }
            }
        });


        // Button click listeners
        startButton.setOnClickListener(v -> {
            if (timerRunning) {
                pauseTimer();
            } else {
                startTimer();
            }
        });



        resetButton.setOnClickListener(v -> resetTimer());
    }

    private void showWalkthrough() {
        new TapTargetSequence(this)
                .targets(
                        TapTarget.forView(startButton, "Start Button", "Tap to start the timer")
                                .outerCircleColor(R.color.colorWTAccent)
                                .outerCircleAlpha(0.46f)
                                .targetCircleColor(R.color.white)
                                .titleTextColor(R.color.black)
                                .descriptionTextColor(R.color.white)
                                .textColor(R.color.white)
                                .dimColor(R.color.black)
                                .drawShadow(true)
                                .cancelable(true)
                                .tintTarget(true)
                                .transparentTarget(true)
                                .id(1),

                        TapTarget.forView(resetButton, "Reset Button", "Tap to reset the timer, but make sure to pause it first!")
                                .outerCircleColor(R.color.colorWTAccent)
                                .outerCircleAlpha(0.46f)
                                .targetCircleColor(R.color.white)
                                .titleTextColor(R.color.black)
                                .descriptionTextColor(R.color.white)
                                .textColor(R.color.white)
                                .dimColor(R.color.white)
                                .drawShadow(true)
                                .cancelable(true)
                                .tintTarget(true)
                                .transparentTarget(true)
                                .id(2)
                )
                .listener(new TapTargetSequence.Listener() {
                    @Override
                    public void onSequenceFinish() {
                        Log.d("Walkthrough", "Finished");
                        // No need to set walkthroughShown here anymore
                    }

                    @Override
                    public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                        Log.d("Walkthrough", "Step shown: " + lastTarget.id());
                    }

                    @Override
                    public void onSequenceCanceled(TapTarget lastTarget) {
                        Log.d("Walkthrough", "Canceled at step: " + lastTarget.id());
                    }
                })
                .start();
    }





    //Bottom Navigation
    private void setupBottomNavigation() {
        ImageButton clockBtn = findViewById(R.id.clockButton);
        ImageButton listBtn = findViewById(R.id.listButton);
        ImageButton homeBtn = findViewById(R.id.homeButton);
        ImageButton profileBtn = findViewById(R.id.profileButton);
        Button rewardBtn = findViewById(R.id.rewardButton);
        Button searchBtn = findViewById(R.id.searchButton);

        // Disable current page button
        clockBtn.setEnabled(false);
        clockBtn.setAlpha(0.5f);

        //toggle visibility of hidden options
        listBtn.setOnClickListener(view -> {
//            int visibility = rewardBtn.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE;
//            rewardBtn.setVisibility(visibility);
//            searchBtn.setVisibility(visibility);

            View popupView = LayoutInflater.from(this).inflate(R.layout.custom_popup, null);

            PopupWindow popupWindow = new PopupWindow(
                    popupView,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    true
            );

            // Set background, animation, etc.
            popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            popupWindow.setOutsideTouchable(true);

            // Find your buttons in the custom layout
            Button rewardButton = popupView.findViewById(R.id.rewardButton);
            Button searchButton = popupView.findViewById(R.id.searchButton);

            rewardButton.setOnClickListener(rewardView -> {
                // Handle reward action
                popupWindow.dismiss();
            });

            searchButton.setOnClickListener(searchView -> {
                // Handle search action
                popupWindow.dismiss();
            });

            // Show the popup
            popupWindow.showAsDropDown(view);
        });

        // Handle reward button click
//        rewardBtn.setOnClickListener(v -> {
//            Intent intent = new Intent(ClockTimer.this, RewardActivity.class); // Replace with your actual class
//            startActivity(intent);
//        });
//
//        // Handle search button click
//        searchBtn.setOnClickListener(v -> {
//            Intent intent = new Intent(ClockTimer.this, SearchActivity.class); // Replace with your actual class
//            startActivity(intent);
//        });
//
//        homeBtn.setOnClickListener(v -> {
//            Intent intent = new Intent(ClockTimer.this, HomeActivity.class); // Change to your actual home activity
//            startActivity(intent);
//        });
//
        profileBtn.setOnClickListener(v -> {
            Intent intent = new Intent(ClockTimer.this, ProfileActivity.class); //Replace with actual class
            startActivity(intent);
        });
    }

    private void startClockUpdates() {
        clockUpdater = new Runnable() {
            @Override
            public void run() {
                if (!timerRunning) {
                    updateDayAndDate();
                }
                clockHandler.postDelayed(this, 60000); // Update every minute
            }
        };
        clockHandler.post(clockUpdater);
    }

    private void updateDayAndDate() {
        Calendar calendar = Calendar.getInstance();

        // Update time
        hoursText.setText(String.format(Locale.getDefault(), "%02d",
                calendar.get(Calendar.HOUR_OF_DAY)));
        minutesText.setText(String.format(Locale.getDefault(), "%02d",
                calendar.get(Calendar.MINUTE)));
        secondsText.setText(String.format(Locale.getDefault(), "%02d",
                calendar.get(Calendar.SECOND)));
        secondsText.setVisibility(View.VISIBLE);
        //secondsText.setVisibility(View.GONE);

        // Update date and day
        dayInfoText.setText(calendar.getDisplayName(
                Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault()));

        dateText.setText(new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                .format(calendar.getTime()));
    }

    private void checkOrientation() {
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // Landscape - hide day info (already handled by rotating clock_container)
            dayInfoText.setVisibility(View.GONE);
        } else {
            // Portrait mode
            dayInfoText.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        checkOrientation();
    }




//    private void startTimer() {
//        if (timeLeftInMillis <= 0) {
//            timeLeftInMillis = selectedStartTime;
//        }
//
//        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
//            @Override
//            public void onTick(long millisUntilFinished) {
//                timeLeftInMillis = millisUntilFinished;
//                updateCountDownText(timeLeftInMillis);
//                blinkColon();
//            }
//
//            @Override
//            public void onFinish() {
//                timerRunning = false;
//                updateButtons();
//                //timeLeftInMillis = 0;
//                //updateCountDownText(0);
//            }
//        }.start();
//
//        timerRunning = true;
//        updateButtons();
//    }

    private void startTimer() {
        if (timerModeToggleGroup.getCheckedButtonId() == R.id.startFromZeroButton) {
            // Count up timer logic
            timeLeftInMillis = 0;
            countDownTimer = new CountDownTimer(Long.MAX_VALUE, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    timeLeftInMillis += 1000;
                    updateCountDownText(timeLeftInMillis);
                }

                @Override
                public void onFinish() {
                    // Never called for count-up timer
                }
            }.start();
        } else {
            // Normal countdown logic
            if (timeLeftInMillis <= 0) {
                timeLeftInMillis = selectedStartTime;
            }
            countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    timeLeftInMillis = millisUntilFinished;
                    updateCountDownText(timeLeftInMillis);
                }

                @Override
                public void onFinish() {
                    timerRunning = false;
                    updateButtons();
                }
            }.start();
        }

        timerRunning = true;
        updateButtons();
    }

    private void pauseTimer() {
        countDownTimer.cancel();
        timerRunning = false;
        updateButtons();
    }

    private void resetTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        timerRunning = false;

        if (timerModeToggleGroup.getCheckedButtonId() == R.id.startFromZeroButton) {
            timeLeftInMillis = 0;
            secondsText.setText("00");
            secondsText.setVisibility(View.VISIBLE);
        } else {
            timeLeftInMillis = selectedStartTime;
            secondsText.setVisibility(View.GONE);
        }

        updateCountDownText(timeLeftInMillis);
        updateButtons();
    }

//    private void resetTimer() {
//        if (countDownTimer != null) {
//            countDownTimer.cancel();
//        }
//        timerRunning = false;
//        timeLeftInMillis = selectedStartTime;
//        updateCountDownText(timeLeftInMillis);
//        updateButtons();
//        colonText.setVisibility(View.VISIBLE); // Make sure colon is visible after reset
//
//    }

    private void updateCountDownText(long timeInMillis) {
        int hours = (int) (timeInMillis / 1000) / 3600;
        int minutes = (int) ((timeInMillis / 1000) % 3600) / 60;
        int seconds = (int) (timeInMillis / 1000) % 60;

        // Always show hours if counting up, or if hours > 0 when counting down
        if (timerModeToggleGroup.getCheckedButtonId() == R.id.startFromZeroButton || hours > 0) {
            hoursText.setText(String.format(Locale.getDefault(), "%02d", hours));
            minutesText.setText(String.format(Locale.getDefault(), "%02d", minutes));
            secondsText.setText(String.format(Locale.getDefault(), "%02d", seconds));
            secondsText.setVisibility(View.VISIBLE);
        } else {
            // Pomodoro mode - normal display
            secondsText.setVisibility(View.GONE);
            if (hours > 0) {
                hoursText.setText(String.format(Locale.getDefault(), "%02d", hours));
                minutesText.setText(String.format(Locale.getDefault(), "%02d", minutes));
            } else {
                hoursText.setText(String.format(Locale.getDefault(), "%02d", minutes));
                minutesText.setText(String.format(Locale.getDefault(), "%02d", seconds));
            }
        }
//        if (hours > 0) {
//            String timeLeftFormatted = String.format(Locale.getDefault(),
//                    "%02d:%02d:%02d", hours, minutes, seconds);
//            hoursText.setText(String.format("%02d", hours));
//            minutesText.setText(String.format("%02d", minutes));
//        } else {
//            String timeLeftFormatted = String.format(Locale.getDefault(),
//                    "%02d:%02d", minutes, seconds);
//            hoursText.setText(String.format("%02d", minutes));
//            minutesText.setText(String.format("%02d", seconds));
//        }
    }

//    private void blinkColon() {
//        if (colonText.getVisibility() == View.VISIBLE) {
//            colonText.setVisibility(View.INVISIBLE);
//        } else {
//            colonText.setVisibility(View.VISIBLE);
//        }
//    }

    private void updateDayInfo() {
        // Simple implementation - you can expand this
        dayInfoText.setText("Weatherday");
    }

    private void updateButtons() {
        if (timerRunning) {
            startButton.setText("Pause");
            resetButton.setEnabled(false);
        } else {
            startButton.setText("Start");
            resetButton.setEnabled(true);
        }
    }

    private void updateDateTime() {
        // Get current date and time
        Calendar calendar = Calendar.getInstance();

        // Update hours
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        hoursText.setText(String.format(Locale.getDefault(), "%02d", hours));

        // Update minutes
        int minutes = calendar.get(Calendar.MINUTE);
        minutesText.setText(String.format(Locale.getDefault(), "%02d", minutes));

        // Update day of week
        String dayOfWeek = calendar.getDisplayName(
                Calendar.DAY_OF_WEEK,
                Calendar.LONG,
                Locale.getDefault()
        );
        dayInfoText.setText(dayOfWeek);

        // Update date
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        dateText.setText(dateFormat.format(calendar.getTime()));
    }

    // Add this to handle automatic date updates
    private void startDateUpdater() {
        Handler handler = new Handler();
        Runnable dateUpdater = new Runnable() {
            @Override
            public void run() {
                updateDateTime();
                // Update every minute (60000 milliseconds)
                handler.postDelayed(this, 60000);
            }
        };
        handler.post(dateUpdater);
    }

    // Clean up handlers
    @Override
    protected void onDestroy() {
        clockHandler.removeCallbacks(clockUpdater);
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        super.onDestroy();
    }
}