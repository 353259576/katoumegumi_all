package cn.katoumegumi.java.common;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class WsStringUtils {
    public static final byte TRIM = 32;


    public static void main(String[] args) {
        /*System.out.println(" ".getBytes()[0]);
        System.out.println(stringTrim("sdf dfsfhh4ghg            6545-*-/-++d"));
        System.out.println((int) '-');
        System.out.println(isNumber("-7.84785555555555877777777777777777777777777771111111111111777777777777777777777777777"));
        System.out.println(stringNotHaveTrim("fd dsf "));
        Integer list1[] = new Integer[]{1,2,3,4,5,6,7,8,9};
        Integer list2[] = new Integer[]{11,22,33,44,55,66,77,88,99};
        System.out.println(JSON.toJSONString(mergeList(list1,list2)));
        List<Byte> bytes = new ArrayList<>();
        bytes.add(Byte.valueOf("1"));
        bytes.add(Byte.valueOf("2"));
        bytes.add(Byte.valueOf("3"));
        bytes.add(Byte.valueOf("4"));
        bytes.add(Byte.valueOf("5"));
        bytes.add(Byte.valueOf("6"));
        bytes.add(Byte.valueOf("7"));
        bytes.add(Byte.valueOf("8"));
        bytes.add(Byte.valueOf("9"));
        bytes.add(Byte.valueOf("10"));
        bytes.add(Byte.valueOf("11"));
        bytes.add(Byte.valueOf("12"));
        bytes.add(Byte.valueOf("13"));
        byte by[] = byteListToArray(bytes);
        for(int i = 0; i < by.length; i++){
            System.out.print(by[i]);
        }
        System.out.println();
        System.out.println(createOrderNo());*/
        /*System.out.println(decodeUnicode("\\U83b7\\U53d6\\U652f\\U4ed8\\U6570\\U636e\\U6210\\U529f"));
        System.out.println(createRandomStr().hashCode());*/
        //System.out.println("你好世界".contains(""));

/*        Date data = stringToDate("2018/12/12 12:12:12");
        System.out.println(data);
        System.out.println(objectDateFormatString("4187848418414198"));*/
        /*try {
            System.out.println(new String(Base64.getDecoder().decode("38hK7fsURA0vt3ef-IW72hvbczDNVASfIlMkBKA6hLWxbe1Vt9ow2snACRPfgjQwGhZ_kWaCLRt0kwnP09pJQw"),"UTF-8"));
        }catch (Exception e){
            e.printStackTrace();
        }*/
        /*String str = decodeUnicode("https://www.bilibili.com/v/douga/other/?rt=V/ymTlOu4ow/y4xxNWPUZ14LyI55s2fZ984sFJL2kpo%D");//https://www.bilibili.com/v/douga/other/?rt=V/ymTlOu4ow/y4xxNWPUZ14LyI55s2fZ984sFJL2kpo%D
        System.out.println(str);*/
        /*System.out.println(decodeUnicode("\\u79DF\\u6237\\u9ED8\\u8BA4\\u89D2\\u8272"));
        System.out.println(camel_case("dDFSDFgd"));
        System.out.println(camelCase("d_d_f_s_d_fgd"));*/
    }

    public static byte[] byteListToArray(List<Byte> bytes) {
        if (bytes == null || bytes.size() == 0) {
            return null;
        }
        byte bys[] = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++) {
            bys[i] = bytes.get(i).byteValue();
        }
        return bys;
    }


    public static <T> T[] mergeList(T[] list1, T[] list2) {
        if (list1 == null || list2 == null || list1.length == 0 || list2.length == 0) {
            if (!(list1 == null || list1.length == 0)) {
                return list1;
            }
            if (!(list2 == null || list2.length == 0)) {
                return list2;
            }
        }
        T[] list = (T[]) new Object[list1.length + list2.length];
        int k = 0;
        for (int i = 0; i < list1.length; i++) {
            list[k] = list1[i];
            k++;
        }
        for (int i = 0; i < list2.length; i++) {
            list[k] = list2[i];
            k++;
        }
        return list;
    }


    public static String jointListString(String strings[], String sign) {
        return jointListString(Arrays.asList(strings), sign);
    }

    public static <T, R> String jointListString(List<T> list, String sign, Function<T, String> function) {
        List<String> stringList = list.stream().map(function).filter(WsStringUtils::isNotBlank).collect(Collectors.toList());
        return jointListString(stringList, sign);


    }

    public static String jointListString(List<String> strings, String sign){
        return jointListString(strings,sign,0,strings.size());
    }

    public static String jointListString(List<String> strings, String sign,int start,int end) {
        if (strings == null) {
            return "";
        }
        if (strings.size() == 0) {
            return "";
        }
        StringBuilder stringBuffer = new StringBuilder();
        for (int i = start; i < end; i++) {
            if (strings.get(i) == null) {
                continue;
            }
            stringBuffer.append(strings.get(i));
            stringBuffer.append(sign);
        }
        stringBuffer.delete(stringBuffer.length() - sign.length(), stringBuffer.length());
        return stringBuffer.toString();
    }


    public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    public static boolean isNotEmpty(final CharSequence cs) {
        return !isEmpty(cs);
    }


    public static boolean isBlank(final CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNotBlank(final CharSequence cs) {
        return !isBlank(cs);
    }


    public static boolean isNumber(String str) {
        if (str == null) {
            return false;
        }
        char chars[] = str.toCharArray();
        if (str.length() == 0) {
            return false;
        }
        boolean isHave = false;
        int i = 0;
        if (chars[0] == '-' || chars[0] == '+') {
            i++;
        }
        for (; i < chars.length; i++) {
            if (chars[i] < 48 || chars[i] > 58) {
                if (chars[i] == '.') {
                    if (i == 0 || i == chars.length - 1 || isHave) {
                        return false;
                    } else {
                        isHave = true;
                    }
                } else {
                    return false;
                }
            }
        }
        return true;
    }


    public static boolean stringNotHaveTrim(String str) {
        if (str == null) {
            return false;
        }
        byte bytes[] = null;
        try {
            bytes = str.getBytes("utf-8");
        } catch (Exception e) {
            bytes = str.getBytes();
        }

        if (bytes.length == 0) {
            return false;
        }
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] == TRIM) {
                return false;
            }
        }
        return true;

    }

    public static String stringTrim(String str) {
        if (str == null) {
            return str;
        }
        byte bytes[] = str.getBytes();
        if (bytes.length == 0) {
            return null;
        }
        List list = new ArrayList();
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] != TRIM) {
                list.add(bytes[i]);
            }
        }
        bytes = new byte[list.size()];
        for (int i = 0; i < list.size(); i++) {
            bytes[i] = (byte) list.get(i);
        }
        if (bytes.length == 0) {
            return null;
        } else {
            return new String(bytes);
        }
    }


    /**
     * unicode转码
     *
     * @param dataStr
     * @return
     */
    public static String decodeUnicode(String dataStr) {

        StringBuffer buffer = new StringBuffer();
        char unicodeChar[] = new char[]{'\\', 'u', 'U'};
        char ch[] = dataStr.toCharArray();
        for (int i = 0; i < ch.length; i++) {
            if (ch[i] == unicodeChar[0]) {
                if (i + 1 < ch.length) {
                    if (ch[i + 1] == unicodeChar[0]) {
                        i++;
                    }
                    if (i + 4 < ch.length) {
                        i++;
                        if (ch[i] == unicodeChar[1] || ch[i] == unicodeChar[2]) {
                            //i++;
                            String str = "";
                            for (int j = 0; j < 4; j++) {
                                i++;
                                str += ch[i];
                            }
                            // 16进制parse整形字符串。
                            char letter = (char) Integer.parseInt(str, 16);
                            buffer.append(letter);
                        }
                    }
                } else {
                    buffer.append(ch[i]);
                }
            } else if (ch[i] == '%') {
                if (i + 3 > ch.length) {
                    buffer.append(ch[i]);
                    //i++;
                } else {
                    List<String> strings = new ArrayList<>();
                    String str = "";
                    StringBuffer stringBuffer = new StringBuffer();
                    for (int j = 0; j < 2; j++) {
                        i++;
                        //str += ch[i];
                        stringBuffer.append(ch[i]);
                    }
                    str = stringBuffer.toString();
                    str = str.toUpperCase();
                    strings.add(str);
                    i++;
                    while (i + 2 < ch.length && ch[i] == '%') {
                        //str = "";
                        stringBuffer = new StringBuffer();
                        for (int j = 0; j < 2; j++) {
                            i++;
                            //str += ch[i];
                            stringBuffer.append(ch[i]);
                        }
                        str = stringBuffer.toString();
                        str = str.toUpperCase();
                        strings.add(str);
                        i++;
                    }
                    i--;
                    byte by[] = new byte[strings.size()];
                    for (int j = 0; j < strings.size(); j++) {
                        by[j] = (byte) Integer.parseInt(strings.get(j), 16);
                    }
                    try {
                        buffer.append(new String(by, "utf-8"));
                    } catch (Exception e) {
                        e.printStackTrace();
                        buffer.append(new String(by));
                    }

                }
            } else {
                buffer.append(ch[i]);
            }
        }
        return buffer.toString();
    }


    public static String anyToString(Object object) {
        if (object == null) {
            return null;
        }
        if (object.getClass().isPrimitive()) {
            return String.valueOf(object);
        } else if (object instanceof Number) {
            return object.toString();
        } else if (object instanceof String) {
            return (String) object;
        } else if (object instanceof Character) {
            return object.toString();
        } else if (object instanceof Boolean) {
            return object.toString();
        } else if (object instanceof Date) {
            Date date = (Date) object;
            return WsDateUtils.dateToString(date, WsDateUtils.LONGTIMESTRING);
        } else if (object instanceof LocalDateTime) {
            return WsDateUtils.objectDateFormatString(object);
        } else if (object instanceof LocalDate) {
            return WsDateUtils.objectDateFormatString(object);
        } else {
            return null;
        }
    }


    public static String dateToDate(String date) {
        char chars[] = date.toCharArray();
        List<String> strings = new ArrayList<>();
        for (int i = 0; i < chars.length; i++) {
            StringBuffer stringBuffer = new StringBuffer();
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
        return null;
    }

    public static String hideString(String string) {
        if (isBlank(string)) {
            return "**";
        }
        if (string.length() == 1) {
            return string + "**" + string;
        }
        return string.charAt(0) + "**" + string.charAt(string.length() - 1);
    }

    public static String createRandomStr() {
        String str = WsDateUtils.dateToString(new Date(), WsDateUtils.LONGTIMESTRINGCOMPACT);
        int i = new Random().nextInt(90000) + 10000;
        return str + i;
    }

    public static String createRandomStr(String type, Integer size) {
        String str = WsDateUtils.dateToString(new Date(), WsDateUtils.LONGTIMESTRINGCOMPACT);
        int i = new Random().nextInt(size * 1000 * 9) + 100 * size;
        return str + i;
    }

    /**
     * 驼峰法则
     *
     * @param str
     * @return
     */
    public static String camel_case(String str) {
        char g = '_';
        char chars[] = str.toCharArray();
        StringBuilder stringBuffer = new StringBuilder();
        int length = str.length();
        char c = chars[0];

        if (c >= 'A' && c <= 'Z') {
            c += 32;
        }
        stringBuffer.append(c);
        for (int i = 1; i < length; i++) {
            c = chars[i];
            if (c >= 'A' && c <= 'Z') {
                c += 32;
                stringBuffer.append(g);
            }

            stringBuffer.append(c);
        }
        return stringBuffer.toString();
    }

    /**
     * 驼峰法则
     *
     * @param str
     * @return
     */
    public static String camelCase(String str) {
        if (!str.contains("_")) {
            return str;
        }
        char g = '_';
        str = str.toLowerCase();
        char chars[] = str.toCharArray();
        StringBuilder stringBuffer = new StringBuilder();
        int length = str.length();
        char c = chars[0];
        boolean nextToUp = false;
        stringBuffer.append(c);
        for (int i = 1; i < length; i++) {
            c = chars[i];
            if (c == g) {
                nextToUp = true;
            } else {
                if (nextToUp) {
                    c -= 32;
                    nextToUp = false;
                }
                stringBuffer.append(c);
            }
        }
        return stringBuffer.toString();
    }

    /**
     * 首字母小写
     *
     * @param str
     * @return
     */
    public static String firstCharToLowerCase(String str) {
        if (WsStringUtils.isBlank(str)) {
            return null;
        }
        int length = str.length();
        if (length == 1) {
            return str.toLowerCase();
        }
        return str.substring(0, 1).toLowerCase() + str.substring(1, length);
    }


    public static List<String> split(String str, char c) {
        char[] cs = str.toCharArray();
        List<String> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cs.length; i++) {
            if (cs[i] != c) {
                sb.append(cs[i]);
            } else {
                list.add(sb.toString());
                sb = new StringBuilder();
            }
        }
        if (sb.length() > 0) {
            list.add(sb.toString());
        }
        return list;
    }

    public static String[] splitArray(String str, char c) {
        List<String> list = WsStringUtils.split(str, c);
        return list.toArray(new String[0]);
    }

    /**
     * 判断是不是字母数字
     *
     * @param c 字符
     * @return
     */
    public static boolean isAlphabetOrNumber(char c) {
        return (48 <= c && c <= 57) || (97 <= c && c <= 122) || (65 <= c && c <= 90);
    }

    /**
     * 判断是不是汉字
     *
     * @param c 字符
     * @return
     */
    public boolean isChinese(char c) {
        if (c >= 19968 && c <= 40869) {
            return true;
        } else {
            return false;
        }
    }


}
