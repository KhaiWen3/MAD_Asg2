package my.edu.madgroupassignment;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Logger;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DisplayTask extends AppCompatActivity {
    private static final String TAG = "DisplayTask";

    private RecyclerView recyclerViewTasks;
    private TaskAdapter taskAdapter;
    private List<Task> allTasks = new ArrayList<>();
    private List<Task> filteredTasks = new ArrayList<>();

    private Button btnAll, btnWork, btnLife;
    private TextView tvTodayTasks;
    private TextView tvNoTasks;
    private FloatingActionButton fabAddTask;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private SearchView searchView;

    private String currentFilter = "All";
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    // Firebase database reference
    private DatabaseReference tasksRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_task);

        // Check network connectivity first
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No internet connection. Using cached data.", Toast.LENGTH_LONG).show();
        }

        // Setup Firebase with offline persistence
        setupFirebase();

        Toast.makeText(this, "Welcome to Task Manager", Toast.LENGTH_SHORT).show();

        initializeViews();
        setupRecyclerView();
        setupListeners();
        setupSwipeRefresh();
        setupBottomNavigation();

        // Test Firebase connection with a simple write/read
        testFirebaseConnection();

        // Initial load of tasks
        loadTasksFromFirebase();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                getSystemService(CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }

    private void setupFirebase() {
        // Don't try to re-enable persistence here, it's already done in MainApplication
        try {
            // Get the current user ID
            String uid = FirebaseAuth.getInstance().getCurrentUser() != null ? 
                    FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
            
            if (uid == null) {
                // No logged-in user, redirect to login
                Toast.makeText(this, "You need to login first", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, Login.class);
                startActivity(intent);
                finish();
                return;
            }
            
            // Get reference to the tasks node with the correct database URL
            FirebaseDatabase database = FirebaseDatabase.getInstance("https://madasg2-2e9c4-default-rtdb.firebaseio.com");
            // Get reference to the current user's tasks only
            tasksRef = database.getReference("tasks").child(uid);

            // Keep cached data synced
            tasksRef.keepSynced(true);

            // Debug info
            Log.d(TAG, "Firebase database URL: " + database.getReference().toString());
            Log.d(TAG, "Tasks reference path for user " + uid + ": " + tasksRef.toString());

            // Verify Firebase connection
            verifyFirebaseConnection();

            // Debug Firebase structure
            debugFirebaseStructure();
        } catch (Exception e) {
            Log.e(TAG, "Error setting up Firebase: " + e.getMessage());
            Toast.makeText(this, "Error connecting to database: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // We don't need to reload tasks here since we're using ValueEventListener
        // which will automatically update when data changes
        Log.d(TAG, "onResume: Waiting for Firebase updates");
    }

    private void initializeViews() {
        recyclerViewTasks = findViewById(R.id.recyclerViewTasks);
        btnAll = findViewById(R.id.btnAll);
        btnWork = findViewById(R.id.btnWork);
        btnLife = findViewById(R.id.btnLife);
        tvTodayTasks = findViewById(R.id.tvTodayTasks);
        fabAddTask = findViewById(R.id.fabAddTask);
        progressBar = findViewById(R.id.progressBar);
        tvNoTasks = findViewById(R.id.tvNoTasks);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        searchView = findViewById(R.id.searchView);
    }

    private void setupRecyclerView() {
        // Initialize adapter with empty list first
        filteredTasks = new ArrayList<>();
        taskAdapter = new TaskAdapter(filteredTasks);

        // Set layout manager
        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(this));

        // Set adapter
        recyclerViewTasks.setAdapter(taskAdapter);

        // Add some padding for better appearance
        recyclerViewTasks.setPadding(0, 8, 0, 8);

        // Improve scrolling performance
        recyclerViewTasks.setHasFixedSize(true);

        Log.d(TAG, "RecyclerView setup complete");
    }

    private void setupListeners() {
        // Filter buttons
        btnAll.setOnClickListener(v -> {
            filterTasks("All");
            updateFilterButtonAppearance("All");
        });

        btnWork.setOnClickListener(v -> {
            filterTasks("Work");
            updateFilterButtonAppearance("Work");
        });

        btnLife.setOnClickListener(v -> {
            filterTasks("Life");
            updateFilterButtonAppearance("Life");
        });

        // FAB to add new task
        fabAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(DisplayTask.this, CreateTask.class);
            startActivity(intent);
        });
        
        // Configure search view
        searchView.setIconifiedByDefault(false);
        searchView.setQueryHint("Search tasks by name...");
        
        // Handler for delayed search (to avoid executing search on every keystroke)
        final Handler searchHandler = new Handler();
        final long SEARCH_DELAY_MS = 300; // 300ms delay
        final Runnable searchRunnable = new Runnable() {
            @Override
            public void run() {
                String query = searchView.getQuery().toString();
                searchTasks(query);
            }
        };
        
        // Search listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Remove callbacks to prevent delayed execution
                searchHandler.removeCallbacks(searchRunnable);
                searchTasks(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Remove any pending searches
                searchHandler.removeCallbacks(searchRunnable);
                
                // If empty, reset immediately
                if (newText.isEmpty()) {
                    searchTasks("");
                    return true;
                }
                
                // Otherwise, add a delay before searching to improve performance
                searchHandler.postDelayed(searchRunnable, SEARCH_DELAY_MS);
                return true;
            }
        });
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setColorSchemeColors(
                getResources().getColor(android.R.color.holo_blue_bright, null),
                getResources().getColor(android.R.color.holo_green_light, null),
                getResources().getColor(android.R.color.holo_orange_light, null),
                getResources().getColor(android.R.color.holo_red_light, null)
        );

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d(TAG, "Performing refresh...");
                Toast.makeText(DisplayTask.this, "Refreshing tasks...", Toast.LENGTH_SHORT).show();
                refreshTasks();
            }
        });
    }

    private void refreshTasks() {
        // Clear current tasks
        allTasks.clear();
        filteredTasks.clear();
        taskAdapter.notifyDataSetChanged();

        // Reset view states
        showLoading(true);

        // Reload from Firebase
        loadTasksFromFirebase();

        // Add a slight delay for better UX
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Stop the refresh animation
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        }, 1000);
    }

    private void updateFilterButtonAppearance(String filter) {
        currentFilter = filter;

        // Reset all buttons
        btnAll.setBackgroundTintList(getColorStateList(android.R.color.white));
        btnAll.setTextColor(getColor(android.R.color.black));

        btnWork.setBackgroundTintList(getColorStateList(android.R.color.white));
        btnWork.setTextColor(getColor(android.R.color.black));

        btnLife.setBackgroundTintList(getColorStateList(android.R.color.white));
        btnLife.setTextColor(getColor(android.R.color.black));

        // Set selected button appearance
        switch (filter) {
            case "All":
                btnAll.setBackgroundTintList(getColorStateList(R.color.colorPrimary));
                btnAll.setTextColor(getColor(android.R.color.white));
                break;
            case "Work":
                btnWork.setBackgroundTintList(getColorStateList(R.color.colorPrimary));
                btnWork.setTextColor(getColor(android.R.color.white));
                break;
            case "Life":
                btnLife.setBackgroundTintList(getColorStateList(R.color.colorPrimary));
                btnLife.setTextColor(getColor(android.R.color.white));
                break;
        }
    }

    private void verifyFirebaseConnection() {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://madasg2-2e9c4-default-rtdb.firebaseio.com");
        database.getReference(".info/connected").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean connected = snapshot.getValue(Boolean.class);
                if (connected != null && connected) {
                    Log.d(TAG, "Firebase connection established");
                    // Try loading tasks again if we've connected
                    if (allTasks.isEmpty()) {
                        loadTasksFromFirebase();
                    }
                } else {
                    Log.e(TAG, "Firebase connection failed");
                    Toast.makeText(DisplayTask.this,
                            "Cannot connect to Firebase. Check your internet connection.",
                            Toast.LENGTH_LONG).show();

                    // Show local data if available
                    updateEmptyState();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Firebase connection check error: " + error.getMessage());
                Toast.makeText(DisplayTask.this,
                        "Connection error: " + error.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void debugFirebaseStructure() {
        // Check the root structure of your Firebase database
        FirebaseDatabase.getInstance("https://madasg2-2e9c4-default-rtdb.firebaseio.com").getReference().addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.d(TAG, "Firebase root structure:");
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Log.d(TAG, "- Root node: " + child.getKey() + " (has " +
                                    child.getChildrenCount() + " children)");
                        }

                        // Specifically check the tasks node
                        if (snapshot.hasChild("tasks")) {
                            Log.d(TAG, "Found 'tasks' node with " +
                                    snapshot.child("tasks").getChildrenCount() + " tasks");
                        } else {
                            Log.e(TAG, "No 'tasks' node found in Firebase database!");
                            Toast.makeText(DisplayTask.this,
                                    "No tasks node found in Firebase. Create a task first.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error debugging Firebase structure: " + error.getMessage());
                    }
                }
        );
    }

    private void loadTasksFromFirebase() {
        showLoading(true);
        Toast.makeText(this, "Loading tasks...", Toast.LENGTH_SHORT).show();

        // Set a timeout for Firebase data loading
        final boolean[] dataReceived = {false};

        // Create timeout handler to show offline data if Firebase doesn't respond
        android.os.Handler handler = new android.os.Handler();
        handler.postDelayed(() -> {
            if (!dataReceived[0]) {
                Log.w(TAG, "Firebase data request timed out, showing cached data");
                Toast.makeText(DisplayTask.this, "Using cached data", Toast.LENGTH_SHORT).show();
                showLoading(false);
                updateEmptyState();
            }
        }, 5000); // 5 second timeout

        // Add debug log
        Log.d(TAG, "Starting Firebase data fetch from: " + tasksRef.toString());

        // Use ValueEventListener instead of SingleValueEvent to get real-time updates
        tasksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dataReceived[0] = true;
                Log.d(TAG, "onDataChange triggered, snapshot exists: " + snapshot.exists());
                Log.d(TAG, "Number of tasks: " + snapshot.getChildrenCount());

                if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                    // No tasks found in Firebase
                    Log.d(TAG, "No tasks found in Firebase database");
                    Toast.makeText(DisplayTask.this, "No tasks found in database", Toast.LENGTH_SHORT).show();
                    showLoading(false);
                    updateEmptyState();
                    return;
                }

                // Clear existing tasks
                allTasks.clear();

                // Debug raw data
                for (DataSnapshot taskSnapshot : snapshot.getChildren()) {
                    Log.d(TAG, "Task key: " + taskSnapshot.getKey());
                    StringBuilder debugData = new StringBuilder("Raw data: {");
                    for (DataSnapshot field : taskSnapshot.getChildren()) {
                        debugData.append(field.getKey()).append(":").append(field.getValue()).append(", ");
                    }
                    debugData.append("}");
                    Log.d(TAG, debugData.toString());

                    try {
                        // Try direct getValue first
                        Task task = new Task();
                        task.setId(taskSnapshot.getKey());

                        // Extract values directly with proper null checking
                        if (taskSnapshot.hasChild("type")) {
                            task.setType(String.valueOf(taskSnapshot.child("type").getValue()));
                        }

                        if (taskSnapshot.hasChild("category")) {
                            task.setCategory(String.valueOf(taskSnapshot.child("category").getValue()));
                        }

                        if (taskSnapshot.hasChild("description")) {
                            task.setDescription(String.valueOf(taskSnapshot.child("description").getValue()));
                        }

                        if (taskSnapshot.hasChild("isDropdown")) {
                            task.setDropdown(Boolean.TRUE.equals(taskSnapshot.child("isDropdown").getValue(Boolean.class)));
                        }

                        if (taskSnapshot.hasChild("timestamp")) {
                            Object timestampObj = taskSnapshot.child("timestamp").getValue();
                            if (timestampObj instanceof Long) {
                                task.setTimestamp((Long) timestampObj);
                            } else if (timestampObj != null) {
                                try {
                                    task.setTimestamp(Long.parseLong(timestampObj.toString()));
                                } catch (NumberFormatException e) {
                                    task.setTimestamp(System.currentTimeMillis());
                                    Log.e(TAG, "Error parsing timestamp: " + e.getMessage());
                                }
                            }
                        }

                        if (taskSnapshot.hasChild("isCompleted")) {
                            task.setCompleted(Boolean.TRUE.equals(taskSnapshot.child("isCompleted").getValue(Boolean.class)));
                        }

                        if (taskSnapshot.hasChild("dropdownItems") && taskSnapshot.child("dropdownItems").getValue() != null) {
                            List<String> items = new ArrayList<>();
                            for (DataSnapshot itemSnapshot : taskSnapshot.child("dropdownItems").getChildren()) {
                                String item = String.valueOf(itemSnapshot.getValue());
                                items.add(item);
                            }
                            task.setDropdownItems(items);
                        }
                        
                        // Fix any null fields
                        task.fixNullFields();

                        // Validate that we have at least description
                        if (task.getDescription() != null && !task.getDescription().isEmpty()) {
                            allTasks.add(task);
                            Log.d(TAG, "Added task: " + task.toString());
                        } else {
                            Log.w(TAG, "Skipping task with empty description, ID: " + task.getId());
                        }

                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing task: " + e.getMessage(), e);
                    }
                }

                Log.d(TAG, "Successfully loaded " + allTasks.size() + " tasks");

                // Sort tasks
                sortTasksByDate();

                // Make sure we apply any sorting/modifications to the list before filtering
                if (allTasks.isEmpty()) {
                    Log.w(TAG, "No tasks to display after loading");
                    filteredTasks.clear();
                    runOnUiThread(() -> {
                        taskAdapter.updateTasks(new ArrayList<>());
                        updateEmptyState();
                    });
                } else {
                    // Apply current filter
                    filterTasks(currentFilter);

                    // Update date headers
                    updateDateHeaders();

                    // Show a toast notification if tasks were successfully loaded
                    runOnUiThread(() -> {
                        Toast.makeText(DisplayTask.this, "Loaded " + allTasks.size() + " tasks", Toast.LENGTH_SHORT).show();
                    });
                }

                showLoading(false);
                updateEmptyState();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dataReceived[0] = true;
                Log.e(TAG, "Firebase error: " + error.getMessage());
                Toast.makeText(DisplayTask.this,
                        "Error loading tasks: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
                showLoading(false);
                updateEmptyState();
            }
        });
    }

    private void showLoading(boolean isLoading) {
        if (progressBar == null || recyclerViewTasks == null || tvNoTasks == null || swipeRefreshLayout == null) {
            Log.e(TAG, "UI components not initialized");
            return;
        }

        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            recyclerViewTasks.setVisibility(View.GONE);
            tvNoTasks.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);

            if (filteredTasks.isEmpty()) {
                recyclerViewTasks.setVisibility(View.GONE);
                tvNoTasks.setVisibility(View.VISIBLE);
            } else {
                recyclerViewTasks.setVisibility(View.VISIBLE);
                tvNoTasks.setVisibility(View.GONE);
            }

            // Log visibility states for debugging
            Log.d(TAG, "RecyclerView visibility: " +
                    (recyclerViewTasks.getVisibility() == View.VISIBLE ? "VISIBLE" : "GONE/INVISIBLE"));
            Log.d(TAG, "NoTasks visibility: " +
                    (tvNoTasks.getVisibility() == View.VISIBLE ? "VISIBLE" : "GONE/INVISIBLE"));
            Log.d(TAG, "FilteredTasks size: " + filteredTasks.size());

            // Ensure the refresh indicator is stopped
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
        }
    }

    private void updateEmptyState() {
        if (recyclerViewTasks == null || tvNoTasks == null) {
            Log.e(TAG, "UI components not initialized in updateEmptyState");
            return;
        }

        if (filteredTasks.isEmpty()) {
            recyclerViewTasks.setVisibility(View.GONE);
            tvNoTasks.setVisibility(View.VISIBLE);
        } else {
            recyclerViewTasks.setVisibility(View.VISIBLE);
            tvNoTasks.setVisibility(View.GONE);
        }

        // Log visibility states for debugging
        Log.d(TAG, "RecyclerView visibility: " +
                (recyclerViewTasks.getVisibility() == View.VISIBLE ? "VISIBLE" : "GONE/INVISIBLE"));
        Log.d(TAG, "NoTasks visibility: " +
                (tvNoTasks.getVisibility() == View.VISIBLE ? "VISIBLE" : "GONE/INVISIBLE"));
        Log.d(TAG, "FilteredTasks size: " + filteredTasks.size());
    }

    private void sortTasksByDate() {
        Collections.sort(allTasks, (task1, task2) -> {
            Date date1 = task1.getDateTime();
            Date date2 = task2.getDateTime();
            return date1.compareTo(date2);
        });
    }

    private void filterTasks(String filter) {
        Log.d(TAG, "Filtering tasks with filter: " + filter + ", allTasks size: " + allTasks.size());

        // Create a new list for filtered tasks
        List<Task> newFilteredTasks = new ArrayList<>();

        if (filter.equals("All")) {
            newFilteredTasks.addAll(allTasks);
        } else {
            for (Task task : allTasks) {
                if (task.getType().equals(filter)) {
                    newFilteredTasks.add(task);
                }
            }
        }

        // Update the class member list
        filteredTasks.clear();
        filteredTasks.addAll(newFilteredTasks);

        Log.d(TAG, "Filtered tasks count (" + filter + "): " + filteredTasks.size());

        // Apply any active search query
        CharSequence query = searchView != null ? searchView.getQuery() : "";
        if (query.length() > 0) {
            searchTasks(query.toString());
        } else {
            // Update the adapter with the new list
            if (taskAdapter != null) {
                taskAdapter.updateTasks(new ArrayList<>(filteredTasks));
            } else {
                Log.e(TAG, "taskAdapter is null in filterTasks");
            }

            // Explicitly update UI visibility based on filtered results
            updateEmptyState();
        }
    }

    private void searchTasks(String query) {
        Log.d(TAG, "Searching tasks with query: " + query);
        String lowerCaseQuery = query.toLowerCase().trim();
        
        if (lowerCaseQuery.isEmpty()) {
            // If search query is empty, just show filtered tasks based on current filter
            if (taskAdapter != null) {
                taskAdapter.updateTasks(new ArrayList<>(filteredTasks));
            }
            updateEmptyState();
            return;
        }
        
        // Create a new list for search results
        List<Task> searchResults = new ArrayList<>();
        
        // Create separate lists for exact and partial matches
        List<Task> exactNameMatches = new ArrayList<>();
        List<Task> partialNameMatches = new ArrayList<>();
        List<Task> otherMatches = new ArrayList<>();
        
        // Filter tasks based on query
        for (Task task : filteredTasks) {
            String taskDescription = task.getDescription().toLowerCase();
            
            // Check for exact name match (highest priority)
            if (taskDescription.equals(lowerCaseQuery)) {
                exactNameMatches.add(task);
            }
            // Check for partial name match (description contains the query)
            else if (taskDescription.contains(lowerCaseQuery)) {
                partialNameMatches.add(task);
            }
            // Check other fields (category, type)
            else if ((task.getCategory() != null && task.getCategory().toLowerCase().contains(lowerCaseQuery)) ||
                     (task.getType() != null && task.getType().toLowerCase().contains(lowerCaseQuery))) {
                otherMatches.add(task);
            }
        }
        
        // Add all matches in priority order
        searchResults.addAll(exactNameMatches);
        searchResults.addAll(partialNameMatches);
        searchResults.addAll(otherMatches);
        
        Log.d(TAG, "Search results count: " + searchResults.size() + 
              " (exact name: " + exactNameMatches.size() + 
              ", partial name: " + partialNameMatches.size() + 
              ", other: " + otherMatches.size() + ")");
        
        // Update the adapter with the search results
        if (taskAdapter != null) {
            taskAdapter.updateTasks(searchResults);
            
            // Update UI for empty search results
            if (searchResults.isEmpty()) {
                tvNoTasks.setText("No tasks matching \"" + query + "\"");
                tvNoTasks.setVisibility(View.VISIBLE);
                recyclerViewTasks.setVisibility(View.GONE);
            } else {
                tvNoTasks.setVisibility(View.GONE);
                recyclerViewTasks.setVisibility(View.VISIBLE);
            }
        }
    }

    private void updateDateHeaders() {
        // Get today's date in just the date part (no time)
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date today = calendar.getTime();

        // Look for tasks for today
        boolean hasTasksForToday = false;
        for (Task task : filteredTasks) {
            Calendar taskCalendar = Calendar.getInstance();
            taskCalendar.setTime(task.getDateTime());
            taskCalendar.set(Calendar.HOUR_OF_DAY, 0);
            taskCalendar.set(Calendar.MINUTE, 0);
            taskCalendar.set(Calendar.SECOND, 0);
            taskCalendar.set(Calendar.MILLISECOND, 0);
            Date taskDate = taskCalendar.getTime();

            if (taskDate.equals(today)) {
                hasTasksForToday = true;
                break;
            }
        }

        // Update the header accordingly
        if (hasTasksForToday) {
            tvTodayTasks.setText("Today Task");
        } else if (!filteredTasks.isEmpty()) {
            // Get the date of the first task
            Date firstTaskDate = filteredTasks.get(0).getDateTime();
            tvTodayTasks.setText(dateFormat.format(firstTaskDate));
        } else {
            tvTodayTasks.setText("No Tasks");
        }
    }

    private void testFirebaseConnection() {
        try {
            // Get a reference to a test node
            DatabaseReference testRef = FirebaseDatabase.getInstance("https://madasg2-2e9c4-default-rtdb.firebaseio.com")
                    .getReference("test_connection");

            // Write a simple value
            String testId = "test_" + System.currentTimeMillis();
            testRef.child(testId).setValue("Test value at " + new Date())
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Firebase test write successful!");

                        // Now try to read it back
                        testRef.child(testId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    Log.d(TAG, "Firebase test read successful: " + snapshot.getValue());
                                    Toast.makeText(DisplayTask.this, "Firebase connection verified", Toast.LENGTH_SHORT).show();
                                } else {
                                    Log.e(TAG, "Firebase test read failed - value not found");
                                    Toast.makeText(DisplayTask.this, "Firebase read issue - check logs", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e(TAG, "Firebase test read failed: " + error.getMessage());
                                Toast.makeText(DisplayTask.this, "Firebase read error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Firebase test write failed: " + e.getMessage());
                        Toast.makeText(DisplayTask.this, "Firebase write error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error testing Firebase connection: " + e.getMessage());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_display, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            // Show toast
            Toast.makeText(this, "Refreshing tasks...", Toast.LENGTH_SHORT).show();

            // Perform refresh
            refreshTasks();
            return true;
        } else if (id == R.id.action_profile) {
            // Navigate to profile
            Intent profileIntent = new Intent(this, ProfileActivity.class);
            startActivity(profileIntent);
            return true;
        } else if (id == R.id.action_help) {
            // Navigate to help
            Intent helpIntent = new Intent(this, HelpActivity.class);
            startActivity(helpIntent);
            return true;
        } else if (id == R.id.action_faq) {
            // Navigate to FAQ
            Intent faqIntent = new Intent(this, FaqActivity.class);
            startActivity(faqIntent);
            return true;
        } else if (id == R.id.action_contact) {
            // Navigate to contact
            Intent contactIntent = new Intent(this, ContactActivity.class);
            startActivity(contactIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //Bottom Navigation
    private void setupBottomNavigation() {
        ImageButton clockBtn = findViewById(R.id.clockButton);
        ImageButton homeBtn = findViewById(R.id.homeButton);
        ImageButton profileBtn = findViewById(R.id.profileButton);
        Button searchBtn = findViewById(R.id.searchButton);

        // Disable current page button since we're in the Display Task (home) screen
        homeBtn.setEnabled(false);
        homeBtn.setAlpha(0.5f);

        // Handle clock button click
        clockBtn.setOnClickListener(v -> {
            Intent intent = new Intent(DisplayTask.this, ClockTimer.class);
            startActivity(intent);
            finish();
        });

        // Handle profile button click
        profileBtn.setOnClickListener(v -> {
            Intent intent = new Intent(DisplayTask.this, ProfileActivity.class);
            startActivity(intent);
            finish();
        });
    }
}