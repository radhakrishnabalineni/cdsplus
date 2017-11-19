package com.hp.concentra.extractor.utils;

import java.util.Date;

import com.documentum.fc.common.IDfTime;

public class IDfTimeConcentra implements IDfTime {
    public static String CONCENTRA_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";
    public static String CONCENTRA_NULL_DATE = "";

    private IDfTime time;

    public IDfTimeConcentra(IDfTime time) {
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
        return CONCENTRA_PATTERN;
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
        return time != null && !time.isNullDate() ? time.asString(CONCENTRA_PATTERN) : CONCENTRA_NULL_DATE;
    }
}
