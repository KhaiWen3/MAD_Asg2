package my.edu.madgroupassignment;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.storage.*;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView imageView;
    private EditText editAge, editBirthday;
    private Button btnSave;
    private Uri selectedImageUri;

    private DatabaseReference userRef;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        imageView = findViewById(R.id.profile_image_edit);
        editAge = findViewById(R.id.edit_age);
        editBirthday = findViewById(R.id.edit_birthday);
        btnSave = findViewById(R.id.btn_save);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        storageRef = FirebaseStorage.getInstance().getReference("ProfileImages").child(userId + ".jpg");

        // Load current data
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    editAge.setText(snapshot.child("age").getValue(String.class));
                    editBirthday.setText(snapshot.child("birthday").getValue(String.class));
                    String imgUrl = snapshot.child("imageUrl").getValue(String.class);
                    if (imgUrl != null) {
                        Glide.with(EditProfileActivity.this).load(imgUrl).into(imageView);
                    }
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });

        // Select new image
        imageView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, 1001);
        });

        // Pick date
        editBirthday.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, year, month, dayOfMonth) ->
                            editBirthday.setText(dayOfMonth + "/" + (month + 1) + "/" + year),
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        // Save profile
        btnSave.setOnClickListener(v -> {
            Map<String, Object> updates = new HashMap<>();
            updates.put("age", editAge.getText().toString());
            updates.put("birthday", editBirthday.getText().toString());

            if (selectedImageUri != null) {
                storageRef.putFile(selectedImageUri).addOnSuccessListener(taskSnapshot -> {
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        updates.put("imageUrl", uri.toString());
                        userRef.updateChildren(updates);
                        Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                });
            } else {
                userRef.updateChildren(updates);
                Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            imageView.setImageURI(selectedImageUri);
        }
    }
}
