package cn.katoumegumi.java.common;

import java.util.HashSet;
import java.util.Set;

/**
 * @author ws
 */
public class BaseTypeCommon {


    public static void main(String[] args) {
        System.out.println(Integer.class.getGenericInterfaces());
    }

    public static final Set<Class> CLASS_SET = new HashSet<>(18);

    public static final Class BYTE_CLASS = byte.class;

    public static final Class W_BYTE_CLASS = Byte.class;

    public static final Class CHAR_CLASS = char.class;

    public static final Class W_CHAR_CLASS = Character.class;

    public static final Class BOOLEAN_CLASS = boolean.class;

    public static final Class W_BOOLEAN_CLASS = Boolean.class;

    public static final Class SHORT_CLASS = short.class;

    public static final Class W_SHORT_CLASS = Short.class;

    public static final Class INT_CALSS = int.class;

    public static final Class W_INT_CLASS = Integer.class;

    public static final Class FLOAT_CLASS = float.class;

    public static final Class W_FLOAT_CLASS = Float.class;

    public static final Class LONG_CLASS = long.class;

    public static final Class W_LONG_CLASS = Long.class;

    public static final Class DOUBLE_CLASS = double.class;

    public static final Class W_DOUBLE_CLASS = Double.class;

    public static final Class STRING_CLASS = String.class;


    static {
        CLASS_SET.add(BYTE_CLASS);
        CLASS_SET.add(W_BYTE_CLASS);
        CLASS_SET.add(CHAR_CLASS);
        CLASS_SET.add(W_CHAR_CLASS);
        CLASS_SET.add(BOOLEAN_CLASS);
        CLASS_SET.add(W_BOOLEAN_CLASS);
        CLASS_SET.add(SHORT_CLASS);
        CLASS_SET.add(W_SHORT_CLASS);
        CLASS_SET.add(FLOAT_CLASS);
        CLASS_SET.add(W_FLOAT_CLASS);
        CLASS_SET.add(INT_CALSS);
        CLASS_SET.add(W_INT_CLASS);
        CLASS_SET.add(DOUBLE_CLASS);
        CLASS_SET.add(W_DOUBLE_CLASS);
        CLASS_SET.add(STRING_CLASS);
        CLASS_SET.add(LONG_CLASS);
        CLASS_SET.add(W_LONG_CLASS);
    }



}
