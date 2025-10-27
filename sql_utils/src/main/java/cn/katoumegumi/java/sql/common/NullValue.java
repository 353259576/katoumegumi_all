package cn.katoumegumi.java.sql.common;

/**
 * 用于标记值为null
 */
public final class NullValue {

    private static NullValue instance;

    private NullValue() {}

    public static NullValue getInstance() {
        if (instance == null) {
            synchronized (NullValue.class) {
                if (instance == null) {
                    instance = new NullValue();
                }
            }
        }
        return instance;
    }
}
