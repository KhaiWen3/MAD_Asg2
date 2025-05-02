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
    private EditText editUsername, editPhone, editAge, editBirthday;
    private Button btnSave;
    private Uri selectedImageUri;

    private DatabaseReference userRef;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        imageView = findViewById(R.id.profile_image_edit);
        editUsername = findViewById(R.id.edit_username);
        editPhone = findViewById(R.id.edit_phone);
        editAge = findViewById(R.id.edit_age);
        editBirthday = findViewById(R.id.edit_birthday);
        btnSave = findViewById(R.id.btn_save);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        storageRef = FirebaseStorage.getInstance().getReference("ProfileImages").child(userId + ".jpg");

        // Load current user data
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    editUsername.setText(snapshot.child("username").getValue(String.class));
                    editPhone.setText(snapshot.child("phone").getValue(String.class));
                    editAge.setText(snapshot.child("age").getValue(String.class));
                    editBirthday.setText(snapshot.child("birthday").getValue(String.class));
                    String imgUrl = snapshot.child("imageUrl").getValue(String.class);
                    if (imgUrl != null) {
                        Glide.with(EditProfileActivity.this).load(imgUrl).into(imageView);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditProfileActivity.this, "Failed to load profile.", Toast.LENGTH_SHORT).show();
            }
        });

        // Pick image from gallery
        imageView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, 1001);
        });

        // Date picker for birthday
        editBirthday.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, year, month, dayOfMonth) -> editBirthday.setText(dayOfMonth + "/" + (month + 1) + "/" + year),
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        // Save profile button logic
        btnSave.setOnClickListener(v -> {
            String username = editUsername.getText().toString().trim();
            String phone = editPhone.getText().toString().trim();
            String age = editAge.getText().toString().trim();
            String birthday = editBirthday.getText().toString().trim();

            // Validation
            if (username.isEmpty()) {
                editUsername.setError("Username is required");
                editUsername.requestFocus();
                return;
            }

            if (phone.isEmpty()) {
                editPhone.setError("Phone number is required");
                editPhone.requestFocus();
                return;
            }

            // Regex for format like 011-13069816
            if (!phone.matches("^\\d{3}-\\d{8}$")) {
                editPhone.setError("Phone format must be like 012-52389245");
                editPhone.requestFocus();
                return;
            }


            if (age.isEmpty()) {
                editAge.setError("Age is required");
                editAge.requestFocus();
                return;
            }

            try {
                int ageInt = Integer.parseInt(age);
                if (ageInt <= 0 || ageInt > 120) {
                    editAge.setError("Enter a valid age");
                    editAge.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                editAge.setError("Age must be a number");
                editAge.requestFocus();
                return;
            }

            if (birthday.isEmpty()) {
                editBirthday.setError("Birthday is required");
                editBirthday.requestFocus();
                return;
            }

            Map<String, Object> updates = new HashMap<>();
            updates.put("username", username);
            updates.put("phone", phone);
            updates.put("age", age);
            updates.put("birthday", birthday);

            if (selectedImageUri != null) {
                storageRef.putFile(selectedImageUri).addOnSuccessListener(taskSnapshot ->
                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            updates.put("imageUrl", uri.toString());
                            userRef.updateChildren(updates);
                            setResult(RESULT_OK);
                            finish();
                        })
                );
            } else {
                userRef.updateChildren(updates);
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    // Handle selected image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            imageView.setImageURI(selectedImageUri);
        }
    }
}
