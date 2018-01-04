package io.cryptomage.eidas.report;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormatter {
    public static String ISO_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS zzz";
    private static final SimpleDateFormat isoFormatter = new SimpleDateFormat(ISO_FORMAT);
    public static String getString(Date date) {
        return isoFormatter.format(date);
    }
}
