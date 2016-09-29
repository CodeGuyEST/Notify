package app.notify;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import org.joda.time.DateTime;

public class UserNotificationsActivity extends AppCompatActivity {

    RecyclerView notificationList;
    NotificationAdapter adapter;
    static boolean firstNotificationLoading = true;//Device may have been rebooted or application forcefully closed.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_notifications);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.main_activity_title);
        notificationList = (RecyclerView)findViewById(R.id.main_recycler_view);
        adapter = new NotificationAdapter(this);
        notificationList.setLayoutManager(new LinearLayoutManager(this));
        notificationList.setAdapter(adapter);
        notificationList.addItemDecoration(new DividerItemDecoration(this));
    }

    @Override
    protected void onStart() {
        super.onStart();
        getNotifications(adapter);
    }

    public void addNotification(View v) {
        Intent intent = new Intent(this, AddNotificationActivity.class);
        startActivity(intent);
    }

    private void getNotifications(NotificationAdapter adapter) {
        adapter.clear();
        SQLiteDatabase database = new DbHelper(this).getWritableDatabase();
        String[] columns = {Utils.NOTIFICATIONS_DB_TITLE,Utils.NOTIFICATIONS_DB_DESCRIPTION,Utils.NOTIFICATIONS_DB_ICON_RESOURCE,
                Utils.NOTIFICATIONS_DB_ICON_BITMAP,Utils.NOTIFICATIONS_DB_PRIORITY,
                Utils.NOTIFICATIONS_DB_TIME,Utils.NOTIFICATIONS_DB_ID};
        Cursor cursor = database.query(Utils.NOTIFICATIONS_DB_NAME,columns,null,null,null,null,null);
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            String title = cursor.getString(0);
            String description = cursor.getString(1);
            int resource = cursor.getInt(2);
            byte[] bitmap = cursor.getBlob(3);
            int priority = cursor.getInt(4);
            DateTime date = DateManager.getDate(cursor.getString(5));
            String id = cursor.getString(6);
            if(date.isBeforeNow() || date.isEqualNow()) {
                deleteNotificationFromSQL(id,database);
                cursor.moveToNext();
                continue;
            }
            assert(bitmap != null || resource != 0);
            Bitmap iconBitmap;
            if(resource == 0) {
                iconBitmap = BitmapFactory.decodeByteArray(bitmap, 0, bitmap.length);
                adapter.addNotification(new Notification(title,description,priority,date,iconBitmap,id));
                if(firstNotificationLoading) {
                    NotificationManager.startAlarm(date, new Notification(title, description, priority, date, iconBitmap, id),this);
                }
            }
            else {
                adapter.addNotification(new Notification(title, description, priority, date, resource, id));
                if(firstNotificationLoading) {
                    NotificationManager.startAlarm(date, new Notification(title, description, priority, date, resource, id),this);
                }
            }
            cursor.moveToNext();
        }
        cursor.close();
        firstNotificationLoading = false;
        //database.close();
    }

    private void deleteNotificationFromSQL(String id, SQLiteDatabase db) {
        db.delete(Utils.NOTIFICATIONS_DB_NAME, Utils.NOTIFICATIONS_DB_ID + "=\"" + id + "\"", null);
        //db.close();
    }
}
