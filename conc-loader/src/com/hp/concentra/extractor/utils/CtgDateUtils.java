package com.hp.concentra.extractor.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class CtgDateUtils {

    public static int getHours(Date date) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(date);
        int adiminRequestHours = cal.get(Calendar.HOUR_OF_DAY);
        return adiminRequestHours;
    }
}
