package com.mmall.util;


import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;

/**
 * @author GEMI
 * @date 2018/3/12/0012 18:04
 */
public class DateTimeUtil {

    public static final String FORMAT_DATE="YYYY-MM-DD HH:mm:ss";

    //str-->date
    public static Date strToDate(String dateStr, String formatDate){
        DateTimeFormatter dateTimeFormatter= DateTimeFormat.forPattern(formatDate);
        DateTime dateTime=dateTimeFormatter.parseDateTime(dateStr);
        return dateTime.toDate();
    }

    //date-->str
    public static String dateToStr(Date date,String formatDate){

        if(date==null){
            return StringUtils.EMPTY;
        }
        DateTime dateTime=new DateTime(date);
        return dateTime.toString(formatDate);
    }

    public static Date strToDate(String dateStr){
        DateTimeFormatter dateTimeFormatter= DateTimeFormat.forPattern(FORMAT_DATE);
        DateTime dateTime=dateTimeFormatter.parseDateTime(dateStr);
        return dateTime.toDate();
    }

    //date-->str
    public static String dateToStr(Date date){

        if(date==null){
            return StringUtils.EMPTY;
        }
        DateTime dateTime=new DateTime(date);
        return dateTime.toString(FORMAT_DATE);
    }

    public static void main(String[] args) {
        System.out.println(DateTimeUtil.strToDate("2017-01-01","YYYY-MM-DD"));
    }
}
