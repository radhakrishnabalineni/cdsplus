package com.hp.soar.priorityLoader.ref;

import java.util.Date;

import com.documentum.fc.common.IDfTime;

public class IDfTimeFormatter implements IDfTime {
    public static String TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";
    public static String TIME_NULL_DATE = "";

    private IDfTime time;

    public IDfTimeFormatter(IDfTime time) {
        this.time = time;
    }

    public String asString(String s) {
        return time.asString(s);
    }

    public int compareTo(IDfTime idftime) {
        return time.compareTo(idftime);
    }

    public Date getDate() {
        return time.getDate();
    }

    public int getDay() {
        return time.getDay();
    }

    public int getHour() {
        return time.getHour();
    }

    public int getMinutes() {
        return time.getMinutes();
    }

    public int getMonth() {
        return time.getMonth();
    }

    public String getPattern() {
        // return time.getPattern();
        return TIME_PATTERN;
    }

    public int getSeconds() {
        return time.getSeconds();
    }

    public int getYear() {
        return time.getYear();
    }

    public boolean isNullDate() {
        return time.isNullDate();
    }

    public boolean isValid() {
        return time.isValid();
    }

    public String toString() {
        return time != null && !time.isNullDate() ? time.asString(TIME_PATTERN) : TIME_NULL_DATE;
    }
}
