package my.edu.madgroupassignment;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.text.TextPaint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Calendar;
import java.util.ArrayList;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private static final String TAG = "TaskAdapter";

    private final List<Task> tasks;
    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy h:mma", Locale.getDefault());
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public TaskAdapter(List<Task> tasks) {
        this.tasks = tasks;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);

        Log.d(TAG, "Binding task at position " + position + ": " + task.getDescription() +
                ", Type: " + task.getType() +
                ", Category: " + task.getCategory() +
                ", Completed: " + task.isCompleted() +
                ", Date: " + dateTimeFormat.format(task.getDateTime()));

        // Set task description, date and completion status
        holder.tvTaskDescription.setText(task.getDescription());
        holder.tvTaskDateTime.setText(dateTimeFormat.format(task.getDateTime()));
        holder.checkBoxTask.setChecked(task.isCompleted());

        // Set task type and category tags
        holder.tvTaskType.setText(task.getType());
        holder.tvTaskCategory.setText(task.getCategory());

        // Set appropriate colors for type tag
        if ("Work".equals(task.getType())) {
            holder.tvTaskType.setBackgroundResource(R.drawable.type_tag_background); // Blue
        } else if ("Life".equals(task.getType())) {
            // Create a custom drawable for Life tasks
            GradientDrawable lifeBackground = new GradientDrawable();
            lifeBackground.setShape(GradientDrawable.RECTANGLE);
            lifeBackground.setColor(Color.parseColor("#FF9800")); // Orange
            lifeBackground.setCornerRadius(4 * holder.itemView.getResources().getDisplayMetrics().density);
            holder.tvTaskType.setBackground(lifeBackground);
        }

        // Set appropriate colors for category
        setCategoryColor(holder.tvTaskCategory, task.getCategory());

        // Set background color based on category
        try {
            int categoryColor = CategoryColors.getColorForCategory(task.getCategory());
            View parent = (View) holder.tvTaskDescription.getParent();
            if (parent.getBackground() instanceof GradientDrawable) {
                GradientDrawable drawable = (GradientDrawable) parent.getBackground();
                drawable.setStroke(2, categoryColor); // Thinner stroke for cleaner look
                Log.d(TAG, "Set border color for category: " + task.getCategory());
            } else {
                Log.e(TAG, "Background is not a GradientDrawable");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting category color: " + e.getMessage());
        }

        // Handle dropdown list
        if (task.isDropdown() && task.getDropdownItems() != null && !task.getDropdownItems().isEmpty()) {
            holder.btnShowList.setVisibility(View.VISIBLE);
            Log.d(TAG, "Task has dropdown items: " + task.getDropdownItems().size() +
                    " - " + task.getDropdownItems());
            setupDropdownList(holder, task);
        } else {
            holder.btnShowList.setVisibility(View.GONE);
            holder.recyclerViewDropdownItems.setVisibility(View.GONE);
            Log.d(TAG, "Task has no dropdown items");
        }

        // Toggle task completion
        holder.checkBoxTask.setOnClickListener(v -> {
            boolean isChecked = holder.checkBoxTask.isChecked();
            task.setCompleted(isChecked);

            // Apply strikethrough to task description when checked
            if (isChecked) {
                holder.tvTaskDescription.setPaintFlags(holder.tvTaskDescription.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                holder.tvTaskDescription.setPaintFlags(holder.tvTaskDescription.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            }

            // Update task completion status in Firebase
            FirebaseDatabase.getInstance("https://madasg2-2e9c4-default-rtdb.firebaseio.com")
                    .getReference("tasks")
                    .child(task.getId())
                    .child("isCompleted")
                    .setValue(isChecked)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Task completion updated: " + isChecked))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to update task completion: " + e.getMessage()));
        });

        // Apply strikethrough to completed tasks
        if (task.isCompleted()) {
            holder.tvTaskDescription.setPaintFlags(holder.tvTaskDescription.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.tvTaskDescription.setPaintFlags(holder.tvTaskDescription.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        }

        // Set up edit button click listener
        holder.btnEditTask.setOnClickListener(v -> {
            showEditDialog(holder.itemView.getContext(), task);
        });

        // Set up delete button click listener
        holder.btnDeleteTask.setOnClickListener(v -> {
            showDeleteConfirmation(holder.itemView.getContext(), task);
        });
    }

    private void setCategoryColor(TextView categoryView, String category) {
        GradientDrawable background = new GradientDrawable();
        background.setShape(GradientDrawable.RECTANGLE);
        background.setCornerRadius(4 * categoryView.getResources().getDisplayMetrics().density);

        int color;
        switch (category) {
            case "Education":
                color = Color.parseColor("#F44336"); // Red
                break;
            case "Shopping":
                color = Color.parseColor("#4CAF50"); // Green
                break;
            case "Meal":
                color = Color.parseColor("#FF9800"); // Orange
                break;
            case "Health":
                color = Color.parseColor("#2196F3"); // Blue
                break;
            default:
                color = Color.parseColor("#9C27B0"); // Purple for Other
                break;
        }

        background.setColor(color);
        categoryView.setBackground(background);
    }

    private void setupDropdownList(TaskViewHolder holder, Task task) {
        // Set up dropdown list button clickListener
        holder.btnShowList.setOnClickListener(v -> {
            if (holder.recyclerViewDropdownItems.getVisibility() == View.VISIBLE) {
                holder.recyclerViewDropdownItems.setVisibility(View.GONE);
                Log.d(TAG, "Hiding dropdown items for: " + task.getDescription());
            } else {
                holder.recyclerViewDropdownItems.setVisibility(View.VISIBLE);
                Log.d(TAG, "Showing dropdown items for: " + task.getDescription());

                // Set up the RecyclerView for dropdown items
                DropdownItemAdapter adapter = new DropdownItemAdapter(task.getDropdownItems());
                holder.recyclerViewDropdownItems.setAdapter(adapter);
                holder.recyclerViewDropdownItems.setLayoutManager(new LinearLayoutManager(
                        holder.recyclerViewDropdownItems.getContext()));
            }
        });
    }

    private void showEditDialog(Context context, Task task) {
        // Create a dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_edit_task, null);
        builder.setView(dialogView);

        // Get dialog components
        EditText etEditDescription = dialogView.findViewById(R.id.etEditDescription);
        Spinner spinnerEditCategory = dialogView.findViewById(R.id.spinnerEditCategory);
        RadioGroup rgEditType = dialogView.findViewById(R.id.rgEditType);
        RadioButton rbEditWork = dialogView.findViewById(R.id.rbEditWork);
        RadioButton rbEditLife = dialogView.findViewById(R.id.rbEditLife);
        TextView tvEditDate = dialogView.findViewById(R.id.tvEditDate);
        TextView tvEditTime = dialogView.findViewById(R.id.tvEditTime);
        ImageButton btnEditDatePicker = dialogView.findViewById(R.id.btnEditDatePicker);
        ImageButton btnEditTimePicker = dialogView.findViewById(R.id.btnEditTimePicker);
        Button btnCancelEdit = dialogView.findViewById(R.id.btnCancelEdit);
        Button btnSaveEdit = dialogView.findViewById(R.id.btnSaveEdit);

        // Set up calendar for date/time
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(task.getDateTime());

        // Fill dialog with current task values
        etEditDescription.setText(task.getDescription());
        tvEditDate.setText(dateFormat.format(task.getDateTime()));
        tvEditTime.setText(timeFormat.format(task.getDateTime()));

        // Set task type
        if ("Work".equals(task.getType())) {
            rbEditWork.setChecked(true);
        } else {
            rbEditLife.setChecked(true);
        }

        // Set up category spinner
        setupCategorySpinner(context, spinnerEditCategory, task.getCategory());

        // Date picker
        btnEditDatePicker.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    context,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        tvEditDate.setText(dateFormat.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        // Time picker
        btnEditTimePicker.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    context,
                    (view, hourOfDay, minute) -> {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);
                        tvEditTime.setText(timeFormat.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
            );
            timePickerDialog.show();
        });

        // Create and show dialog
        AlertDialog dialog = builder.create();

        // Cancel button
        btnCancelEdit.setOnClickListener(v -> dialog.dismiss());

        // Save button
        btnSaveEdit.setOnClickListener(v -> {
            // Validate input
            String description = etEditDescription.getText().toString().trim();
            if (description.isEmpty()) {
                Toast.makeText(context, "Please enter a task description", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get selected type
            String type = rbEditWork.isChecked() ? "Work" : "Life";

            // Get selected category
            String category = spinnerEditCategory.getSelectedItem().toString();

            // Update task
            task.setDescription(description);
            task.setType(type);
            task.setCategory(category);
            task.setDateTime(calendar.getTime());

            // Save to Firebase
            updateTaskInFirebase(context, task);

            // Close dialog
            dialog.dismiss();
        });

        dialog.show();
    }

    private void setupCategorySpinner(Context context, Spinner spinner, String currentCategory) {
        List<String> categories = new ArrayList<>();
        categories.add("Education");
        categories.add("Shopping");
        categories.add("Meal");
        categories.add("Health");
        categories.add("Other");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                context, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Set current selection
        int position = categories.indexOf(currentCategory);
        if (position >= 0) {
            spinner.setSelection(position);
        }
    }

    private void updateTaskInFirebase(Context context, Task task) {
        // Get reference to Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://madasg2-2e9c4-default-rtdb.firebaseio.com");
        DatabaseReference taskRef = database.getReference("tasks").child(task.getId());

        // Update task in Firebase
        taskRef.setValue(task)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Task updated successfully", Toast.LENGTH_SHORT).show();
                    notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to update task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error updating task: " + e.getMessage());
                });
    }

    private void showDeleteConfirmation(Context context, Task task) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete Task");
        builder.setMessage("Are you sure you want to delete this task?");

        // Add buttons
        builder.setPositiveButton("Delete", (dialog, which) -> deleteTask(context, task));
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        // Create and show the dialog
        builder.create().show();
    }

    private void deleteTask(Context context, Task task) {
        // Get the current user ID
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null ? 
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
                
        if (uid == null) {
            // No logged-in user, redirect to login
            Toast.makeText(context, "You need to login first", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(context, Login.class);
            context.startActivity(intent);
            return;
        }
        
        // Get reference to Firebase with the user-specific path
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://madasg2-2e9c4-default-rtdb.firebaseio.com");
        DatabaseReference taskRef = database.getReference("tasks").child(uid).child(task.getId());
        
        // Log information about what's being deleted
        Log.d(TAG, "Deleting task: " + task.getDescription() + " with ID: " + task.getId());
        if (task.isDropdown() && task.getDropdownItems() != null && !task.getDropdownItems().isEmpty()) {
            Log.d(TAG, "This task has " + task.getDropdownItems().size() + " dropdown items that will also be deleted");
        }

        // Delete task from Firebase
        taskRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Task deleted successfully", Toast.LENGTH_SHORT).show();

                    // Remove task from local list and update adapter
                    int position = tasks.indexOf(task);
                    if (position >= 0) {
                        tasks.remove(position);
                        notifyItemRemoved(position);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to delete task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error deleting task: " + e.getMessage());
                });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void updateTasks(List<Task> newTasks) {
        Log.d(TAG, "Updating adapter with " + newTasks.size() + " tasks");

        // Create a new list to avoid reference issues
        tasks.clear();
        if (newTasks != null && !newTasks.isEmpty()) {
            tasks.addAll(newTasks);
        }

        Log.d(TAG, "After update, adapter has " + tasks.size() + " tasks");
        notifyDataSetChanged();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBoxTask;
        TextView tvTaskDescription;
        TextView tvTaskDateTime;
        TextView tvTaskCategory;
        TextView tvTaskType;
        ImageButton btnShowList;
        ImageButton btnEditTask;
        ImageButton btnDeleteTask;
        RecyclerView recyclerViewDropdownItems;

        TaskViewHolder(View itemView) {
            super(itemView);
            checkBoxTask = itemView.findViewById(R.id.checkBoxTask);
            tvTaskDescription = itemView.findViewById(R.id.tvTaskDescription);
            tvTaskDateTime = itemView.findViewById(R.id.tvTaskDateTime);
            tvTaskCategory = itemView.findViewById(R.id.tvTaskCategory);
            tvTaskType = itemView.findViewById(R.id.tvTaskType);
            btnShowList = itemView.findViewById(R.id.btnShowList);
            btnEditTask = itemView.findViewById(R.id.btnEditTask);
            btnDeleteTask = itemView.findViewById(R.id.btnDeleteTask);
            recyclerViewDropdownItems = itemView.findViewById(R.id.recyclerViewDropdownItems);
        }
    }
}