package com.thangbt1.example.todolist;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.io.InputStream;
import java.util.Calendar;

public class TaskActivity extends AppCompatActivity {

    EditText edtTitle, edtDescription;
    RadioGroup categoryGroup;
    RadioButton rbWork, rbStudy, rbPersonal;
    Button btnPickImage, btnPickDate, btnSave;
    ImageView imageViewPreview;

    String selectedImageUri = null;
    long dueTimeMillis;

    static final int PICK_IMAGE = 1000;

    Task existingTask = null;
    int editPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        edtTitle = findViewById(R.id.edtTitle);
        edtDescription = findViewById(R.id.edtDescription);
        categoryGroup = findViewById(R.id.categoryGroup);
        rbWork = findViewById(R.id.rbWork);
        rbStudy = findViewById(R.id.rbStudy);
        rbPersonal = findViewById(R.id.rbPersonal);
        btnPickImage = findViewById(R.id.btnPickImage);
        btnPickDate = findViewById(R.id.btnPickDate);
        btnSave = findViewById(R.id.btnSave);
        imageViewPreview = findViewById(R.id.imageViewPreview);

        // Nếu sửa task
        if (getIntent() != null && getIntent().hasExtra("task")) {
            existingTask = (Task) getIntent().getSerializableExtra("task");
            editPosition = getIntent().getIntExtra("edit_position", -1);

            edtTitle.setText(existingTask.getTitle());
            edtDescription.setText(existingTask.getDescription());

            switch (existingTask.getCategory()) {
                case "Công việc":
                    rbWork.setChecked(true); break;
                case "Học tập":
                    rbStudy.setChecked(true); break;
                case "Cá nhân":
                    rbPersonal.setChecked(true); break;
            }

            selectedImageUri = existingTask.getImageUri();

            if (selectedImageUri != null) {
                try {
                    Uri uri = Uri.parse(selectedImageUri);
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    imageViewPreview.setImageBitmap(bitmap);
                    inputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            dueTimeMillis = existingTask.getDueTimeMillis();
        }

        btnPickImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("image/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, PICK_IMAGE);
        });

        btnPickDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(this,
                    (view, year, month, day) -> {
                        calendar.set(year, month, day, 8, 0);
                        dueTimeMillis = calendar.getTimeInMillis();
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            dialog.show();
        });

        btnSave.setOnClickListener(v -> {
            String title = edtTitle.getText().toString().trim();
            String desc = edtDescription.getText().toString().trim();

            String category = "";
            if (rbWork.isChecked()) category = "Công việc";
            else if (rbStudy.isChecked()) category = "Học tập";
            else if (rbPersonal.isChecked()) category = "Cá nhân";

            // Nếu chưa chọn ngày, lấy ngày hôm nay 08:00
            if (dueTimeMillis == 0) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, 8);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                dueTimeMillis = calendar.getTimeInMillis();
            }

            // Nếu chưa chọn ảnh, giữ ảnh cũ (nếu đang sửa)
            if (selectedImageUri == null && existingTask != null) {
                selectedImageUri = existingTask.getImageUri();
            }

            Task task = new Task(title, desc, category, selectedImageUri, dueTimeMillis);

            Intent resultIntent = new Intent();
            resultIntent.putExtra("task", task);
            if (editPosition != -1) {
                resultIntent.putExtra("edit_position", editPosition);
            }

            setResult(RESULT_OK, resultIntent);
            setReminder(task);
            finish();
        });
    }

    private void setReminder(Task task) {
        Intent intent = new Intent(this, TaskReminderReceiver.class);
        intent.putExtra("title", task.getTitle());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarm.set(AlarmManager.RTC_WAKEUP, task.getDueTimeMillis(), pendingIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            selectedImageUri = imageUri.toString();

            // Cấp quyền truy cập vĩnh viễn với ảnh (persistable)
            final int takeFlags = data.getFlags() &
                    (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            try {
                getContentResolver().takePersistableUriPermission(imageUri, takeFlags);
            } catch (SecurityException e) {
                e.printStackTrace();
            }

            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                imageViewPreview.setImageBitmap(bitmap);
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
