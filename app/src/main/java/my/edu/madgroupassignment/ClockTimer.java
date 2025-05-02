package my.edu.madgroupassignment;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ClockTimer extends AppCompatActivity {

    private TextView hoursText, minutesText, colonText, dayInfoText, dateText; //Views
    private Button startButton, resetButton;
    private CountDownTimer countDownTimer;
    private boolean timerRunning;
    private long timeLeftInMillis = 0;
    private static final long DEFAULT_TIME = 25 * 60 * 1000; // 25 minutes in milliseconds

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
        colonText = findViewById(R.id.colonText);
        dayInfoText = findViewById(R.id.dayInfoText);
        startButton = findViewById(R.id.startButton);
        resetButton = findViewById(R.id.resetButton);
        dateText = findViewById(R.id.dateText);

        // Initialize bottom navigation
        setupBottomNavigation();

        // Set initial time
        updateCountDownText(DEFAULT_TIME);
        updateDayInfo();

        // Start live clock updates
        startClockUpdates();

        // Handle rotation
        checkOrientation();

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
        listBtn.setOnClickListener(v -> {
            int visibility = rewardBtn.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE;
            rewardBtn.setVisibility(visibility);
            searchBtn.setVisibility(visibility);
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
//        profileBtn.setOnClickListener(v -> {
//            Intent intent = new Intent(ClockTimer.this, Profile.class); //Replace with actual class
//            startActivity(intent);
//        });
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

    private void startTimer() {
        if (timeLeftInMillis == 0) {
            timeLeftInMillis = DEFAULT_TIME;
        }

        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountDownText(timeLeftInMillis);
                blinkColon();
            }

            @Override
            public void onFinish() {
                timerRunning = false;
                updateButtons();
                timeLeftInMillis = 0;
                updateCountDownText(0);
            }
        }.start();

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
        timeLeftInMillis = DEFAULT_TIME;
        updateCountDownText(timeLeftInMillis);
        updateButtons();
        colonText.setVisibility(View.VISIBLE); // Make sure colon is visible after reset
    }

    private void updateCountDownText(long timeInMillis) {
        int hours = (int) (timeInMillis / 1000) / 3600;
        int minutes = (int) ((timeInMillis / 1000) % 3600) / 60;
        int seconds = (int) (timeInMillis / 1000) % 60;

        if (hours > 0) {
            String timeLeftFormatted = String.format(Locale.getDefault(),
                    "%02d:%02d:%02d", hours, minutes, seconds);
            hoursText.setText(String.format("%02d", hours));
            minutesText.setText(String.format("%02d", minutes));
        } else {
            String timeLeftFormatted = String.format(Locale.getDefault(),
                    "%02d:%02d", minutes, seconds);
            hoursText.setText(String.format("%02d", minutes));
            minutesText.setText(String.format("%02d", seconds));
        }
    }

    private void blinkColon() {
        if (colonText.getVisibility() == View.VISIBLE) {
            colonText.setVisibility(View.INVISIBLE);
        } else {
            colonText.setVisibility(View.VISIBLE);
        }
    }

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