package my.edu.madgroupassignment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ContactActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Contact Us");
        }

        getSupportActionBar().setTitle("Contact Us"); // Optional: set title

        // Facebook button
        LinearLayout facebookButton = findViewById(R.id.facebook_button);
        facebookButton.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/profile.php?id=100001276067746"));
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Facebook app not installed", Toast.LENGTH_SHORT).show();
            }
        });

        // Instagram button
        LinearLayout instagramButton = findViewById(R.id.instagram_button);
        instagramButton.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/yong_yong2002"));
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Instagram app not installed", Toast.LENGTH_SHORT).show();
            }
        });

        // Whatsapp button
        LinearLayout whatsappButton = findViewById(R.id.whatsapp_button);
        whatsappButton.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://wa.me/601113069816")); // Replace with your actual number like "60123456789"
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "WhatsApp app not installed", Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Closes this activity and returns to the previous one
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}