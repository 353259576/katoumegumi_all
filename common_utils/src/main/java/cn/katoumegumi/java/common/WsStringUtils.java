package cn.katoumegumi.java.common;


import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

public class WsStringUtils {
    private static final byte SPACE = 32;

    /**
     * 空白字符串
     */
    private static final String BLANK_STRING = "";

    private static final String[] CN_NUMBER_NAME = new String[]{"零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"};

    private static final String[] CN_DECIMALISM_NAME = new String[]{"", "拾", "佰", "仟", "万", "拾", "佰", "仟", "亿"};

    public static String jointListString(String[] strings, String sign) {
        return jointListString(Arrays.asList(strings), sign);
    }

    public static <T, R> String jointListString(List<T> list, String sign, Function<T, String> function) {
        List<String> stringList = list.stream().map(function).filter(WsStringUtils::isNotBlank).collect(Collectors.toList());
        return jointListString(stringList, sign);


    }

    public static String jointListString(List<String> strings, String sign) {
        return jointListString(strings, sign, 0, strings.size());
    }

    public static String jointListString(List<String> strings, String sign, int start, int end) {
        if (strings == null) {
            return "";
        }
        if (strings.isEmpty()) {
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

    public static boolean hasLength(final String str) {
        return !notHasLength(str);
    }

    public static boolean notHasLength(final String str) {
        return str == null || str.isEmpty();
    }

    /**
     * 判断字符串是否都是数字
     *
     * @param str
     * @return
     */
    public static boolean isNumber(String str) {
        if (str == null) {
            return false;
        }
        char[] chars = str.toCharArray();
        if (str.isEmpty()) {
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

    public static boolean isNumber(char c) {
        return c >= 48 && c <= 58;
    }

    /**
     * 是否是大写字母
     *
     * @param c
     * @return
     */
    public static boolean isMajuscule(char c) {
        return c >= 'A' && c <= 'Z';
    }

    /**
     * 是否是小写字母
     *
     * @param c
     * @return
     */
    public static boolean isMinuscule(char c) {
        return c >= 'a' && c <= 'z';
    }


    /**
     * 判断字符串是否含有空格
     *
     * @param str
     * @return
     */
    public static boolean stringExistSpace(String str) {
        if (str == null) {
            return false;
        }
        byte[] bytes;
        try {
            bytes = str.getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            bytes = str.getBytes();
        }

        if (bytes.length == 0) {
            return false;
        }
        for (byte aByte : bytes) {
            if (aByte == SPACE) {
                return false;
            }
        }
        return true;

    }

    /**
     * 去掉string里的空格
     *
     * @param str
     * @return
     */
    public static String stringTrim(String str) {
        if (str == null) {
            return null;
        }
        byte[] bytes = str.getBytes();
        if (bytes.length == 0) {
            return "";
        }
        int newIndex = 0;
        for (byte aByte : bytes) {
            if (aByte != SPACE) {
                bytes[newIndex++] = aByte;
            }
        }
        if (newIndex == 0) {
            return "";
        } else if (newIndex == bytes.length) {
            return new String(bytes);
        }
        byte[] newBytes = Arrays.copyOfRange(bytes, 0, newIndex);
        return new String(newBytes);
    }


    /**
     * unicode转码
     *
     * @param dataStr
     * @return
     */
    public static String decodeUnicode(String dataStr) {

        StringBuilder buffer = new StringBuilder();
        char[] unicodeChar = new char[]{'\\', 'u', 'U'};
        char[] ch = dataStr.toCharArray();
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
                            StringBuilder str = new StringBuilder();
                            for (int j = 0; j < 4; j++) {
                                i++;
                                str.append(ch[i]);
                            }
                            // 16进制parse整形字符串。
                            char letter = (char) Integer.parseInt(str.toString(), 16);
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
                    String str;
                    StringBuilder stringBuffer = new StringBuilder();
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
                        stringBuffer = new StringBuilder();
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
                    byte[] by = new byte[strings.size()];
                    for (int j = 0; j < strings.size(); j++) {
                        by[j] = (byte) Integer.parseInt(strings.get(j), 16);
                    }
                    try {
                        buffer.append(new String(by, StandardCharsets.UTF_8));
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

    /**
     * 对象转String
     *
     * @param object
     * @return
     */
    public static String anyToString(Object object) {
        if (object == null) {
            return null;
        }
        if (object.getClass().equals(String.class)) {
            return (String) object;
        } else if (object instanceof Number || object instanceof Character || object instanceof Boolean) {
            return object.toString();
        } else if (object.getClass().isPrimitive()) {
            return String.valueOf(object);
        } else if (object instanceof Date) {
            Date date = (Date) object;
            return WsDateUtils.dateToString(date, WsDateUtils.LONGTIMESTRING);
        } else if (object instanceof LocalDateTime) {
            return WsDateUtils.objectDateFormatString(object);
        } else if (object instanceof LocalDate) {
            return WsDateUtils.objectDateFormatString(object);
        } else {
            return object.toString();
        }
    }


    /**
     * 字符串脱敏
     *
     * @param string
     * @return
     */
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
        int i = new SecureRandom().nextInt(90000) + 10000;
        return str + i;
    }

    /**
     * 创建随机字符串
     *
     * @param startStr      开始字符串
     * @param timeTemplates 日期格式
     * @param size          总大小
     * @return
     */
    public static String createRandomStr(String startStr, String timeTemplates, Integer size) {
        StringBuilder stringBuilder = new StringBuilder();
        if (isNotBlank(startStr)) {
            stringBuilder.append(startStr);
        }
        if (isNotBlank(timeTemplates)) {
            stringBuilder.append(WsDateUtils.dateToString(new Date(), timeTemplates));
        }
        int length = stringBuilder.length() - size;
        if (length > 0) {
            return stringBuilder.substring(0, size);
        } else if (length < 0) {
            length = -length;
            stringBuilder.append(createRandomNum(length));
            return stringBuilder.toString();

        } else {
            return stringBuilder.toString();
        }
    }

    /**
     * 随机数字字符串
     *
     * @param size 长度
     * @return
     */
    public static String createRandomNum(int size) {
        StringBuilder stringBuilder = new StringBuilder();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int length = size / 9;
        int endSize = size % 9;
        int i;
        String si;
        int sLength;
        int replenishSize;
        while (length > 0) {
            i = random.nextInt(999999999);
            si = Integer.toString(i);
            sLength = si.length();
            replenishSize = 9 - sLength;
            while (replenishSize > 0) {
                stringBuilder.append(0);
                replenishSize--;
            }
            stringBuilder.append(si);
            length--;
        }
        if (endSize > 0) {
            double value = Math.pow(10, endSize);
            i = random.nextInt((int) value);
            si = Integer.toString(i);
            sLength = si.length();
            replenishSize = endSize - sLength;
            while (replenishSize > 0) {
                stringBuilder.append(0);
                replenishSize--;
            }
            stringBuilder.append(si);
        }
        return stringBuilder.toString();


    }

    /**
     * 驼峰法则
     *
     * @param str
     * @return
     */
    public static String camel_case(String str) {
        char g = '_';
        char[] chars = str.toCharArray();
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
        char[] chars = str.toCharArray();
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

    /**
     * 拆分字符串
     *
     * @param str
     * @param c
     * @return
     */
    public static List<String> split(String str, char c) {
        char[] cs = str.toCharArray();
        List<String> list = new ArrayList<>();

        int startIndex = 0;
        int endIndex;

        for (int i = 0; i < cs.length; ++i) {
            if (cs[i] == c) {
                endIndex = i;
                if (startIndex == endIndex) {
                    list.add(BLANK_STRING);
                } else {
                    list.add(new String(Arrays.copyOfRange(cs, startIndex, endIndex)));
                }
                startIndex = i + 1;
            }

        }
        endIndex = cs.length;
        if (startIndex == endIndex) {
            list.add(BLANK_STRING);
        } else {
            list.add(new String(Arrays.copyOfRange(cs, startIndex, endIndex)));
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
     * 判断字符是不是汉字
     *
     * @param c 字符
     * @return
     */
    public static boolean isChinese(char c) {
        if (c >= 19968 && c <= 40869) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断字符串是不是都是汉字
     *
     * @param string
     * @return
     */
    public static boolean isChinese(String string) {
        string = new String(string.getBytes(StandardCharsets.UTF_8));
        for (char c : string.toCharArray()) {
            if (!isChinese(c)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获取字符串里所有的汉语字符
     *
     * @param str
     * @return
     */
    public static String interceptedChinese(String str) {
        StringBuilder sb = new StringBuilder();
        str = new String(str.getBytes(StandardCharsets.UTF_8));
        for (char c : str.toCharArray()) {
            if (isChinese(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }


    /**
     * 获取两个字符串相同的部分
     *
     * @param str1
     * @param str2
     * @return
     */
    public static String getIdentical(String str1, String str2) {
        char[] s1 = str1.toCharArray();
        char[] s2 = str2.toCharArray();
        int size = Math.min(s1.length, s2.length);
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < size; i++) {
            if (s1[i] == s2[i]) {
                stringBuilder.append(s1[i]);
            } else {
                break;
            }
        }
        return stringBuilder.length() == 0 ? null : stringBuilder.toString();
    }

    /**
     * 获取不同的字符串
     *
     * @param part
     * @param intact
     * @return
     */
    public static String getDisparate(String part, String intact) {
        char[] s1 = part.toCharArray();
        char[] s2 = intact.toCharArray();
        int size = Math.min(s1.length, s2.length);
        int i = 0;
        for (; i < size; i++) {
            if (s1[i] != s2[i]) {
                break;
            }
        }

        if (i >= s2.length) {
            return null;
        }
        return String.copyValueOf(s2, i, s2.length - i);
    }

    /**
     * 数字转换成中文
     *
     * @param number 整形数字
     * @return
     */
    public static String toCnNumber(BigDecimal number) {
        int signNum = number.signum();

        if (signNum < 0) {
            number = number.abs();
        }
        long num = number.longValue();
        char[] numChars = Long.valueOf(num).toString().toCharArray();
        int cLength = numChars.length;
        for (int i = 0; i < cLength / 2; i++) {
            char c = numChars[i];
            numChars[i] = numChars[cLength - i - 1];
            numChars[cLength - i - 1] = c;
        }
        List<String> list = new ArrayList<>();
        int decimalismIndex = 0;
        int cnNumberNameIndex;
        //是否需要增加w 0 不需要 1 需要 2 已增加
        int needAddW = 0;
        //是否需要添加0
        int needAddZ = 0;
        for (char numChar : numChars) {
            cnNumberNameIndex = numChar - 48;
            //当数字为0时
            if (cnNumberNameIndex == 0) {
                if (decimalismIndex == 8) {
                    //当亿单位是0时
                    list.add(CN_DECIMALISM_NAME[decimalismIndex]);
                    needAddW = 0;
                } else if (decimalismIndex == 4) {
                    needAddZ = 0;
                    needAddW = 1;
                } else {
                    if (decimalismIndex != 0 && needAddZ == 1) {
                        list.add(CN_NUMBER_NAME[cnNumberNameIndex]);
                        needAddZ = 0;
                    }
                }
            } else {
                if (needAddW == 1 && decimalismIndex != 8) {
                    list.add(CN_DECIMALISM_NAME[4]);
                    needAddW = 2;
                }
                String numCn = CN_NUMBER_NAME[cnNumberNameIndex];
                String decimalismCn = CN_DECIMALISM_NAME[decimalismIndex];
                list.add(numCn + decimalismCn);
                needAddZ = 1;

            }
            ++decimalismIndex;
            if (CN_DECIMALISM_NAME.length == decimalismIndex) {
                decimalismIndex = 1;
                needAddZ = 0;
                needAddW = 0;
            }
        }
        Collections.reverse(list);
        return String.join("", list);
    }

}
