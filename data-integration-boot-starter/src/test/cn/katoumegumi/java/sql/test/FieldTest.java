package cn.katoumegumi.java.sql.test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.logging.Logger;

public class FieldTest {
    private static final Logger log = Logger.getLogger(FieldTest.class.getName());
    public static void main(String[] args) {
        Field[] fields = Field.class.getDeclaredFields();
        for (Field field : fields) {
            log.info(field.getName());
        }
        Method[] methods = Field.class.getDeclaredMethods();
        for (Method method : methods) {
            log.info(method.getName());
        }
    }
}
