package pnj.jejaringsosial.chandrasa;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import pnj.jejaringsosial.chandrasa.notifications.ReminderBroadcast;

public class JoinClicked extends AppCompatActivity {

    FirebaseAuth firebaseAuth;

    String timeJoinClicked;
    String timeEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_clicked);

        createNotificationChannel();

        Toast.makeText(this, "Reminder Set!", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(JoinClicked.this, ReminderBroadcast.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(JoinClicked.this,0,intent,0);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        long longJoinClickedTime = Long.parseLong(timeJoinClicked);
        long longEventTime = Long.parseLong(timeEvent);

        alarmManager.set(AlarmManager.RTC_WAKEUP,
                longJoinClickedTime+longEventTime,pendingIntent);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Depok Social Stories";
            String description = "Channel for DSS Reminder";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("notifyDSS", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}