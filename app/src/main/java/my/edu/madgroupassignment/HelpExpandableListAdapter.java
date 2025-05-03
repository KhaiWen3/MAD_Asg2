package my.edu.madgroupassignment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import android.widget.ImageView;

import java.util.HashMap;
import java.util.List;
public class HelpExpandableListAdapter extends BaseExpandableListAdapter {

    Context context;
    List<String> listGroup;
    HashMap<String, List<String>> listItem;

    public HelpExpandableListAdapter(Context context, List<String> listGroup, HashMap<String, List<String>> listItem) {
        this.context = context;
        this.listGroup = listGroup;
        this.listItem = listItem;
    }

    @Override
    public int getGroupCount() {
        return listGroup.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return listItem.get(listGroup.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return listGroup.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return listItem.get(listGroup.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.help_list_group, parent, false);
        }

        ImageView arrowIcon = convertView.findViewById(R.id.arrowIcon);
        TextView listTitle = convertView.findViewById(R.id.listTitle);
        ImageView groupIcon = convertView.findViewById(R.id.groupIcon);

        if (isExpanded) {
            arrowIcon.setImageResource(R.drawable.ic_arrow_up);
        } else {
            arrowIcon.setImageResource(R.drawable.ic_arrow_down);
        }

        listTitle.setText((String) getGroup(groupPosition));

        switch (groupPosition) {
            case 0:
                groupIcon.setImageResource(R.drawable.ic_task);
                break;
            case 1:
                groupIcon.setImageResource(R.drawable.ic_clock);
                break;
            case 2:
                groupIcon.setImageResource(R.drawable.ic_help);
                break;
        }

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        String detail = (String) getChild(groupPosition, childPosition);
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false);
        }
        TextView textView = convertView.findViewById(android.R.id.text1);
        textView.setText(detail);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}