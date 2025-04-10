package cn.katoumegumi.java.sql.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class FieldTest {
    private static final Log log = LogFactory.getLog(FieldTest.class);
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
