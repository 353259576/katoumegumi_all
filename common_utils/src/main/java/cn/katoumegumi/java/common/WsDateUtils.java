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



    public static final String DEFAULT_TIME_TEMPLATE = "yyyy-MM-dd'T'HH:mm:ss";

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
    private static final Long SECONDS = 1000L;
    private static final Long MINUTES = 60L * SECONDS;
    private static final Long HOUR = 60L * MINUTES;
    private static final Long DAY = 24 * HOUR;
    private static final Long WEEK = 7 * DAY;

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

    public static int getCnWeek(int westWeek) {
        int cnWeek = westWeek - 1;
        if (cnWeek == 0) {
            cnWeek = 7;
        }
        return cnWeek;
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
        weekday = getCnWeek(weekday);
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

    /**
     * 计算两个时间相差的天数
     *
     * @param before
     * @param after
     * @return
     */
    public static long getTimeDifferenceByDay(Date before, Date after) {
        return (after.getTime() - before.getTime()) / DAY;
    }

    /**
     * 计算两个时间相差的秒数
     *
     * @param before
     * @param after
     * @return
     */
    public static long getTimeDifferenceBySeconds(Date before, Date after) {
        return (after.getTime() - before.getTime()) / SECONDS;
    }

    /**
     * 计算两个时间相差的分钟数
     *
     * @param before
     * @param after
     * @return
     */
    public static long getTimeDifferenceByMinutes(Date before, Date after) {
        return (after.getTime() - before.getTime()) / MINUTES;
    }

    /**
     * 计算两个时间相差的小时数
     *
     * @param before
     * @param after
     * @return
     */
    public static long getTimeDifferenceByHour(Date before, Date after) {
        return (after.getTime() - before.getTime()) / HOUR;
    }

    /**
     * 计算两个时间相差的周数
     *
     * @param before
     * @param after
     * @return
     */
    public static long getTimeDifferenceByWeek(Date before, Date after) {
        return (after.getTime() - before.getTime()) / WEEK;
    }

    /**
     * 计算两个时间相差的时间
     *
     * @param before
     * @param after
     * @param length 最大为4 最小为1 不足4 忽略前面的 4 - length
     * @return 数组 周 天 时 分 秒
     */
    public static long[] getTimeDifferenceDetail(Date before, Date after, int length) {
        long[] longs = new long[length];
        int index = 0;
        long difference = after.getTime() - before.getTime();

        switch (length) {
            case 5:
                longs[index++] = difference / WEEK;
                difference = difference % WEEK;
            case 4:
                longs[index++] = difference / DAY;
                difference = difference % DAY;
            case 3:
                longs[index++] = difference / HOUR;
                difference = difference % HOUR;
            case 2:
                longs[index++] = difference / MINUTES;
                difference = difference % MINUTES;
            case 1:
                longs[index] = difference / SECONDS;
                break;
            default:
                throw new IllegalArgumentException("length [1,5]");
        }
        return longs;
    }
}
