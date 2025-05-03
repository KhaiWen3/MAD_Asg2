package my.edu.madgroupassignment;

import android.os.Bundle;
import android.widget.ExpandableListView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HelpActivity extends AppCompatActivity{
    ExpandableListView expandableListView;
    List<String> listGroup;
    HashMap<String, List<String>> listItem;
    HelpExpandableListAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Help Center"); // Optional: set title

        expandableListView = findViewById(R.id.help_expandable_list);
        listGroup = new ArrayList<>();
        listItem = new HashMap<>();
        listAdapter = new HelpExpandableListAdapter(this, listGroup, listItem);
        expandableListView.setAdapter(listAdapter);

        initListData();
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Closes this activity and returns to the previous one
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initListData() {
        listGroup.add("Task");
        listGroup.add("Clock");
        listGroup.add("Login");

        List<String> taskDesc = new ArrayList<>();
        taskDesc.add("1. Use subtasks for complex activities. E.g., under 'Shopping', add items; under 'Study', list topics.");
        taskDesc.add("2. Create, view and manage your tasks from the home page.");
        taskDesc.add("3. Use task templates for quick task creation.");

        List<String> clockDesc = new ArrayList<>();
        clockDesc.add("1. Use the Clock Timer to help concentrate while working on tasks.");
        clockDesc.add("2. Track time spent on each task effectively.");

        List<String> rewardsDesc = new ArrayList<>();
        rewardsDesc.add("1. Enter Your Credentials: Input your registered email and password in the provided fields.");
        rewardsDesc.add("2. Tap the \"Login\" button to log into your account");
        rewardsDesc.add("3. Forgot Password?: If you've forgotten your password, tap the \"Forgot Password?\" link to reset it.");
        rewardsDesc.add("4. Sign Up: If you don’t have an account, click on the \"Sign Up\" link to create one.");



        listItem.put(listGroup.get(0), taskDesc);
        listItem.put(listGroup.get(1), clockDesc);
        listItem.put(listGroup.get(2), rewardsDesc);

        listAdapter.notifyDataSetChanged();
    }
}

