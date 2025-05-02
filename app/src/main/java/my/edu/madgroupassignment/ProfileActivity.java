package my.edu.madgroupassignment;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import java.util.Calendar;


import androidx.appcompat.app.AppCompatActivity;
public class ProfileActivity extends AppCompatActivity {

    private Switch switchNotification;
    private LinearLayout helpItem, faqItem, contactItem;
    private TextView signOutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile); // Make sure this matches your XML file name

        // Initialize views
        switchNotification = findViewById(R.id.switch_notification);
        signOutBtn = findViewById(R.id.sign_out);

        // Find layout items (Help, FAQ, Contact Us)
        helpItem = findViewById(R.id.help_item);
        faqItem = findViewById(R.id.faq_item);
        contactItem = findViewById(R.id.contact_item);

        setupBottomNavigation();

        // Handle Notification Switch
        switchNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Toast.makeText(ProfileActivity.this, "Notifications Enabled", Toast.LENGTH_SHORT).show();
                scheduleNotification();
            } else {
                Toast.makeText(ProfileActivity.this, "Notifications Disabled", Toast.LENGTH_SHORT).show();
                cancelNotification();
            }
        });



        // Click listeners
        if (helpItem != null)
            helpItem.setOnClickListener(view -> openHelp());

        if (faqItem != null)
            faqItem.setOnClickListener(view -> openFAQ());

        if (contactItem != null)
            contactItem.setOnClickListener(view -> openContact());

        signOutBtn.setOnClickListener(view -> {
            Toast.makeText(ProfileActivity.this, "Signed Out", Toast.LENGTH_SHORT).show();
            // Perform sign out logic here
            // For example: clear SharedPreferences, go back to login screen
            finish(); // Or redirect to LoginActivity
        });
    }

    private void openHelp() {
        Toast.makeText(this, "Opening Help...", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, HelpActivity.class));
    }

    private void openFAQ() {
        Toast.makeText(this, "Opening FAQ...", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, FaqActivity.class));
    }

    private void openContact() {
        Toast.makeText(this, "Opening Contact Us...", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, ContactActivity.class));
    }

    private void scheduleNotification() {
        // Set your event time here
        Calendar eventTime = Calendar.getInstance();
        eventTime.set(Calendar.HOUR_OF_DAY, 15); // 3 PM
        eventTime.set(Calendar.MINUTE, 0);
        eventTime.set(Calendar.SECOND, 0);

        // Schedule 1 hour before the event
        eventTime.add(Calendar.HOUR_OF_DAY, -1);

        long triggerAtMillis = eventTime.getTimeInMillis();

        Intent intent = new Intent(this, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        }
    }

    private void cancelNotification() {
        Intent intent = new Intent(this, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
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
        profileBtn.setEnabled(false);
        profileBtn.setAlpha(0.5f);

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

        clockBtn.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ClockTimer.class); // Replace 'CurrentActivity' with your current context
            startActivity(intent);
        });

    }

}