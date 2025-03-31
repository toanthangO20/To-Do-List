package com.thangbt1.example.todolist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class TaskReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String taskTitle = intent.getStringExtra("title");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "TASK_REMINDER")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Nhắc nhở task")
                .setContentText("Đến giờ thực hiện: " + taskTitle)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        manager.notify(1, builder.build());
    }
}