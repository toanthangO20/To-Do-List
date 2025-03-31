package com.thangbt1.example.todolist;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ADD_TASK = 1;
    private static final int REQUEST_CODE_EDIT_TASK = 2;

    ListView listViewTasks;
    Spinner spinnerFilter;
    Button btnAddTask;

    List<Task> allTasks = new ArrayList<>();
    TaskAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listViewTasks = findViewById(R.id.listViewTasks);
        spinnerFilter = findViewById(R.id.spinnerFilter);
        btnAddTask = findViewById(R.id.btnAddTask);

        // Load task từ SharedPreferences
        loadTasksFromStorage();

        adapter = new TaskAdapter(this, allTasks);
        listViewTasks.setAdapter(adapter);

        String[] categories = {"Tất cả", "Công việc", "Học tập", "Cá nhân"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(spinnerAdapter);

        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterTasks(categories[position]);
            }

            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(this, TaskActivity.class);
            startActivityForResult(intent, REQUEST_CODE_ADD_TASK);
        });

        listViewTasks.setOnItemLongClickListener((parent, view, position, id) -> {
            allTasks.remove(position);
            adapter.notifyDataSetChanged();
            saveTasksToStorage();
            return true;
        });

        listViewTasks.setOnItemClickListener((parent, view, position, id) -> {
            Task selectedTask = adapter.getItem(position);

            View dialogView = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_task_detail, null);
            TextView txtTitle = dialogView.findViewById(R.id.txtDetailTitle);
            TextView txtCategory = dialogView.findViewById(R.id.txtDetailCategory);
            TextView txtDate = dialogView.findViewById(R.id.txtDetailDate);
            TextView txtDesc = dialogView.findViewById(R.id.txtDetailDescription);
            ImageView img = dialogView.findViewById(R.id.imgDetailTask);

            txtTitle.setText(selectedTask.getTitle());
            txtCategory.setText("Chủ đề: " + selectedTask.getCategory());
            txtDesc.setText(selectedTask.getDescription());

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            txtDate.setText("Ngày: " + sdf.format(new Date(selectedTask.getDueTimeMillis())));

            if (selectedTask.getImageUri() != null) {
                try {
                    Uri uri = Uri.parse(selectedTask.getImageUri());
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    img.setImageBitmap(bitmap);
                    inputStream.close();
                    img.setVisibility(View.VISIBLE);
                    txtDesc.setGravity(Gravity.START);
                } catch (Exception e) {
                    img.setVisibility(View.GONE);
                    txtDesc.setGravity(Gravity.CENTER);
                    e.printStackTrace();
                }
            } else {
                img.setVisibility(View.GONE);
                txtDesc.setGravity(Gravity.CENTER);
            }

            new AlertDialog.Builder(MainActivity.this)
                    .setView(dialogView)
                    .setPositiveButton("Sửa task", (dialog, which) -> {
                        Intent intent = new Intent(MainActivity.this, TaskActivity.class);
                        intent.putExtra("task", selectedTask);
                        intent.putExtra("edit_position", position);
                        startActivityForResult(intent, REQUEST_CODE_EDIT_TASK);
                    })
                    .setNegativeButton("Đóng", null)
                    .show();
        });
    }

    void filterTasks(String category) {
        if (category.equals("Tất cả")) {
            adapter = new TaskAdapter(this, allTasks);
        } else {
            List<Task> filtered = new ArrayList<>();
            for (Task t : allTasks) {
                if (t.getCategory().equals(category)) filtered.add(t);
            }
            adapter = new TaskAdapter(this, filtered);
        }
        listViewTasks.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ((requestCode == REQUEST_CODE_ADD_TASK || requestCode == REQUEST_CODE_EDIT_TASK) && resultCode == RESULT_OK) {
            Task task = (Task) data.getSerializableExtra("task");

            if (requestCode == REQUEST_CODE_ADD_TASK) {
                allTasks.add(task);
            } else if (requestCode == REQUEST_CODE_EDIT_TASK) {
                int position = data.getIntExtra("edit_position", -1);
                if (position != -1) {
                    allTasks.set(position, task);
                }
            }

            adapter.notifyDataSetChanged();
            saveTasksToStorage();
        }
    }

    private void saveTasksToStorage() {
        SharedPreferences prefs = getSharedPreferences("task_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(allTasks);
        editor.putString("task_list", json);
        editor.apply();
    }

    private void loadTasksFromStorage() {
        SharedPreferences prefs = getSharedPreferences("task_prefs", MODE_PRIVATE);
        String json = prefs.getString("task_list", null);
        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<Task>>() {}.getType();
            allTasks = gson.fromJson(json, type);
        } else {
            allTasks = new ArrayList<>();
        }
    }
}
