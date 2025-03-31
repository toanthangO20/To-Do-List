package com.thangbt1.example.todolist;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class TaskAdapter extends ArrayAdapter<Task> {
    private Context context;
    private List<Task> taskList;

    public TaskAdapter(Context context, List<Task> tasks) {
        super(context, 0, tasks);
        this.context = context;
        this.taskList = tasks;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Task task = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.task_item, parent, false);
        }

        TextView txtTitle = convertView.findViewById(R.id.txtTitle);
        TextView txtCategory = convertView.findViewById(R.id.txtCategory);
        ImageView imgTask = convertView.findViewById(R.id.imgTask);

        txtTitle.setText(task.getTitle());
        txtCategory.setText(task.getCategory());

        if (task.getImageUri() != null) {
            imgTask.setImageURI(Uri.parse(task.getImageUri()));
        } else {
            imgTask.setImageResource(R.drawable.ic_launcher_foreground);
        }

        return convertView;
    }
}
