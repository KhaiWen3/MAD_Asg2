package my.edu.madgroupassignment;

import android.os.Bundle;
import android.widget.ExpandableListView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FaqActivity extends AppCompatActivity {

    ExpandableListView faqExpandableListView;
    List<String> listGroup;
    HashMap<String, List<String>> listItem;
    FaqExpandableListAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("FAQ"); // Optional: set title


        faqExpandableListView = findViewById(R.id.faqExpandableListView);

        listGroup = new ArrayList<>();
        listItem = new HashMap<>();

        // Existing FAQ data
        listGroup.add("How do I reset my password?");
        listGroup.add("Where can I view my rewards?");
        listGroup.add("How do I contact support?");

        List<String> answer1 = new ArrayList<>();
        answer1.add("Go to Settings > Account > Reset Password and follow the instructions.");

        List<String> answer2 = new ArrayList<>();
        answer2.add("Rewards are visible under the 'My Rewards' tab in the main menu.");

        List<String> answer3 = new ArrayList<>();
        answer3.add("Tap the 'Support' button in the Help section to chat or email our team.");

        listItem.put(listGroup.get(0), answer1);
        listItem.put(listGroup.get(1), answer2);
        listItem.put(listGroup.get(2), answer3);

        // New FAQ additions
        listGroup.add("How to create a new task?");
        listGroup.add("Can I share tasks with others?");

        List<String> answer4 = new ArrayList<>();
        answer4.add("Tap the '+' button on the Home screen and fill in the task details.");

        List<String> answer5 = new ArrayList<>();
        answer5.add("Currently, task sharing is not supported. Stay tuned for future updates!");

        listItem.put("How to create a new task?", answer4);
        listItem.put("Can I share tasks with others?", answer5);

        adapter = new FaqExpandableListAdapter(this, listGroup, listItem);
        faqExpandableListView.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Closes this activity and returns to the previous one
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
