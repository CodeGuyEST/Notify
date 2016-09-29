package app.notify;

import android.graphics.Bitmap;

import org.joda.time.DateTime;

/**
 * Created by Georg on 23.09.2016.
 */
public class Notification {
    public String title,description;
    public int priority;
    public DateTime time;
    public Bitmap icon;
    public int iconResource;
    private String id;//Date of adding

    public Notification(String title, String description, int priority, DateTime time, Bitmap icon,String id) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.time = time;
        this.icon = icon;
        this.id = id;
        this.iconResource = 0;
    }

    public Notification(String title, String description, int priority, DateTime time, int resource ,String id) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.time = time;
        this.iconResource = resource;
        this.id = id;
        this.icon = null;
    }

    public String getId() {return id;}

    public boolean iconFromResources() {
        return iconResource != 0;
    }
}
