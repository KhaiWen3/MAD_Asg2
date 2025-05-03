package my.edu.madgroupassignment;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CreateTask extends AppCompatActivity {

    EditText titleEditText, descriptionEditText;
    Spinner notificationTimeSpinner;
    Button saveButton;

    DatabaseReference tasksRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);

        titleEditText = findViewById(R.id.titleEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        notificationTimeSpinner = findViewById(R.id.notificationTimeSpinner);
        saveButton = findViewById(R.id.saveButton);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        tasksRef = database.getReference("tasks");

        saveButton.setOnClickListener(v -> {
            Log.d("CreateTask", "Button clicked");
            saveTask();
        });
    }

    private void saveTask() {
        String title = titleEditText.getText().toString();
        String description = descriptionEditText.getText().toString();
        String notificationTime = notificationTimeSpinner.getSelectedItem().toString();
        long currentTime = System.currentTimeMillis();
        long delayMillis = getDelayFromSpinner(notificationTime); // 自定义方法
        long triggerTime = currentTime + delayMillis;

        scheduleNotification(title, triggerTime);

        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter title", Toast.LENGTH_SHORT).show();
            return;
        }

        String taskId = tasksRef.push().getKey();
        Task task = new Task(title, description, notificationTime);

        tasksRef.child(taskId).setValue(task);

        Toast.makeText(this, "Task saved", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(CreateTask.this, TaskList.class));
        finish();
    }

    private long getDelayFromSpinner(String option) {
        switch (option) {
            case "10 minutes before": return 10 * 60 * 1000;
            case "30 minutes before": return 30 * 60 * 1000;
            case "1 hour before": return 60 * 60 * 1000;
            default: return 0; // No delay
        }
    }

    private void scheduleNotification(String title, long triggerTimeMillis) {
        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.putExtra("title", title);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent);
    }
}
