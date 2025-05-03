package my.edu.madgroupassignment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mAuth = FirebaseAuth.getInstance();

        // Initialize logo view
        ImageView logo = findViewById(R.id.logoImageView);

        // Load and start pop-out animation
        Animation popOut = AnimationUtils.loadAnimation(this, R.anim.pop_out);
        logo.startAnimation(popOut);

        // Navigate after delay
        new Handler().postDelayed(() -> {
            FirebaseUser currentUser = mAuth.getCurrentUser();

            if (currentUser != null) {
                // User is signed in, go to main/clock/home activity
                Intent intent = new Intent(SplashActivity.this, DisplayTask.class); // Or ClockTimer if preferred
                startActivity(intent);
            } else {
                // No user signed in, go to login screen
                Intent intent = new Intent(SplashActivity.this, Login.class);
                startActivity(intent);
            }

            finish(); // Close SplashActivity
        }, 2000); // 2 seconds delay
    }
}
