package net.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeUtil {
    private static final SimpleDateFormat FORMAT_YYYYMMDDHHMMSS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat FORMAT_YYYYMMDDHHMMSSLong = new SimpleDateFormat("yyyyMMddHHmmss");

    /**
     * 获取yyyyMMddhhmmss格式日期
     *
     * @return 返回格式化后的日期字符串
     */
    public static long getUtcYYYYMMDDHHMMSSTime() {
        return Long.parseLong(FORMAT_YYYYMMDDHHMMSSLong.format(new Date(getUtcCalendar().getTimeInMillis())));
    }

    public static Calendar getUtcCalendar() {
        //1、取得本地时间：
        Calendar cal = Calendar.getInstance();
        //2、取得时间偏移量：
        int zoneOffset = cal.get(Calendar.ZONE_OFFSET);
        //3、取得夏令时差：
        int dstOffset = cal.get(Calendar.DST_OFFSET);
        //4、从本地时间里扣除这些差量，即可以取得UTC时间：
        cal.add(Calendar.MILLISECOND, -(zoneOffset + dstOffset));

        return cal;
    }
}
