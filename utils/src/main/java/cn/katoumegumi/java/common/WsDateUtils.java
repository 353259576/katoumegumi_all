package cn.katoumegumi.java.common;

import cn.katoumegumi.java.common.model.WsRun;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

public class WsDateUtils {


    public static final String[] CN_MONTH_NAMES = new String[]{"一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月"};

    public static final String[] CN_WEEK_NAMES = new String[]{"星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"};


    public static final String LONGTIMESTRING = "yyyy-MM-dd HH:mm:ss";
    public static final String LONGTIMESTRINGCOMPACT = "yyyyMMddHHmmss";
    public static final String CNLONGTIMESTRING = "yyyy年MM月dd日 HH时mm分ss秒";
    public static final String SMALLTIMESTRING = "yyyy-MM-dd";
    public static final String CNSMALLTIMESTRING = "yyyy年MM月dd日";

    /**
     * 获取一段程序的执行时间
     */
    public static final Consumer<WsRun> getExecutionTime = wsRun -> {
        long start = System.currentTimeMillis();
        wsRun.run();
        long end = System.currentTimeMillis();
        System.out.println("执行时间为：" + (end - start));
    };


    public static String dateStringFormat(String date) {
        char[] chars = date.toCharArray();
        List<String> strings = new ArrayList<>();
        for (int i = 0; i < chars.length; i++) {
            StringBuilder stringBuffer = new StringBuilder();
            while (i < chars.length && chars[i] > 47 && chars[i] < 58) {
                stringBuffer.append(chars[i]);
                i++;
            }
            if (stringBuffer.length() > 0) {
                strings.add(stringBuffer.toString());
            }
        }
        if (strings.size() > 5) {
            return strings.get(0) + "-" + strings.get(1) + "-" + strings.get(2) + " " + strings.get(3) + ":" + strings.get(4) + ":" + strings.get(5);
        }
        if (strings.size() > 2) {
            return strings.get(0) + "-" + strings.get(1) + "-" + strings.get(2) + " 00:00:00";
        }
        if (strings.size() == 1) {
            if (WsStringUtils.isNumber(strings.get(0))) {
                Date date1 = new Date(Long.parseLong(strings.get(0)));
                return WsDateUtils.dateToString(date1, WsDateUtils.LONGTIMESTRING);
            }

        }
        return null;
    }

    public static Date stringToDate(String date, String mode) {
        try {
            date = dateStringFormat(date);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(mode);
            return simpleDateFormat.parse(date);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public static Date stringToDate(String date) {
        return stringToDate(date, LONGTIMESTRING);
    }

    public static Date objectToDate(Object date) {
        try {
            String dateString = objectDateFormatString(date);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(LONGTIMESTRING);
            return simpleDateFormat.parse(dateString);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public static String dateToString(Date date, String dateType) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateType);
        return simpleDateFormat.format(date);
    }

    public static <T> String objectDateFormatString(T object) {
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
            } else if (object instanceof LocalDate) {
                return ((LocalDate) object).format(DateTimeFormatter.ofPattern(LONGTIMESTRING));
            } else if (object instanceof LocalDateTime) {
                return ((LocalDateTime) object).format(DateTimeFormatter.ofPattern(LONGTIMESTRING));
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public static int chinaWeek(int westWeek) {
        int chinaWeek = westWeek - 1;
        if (chinaWeek == 0) {
            chinaWeek = 7;
        }
        return chinaWeek;
    }

    /**
     * 获取中文星期
     *
     * @param date 时间
     * @return 中文星期
     */
    public static String getCNWeekdayName(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int weekday = calendar.get(Calendar.DAY_OF_WEEK);
        weekday = chinaWeek(weekday);
        return CN_WEEK_NAMES[weekday - 1];
    }

    /**
     * 获取中文月
     *
     * @param date 时间
     * @return 中文月
     */
    public static String getCNMonthName(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return CN_MONTH_NAMES[calendar.get(Calendar.MONTH)];
    }
}
