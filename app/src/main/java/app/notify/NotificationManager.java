package app.notify;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import org.joda.time.DateTime;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;

/**
 * Created by Georg on 25.09.2016.
 */
public final class NotificationManager {

    public static void startAlarm(DateTime time, Notification notification, Context ctx) {
        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(ctx.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        int year = time.getYear();
        int month = time.getMonthOfYear() - 1;
        int day = time.getDayOfMonth();
        int hour = time.getHourOfDay();
        int minute = time.getMinuteOfHour();
        calendar.set(year, month, day, hour, minute);
        long when = calendar.getTimeInMillis();// notification time

        Intent intent = new Intent(ctx, NotificationService.class);
        intent.putExtra(Utils.title,notification.title);
        intent.putExtra(Utils.description,notification.description);
        intent.putExtra(Utils.priority,notification.priority);
        intent.putExtra(Utils.id,notification.getId());
        if(notification.iconFromResources())
            intent.putExtra(Utils.icon_resource,notification.iconResource);
        else
            intent.putExtra(Utils.icon_bitmap,getBytes(notification.icon));
        PendingIntent pendingIntent = PendingIntent.getService(ctx,
                (int)DateTime.parse(notification.getId(),DateManager.dateFormat).getMillis()%1000000000, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.set(AlarmManager.RTC_WAKEUP, when, pendingIntent);
    }

    public static void updateNotification(Notification newData, DateTime newTime, Context ctx) {
        cancelNotification(newData.getId(),ctx);
        startAlarm(newTime,newData,ctx);
    }

    public static void cancelNotification(String id, Context ctx) {
        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(ctx.ALARM_SERVICE);
        Intent intent = new Intent(ctx,NotificationService.class);
        int notificationId = (int) DateTime.parse(id, DateManager.dateFormat).getMillis()%1000000000;
        alarmManager.cancel(PendingIntent.getService(ctx, notificationId, intent, PendingIntent.FLAG_NO_CREATE));
    }

    private static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

}
