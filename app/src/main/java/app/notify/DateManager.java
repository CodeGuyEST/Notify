package app.notify;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Created by Georg on 21.09.2016.
 */
public final class DateManager {

    static DateTimeFormatter dateFormat = DateTimeFormat.forPattern("dd:MM:yyyy HH:mm:ss");

    static DateTime getDate(String dateString) {
        return dateFormat.parseDateTime(dateString);
    }
}
