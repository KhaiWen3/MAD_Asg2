package my.edu.madgroupassignment;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;

import java.util.Calendar;
public class ProfileActivity extends AppCompatActivity {

    private Switch switchNotification;
    private LinearLayout helpItem, faqItem, contactItem;
    private TextView signOutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile); // Make sure this matches your XML file name

        ImageView profileImage = findViewById(R.id.profile_image);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        ref.child("imageUrl").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                String url = snapshot.getValue(String.class);
                if (url != null) {
                    Glide.with(ProfileActivity.this).load(url).into(profileImage);
                }
            }

            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });


        profileImage.setOnClickListener(view -> {
            startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class));
        });


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

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            // No logged-in user, redirect to login
            Intent intent = new Intent(this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }



        // Click listeners
        if (helpItem != null)
            helpItem.setOnClickListener(view -> openHelp());

        if (faqItem != null)
            faqItem.setOnClickListener(view -> openFAQ());

        if (contactItem != null)
            contactItem.setOnClickListener(view -> openContact());

        signOutBtn.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut(); // Sign out from Firebase
            Toast.makeText(ProfileActivity.this, "Signed Out", Toast.LENGTH_SHORT).show();

            // Redirect to Login activity and clear back stack
            Intent intent = new Intent(ProfileActivity.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish(); // Finish current activity
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

    // Sign out method
    private void signOut() {
        // Show progress bar or indicator to show user is being logged out
        Toast.makeText(ProfileActivity.this, "Logging out...", Toast.LENGTH_SHORT).show();

        // Perform Firebase logout asynchronously
        FirebaseAuth.getInstance().signOut();

        // Redirect to login activity
        Intent intent = new Intent(ProfileActivity.this, Login.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            if (data != null) {
                String updatedUsername = data.getStringExtra("updatedUsername");
                TextView usernameTextView = findViewById(R.id.username_textview);  // Assuming you have a TextView to show username
                if (updatedUsername != null) {
                    usernameTextView.setText(updatedUsername); // Set the updated username
                }
            }
        }
    }


    //Bottom Navigation
    private void setupBottomNavigation() {
        ImageButton clockBtn = findViewById(R.id.clockButton);
        ImageButton homeBtn = findViewById(R.id.homeButton);
        ImageButton profileBtn = findViewById(R.id.profileButton);

        // Disable current page button
        profileBtn.setEnabled(false);
        profileBtn.setAlpha(0.5f);


       homeBtn.setOnClickListener(v -> {
           Intent intent = new Intent(ProfileActivity.this, DisplayTask.class); // Change to your actual home activity
            startActivity(intent);
        });

        clockBtn.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ClockTimer.class); // Replace 'CurrentActivity' with your current context
            startActivity(intent);
        });

    }

}