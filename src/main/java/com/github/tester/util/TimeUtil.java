package com.github.tester.util;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class TimeUtil {
    public static String getYYYYMMDDHH24MISS() {
        Calendar c = GregorianCalendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;
        int date = c.get(Calendar.DAY_OF_MONTH);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int second = c.get(Calendar.SECOND);
        StringBuilder sb = new StringBuilder();
        sb.append(year).append(month < 10 ? "0" + month : month).append(date < 10 ? "0" + date : date)
                .append(hour < 10 ? "0" + hour : hour).append(minute < 10 ? "0" + minute : minute).append(second < 10 ? "0" + second : second);
        return sb.toString();
    }

    private static java.sql.Timestamp getCurrentTimeStamp() {
        java.util.Date today = new java.util.Date();
        return new java.sql.Timestamp(today.getTime());
    }
    
    
    
    public static Date makeDateFromYYYYMMDD(String date) {
        int year = Integer.parseInt(date.substring(0,4));
        int month = Integer.parseInt(date.substring(4,6));
        int d = Integer.parseInt(date.substring(6,8));
        return new Date(year-1900, month-1, d);
    }
}
