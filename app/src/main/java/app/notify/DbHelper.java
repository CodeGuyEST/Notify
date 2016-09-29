package app.notify;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.util.Log;

import org.joda.time.DateTime;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Georg on 21.09.2016.
 */
public class DbHelper extends SQLiteOpenHelper {

    static Context ctx;

    public static final String DB_NAME = Utils.NOTIFICATIONS_DB_NAME;

    public static final int DB_VERSION = Utils.NOTIFICATIONS_DB_VERSION;

    public static final String TITLE = Utils.NOTIFICATIONS_DB_TITLE;

    public static final String DESCRIPTION = Utils.NOTIFICATIONS_DB_DESCRIPTION;

    public static final String TIME = Utils.NOTIFICATIONS_DB_TIME;

    public static final String PRIORITY = Utils.NOTIFICATIONS_DB_PRIORITY;

    public static final String ICON = Utils.NOTIFICATIONS_DB_ICON_RESOURCE;

    public static final String ICON_BITMAP = Utils.NOTIFICATIONS_DB_ICON_BITMAP;

    public static final String NOTIF_ID = Utils.NOTIFICATIONS_DB_ID;

    private static final String CREATE_ENTRIES = "CREATE TABLE " + DB_NAME + " (" +
            "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            TITLE + " TEXT," +
            DESCRIPTION + " TEXT," +
            TIME + " TEXT," +
            PRIORITY + " INTEGER," +
            ICON + " TEXT," +
            ICON_BITMAP + " BLOB," +
            NOTIF_ID + " TEXT );";

    private static final String DROP_TABLE = "DROP TABLE IF EXISTS " + DB_NAME;

    public DbHelper(Context ctx) {
        super(ctx,DB_NAME,null,DB_VERSION);
        this.ctx = ctx;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE);
        db.execSQL(CREATE_ENTRIES);
    }

    public static void startAlarm(DateTime time, Notification notification) {
        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(ctx.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        int year = time.getYear();
        int month = time.getMonthOfYear() - 1;
        Log.d("ALARMMONTH", Integer.toString(time.getMonthOfYear()));
        int day = time.getDayOfMonth();
        int hour = time.getHourOfDay();
        int minute = time.getMinuteOfHour();
        calendar.set(year, month, day, hour, minute);
        long when = calendar.getTimeInMillis();// notification time
        Log.d("WHEN",Long.toString(when - new Date().getTime()));

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
        Log.d("PENDINGINTENTID",Integer.toString((int)DateTime.parse(
                notification.getId(),DateManager.dateFormat).getMillis()%1000000000));
        alarmManager.set(AlarmManager.RTC_WAKEUP, when, pendingIntent);
    }

    private static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }
}
