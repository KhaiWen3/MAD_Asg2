package my.edu.madgroupassignment;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DropdownItemAdapter extends RecyclerView.Adapter<DropdownItemAdapter.ViewHolder> {

    private final List<String> items;
    private final boolean[] itemCompletionStatus;

    public DropdownItemAdapter(List<String> items) {
        this.items = items;
        this.itemCompletionStatus = new boolean[items.size()];
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dropdown, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String item = items.get(position);
        holder.tvDropdownItem.setText(item);

        // Set strike-through based on completion status
        updateTextAppearance(holder.tvDropdownItem, itemCompletionStatus[position]);

        holder.radioButtonItem.setChecked(itemCompletionStatus[position]);

        holder.radioButtonItem.setOnClickListener(v -> {
            // Toggle completion status
            itemCompletionStatus[position] = !itemCompletionStatus[position];
            holder.radioButtonItem.setChecked(itemCompletionStatus[position]);
            updateTextAppearance(holder.tvDropdownItem, itemCompletionStatus[position]);
        });

        holder.tvDropdownItem.setOnClickListener(v -> {
            // Same behavior as clicking the radio button
            itemCompletionStatus[position] = !itemCompletionStatus[position];
            holder.radioButtonItem.setChecked(itemCompletionStatus[position]);
            updateTextAppearance(holder.tvDropdownItem, itemCompletionStatus[position]);
        });
    }

    private void updateTextAppearance(TextView textView, boolean isCompleted) {
        if (isCompleted) {
            textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            textView.setPaintFlags(textView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        RadioButton radioButtonItem;
        TextView tvDropdownItem;

        ViewHolder(View itemView) {
            super(itemView);
            radioButtonItem = itemView.findViewById(R.id.radioButtonItem);
            tvDropdownItem = itemView.findViewById(R.id.tvDropdownItem);
        }
    }
}