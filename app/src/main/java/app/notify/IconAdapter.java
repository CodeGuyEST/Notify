package app.notify;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

/**
 * Created by Georg on 20.09.2016.
 */
public class IconAdapter extends BaseAdapter {

    Context ctx;

    public IconAdapter(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public int getCount() {
        return iconIds.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(ctx);
            imageView.setLayoutParams(new GridView.LayoutParams(120, 120));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageResource(iconIds[position]);
        return imageView;
    }

    public int getResource(int position) {
        return iconIds[position];
    }

    private Integer[] iconIds = {
            R.drawable.ic_clear_search_api_holo_light, R.drawable.ic_menu_call,
            R.drawable.ic_menu_camera, R.drawable.ic_menu_cc_am,
            R.drawable.ic_menu_close_clear_cancel, R.drawable.ic_menu_compass,
            R.drawable.ic_menu_compose, R.drawable.ic_menu_cut,
            R.drawable.ic_menu_day, R.drawable.ic_menu_delete,
            R.drawable.ic_menu_directions, R.drawable.ic_menu_edit,
            R.drawable.ic_menu_emoticons, R.drawable.ic_menu_help_holo_light,
            R.drawable.ic_menu_home, R.drawable.ic_menu_mapmode,
            R.drawable.ic_menu_mark, R.drawable.ic_menu_month,
            R.drawable.ic_menu_more, R.drawable.ic_menu_mylocation,
            R.drawable.ic_menu_myplaces, R.drawable.ic_menu_notifications,
            R.drawable.ic_menu_paste, R.drawable.ic_menu_star,
            R.drawable.ic_menu_start_conversation, R.drawable.ic_menu_stop,
            R.drawable.ic_menu_today, R.drawable.ic_menu_upload,
            R.drawable.ic_menu_zoom
    };
}
