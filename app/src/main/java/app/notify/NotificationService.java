package app.notify;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.app.Notification;
import android.util.Log;

import org.joda.time.DateTime;

/**
 * Created by Georg on 24.09.2016.
 */
public class NotificationService extends IntentService {

    public NotificationService(){
        super("NotificationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("HANDLE","INTENT");
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Bundle extras = intent.getExtras();
        String title = extras.getString(Utils.title);
        String description = extras.getString(Utils.description);
        int priority = extras.getInt(Utils.priority);
        int notifPriority = Notification.PRIORITY_DEFAULT;
        String id = extras.getString(Utils.id);
        Log.d("PRIORITY",Integer.toString(priority));

        switch(priority) {
            case 0: notifPriority = Notification.PRIORITY_MAX;
                break;
            case 1: notifPriority = Notification.PRIORITY_HIGH;
                break;
            case 2: notifPriority = Notification.PRIORITY_DEFAULT;
                break;
            case 3: notifPriority = Notification.PRIORITY_LOW;
                break;
            case 4: notifPriority = Notification.PRIORITY_MIN;
                break;
        }

        Notification.Builder builder = new Notification.Builder(getApplicationContext()).
                setAutoCancel(true).
                setContentTitle(title).
                setContentText(description).
                setPriority(notifPriority);

        if(extras.containsKey(Utils.icon_resource)) {
            builder.setSmallIcon(extras.getInt(Utils.icon_resource));
            builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),extras.getInt(Utils.icon_resource)));
        }
        else if (extras.containsKey(Utils.icon_bitmap)) {
            byte[] bitmap = extras.getByteArray(Utils.icon_bitmap);
            builder.setSmallIcon(R.drawable.ic_menu_compass);
            builder.setLargeIcon(BitmapFactory.decodeByteArray(bitmap,0,bitmap.length));
        }
        Notification notification = builder.build();
        notification.defaults += Notification.DEFAULT_SOUND;

        int notificationId = (int)DateTime.parse(id,DateManager.dateFormat).getMillis()%1000000000;
        nm.notify(notificationId, notification);
    }
}
