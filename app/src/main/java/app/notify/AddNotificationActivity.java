package app.notify;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import org.joda.time.DateTime;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;

public class AddNotificationActivity extends AppCompatActivity {
    EditText title,description;
    DatePicker datePicker;
    TimePicker timePicker;
    Spinner priority;
    ImageView icon;
    Bitmap iconBitmap;
    String id;
    boolean updating;//Are we adding a notification or updating current one?
    AlertDialog dialog;
    //LockableScrollView scrollView;

    // this is the action code we use in our intent,
    // this way we know we're looking at the response from our own action
    private static final int SELECT_PICTURE = 1;

    private int iconResourceName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_notification);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        title = (EditText)findViewById(R.id.title);
        description = (EditText)findViewById(R.id.description);
        datePicker = (DatePicker)findViewById(R.id.date_picker);
        timePicker = (TimePicker)findViewById(R.id.time_picker);
        priority = (Spinner)findViewById(R.id.priority);
        icon = (ImageView)findViewById(R.id.icon);
        //scrollView = (LockableScrollView)findViewById(R.id.scroll_view);

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        dataAdapter.addAll("Max", "High", "Default", "Low", "Min");
        datePicker.setMinDate(new Date().getTime());

        priority.setAdapter(dataAdapter);
        priority.setSelection(2);//Default priority.

        if(getIntent().getExtras() != null && !getIntent().getExtras().isEmpty()) {//We are updating existing notification.
            updating = true;
            Bundle extras = getIntent().getExtras();
            String title = extras.getString(Utils.title);
            String description = extras.getString(Utils.description);
            String time = extras.getString(Utils.time);
            String id = extras.getString(Utils.id);
            int priority = extras.getInt(Utils.priority);
            byte[] bitmap = null;
            int resource = 0;
            if(extras.containsKey(Utils.icon_bitmap)) {
                bitmap = extras.getByteArray(Utils.icon_bitmap);
                this.iconBitmap = BitmapFactory.decodeByteArray(bitmap, 0, bitmap.length);
                this.icon.setImageBitmap(iconBitmap);
            }
            else if (extras.containsKey(Utils.icon_resource)) {
                resource = extras.getInt(
                        Utils.icon_resource);
                this.iconResourceName = resource;
                this.icon.setImageResource(iconResourceName);
            }

            this.id = id;
            this.title.setText(title);
            this.title.setSelection(title.length());
            this.description.setText(description);
            this.description.setSelection(description.length());
            this.priority.setSelection(priority);
            if(bitmap != null){
                iconBitmap = BitmapFactory.decodeByteArray(bitmap,0,bitmap.length);
                icon.setImageBitmap(iconBitmap);
            }
            else {
                iconResourceName = resource;
                icon.setImageResource(iconResourceName);
            }

            DateTime date = DateManager.dateFormat.parseDateTime(time);
            if(Build.VERSION.SDK_INT >= 23) {
                this.timePicker.setHour(date.getHourOfDay());
                this.timePicker.setMinute(date.getMinuteOfHour());
            }
            else {
                this.timePicker.setCurrentHour(date.getHourOfDay());
                this.timePicker.setCurrentMinute(date.getMinuteOfHour());
            }
            this.datePicker.updateDate(date.getYear(), date.getMonthOfYear() - 1, date.getDayOfMonth());
            Button button = (Button)findViewById(R.id.button);
            button.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.added, 0, 0);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String title = AddNotificationActivity.this.title.getText().toString();
                    String description = AddNotificationActivity.this.description.getText().toString();
                    String id = AddNotificationActivity.this.id;
                    int priority = AddNotificationActivity.this.priority.getSelectedItemPosition();
                    Bitmap bitmap = AddNotificationActivity.this.iconBitmap;

                    int year = AddNotificationActivity.this.datePicker.getYear();
                    int month = AddNotificationActivity.this.datePicker.getMonth();
                    int day = AddNotificationActivity.this.datePicker.getDayOfMonth();

                    int hours, minutes;

                    if (Build.VERSION.SDK_INT >= 23) {
                        hours = AddNotificationActivity.this.timePicker.getHour();
                        minutes = AddNotificationActivity.this.timePicker.getMinute();
                    } else {
                        hours = AddNotificationActivity.this.timePicker.getCurrentHour();
                        minutes = AddNotificationActivity.this.timePicker.getCurrentMinute();
                    }

                    DateTime currentDate = new DateTime(year, month + 1, day, hours, minutes);

                    if(!isValidDate(currentDate)) {
                        Toast.makeText(AddNotificationActivity.this,R.string.not_valid_time,Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (bitmap != null) {
                        updateNotificationInSQL(id, title, description, priority, getBytes(bitmap), currentDate);
                        NotificationManager.updateNotification(new Notification(title, description, priority, currentDate, bitmap, id),
                                currentDate, AddNotificationActivity.this);
                    }
                    else
                    {
                        updateNotificationInSQL(id, title, description, priority, iconResourceName, currentDate);
                        NotificationManager.updateNotification
                                (new Notification(title, description, priority, currentDate, iconResourceName, id),
                                        currentDate, AddNotificationActivity.this);
                    }
                    finish();
                }
            });
        }
        else {
            icon.setImageResource(Utils.DEFAULT_ICON);
            iconResourceName = Utils.DEFAULT_ICON;
            updating = false;
        }

    }

    private boolean isValidDate(DateTime dateTime) {
        Log.d("MILLIS",Long.toString(dateTime.getMillis() - new DateTime().getMillis()));
        return dateTime.isAfterNow();
    }

    private void updateNotificationInSQL(String id, String title, String description, int priority,
                                         byte[] icon, DateTime time) {
        SQLiteDatabase database = new DbHelper(this).getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(Utils.NOTIFICATIONS_DB_TITLE,title);
        contentValues.put(Utils.NOTIFICATIONS_DB_DESCRIPTION, description);
        contentValues.put(Utils.NOTIFICATIONS_DB_PRIORITY, priority);
        contentValues.put(Utils.NOTIFICATIONS_DB_ICON_BITMAP, icon);
        String notifTime = DateManager.dateFormat.print(time);
        contentValues.put(Utils.NOTIFICATIONS_DB_TIME, notifTime);
        String noResource = null;
        contentValues.put(Utils.NOTIFICATIONS_DB_ICON_RESOURCE, noResource);

        String whereClause = Utils.NOTIFICATIONS_DB_ID + "=\"" + id + "\"";

        database.update(Utils.NOTIFICATIONS_DB_NAME, contentValues, whereClause, null);
        database.close();
    }

    private void updateNotificationInSQL(String id, String title, String description, int priority,
                                         int resource, DateTime time) {
        SQLiteDatabase database = new DbHelper(this).getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(Utils.NOTIFICATIONS_DB_TITLE,title);
        contentValues.put(Utils.NOTIFICATIONS_DB_DESCRIPTION, description);
        contentValues.put(Utils.NOTIFICATIONS_DB_PRIORITY, priority);
        contentValues.put(Utils.NOTIFICATIONS_DB_ICON_RESOURCE, resource);
        String notifTime = DateManager.dateFormat.print(time);
        contentValues.put(Utils.NOTIFICATIONS_DB_TIME,notifTime);
        byte[] emptyArr = null;
        contentValues.put(Utils.NOTIFICATIONS_DB_ICON_BITMAP, emptyArr);

        String whereClause = Utils.NOTIFICATIONS_DB_ID + "=\"" + id + "\"";

        database.update(Utils.NOTIFICATIONS_DB_NAME, contentValues, whereClause, null);
        database.close();
    }

    public void setIcon(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.icon_dialog_title);
        View iconGrid = getLayoutInflater().inflate(R.layout.icon_select_layout, null);
        dialog = builder.create();
        dialog.setView(iconGrid);
        dialog.show();
        final int dialogWidth = 850;
        final int dialogHeight = 850;
        dialog.getWindow().setLayout(dialogWidth, dialogHeight);
        GridView iconList = (GridView)dialog.findViewById(R.id.grid_view);
        iconList.setAdapter(new IconAdapter(this));
        iconList.setOnItemClickListener(new GridView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                icon.setImageResource(((IconAdapter) (parent.getAdapter())).getResource(position));
                iconResourceName = ((IconAdapter) parent.getAdapter()).getResource(position);
                iconBitmap = null;
                dialog.dismiss();
            }
        });
    }

    public void saveNotificationToStorage(View v) {
        String title = this.title.getText().toString();
        String description = this.description.getText().toString();
        int priority = this.priority.getSelectedItemPosition();
        DateTime dateTime = new DateTime();
        if (Build.VERSION.SDK_INT >= 23) {
            dateTime = new DateTime(datePicker.getYear(),datePicker.getMonth() + 1,datePicker.getDayOfMonth(),
                    timePicker.getHour(),timePicker.getMinute());
        }

        else {
            dateTime = new DateTime(datePicker.getYear(),datePicker.getMonth() + 1,datePicker.getDayOfMonth(),
                    timePicker.getCurrentHour(),timePicker.getCurrentMinute());
        }

        if(dateTime.isBefore(new DateTime())) {
            Toast.makeText(this,R.string.not_valid_time,Toast.LENGTH_SHORT).show();
            return;
        }

        String date = dateTime.toString(DateManager.dateFormat);
        SQLiteDatabase database = new DbHelper(this).getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(Utils.NOTIFICATIONS_DB_TITLE, title);
        contentValues.put(Utils.NOTIFICATIONS_DB_DESCRIPTION, description);
        contentValues.put(Utils.NOTIFICATIONS_DB_PRIORITY, priority);
        contentValues.put(Utils.NOTIFICATIONS_DB_TIME, date);
        contentValues.put(Utils.NOTIFICATIONS_DB_ICON_RESOURCE, iconResourceName);
        byte[] bitmap = iconBitmap == null ? null : getBytes(iconBitmap);//CHANGED FroM bYte[0]
        contentValues.put(Utils.NOTIFICATIONS_DB_ICON_BITMAP,bitmap);
        contentValues.put(Utils.NOTIFICATIONS_DB_ID,DateManager.dateFormat.print(dateTime));
        database.insert(Utils.NOTIFICATIONS_DB_NAME, null, contentValues);
        DateTime alarmTrigger = new DateTime(datePicker.getYear(),datePicker.getMonth()+1,datePicker.getDayOfMonth(),
                Build.VERSION.SDK_INT >= 23 ? timePicker.getHour() : timePicker.getCurrentHour(),
                Build.VERSION.SDK_INT >= 23 ? timePicker.getMinute(): timePicker.getCurrentMinute());
        if(iconResourceName == 0){
            assert(iconBitmap != null);
            Notification notification = new Notification(title,description,priority,alarmTrigger,iconBitmap,
                    DateManager.dateFormat.print(dateTime));
            NotificationManager.startAlarm(alarmTrigger, notification, this);
        }
        else {
            assert(iconResourceName != 0);
            Notification notification = new Notification(title,description,priority,alarmTrigger,iconResourceName,
                    DateManager.dateFormat.print(dateTime));
            NotificationManager.startAlarm(alarmTrigger, notification, this);
        }
        finish();
    }

    private static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    public void chooseIconFromGallery(View v) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,
                getResources().getString(R.string.icon_chooser_text)), SELECT_PICTURE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                try {
                    Uri selectedImageUri = data.getData();
                    //iconResourceName = selectedImageUri.getPath();

                    InputStream input = this.getContentResolver().openInputStream(selectedImageUri);

                    BitmapFactory.Options boundsOptions = new BitmapFactory.Options();
                    boundsOptions.inJustDecodeBounds = true;
                    BitmapFactory.decodeStream(input,null,boundsOptions);
                    input.close();
                    int originalSize = (boundsOptions.outHeight > boundsOptions.outWidth)
                            ? boundsOptions.outHeight : boundsOptions.outWidth;

                    double ratio = (originalSize > Math.max(icon.getWidth(),icon.getHeight())) ? (originalSize /
                            Math.max(icon.getWidth(),icon.getHeight())) : 1.0;

                    BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
                    bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
                    input = getContentResolver().openInputStream(selectedImageUri);
                    iconBitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
                    input.close();
                    iconResourceName = 0;
                    icon.setImageBitmap(iconBitmap);
                    if(dialog != null && dialog.isShowing())dialog.dismiss();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void startAlarm(DateTime time, Notification notification) {
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(this.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        int year = time.getYear();
        int month = time.getMonthOfYear();
        int day = time.getDayOfMonth();
        int hour = time.getHourOfDay();
        int minute = time.getMinuteOfHour();
        calendar.set(year, month, day, hour, minute);
        long when = calendar.getTimeInMillis();// notification time

        Intent intent = new Intent(this, NotificationService.class);
        intent.putExtra(Utils.title,notification.title);
        intent.putExtra(Utils.description,notification.description);
        intent.putExtra(Utils.priority,notification.priority);
        intent.putExtra(Utils.id,notification.getId());
        if(notification.iconFromResources())
            intent.putExtra(Utils.icon_resource,notification.iconResource);
        else
            intent.putExtra(Utils.icon_bitmap,getBytes(notification.icon));
        PendingIntent pendingIntent = PendingIntent.getService(this,
                (int)DateTime.parse(notification.getId(),DateManager.dateFormat).getMillis()%1000000000, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.set(AlarmManager.RTC_WAKEUP, when, pendingIntent);
    }

    private static int getPowerOfTwoForSampleRatio(double ratio){
        int k = Integer.highestOneBit((int)Math.floor(ratio));
        if(k==0) return 1;
        else return k;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)finish();
        return super.onOptionsItemSelected(item);
    }

    /*private void updateNotification(Notification newData, DateTime newTime) {
        cancelNotification(newData.getId());
        startAlarm(newTime,newData);
    }*/

    private void cancelNotification(String id) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(this.ALARM_SERVICE);
        Intent intent = new Intent(this,NotificationService.class);
        int notificationId = (int) DateTime.parse(id, DateManager.dateFormat).getMillis()%1000000000;

        alarmManager.cancel(PendingIntent.getService(this, notificationId, intent, PendingIntent.FLAG_NO_CREATE));
    }
}
