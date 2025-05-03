package my.edu.madgroupassignment;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Logger;

public class MainApplication extends Application {
    private static final String TAG = "MainApplication";

    @Override
    public void onCreate() {
        super.onCreate();

        // Configure Firebase
        try {
            Log.d(TAG, "Starting Firebase configuration");

            // Enable Firebase persistence for offline capabilities - ONLY ONCE
            try {
                FirebaseDatabase.getInstance("https://madasg2-2e9c4-default-rtdb.firebaseio.com").setPersistenceEnabled(true);
                Log.d(TAG, "Firebase persistence enabled");
            } catch (Exception e) {
                Log.e(TAG, "Error enabling persistence: " + e.getMessage());
            }

            // Enable verbose Firebase logging
            FirebaseDatabase.getInstance("https://madasg2-2e9c4-default-rtdb.firebaseio.com").setLogLevel(Logger.Level.DEBUG);
            Log.d(TAG, "Firebase logging set to DEBUG");

            // Make sure the database URL is set explicitly to fix connection issues
            FirebaseDatabase database = FirebaseDatabase.getInstance("https://madasg2-2e9c4-default-rtdb.firebaseio.com");

            // Test database connection
            DatabaseReference connectedRef = database.getReference(".info/connected");
            connectedRef.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
                @Override
                public void onDataChange(@androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot) {
                    boolean connected = Boolean.TRUE.equals(snapshot.getValue(Boolean.class));
                    Log.d(TAG, "Firebase connection state: " + (connected ? "CONNECTED" : "DISCONNECTED"));
                }

                @Override
                public void onCancelled(@androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {
                    Log.e(TAG, "Firebase connection listener cancelled: " + error.getMessage());
                }
            });

            Log.d(TAG, "Firebase URL: " + database.getReference().toString());
            Log.d(TAG, "Firebase configured successfully with persistence enabled");
        } catch (Exception e) {
            // Don't crash if Firebase is already configured
            Log.e(TAG, "Error configuring Firebase: " + e.getMessage(), e);
        }
    }
}