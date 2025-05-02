package my.edu.madgroupassignment;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Register extends AppCompatActivity {
    private EditText emailEditText, passwordEditText;
    private Button register_Button;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference(); // Initialize Database reference

        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        register_Button = findViewById(R.id.register_button);

        register_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    private void registerUser() {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        if (email.isEmpty()) {
            emailEditText.setError("Email cannot be empty");
            emailEditText.requestFocus();
            return;
        }

        // 添加电子邮件格式验证
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Please enter a valid email address");
            emailEditText.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            passwordEditText.setError("Password cannot be empty");
            passwordEditText.requestFocus();
            return;
        }

        // 添加密码强度检查，密码至少6位
        if (password.length() < 6) {
            passwordEditText.setError("Password must be at least 6 characters long");
            passwordEditText.requestFocus();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // 用户注册成功，接着存储用户数据
                String userId = mAuth.getCurrentUser().getUid(); // 获取用户ID
                User user = new User(email); // 创建用户对象
                mDatabase.child("users").child(userId).setValue(user) // 存储用户数据
                        .addOnCompleteListener(dbTask -> {
                            if (dbTask.isSuccessful()) {
                                Toast.makeText(Register.this, "User data saved", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(Register.this, "Failed to save user data", Toast.LENGTH_SHORT).show();
                            }
                        });
                finish(); // 可选，注册成功后返回登录界面
            } else {
                String errorMessage = task.getException() != null ? task.getException().getMessage() : "Registration failed";
                Toast.makeText(Register.this, "Registration failed: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
    public static class User {
        public String email;

        // 无参构造器，Firebase 需要
        public User() {}

        // 带参构造器
        public User(String email) {
            this.email = email;
        }
    }
}