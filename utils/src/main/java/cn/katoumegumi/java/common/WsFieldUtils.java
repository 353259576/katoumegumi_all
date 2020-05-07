package cn.katoumegumi.java.common;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class WsFieldUtils {

    private static final String METHOD_NAME_GET = "get";
    private static final String METHOD_NAME_IS = "is";


    public static Field getFieldForObject(String name, Object object) {
        Class clazz = object.getClass();
        return getFieldForClass(name, clazz);
    }


    public static Field getFieldForClass(String name, Class clazz) {
        Field field = null;

        for (; !(clazz == Object.class || clazz == null); clazz = clazz.getSuperclass()) {
            try {
                field = clazz.getDeclaredField(name);
                if (field != null) {
                    break;
                }
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }

        return field;
    }

    public static boolean setFieldValueForName(Field field, Object object, Object value) {
        field.setAccessible(true);
        try {
            field.set(object, value);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            field.setAccessible(false);
        }
        return true;
    }

    public static Object getFieldValueForName(Field field, Object object) {
        Object value = null;
        field.setAccessible(true);
        try {
            value = field.get(object);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            field.setAccessible(false);
        }
        return value;
    }

    public static Field[] getFieldAll(Class clazz) {
        Set<Field> fieldSet = new HashSet<>();
        try {
            Field fields[];
            fields = clazz.getFields();
            for (int i = 0; i < fields.length; i++) {
                fieldSet.add(fields[i]);
            }
            fields = clazz.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                fieldSet.add(fields[i]);
            }
            fieldSet = fieldSet.stream().filter(field -> (!Modifier.isStatic(field.getModifiers()))).collect(Collectors.toSet());
            /*for(;!(clazz==Object.class || clazz == null);clazz = clazz.getSuperclass()){
                fields = clazz.getDeclaredFields();
                if(!(fields == null || fields.length == 0)){
                    for(int i = 0; i < fields.length; i++){
                        fieldSet.add(fields[i]);
                    }
                }

            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (fieldSet.isEmpty()) {
            return null;
        } else {
            return fieldSet.toArray(new Field[fieldSet.size()]);
        }
    }


    public static Method[] getObjectMethodByName(String methodName, Class clazz) {
        Method methods[] = null;
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
        return methodList.toArray(new Method[methodList.size()]);

    }

    public static boolean classCompare(Class child, Class parent) {
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
                Class[] cs = child.getInterfaces();
                if (WsListUtils.isEmpty(cs)) {
                    return false;
                }
                for (Class clazz : cs) {
                    return classCompare(clazz, parent);
                }
            } else {
                Class parentClazz = child.getSuperclass();
                if (classCompare(parentClazz, parent)) {
                    return true;
                } else {
                    Class[] cs = child.getInterfaces();
                    if (WsListUtils.isEmpty(cs)) {
                        return false;
                    }
                    for (Class clazz : cs) {
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
        try {
            Method method = supplierFunc.getClass().getDeclaredMethod("writeReplace");
            method.setAccessible(true);
            SerializedLambda serializedLambda = (SerializedLambda) method.invoke(supplierFunc);
            String name = serializedLambda.getImplMethodName();
            return methodToFieldName(name);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return null;
        }

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
                if (nowField == null) {
                    nowField = tClass.getField(nowName);
                    if (nowField == null) {
                        return null;
                    }
                }
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


    public static <T> Class<?> getClassListType(Field field) {
        Class<?> tClass = field.getType();
        if (tClass.isArray() || WsFieldUtils.classCompare(tClass, Collection.class)) {
            String listClassName = field.getGenericType().getTypeName();
            String className = listClassName.substring(listClassName.indexOf("<") + 1, listClassName.lastIndexOf(">"));
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


}





