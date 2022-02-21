package pnj.jejaringsosial.chandrasa.notifications;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Date;

import pnj.jejaringsosial.chandrasa.R;

public class ReminderBroadcast extends BroadcastReceiver {

    String eventTitle;
    String eventDate;
    int requestCodeUnique;

    @Override
    public void onReceive(Context context, Intent intent) {

        eventTitle = intent.getStringExtra("aTitle");
        eventDate = intent.getStringExtra("aDate");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "notifyDSS")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Today's Agenda - "+eventTitle)
                .setContentText(eventDate)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);

        notificationManagerCompat.notify(requestCodeUnique, builder.build());
    }

}
