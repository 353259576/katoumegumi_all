package cn.katoumegumi.java.common;


import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class WsStringUtils {
    private static final byte SPACE = 32;

    /**
     * 空白字符串
     */
    private static final String BLANK_STRING = "";

    private static final String[] CN_NUMBER_NAME = new String[]{"零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"};

    private static final String[] CN_DECIMALISM_NAME = new String[]{"", "拾", "佰", "仟", "万", "拾", "佰", "仟", "亿"};

    // 使用 Unicode 块来定义汉字字符范围，包括 CJK 统一表意文字和其他扩展区
    private static final Pattern CHINESE_CHAR_PATTERN = Pattern.compile("\\p{IsHan}");

    private static final Set<Character.UnicodeBlock> CJK_UNICODE_BLOCK_SET = new HashSet<>();

    /**
     * 格式化文本用
     */
    private static final char DELIM_START = '{';
    private static final char DELIM_STOP = '}';
    private static final char ESCAPE_CHAR = '\\';


    static {
        addCJKUniCodeBlock(Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS);
        addCJKUniCodeBlock(Character.UnicodeBlock.CJK_COMPATIBILITY);
        addCJKUniCodeBlock(Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS);
        addCJKUniCodeBlock(Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT);
        addCJKUniCodeBlock(Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A);
        addCJKUniCodeBlock(Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B);
        addCJKUniCodeBlock(Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_C);
        addCJKUniCodeBlock(Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_D);
    }

    public static void main(String[] args) {
        System.out.println("你好世界".indexOf("你"));
        System.out.println(
                format("你好，世界 {{你好}}，\\\\{{{我好}，{大家好",s->{
                    return s;
                })
        );
        System.out.println(getIdentical("你好世界","你好世界"));
        System.out.println(getIdentical("你好2世界","你好1世界"));
        System.out.println(getDisparate("你好2世界","你好1世界"));
        List<String> list = split("你好,世界,你好,世界,世界,你好",',');
        for (String s : list) {
            System.out.println(s);
        }

    }

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
        if (WsCollectionUtils.isEmpty(strings)) {
            return "";
        }
        if (sign == null){
            sign = "";
        }
        start = Math.max(start, 0);
        end = Math.min(end, strings.size());
        if (end <= start){
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        String s;
        for (int i = start; i < end; i++) {
            s = strings.get(i);
            if (s == null) {
                continue;
            }
            if (stringBuilder.length() != 0){
                stringBuilder.append(sign);
            }
            stringBuilder.append(s);
        }
        return stringBuilder.toString();
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

    /**
     * 判断字符串是否都是数字
     *
     * @param str
     * @return
     */
    public static boolean isNumber(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        char[] chars = str.toCharArray();
        int i = 0;
        if (chars[0] == '-' || chars[0] == '+') {
            i++;
        }
        boolean existNumber = false;
        boolean existPoint = false;
        for (; i < chars.length; i++) {
            char c = chars[i];
            if (isNumber(c)){
                existNumber = true;
            }else if (c == '.'){
                if (!existNumber || existPoint){
                    return false;
                }
                existPoint = true;
            }else {
                return false;
            }
        }
        return existNumber;
    }

    public static boolean isNumber(char c) {
        return Character.isDigit(c);
        //return c >= 48 && c <= 58;
    }

    /**
     * 是否是大写字母
     *
     * @param c
     * @return
     */
    public static boolean isMajuscule(char c) {
        return Character.isUpperCase(c);
    }

    /**
     * 是否是小写字母
     *
     * @param c
     * @return
     */
    public static boolean isMinuscule(char c) {
        return Character.isLowerCase(c);
        //return c >= 'a' && c <= 'z';
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
        return str.indexOf(SPACE) > 0;
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
        if (size < 1){
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder(size);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int i;
        String s;
        int replenish;
        while (size > 0){
            size -= 9;
            if (size > 0){
                i = random.nextInt(999999999);
                s = Integer.toString(i);
                replenish = 9 - s.length();
            }else {
                i = random.nextInt((int) Math.pow(10, 9 + size));
                s = Integer.toString(i);
                replenish = 9 + size - s.length();
            }
            while (replenish > 0) {
                stringBuilder.append('0');
                replenish--;
            }
            stringBuilder.append(s);
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
        if (WsStringUtils.isEmpty(str)){
            return "";
        }
        char[] chars = str.toCharArray();
        StringBuilder stringBuilder = new StringBuilder();
        int length = str.length();
        char c;
        for (int i = 0; i < length; i++){
            c = chars[i];
            if (Character.isUpperCase(c)){
                if (stringBuilder.length() != 0){
                    stringBuilder.append('_');
                }
                c = Character.toLowerCase(c);
            }
            stringBuilder.append(c);
        }
        return stringBuilder.length() == str.length()?str:stringBuilder.toString();
    }

    /**
     * 驼峰法则
     *
     * @param str
     * @return
     */
    public static String camelCase(String str) {
        if (WsStringUtils.isEmpty(str)){
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        char[] chars = str.toCharArray();
        boolean needUp = false;
        for (char c : chars) {
            if (c == '_') {
                needUp = true;
                continue;
            }
            if (needUp) {
                if (Character.isLowerCase(c)) {
                    c = Character.toUpperCase(c);
                }
            }else {
                if (Character.isUpperCase(c)){
                    c = Character.toLowerCase(c);
                }
            }
            stringBuilder.append(c);
            needUp = false;
        }
        return stringBuilder.toString();
    }

    /**
     * 首字母小写
     *
     * @param str
     * @return
     */
    public static String firstCharToLowerCase(String str) {
        if (WsStringUtils.isEmpty(str)) {
            return str;
        }
        char c = str.charAt(0);
        if (Character.isWhitespace(c)){
            return firstCharToLowerCase(str.trim());
        }
        if (!Character.isLetter(c) || Character.isLowerCase(c)){
            return str;
        }
        if (str.length() == 1){
            return str.toLowerCase();
        }
        return Character.toLowerCase(c) + str.substring(1);
    }

    /**
     * 拆分字符串
     *
     * @param str
     * @param c
     * @return
     */
    public static List<String> split(String str, char c) {
        if (isEmpty(str)){
            return Collections.emptyList();
        }
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
                    list.add(str.substring(startIndex,endIndex));
                }
                startIndex = i + 1;
            }

        }
        endIndex = cs.length;
        if (startIndex == endIndex) {
            list.add(BLANK_STRING);
        } else {
            list.add(str.substring(startIndex,endIndex));
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
        return Character.isLetterOrDigit(c);
        //return (48 <= c && c <= 57) || (97 <= c && c <= 122) || (65 <= c && c <= 90);
    }

    /**
     * 判断字符是不是汉字
     *
     * @param c 字符
     * @return
     */
    public static boolean isChinese(char c) {
        Character.UnicodeBlock unicodeBlock = Character.UnicodeBlock.of(c);
        return CJK_UNICODE_BLOCK_SET.contains(unicodeBlock);
    }

    public synchronized static void addCJKUniCodeBlock(Character.UnicodeBlock unicodeBlock) {
        if (unicodeBlock == null){
            return;
        }
        CJK_UNICODE_BLOCK_SET.add(unicodeBlock);
    }

    public static boolean isChinese(String s) {
        if (WsStringUtils.isEmpty(s)){
            return false;
        }
        for (int i = 0; i < s.length(); i++){
            if (!isChinese(s.charAt(i))){
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
        if (isEmpty(str)){
            return str;
        }
        StringBuilder sb = new StringBuilder();
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
        if (isEmpty(str1) || isEmpty(str2)){
            return "";
        }
        char[] s1 = str1.toCharArray();
        char[] s2 = str2.toCharArray();
        int size = Math.min(s1.length, s2.length);
        int i = 0;
        for (; i < size; i++) {
            if (s1[i] != s2[i]) {
                break;
            }
        }
        if (i == 0) {
            return "";
        }
        if (str1.length() > str2.length()){
            str1 = str2;
        }
        if (i == size){
            return str1;
        }
        return str1.substring(0, i);
    }

    /**
     * 获取不同的字符串
     *
     * @param part
     * @param intact
     * @return
     */
    public static String getDisparate(String part, String intact) {
        if (isEmpty(part) || isEmpty(intact)){
            return "";
        }
        char[] s1 = part.toCharArray();
        char[] s2 = intact.toCharArray();
        int size = Math.min(s1.length, s2.length);
        int i = 0;
        for (; i < s1.length; i++) {
            if (s1[i] != s2[i]) {
                break;
            }
        }
        if (i >= s2.length) {
            return "";
        }
        return intact.substring(i);
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

    /**
     * 格式化文本
     * 将{}包裹的文本进行转化
     * @param format
     * @param handleFunction
     * @return
     */
    public static String format(String format, Function<String,String> handleFunction) {
        char[] chars = format.toCharArray();
        StringBuilder ans = new StringBuilder();
        StringBuilder sub = null;
        boolean ignore = false;
        int count = -1;
        for (char b : chars) {
            if (ignore) {
                ignore = false;
                if (count == -1) {
                    ans.append(b);
                } else {
                    sub.append(b);
                }
                continue;
            }
            if (b == DELIM_START) {
                count = count == -1 ? 1 : count + 1;
            } else if (b == DELIM_STOP) {
                count = count == -1 ? -1 : 0;
            } else if (b == ESCAPE_CHAR) {
                ignore = true;
                continue;
            }
            if (count == 1) {
                if (sub == null) {
                    sub = new StringBuilder();
                }else {
                    sub.append(b);
                }
            } else if (count == 0) {
                String temp =handleFunction.apply(sub.toString());
                if (!WsStringUtils.isEmpty(temp)) {
                    ans.append(temp);
                }
                count = -1;
                sub = null;
            } else if (count == -1) {
                ans.append(b);
            } else {
                sub.append(b);
            }
        }
        if (sub != null) {
            ans.append(DELIM_START);
            ans.append(sub);
        }
        return ans.toString();
    }

}
