package cn.katoumegumi.java.sql.common;

/**
 * 用于标记值为null
 */
public class NullValue {

    private static final NullValue instance = new NullValue();

    private NullValue() {}

    public static NullValue getInstance() {
        return instance;
    }
}
