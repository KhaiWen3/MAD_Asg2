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
        listGroup.add("Rewards");

        List<String> taskDesc = new ArrayList<>();
        taskDesc.add("Use subtasks for complex activities. E.g., under 'Shopping', add items; under 'Study', list topics.");
        taskDesc.add("Create, view and manage your tasks from the home page.");
        taskDesc.add("Use task templates for quick task creation.");

        List<String> clockDesc = new ArrayList<>();
        clockDesc.add("Use the Focus Clock to help concentrate while working on tasks.");
        clockDesc.add("Track time spent on each task effectively.");

        List<String> rewardsDesc = new ArrayList<>();
        rewardsDesc.add("Set personal rewards for motivation after completing tasks.");
        rewardsDesc.add("Enable notifications to remind and plan your rewards.");

        listItem.put(listGroup.get(0), taskDesc);
        listItem.put(listGroup.get(1), clockDesc);
        listItem.put(listGroup.get(2), rewardsDesc);

        listAdapter.notifyDataSetChanged();
    }
}

