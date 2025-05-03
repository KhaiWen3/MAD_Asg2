package my.edu.madgroupassignment;

import android.util.Log;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@IgnoreExtraProperties
public class Task {
    private static final String TAG = "Task";

    private String id;
    private String type; // "Work" or "Life"
    private String category;
    private String description;

    // Make sure field names match exactly what's in Firebase
    @PropertyName("isDropdown")
    private boolean isDropdown;

    @PropertyName("timestamp")
    private long timestamp; // Store as timestamp for Firebase compatibility

    @PropertyName("isCompleted")
    private boolean isCompleted;

    @PropertyName("dropdownItems")
    private List<String> dropdownItems; // List of items for dropdown to-do list

    // Required empty constructor for Firebase
    public Task() {
        this.id = UUID.randomUUID().toString();
        this.isCompleted = false;
        this.dropdownItems = new ArrayList<>();
        Log.d(TAG, "Created empty Task with ID: " + this.id);
    }

    public Task(String type, String category, String description, boolean isDropdown, Date dateTime) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.category = category;
        this.description = description;
        this.isDropdown = isDropdown;
        this.timestamp = dateTime != null ? dateTime.getTime() : System.currentTimeMillis();
        this.isCompleted = false;
        this.dropdownItems = new ArrayList<>();
        Log.d(TAG, "Created new Task: " + description + ", ID: " + this.id);
    }

    // Fix potential issues with Firebase serialization
    public void fixNullFields() {
        if (id == null) {
            id = UUID.randomUUID().toString();
            Log.w(TAG, "Generated missing task ID: " + id);
        }

        if (type == null) {
            type = "Work"; // Default to Work
            Log.w(TAG, "Set default type for task: " + id);
        }

        if (category == null) {
            category = "Other"; // Default to Other
            Log.w(TAG, "Set default category for task: " + id);
        }

        if (description == null) {
            description = ""; // Empty string instead of null
            Log.w(TAG, "Set empty description for task: " + id);
        }

        if (timestamp == 0) {
            timestamp = System.currentTimeMillis(); // Current time
            Log.w(TAG, "Set current time for task: " + id);
        }

        if (dropdownItems == null) {
            dropdownItems = new ArrayList<>();
            Log.w(TAG, "Initialize empty dropdown items for task: " + id);
        }
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type != null ? type : "Work"; // Default to Work if null
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategory() {
        return category != null ? category : "Other"; // Default to Other if null
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @PropertyName("isDropdown")
    public boolean isDropdown() {
        return isDropdown;
    }

    @PropertyName("isDropdown")
    public void setDropdown(boolean dropdown) {
        isDropdown = dropdown;
    }

    // Store as timestamp in Firebase
    @PropertyName("timestamp")
    public long getTimestamp() {
        return timestamp;
    }

    @PropertyName("timestamp")
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    // Date object is not directly serializable to Firebase
    @Exclude
    public Date getDateTime() {
        return new Date(timestamp);
    }

    @Exclude
    public void setDateTime(Date dateTime) {
        this.timestamp = dateTime != null ? dateTime.getTime() : System.currentTimeMillis();
    }

    @PropertyName("isCompleted")
    public boolean isCompleted() {
        return isCompleted;
    }

    @PropertyName("isCompleted")
    public void setCompleted(boolean completed) {
        this.isCompleted = completed;
    }

    @PropertyName("dropdownItems")
    public List<String> getDropdownItems() {
        if (dropdownItems == null) {
            dropdownItems = new ArrayList<>();
        }
        return dropdownItems;
    }

    @PropertyName("dropdownItems")
    public void setDropdownItems(List<String> dropdownItems) {
        this.dropdownItems = dropdownItems != null ? dropdownItems : new ArrayList<>();
    }

    @Override
    public String toString() {
        return "Task{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", category='" + category + '\'' +
                ", description='" + description + '\'' +
                ", isDropdown=" + isDropdown +
                ", timestamp=" + timestamp +
                ", isCompleted=" + isCompleted +
                ", dropdownItems=" + (dropdownItems != null ? dropdownItems.size() : 0) +
                '}';
    }
}