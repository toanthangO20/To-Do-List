package com.thangbt1.example.todolist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends ArrayAdapter<Task> {
    private Context context;
    private boolean showCheckboxes = false;

    public TaskAdapter(Context context, List<Task> tasks) {
        super(context, 0, tasks);
        this.context = context;
    }

    public void setShowCheckboxes(boolean show) {
        this.showCheckboxes = show;
        notifyDataSetChanged();
    }

    public boolean isShowCheckboxes() {
        return showCheckboxes;
    }

    public List<Task> getCheckedTasks() {
        List<Task> checked = new ArrayList<>();
        for (int i = 0; i < getCount(); i++) {
            Task task = getItem(i);
            if (task.isCompleted()) {
                checked.add(task);
            }
        }
        return checked;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Task task = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.task_item, parent, false);
        }

        TextView txtTitle = convertView.findViewById(R.id.txtTitle);
        TextView txtCategory = convertView.findViewById(R.id.txtCategory);
        CheckBox chkCompleted = convertView.findViewById(R.id.chkCompleted);

        txtTitle.setText(task.getTitle());
        txtCategory.setText(task.getCategory());

        chkCompleted.setVisibility(showCheckboxes ? View.VISIBLE : View.GONE);
        chkCompleted.setOnCheckedChangeListener(null); // reset listener
        chkCompleted.setChecked(task.isCompleted());

        if (showCheckboxes) {
            chkCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
                task.setCompleted(isChecked);
                ((MainActivity) context).onTaskCheckedChanged(); // gọi để cập nhật nút Xóa
            });
        }

        return convertView;
    }
}
