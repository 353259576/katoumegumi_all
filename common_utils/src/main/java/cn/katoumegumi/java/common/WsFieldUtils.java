package cn.katoumegumi.java.common;

import java.lang.invoke.SerializedLambda;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WsFieldUtils {

    private static final String METHOD_NAME_GET = "get";

    private static final String METHOD_NAME_SET = "set";

    private static final String METHOD_NAME_IS = "is";

    private static final Map<String, WeakReference<String>> FIELD_NAME_MAP = new ConcurrentHashMap<>();

    public static Field getFieldForObject(String name, Object object) {
        Class<?> clazz = object.getClass();
        return getFieldForClass(name, clazz);
    }


    public static Field getFieldForClass(String name, Class clazz) {
        Field field = null;

        for (; !(clazz == Object.class || clazz == null); clazz = clazz.getSuperclass()) {
            try {
                field = clazz.getDeclaredField(name);
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }

        return field;
    }

    public static Field[] getFieldAll(Class<?> clazz) {
        Set<Field> fieldSet = new HashSet<>();
        Map<String, Field> fieldMap = new HashMap<>();
        try {
            Field[] fields;

            fields = clazz.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                if (!Modifier.isStatic(fields[i].getModifiers())) {
                    fieldMap.put(fields[i].getName(), fields[i]);
                }

            }
            for (; !(clazz == Object.class || clazz == null); clazz = clazz.getSuperclass()) {
                fields = clazz.getDeclaredFields();
                if (!(fields.length == 0)) {
                    for (int i = 0; i < fields.length; i++) {
                        if (!Modifier.isStatic(fields[i].getModifiers())) {
                            if (!fieldMap.containsKey(fields[i].getName())) {
                                fieldMap.put(fields[i].getName(), fields[i]);
                            }
                        }

                    }
                }
            }
            //fieldSet = fieldSet.stream().filter(field -> (!Modifier.isStatic(field.getModifiers()))).collect(Collectors.toSet());
            fieldMap.forEach((s, field) -> {
                fieldSet.add(field);
            });
            fieldMap.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (fieldSet.isEmpty()) {
            return null;
        } else {
            for(Field field:fieldSet){
                makeAccessible(field);
            }
            return fieldSet.toArray(new Field[0]);
        }
    }


    public static Method[] getObjectMethodByName(String methodName, Class<?> clazz) {
        Method[] methods = null;
        Set<Method> methodSet = new HashSet<>();
        methods = clazz.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            methodSet.add(methods[i]);
        }
        methods = clazz.getMethods();
        for (int i = 0; i < methods.length; i++) {
            methodSet.add(methods[i]);
        }
        List<Method> methodList = new ArrayList<>();
        methodSet.parallelStream().forEach(method -> {
            if (method.getName().equals(methodName)) {
                methodList.add(method);
            }
        });
        return methodList.toArray(new Method[0]);

    }

    public static boolean classCompare(Class<?> child, Class<?> parent) {
        if (parent == null || child == null) {
            return false;
        }
        if (parent == Object.class) {
            return true;
        }
        if (child == Object.class) {
            return false;
        }

        if (parent == child) {
            return true;
        }

        if (parent.isInterface()) {
            if (child.isInterface()) {
                Class<?>[] cs = child.getInterfaces();
                if (WsListUtils.isEmpty(cs)) {
                    return false;
                }
                for (Class<?> clazz : cs) {
                    return classCompare(clazz, parent);
                }
            } else {
                Class<?> parentClazz = child.getSuperclass();
                if (classCompare(parentClazz, parent)) {
                    return true;
                } else {
                    Class<?>[] cs = child.getInterfaces();
                    if (WsListUtils.isEmpty(cs)) {
                        return false;
                    }
                    for (Class<?> clazz : cs) {
                        return classCompare(clazz, parent);
                    }
                }
            }

        } else {
            if (child.isInterface()) {
                return false;
            } else {
                Class parentClazz = child.getSuperclass();
                return classCompare(parentClazz, parent);
            }
        }
        return false;
    }


    public static <T> String getFieldName(SupplierFunc<T> supplierFunc) {
        String n = supplierFunc.getClass().getName();
        return Optional.ofNullable(FIELD_NAME_MAP.get(n)).map(WeakReference::get).orElseGet(() -> {
            try {
                Method method = supplierFunc.getClass().getDeclaredMethod("writeReplace");
                method.setAccessible(true);
                SerializedLambda serializedLambda = (SerializedLambda) method.invoke(supplierFunc);
                String name = serializedLambda.getImplMethodName();
                String value = methodToFieldName(name);
                FIELD_NAME_MAP.put(n, new WeakReference<>(value));
                return value;
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
                return null;
            }
        });


    }

    public static <T> String getFieldName(SFunction<T, ?> sFunction) {

        String n = sFunction.getClass().getName();

        return Optional.ofNullable(FIELD_NAME_MAP.get(n)).map(WeakReference::get).orElseGet(() -> {
            try {
                Method method = sFunction.getClass().getDeclaredMethod("writeReplace");
                method.setAccessible(true);
                SerializedLambda serializedLambda = (SerializedLambda) method.invoke(sFunction);
                String name = serializedLambda.getImplMethodName();
                String value = methodToFieldName(name);
                FIELD_NAME_MAP.put(n, new WeakReference<>(value));
                return value;
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
                return null;
            }
        });


    }


    public static String methodToFieldName(String methodName) {
        if (methodName.startsWith(METHOD_NAME_GET)) {
            return WsStringUtils.firstCharToLowerCase(methodName.substring(3));
        } else if (methodName.startsWith(METHOD_NAME_IS)) {
            return WsStringUtils.firstCharToLowerCase(methodName.substring(2));
        } else {
            return methodName;
        }
    }


    public static <T> Field getFieldByName(Class<T> tClass, String fieldName) {

        try {

            List<String> stringList = WsStringUtils.split(fieldName, '.');
            Iterator<String> iterator = stringList.iterator();
            String nowName;
            Class<?> nowC = tClass;
            Field nowField = null;
            while (iterator.hasNext()) {
                nowName = iterator.next();
                nowField = nowC.getDeclaredField(nowName);
                nowC = nowField.getType();
            }
            return nowField;

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;

    }


    public static Field getFieldByType(Type type, Class clazz) {
        Field[] fields = getFieldAll(clazz);
        for (Field field : fields) {
            if (field.getType().equals(type)) {
                return field;
            }
        }
        return null;
    }


    public static <T> Class<?> getClassTypeof(Field field) {
        Class<?> tClass = field.getType();
        if (tClass.isArray() || WsFieldUtils.classCompare(tClass, Collection.class)) {
            String listClassName = field.getGenericType().getTypeName();
            int start = listClassName.indexOf("<") + 1;
            if (start == 0) {
                return null;
            }
            int end = listClassName.lastIndexOf(">");
            if (end == -1) {
                return null;
            }
            String className = listClassName.substring(start, end);
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return tClass;
        }

    }

    public static boolean isArrayType(Field field) {
        Class<?> tClass = field.getType();
        return (tClass.isArray() || WsFieldUtils.classCompare(tClass, Collection.class));

    }

    /**
     * 获得值
     * @param o
     * @param field
     * @return
     */
    public static Object getValue(Object o, Field field) {
        try {
            field.setAccessible(true);
            return field.get(o);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return getLastValue(o,field);
        }
    }

    private static Object getLastValue(Object o, Field field) {
        try {
            makeAccessible(field);
            return field.get(o);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 设置值
     * @param o
     * @param value
     * @param field
     * @return
     */
    public static boolean setValue(Object o, Object value, Field field) {
        if (value == null) {
            return true;
        }
        try {
            //field.setAccessible(true);
            field.set(o, value);
            return true;
        } catch (IllegalAccessException e) {
            //e.printStackTrace();
            return setLastValue(o,value,field);
        }
    }

    private static boolean setLastValue(Object o, Object value, Field field) {
        if (value == null) {
            return true;
        }
        try {
            makeAccessible(field);
            field.set(o, value);
            return true;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void makeAccessible(Field field) {
        if ((!Modifier.isPublic(field.getModifiers()) ||
                !Modifier.isPublic(field.getDeclaringClass().getModifiers()) ||
                Modifier.isFinal(field.getModifiers())) && !field.isAccessible()) {
            field.setAccessible(true);
        }
    }

}





