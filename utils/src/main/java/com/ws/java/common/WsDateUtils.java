package com.ws.java.common;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class WsDateUtils {
    public static final String LONGTIMESTRING = "yyyy-MM-dd HH:mm:ss";
    public static final String LONGTIMESTRINGCOMPACT = "yyyyMMddHHmmss";
    public static final String CNLONGTIMESTRING = "yyyy年MM月dd日 HH时mm分ss秒";
    public static final String SMALLTIMESTRING = "yyyy-MM-dd";
    public static final String CNSMALLTIMESTRING = "yyyy年MM月dd日";


    public static void main(String[] args) {
        System.out.println(chinaWeek(1));
    }




    public enum  WsDateUtilsEnum{
        YEAR(1),MONTH(2),DAY(3),HOUR(4),MINUTE(5),SECOND(6);
        private int code;
        WsDateUtilsEnum(int code){
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }







    public Date changeDate(Date date,int addNum,int type){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(type,addNum);
        return calendar.getTime();
    }

    public static long dateDifference(Date oldDate,Date newDate,WsDateUtilsEnum wsDateUtilsEnum){
        long difference = 0L;
        switch (wsDateUtilsEnum){
/*            case YEAR:
                Calendar calendar = Calendar.getInstance();
                calendar.
                break;
            case MONTH:break;*/
            case DAY: difference = newDate.getTime() - oldDate.getTime();return difference/1000/60/60/24;
            case HOUR:difference = newDate.getTime() - oldDate.getTime();return difference/1000/60/60;
            case MINUTE:difference = newDate.getTime() - oldDate.getTime();return difference/1000/60;
            case SECOND:difference = newDate.getTime() - oldDate.getTime();return difference/1000;
            default:break;
        }
        return 0;
    }





    public static String dateStringFormat(String date){
        char chars[] = date.toCharArray();
        List<String> strings = new ArrayList<>();
        for(int i = 0; i < chars.length; i++){
            StringBuffer stringBuffer = new StringBuffer();
            while (i<chars.length&&chars[i]>47&&chars[i]<58){
                stringBuffer.append(chars[i]);
                i++;
            }
            if(stringBuffer.length()>0){
                strings.add(stringBuffer.toString());
            }
        }
        if(strings.size()>5){
            return strings.get(0)+"-"+strings.get(1)+"-"+strings.get(2)+" "+strings.get(3)+":"+strings.get(4)+":"+strings.get(5);
        }
        if(strings.size()>2){
            return strings.get(0)+"-"+strings.get(1)+"-"+strings.get(2)+" 00:00:00";
        }
        if(strings.size() == 1){
            if(WsStringUtils.isNumber(strings.get(0))){
                Date date1 = new Date(Long.parseLong(strings.get(0)));
                return WsDateUtils.dateToString(date1, WsDateUtils.LONGTIMESTRING);
            }

        }
        return null;
    }


    public static Date stringToDate(String date,String mode) {
        try {
            date = dateStringFormat(date);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(mode);
            return simpleDateFormat.parse(date);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public static Date stringToDate(String date){
        return stringToDate(date,LONGTIMESTRING);
    }


    public static Date objectToDate(Object date){
        try {
            String dateString = objectDateFormatString(date);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(LONGTIMESTRING);
            return simpleDateFormat.parse(dateString);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }

    }


    public static String dateToString(Date date,String dateType){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateType);
        return simpleDateFormat.format(date);
    }


    public static String objectDateFormatString(Object object) {
        try {
            if (object instanceof String) {
                if (WsStringUtils.isNumber((String) object)) {
                    return dateToString(new Date(Long.parseLong((String) object)), LONGTIMESTRING);
                } else {
                    return dateStringFormat((String) object);
                }

            } else if (object instanceof Date) {
                return dateToString((Date) object, LONGTIMESTRING);
            } else if (object.getClass().isPrimitive()) {
                return dateToString(new Date(Long.parseLong(String.valueOf(object))), LONGTIMESTRING);
            } else if (object instanceof Number) {
                return dateToString(new Date(Long.parseLong(String.valueOf(object))), LONGTIMESTRING);
            } else if (object instanceof java.sql.Date) {
                return dateToString((Date) object, LONGTIMESTRING);
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }


    public static int chinaWeek(int westWeek){
        int chinaWeek = westWeek - 1;
        if(chinaWeek == 0){
            chinaWeek = 7;
        }
        return chinaWeek;
    }
}
