package app.notify;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.joda.time.DateTime;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

/**
 * Created by Georg on 23.09.2016.
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private ArrayList<Notification> items;
    private Context ctx;

    public NotificationAdapter(Context ctx) {
        items = new ArrayList<>();
        this.ctx = ctx;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.single_notification, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.title.setText(items.get(position).title);
        holder.description.setText(items.get(position).description);
        if(items.get(position).iconFromResources()) {
            Log.d("ICONFROMRESOURCES","TRUE");
            holder.icon.setImageResource(items.get(position).iconResource);
        }
        else {
            Log.d("ICONFROMRESOURCES","FALSE");
            holder.icon.setImageBitmap(items.get(position).icon);
        }
        holder.cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteFromSQL(items.get(holder.getAdapterPosition()).getId());
                NotificationManager.cancelNotification(items.get(holder.getAdapterPosition()).getId(), ctx);
                removeItem(holder.getAdapterPosition());
                notifyItemRemoved(holder.getAdapterPosition());
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ctx,AddNotificationActivity.class);
                Notification notification = items.get(holder.getAdapterPosition());
                intent.putExtra(Utils.title,notification.title);
                intent.putExtra(Utils.description,notification.description);
                intent.putExtra(Utils.priority,notification.priority);
                intent.putExtra(Utils.time,DateManager.dateFormat.print(notification.time));
                if(items.get(holder.getAdapterPosition()).iconFromResources())
                    intent.putExtra(Utils.icon_resource,notification.iconResource);
                else intent.putExtra(Utils.icon_bitmap,getBytes(notification.icon));
                intent.putExtra(Utils.id,notification.getId());
                ctx.startActivity(intent);
            }
        });
    }

    private void cancelNotification(String id) {
        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(ctx.ALARM_SERVICE);
        Intent intent = new Intent(ctx,NotificationService.class);
        int notificationId = (int) DateTime.parse(id, DateManager.dateFormat).getMillis()%1000000000;
        alarmManager.cancel(PendingIntent.getService(ctx, notificationId, intent, PendingIntent.FLAG_CANCEL_CURRENT));
    }

    private static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addNotification(Notification notification) {
        items.add(notification);
        notifyItemInserted(items.size() - 1);
    }

    public void removeItem(int position) {
        items.remove(position);
    }

    public void clear() {
        items.clear();
        notifyDataSetChanged();
    }

    private boolean deleteFromSQL(String id) {
        SQLiteDatabase db = new DbHelper(ctx).getWritableDatabase();
        return db.delete(Utils.NOTIFICATIONS_DB_NAME, Utils.NOTIFICATIONS_DB_ID + "=\"" + id + "\"", null) > 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon,cancel;
        TextView title,description;
        View itemView;
        public ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            this.icon = (ImageView)itemView.findViewById(R.id.icon);
            this.title = (TextView)itemView.findViewById(R.id.title_textview);
            this.description = (TextView)itemView.findViewById(R.id.description_textview);
            this.cancel = (ImageView)itemView.findViewById(R.id.cancel);
        }
    }
}
