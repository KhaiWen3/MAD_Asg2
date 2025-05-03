package my.edu.madgroupassignment;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.storage.*;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PICK_IMAGE = 1001;
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 100;

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


        // Handle image selection with permission
        imageView.setOnClickListener(v -> {
            if (checkStoragePermission()) {
                openImagePicker();
            } else {
                requestStoragePermission();
            }
        });

        // Date picker
        editBirthday.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, year, month, dayOfMonth) -> editBirthday.setText(dayOfMonth + "/" + (month + 1) + "/" + year),
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        // Save profile
        btnSave.setOnClickListener(v -> saveProfile());

    }

    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true;  // Permissions automatically granted for versions lower than Marshmallow
    }


    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission Needed")
                    .setMessage("Permission is needed to access your gallery.")
                    .setPositiveButton("OK", (dialog, which) ->
                            ActivityCompat.requestPermissions(this,
                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                    REQUEST_CODE_STORAGE_PERMISSION))
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_CODE_STORAGE_PERMISSION);
        }
    }



    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    private void saveProfile() {
        String username = editUsername.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();
        String age = editAge.getText().toString().trim();
        String birthday = editBirthday.getText().toString().trim();

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
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("updatedUsername", username); // Pass updated username
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    })
            );
        } else {
            userRef.updateChildren(updates);
            Intent resultIntent = new Intent();
            resultIntent.putExtra("updatedUsername", username); // Pass updated username
            setResult(RESULT_OK, resultIntent);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            imageView.setImageURI(selectedImageUri);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                Toast.makeText(this, "Permission denied to access storage.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
