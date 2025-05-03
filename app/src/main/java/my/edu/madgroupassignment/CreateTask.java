package my.edu.madgroupassignment;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CreateTask extends AppCompatActivity {

    private Switch switchWorkLife;
    private Spinner spinnerCategory;
    private EditText etDescription, etCustomCategory, etDropdownItem;
    private Switch switchDropdown;
    private LinearLayout layoutDropdownItems;
    private Button btnAddItem;
    private ListView listViewItems;
    private TextView tvSelectedDate, tvSelectedTime;
    private ImageButton btnDatePicker, btnTimePicker;
    private Button btnCreate, btnCancel;

    private Calendar selectedDateTime = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private static final String CUSTOMIZE_OPTION = "Customize";

    private ArrayList<String> dropdownItems = new ArrayList<>();
    private ArrayAdapter<String> itemsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_task);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI components
        initializeViews();
        setupCategorySpinner();
        setupDropdownList();
        setupListeners();
    }

    private void initializeViews() {
        switchWorkLife = findViewById(R.id.switchWorkLife);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        etDescription = findViewById(R.id.etDescription);
        etCustomCategory = findViewById(R.id.etCustomCategory);
        switchDropdown = findViewById(R.id.switchDropdown);
        layoutDropdownItems = findViewById(R.id.layoutDropdownItems);
        etDropdownItem = findViewById(R.id.etDropdownItem);
        btnAddItem = findViewById(R.id.btnAddItem);
        listViewItems = findViewById(R.id.listViewItems);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvSelectedTime = findViewById(R.id.tvSelectedTime);
        btnDatePicker = findViewById(R.id.btnDatePicker);
        btnTimePicker = findViewById(R.id.btnTimePicker);
        btnCreate = findViewById(R.id.btnCreate);
        btnCancel = findViewById(R.id.btnCancel);

        // Initialize date and time display
        updateDateDisplay();
        updateTimeDisplay();
    }

    private void setupCategorySpinner() {
        List<String> categories = new ArrayList<>();
        categories.add("Education");
        categories.add("Shopping");
        categories.add("Meal");
        categories.add("Health");
        categories.add("Other");
        categories.add(CUSTOMIZE_OPTION);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        // Show/hide custom category field based on spinner selection
        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCategory = parent.getItemAtPosition(position).toString();
                if (selectedCategory.equals(CUSTOMIZE_OPTION)) {
                    etCustomCategory.setVisibility(View.VISIBLE);
                } else {
                    etCustomCategory.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                etCustomCategory.setVisibility(View.GONE);
            }
        });
    }

    private void setupDropdownList() {
        // Setup adapter for the list view
        itemsAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, dropdownItems);
        listViewItems.setAdapter(itemsAdapter);

        // Show/hide dropdown items section based on switch state
        switchDropdown.setOnCheckedChangeListener((buttonView, isChecked) -> {
            layoutDropdownItems.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        // Setup add button click listener
        btnAddItem.setOnClickListener(v -> {
            String item = etDropdownItem.getText().toString().trim();
            if (!item.isEmpty()) {
                dropdownItems.add(item);
                itemsAdapter.notifyDataSetChanged();
                etDropdownItem.setText("");
            } else {
                Toast.makeText(CreateTask.this, "Please enter an item", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        // Date picker
        btnDatePicker.setOnClickListener(v -> showDatePicker());

        // Time picker
        btnTimePicker.setOnClickListener(v -> showTimePicker());

        // Create button
        btnCreate.setOnClickListener(v -> createTask());

        // Cancel button
        btnCancel.setOnClickListener(v -> finish());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDayOfMonth) -> {
                    selectedDateTime.set(Calendar.YEAR, selectedYear);
                    selectedDateTime.set(Calendar.MONTH, selectedMonth);
                    selectedDateTime.set(Calendar.DAY_OF_MONTH, selectedDayOfMonth);

                    // Update the date display
                    updateDateDisplay();
                },
                year, month, day);

        datePickerDialog.show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, selectedHour, selectedMinute) -> {
                    selectedDateTime.set(Calendar.HOUR_OF_DAY, selectedHour);
                    selectedDateTime.set(Calendar.MINUTE, selectedMinute);

                    // Update the time display
                    updateTimeDisplay();
                },
                hour, minute, true);

        timePickerDialog.show();
    }

    private void updateDateDisplay() {
        tvSelectedDate.setText(dateFormat.format(selectedDateTime.getTime()));
    }

    private void updateTimeDisplay() {
        tvSelectedTime.setText(timeFormat.format(selectedDateTime.getTime()));
    }

    private void createTask() {
        // Validate input fields
        if (etDescription.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter a task description", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate custom category if selected
        String selectedCategory = spinnerCategory.getSelectedItem().toString();
        if (selectedCategory.equals(CUSTOMIZE_OPTION) && etCustomCategory.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter a custom category", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get all task data
        String taskType = switchWorkLife.isChecked() ? "Life" : "Work";

        // Use custom category if customize is selected
        String category = selectedCategory;
        if (selectedCategory.equals(CUSTOMIZE_OPTION)) {
            category = etCustomCategory.getText().toString().trim();
        }

        String description = etDescription.getText().toString().trim();
        boolean isDropdown = switchDropdown.isChecked();
        Date taskDate = selectedDateTime.getTime();

        // Create a Task object
        Task task = new Task(taskType, category, description, isDropdown, taskDate);

        // Add dropdown items if dropdown is enabled
        if (isDropdown && !dropdownItems.isEmpty()) {
            task.setDropdownItems(new ArrayList<>(dropdownItems));
        }

        // Save the task to Firebase
        saveTask(task);
    }

    private void saveTask(Task task) {
        // Check if user is logged in
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null ? 
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
                
        if (uid == null) {
            // No logged-in user, redirect to login
            Toast.makeText(CreateTask.this, "You need to login first", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(CreateTask.this, Login.class);
            startActivity(intent);
            finish();
            return;
        }
        
        // Get reference to Firebase database
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://madasg2-2e9c4-default-rtdb.firebaseio.com");
        
        // Reference to the current user's tasks
        DatabaseReference tasksRef = database.getReference("tasks").child(uid);

        // Use the task's unique ID as the key in Firebase
        DatabaseReference newTaskRef = tasksRef.child(task.getId());

        // Save the task to Firebase under the user's ID
        newTaskRef.setValue(task)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(CreateTask.this, "Task saved successfully!", Toast.LENGTH_SHORT).show();

                    // Navigate to DisplayTask activity
                    Intent intent = new Intent(CreateTask.this, DisplayTask.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish(); // Close the activity after successful save
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CreateTask.this, "Error saving task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    System.out.println("Firebase save error: " + e.getMessage());
                });

        // For demonstration, we'll still log the task details
        String taskDetails = "Task created: " +
                "\nType: " + task.getType() +
                "\nCategory: " + task.getCategory() +
                "\nDescription: " + task.getDescription() +
                "\nDropdown: " + (task.isDropdown() ? "Yes" : "No");

        // Log dropdown items if present
        if (task.isDropdown() && task.getDropdownItems() != null && !task.getDropdownItems().isEmpty()) {
            taskDetails += "\nDropdown Items: " + task.getDropdownItems().toString();
        }

        taskDetails += "\nDate & Time: " + dateFormat.format(task.getDateTime()) + " " +
                timeFormat.format(task.getDateTime());

        // Log the task details
        System.out.println(taskDetails);
    }
}