package my.edu.madgroupassignment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialize logo view
        ImageView logo = findViewById(R.id.logoImageView);

        // Load and start pop-out animation
        Animation popOut = AnimationUtils.loadAnimation(this, R.anim.pop_out);
        logo.startAnimation(popOut);

        // Navigate to MainActivity after delay
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, ClockTimer.class);
            startActivity(intent);
            finish(); // Finish SplashActivity so it won't return on back press
        }, 2000); // 2 seconds delay
    }
}
