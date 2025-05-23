package cn.katoumegumi.java.common;

import cn.katoumegumi.java.common.model.BeanModel;
import cn.katoumegumi.java.common.model.BeanPropertyModel;
import cn.katoumegumi.java.common.model.GenericsTypeModel;

import java.io.ObjectStreamClass;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.SerializedLambda;
import java.lang.ref.WeakReference;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 反射工具类
 */
public class WsReflectUtils {

    private static final String METHOD_NAME_GET = "get";

    private static final String METHOD_NAME_SET = "set";

    private static final String METHOD_NAME_IS = "is";

    private static final String WRITE_REPLACE_FUNCTION_NAME = "writeReplace";

    private static final Map<String, WeakReference<String>> FIELD_NAME_CACHE = new ConcurrentHashMap<>();

    private static final Map<Class<?>, WeakReference<Map<String,Field>>> FIELD_MAP_CACHE = new ConcurrentHashMap<>();

    private static final Field[] EMPTY_FIELD_ARRAY = new Field[0];

    private static final Field WRITE_REPLACE_METHOD_FIELD = WsReflectUtils.getFieldByName(ObjectStreamClass.class, "writeReplaceMethod");

    public static Field getFieldForObject(String name, Object object) {
        Class<?> clazz = object.getClass();
        return getFieldForClass(name, clazz);
    }

    public static Field getFieldForClass(String name, Class<?> clazz) {
        Field field = null;
        for (; !(clazz == Object.class || clazz == null); clazz = clazz.getSuperclass()) {
            try {
                field = clazz.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                //e.printStackTrace();
            }
        }
        return field;
    }

    /**
     * 获取所有非静态的字段
     *
     * @param clazz
     * @return
     */
    public static Field[] getFieldAll(Class<?> clazz) {
        return getFieldList(clazz).toArray(EMPTY_FIELD_ARRAY);
    }

    public static List<Field> getFieldList(Class<?> clazz) {
        Map<String,Field> fieldMap = getFieldMap(clazz);
        List<Field> fieldList = new ArrayList<>(fieldMap.size());
        for (Field field : fieldMap.values()) {
            if (Modifier.isStatic(field.getModifiers())){
                continue;
            }
            fieldList.add(field);
        }
        return fieldList;
    }

    /**
     * 获取所有field
     * @param clazz
     * @return
     */
    public static Map<String,Field> getFieldMap(Class<?> clazz) {
        if (clazz == null) {
            return Collections.emptyMap();
        }
        WeakReference<Map<String,Field>> fieldMapCache = FIELD_MAP_CACHE.get(clazz);
        Map<String,Field> fieldNameAndFeildMap;
        if (fieldMapCache != null && (fieldNameAndFeildMap = fieldMapCache.get()) != null) {
            return fieldNameAndFeildMap;
        }
        fieldNameAndFeildMap = new LinkedHashMap<>();
        Class<?> originalClass = clazz;
        for (; !(clazz == Object.class || clazz == null); clazz = clazz.getSuperclass()) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field value : fields) {
                String name = value.getName();
                fieldNameAndFeildMap.putIfAbsent(name, value);
            }
        }
        FIELD_MAP_CACHE.put(originalClass, new WeakReference<>(fieldNameAndFeildMap));
        return fieldNameAndFeildMap;
    }


    /**
     * 通过方法名获取方法
     *
     * @param methodName
     * @param clazz
     * @return
     */
    public static Method[] getObjectMethodByName(String methodName, Class<?> clazz) {

        List<Method> methodList = new ArrayList<>();
        Set<Method> methodSet = new HashSet<>();
        for (Method declaredMethod : clazz.getDeclaredMethods()) {
            if (!methodSet.add(declaredMethod)) {
                continue;
            }
            if (declaredMethod.getName().equals(methodName)) {
                methodList.add(declaredMethod);
            }
        }
        for (Method method : clazz.getMethods()) {
            if (!methodSet.add(method)) {
                continue;
            }
            if (method.getName().equals(methodName)) {
                methodList.add(method);
            }
        }
        return methodList.toArray(new Method[0]);
    }

    /**
     * 判断是否为基类的子类
     *
     * @param child
     * @param parent
     * @return
     */
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
                if (WsCollectionUtils.isEmpty(cs)) {
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
                    if (WsCollectionUtils.isEmpty(cs)) {
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
                Class<?> parentClazz = child.getSuperclass();
                return classCompare(parentClazz, parent);
            }
        }
        return false;
    }


    /**
     * 获取字段名称
     *
     * @param supplierFunc
     * @param <T>
     * @return
     */
    public static <T> String getFieldName(SupplierFunc<T> supplierFunc) {
        String n = supplierFunc.getClass().getName();
        return Optional.ofNullable(FIELD_NAME_CACHE.get(n)).map(WeakReference::get).orElseGet(() -> {
            try {
                Method method = supplierFunc.getClass().getDeclaredMethod(WRITE_REPLACE_FUNCTION_NAME);
                method.setAccessible(true);
                SerializedLambda serializedLambda = (SerializedLambda) method.invoke(supplierFunc);
                String name = serializedLambda.getImplMethodName();
                String value = methodToFieldName(name);
                FIELD_NAME_CACHE.put(n, new WeakReference<>(value));
                return value;
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    /**
     * 获取字段名称
     *
     * @param sFunction
     * @param <T>
     * @return
     */
    public static <T> String getFieldName(SFunction<T, ?> sFunction) {
        String n = sFunction.getClass().getName();
        return Optional.ofNullable(FIELD_NAME_CACHE.get(n)).map(WeakReference::get).orElseGet(() -> {
            SerializedLambda serializedLambda = getSerializedLambda(sFunction);
            String name = serializedLambda.getImplMethodName();
            String value = methodToFieldName(name);
            FIELD_NAME_CACHE.put(n, new WeakReference<>(value));
            return value;
        });
    }

    private static SerializedLambda getSerializedLambda(Object o) {
        SerializedLambda serializedLambda;
        if ((serializedLambda = getSerializedLambdaByReflect(o)) == null) {
            serializedLambda = getSerializedLambdaByObjectStreamClass(o);
        }
        if (serializedLambda == null) {
            throw new IllegalArgumentException("获取SerializedLambda失败");
        }
        return serializedLambda;
    }

    private static SerializedLambda getSerializedLambdaByObjectStreamClass(Object o) {
        try {
            Class<?> cl = o.getClass();
            ObjectStreamClass desc;
            Object obj = o;
            for (; ; ) {
                Class<?> repCl;
                desc = ObjectStreamClass.lookup(cl);
                Method method = (Method) WsReflectUtils.getValue(desc, WRITE_REPLACE_METHOD_FIELD);
                if (method == null ||
                        (obj = method.invoke(obj, (Object[]) null)) == null ||
                        obj instanceof SerializedLambda ||
                        (repCl = obj.getClass()) == cl) {
                    break;
                }
                cl = repCl;
            }
            return obj instanceof SerializedLambda ? (SerializedLambda) obj : null;
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static SerializedLambda getSerializedLambdaByReflect(Object o) {
        try {
            Method method = o.getClass().getDeclaredMethod(WRITE_REPLACE_FUNCTION_NAME);
            method.setAccessible(true);
            return (SerializedLambda) method.invoke(o);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 去除方法名的get set is
     *
     * @param methodName
     * @return
     */
    public static String methodToFieldName(String methodName) {
        if (methodName.startsWith(METHOD_NAME_GET) || methodName.startsWith(METHOD_NAME_SET)) {
            return WsStringUtils.firstCharToLowerCase(methodName.substring(3));
        } else if (methodName.startsWith(METHOD_NAME_IS)) {
            return WsStringUtils.firstCharToLowerCase(methodName.substring(2));
        } else {
            return methodName;
        }
    }

    /**
     * 通过字段名获取字段
     *
     * @param tClass
     * @param fieldName
     * @param <T>
     * @return
     */
    public static <T> Field getFieldByName(Class<T> tClass, String fieldName) {
        List<String> stringList = WsStringUtils.split(fieldName, '.');
        Class<?> currentClass = tClass;
        Field field = null;
        for (String name : stringList) {
            field = getFieldForClass(name, currentClass);
            assert field != null;
            currentClass = field.getType();
        }
        return field;
    }


    public static List<Field> getFieldByType(Type type, Class<?> clazz) {
        List<Field> fieldList = new ArrayList<>();
        Field[] fields = getFieldAll(clazz);
        for (Field field : fields) {
            if (field.getType().equals(type)) {
                //return field;
                fieldList.add(field);
            }
        }
        return fieldList;
    }

    /**
     * 获取泛型
     *
     * @param field
     * @param <T>
     * @return
     */
    public static <T> Class<?> getClassTypeof(Field field) {
        Class<?> tClass = field.getType();
        if (tClass.isArray() || WsReflectUtils.classCompare(tClass, Collection.class)) {
            return getGenericsType(field.getGenericType());
        } else {
            return tClass;
        }
    }

    public static Class<?> getGenericsType(Type type){
        if (type == null){
            return null;
        }
        GenericsTypeModel genericsTypeModel = getGenericsType(type.getTypeName());
        if (genericsTypeModel.getGenericsTypeModelList() == null || genericsTypeModel.getGenericsTypeModelList().size() != 1){
            return null;
        }
        String className = genericsTypeModel.getGenericsTypeModelList().get(0).getClassName();
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static GenericsTypeModel getGenericsType(String typeName){
        return getGenericsType(typeName,new int[]{0,typeName.length()}).get(0);
    }

    private static List<GenericsTypeModel> getGenericsType(String typeName,int[] startAndEnd){
        StringBuilder sb = new StringBuilder();
        List<GenericsTypeModel> list = new ArrayList<>();
        GenericsTypeModel genericsTypeModel = new GenericsTypeModel();
        ignoreBeginBlank(startAndEnd,typeName,sb);
        int statIndex = startAndEnd[0];
        int endIndex = startAndEnd[1];
        for (; statIndex < endIndex; statIndex++){
            char c = typeName.charAt(statIndex);
            if (c == '<'){

                startAndEnd[0] = statIndex + 1;
                genericsTypeModel.setGenericsTypeModelList(getGenericsType(typeName,startAndEnd));
                statIndex = startAndEnd[0] - 1;
            }else if (c == '>'){
                statIndex++;
                break;
            }else if (c == ','){
                genericsTypeModel.setClassName(sb.toString());
                list.add(genericsTypeModel);
                sb = new StringBuilder();
                genericsTypeModel = new GenericsTypeModel();
                startAndEnd[0] = statIndex + 1;
                ignoreBeginBlank(startAndEnd,typeName,sb);
                statIndex = startAndEnd[0] - 1;
            }else {
                sb.append(c);
            }
        }
        startAndEnd[0] = statIndex;
        genericsTypeModel.setClassName(sb.toString());
        list.add(genericsTypeModel);
        return list;
    }

    private static void ignoreBeginBlank(int[] startAndEnd,String typeName,StringBuilder stringBuilder){
        int statIndex = startAndEnd[0];
        int endIndex = startAndEnd[1];
        for (;statIndex < endIndex;statIndex++){
            char c = typeName.charAt(statIndex);
            if (c == ' '){
                continue;
            }
            stringBuilder.append(c);
            statIndex++;
            break;
        }
        startAndEnd[0] = statIndex;
    }

    /**
     * 判读字段是不是数组
     *
     * @param field
     * @return
     */
    public static boolean isArrayType(Field field) {
        return isArrayType(field.getType());
    }

    public static boolean isArrayType(Class<?> tClass) {
        return (tClass.isArray() || WsReflectUtils.classCompare(tClass, Collection.class));
    }

    /**
     * 获得值
     *
     * @param o
     * @param field
     * @return
     */
    public static Object getValue(Object o, Field field) {
        long address;
        if (Modifier.isStatic(field.getModifiers())) {
            o = WsUnsafeUtils.staticFieldBase(field);
            address = WsUnsafeUtils.staticFieldOffset(field);
        } else {
            address = WsUnsafeUtils.objectFieldOffset(field);
        }
        if (field.getType().isPrimitive()) {
            Class<?> clazz = field.getType();
            if (clazz.equals(int.class)) {
                if (Modifier.isVolatile(field.getModifiers())) {
                    return WsUnsafeUtils.getIntVolatile(o, address);
                } else {
                    return WsUnsafeUtils.getInt(o, address);
                }
            } else if (clazz.equals(long.class)) {
                if (Modifier.isVolatile(field.getModifiers())) {
                    return WsUnsafeUtils.getLongVolatile(o, address);
                } else {
                    return WsUnsafeUtils.getLong(o, address);
                }
            } else if (clazz.equals(double.class)) {
                if (Modifier.isVolatile(field.getModifiers())) {
                    return WsUnsafeUtils.getDoubleVolatile(o, address);
                } else {
                    return WsUnsafeUtils.getDouble(o, address);
                }
            } else if (clazz.equals(boolean.class)) {
                if (Modifier.isVolatile(field.getModifiers())) {
                    return WsUnsafeUtils.getBooleanVolatile(o, address);
                } else {
                    return WsUnsafeUtils.getBoolean(o, address);
                }
            } else if (clazz.equals(float.class)) {
                if (Modifier.isVolatile(field.getModifiers())) {
                    return WsUnsafeUtils.getObjectVolatile(o, address);
                } else {
                    return WsUnsafeUtils.getObject(o, address);
                }
            } else if (clazz.equals(short.class)) {
                if (Modifier.isVolatile(field.getModifiers())) {
                    return WsUnsafeUtils.getShortVolatile(o, address);
                } else {
                    return WsUnsafeUtils.getShort(o, address);
                }
            } else if (clazz.equals(byte.class)) {
                if (Modifier.isVolatile(field.getModifiers())) {
                    return WsUnsafeUtils.getByteVolatile(o, address);
                } else {
                    return WsUnsafeUtils.getByte(o, address);
                }
            } else if (clazz.equals(char.class)) {
                if (Modifier.isVolatile(field.getModifiers())) {
                    return WsUnsafeUtils.getCharVolatile(o, address);
                } else {
                    return WsUnsafeUtils.getChar(o, address);
                }
            } else {
                throw new RuntimeException("不支持的类型");
            }
        } else {
            if (Modifier.isVolatile(field.getModifiers())) {
                return WsUnsafeUtils.getObjectVolatile(o, address);
            } else {
                return WsUnsafeUtils.getObject(o, address);
            }
        }

    }

    /**
     * 设置值
     *
     * @param o
     * @param value
     * @param field
     * @return
     */
    public static boolean setValue(Object o, Object value, Field field) {
        if (o == null && !Modifier.isStatic(field.getModifiers())) {
            return false;
        }
        long address;
        if (Modifier.isStatic(field.getModifiers())) {
            o = WsUnsafeUtils.staticFieldBase(field);
            address = WsUnsafeUtils.staticFieldOffset(field);
        } else {
            address = WsUnsafeUtils.objectFieldOffset(field);
        }
        if (Modifier.isVolatile(field.getModifiers())) {
            WsUnsafeUtils.putObjectVolatile(o, address, value);
        } else {
            WsUnsafeUtils.putObject(o, address, value);
        }
        return true;


    }

    public static boolean setValue(Object o, int value, Field field) {
        long address;
        if (Modifier.isStatic(field.getModifiers())) {
            o = WsUnsafeUtils.staticFieldBase(field);
            address = WsUnsafeUtils.staticFieldOffset(field);
        } else {
            address = WsUnsafeUtils.objectFieldOffset(field);
        }
        if (Modifier.isVolatile(field.getModifiers())) {
            WsUnsafeUtils.putIntVolatile(o, address, value);
        } else {
            WsUnsafeUtils.putInt(o, address, value);
        }
        return true;
    }

    public static boolean setValue(Object o, boolean value, Field field) {
        long address;
        if (Modifier.isStatic(field.getModifiers())) {
            o = WsUnsafeUtils.staticFieldBase(field);
            address = WsUnsafeUtils.staticFieldOffset(field);
        } else {
            address = WsUnsafeUtils.objectFieldOffset(field);
        }
        if (Modifier.isVolatile(field.getModifiers())) {
            WsUnsafeUtils.putBooleanVolatile(o, address, value);
        } else {
            WsUnsafeUtils.putBoolean(o, address, value);
        }
        return true;
    }

    public static boolean setValue(Object o, char value, Field field) {
        long address;
        if (Modifier.isStatic(field.getModifiers())) {
            o = WsUnsafeUtils.staticFieldBase(field);
            address = WsUnsafeUtils.staticFieldOffset(field);
        } else {
            address = WsUnsafeUtils.objectFieldOffset(field);
        }
        if (Modifier.isVolatile(field.getModifiers())) {
            WsUnsafeUtils.putCharVolatile(o, address, value);
        } else {
            WsUnsafeUtils.putChar(o, address, value);
        }
        return true;
    }

    public static boolean setValue(Object o, byte value, Field field) {
        long address;
        if (Modifier.isStatic(field.getModifiers())) {
            o = WsUnsafeUtils.staticFieldBase(field);
            address = WsUnsafeUtils.staticFieldOffset(field);
        } else {
            address = WsUnsafeUtils.objectFieldOffset(field);
        }
        if (Modifier.isVolatile(field.getModifiers())) {
            WsUnsafeUtils.putByteVolatile(o, address, value);
        } else {
            WsUnsafeUtils.putByte(o, address, value);
        }
        return true;
    }

    public static boolean setValue(Object o, short value, Field field) {
        long address;
        if (Modifier.isStatic(field.getModifiers())) {
            o = WsUnsafeUtils.staticFieldBase(field);
            address = WsUnsafeUtils.staticFieldOffset(field);
        } else {
            address = WsUnsafeUtils.objectFieldOffset(field);
        }
        if (Modifier.isVolatile(field.getModifiers())) {
            WsUnsafeUtils.putShortVolatile(o, address, value);
        } else {
            WsUnsafeUtils.putShort(o, address, value);
        }
        return true;
    }

    public static boolean setValue(Object o, long value, Field field) {
        long address;
        if (Modifier.isStatic(field.getModifiers())) {
            o = WsUnsafeUtils.staticFieldBase(field);
            address = WsUnsafeUtils.staticFieldOffset(field);
        } else {
            address = WsUnsafeUtils.objectFieldOffset(field);
        }
        if (Modifier.isVolatile(field.getModifiers())) {
            WsUnsafeUtils.putLongVolatile(o, address, value);
        } else {
            WsUnsafeUtils.putLong(o, address, value);
        }
        return true;
    }

    public static boolean setValue(Object o, float value, Field field) {
        long address;
        if (Modifier.isStatic(field.getModifiers())) {
            o = WsUnsafeUtils.staticFieldBase(field);
            address = WsUnsafeUtils.staticFieldOffset(field);
        } else {
            address = WsUnsafeUtils.objectFieldOffset(field);
        }
        if (Modifier.isVolatile(field.getModifiers())) {
            WsUnsafeUtils.putFloatVolatile(o, address, value);
        } else {
            WsUnsafeUtils.putFloat(o, address, value);
        }
        return true;
    }

    public static boolean setValue(Object o, double value, Field field) {
        long address;
        if (Modifier.isStatic(field.getModifiers())) {
            o = WsUnsafeUtils.staticFieldBase(field);
            address = WsUnsafeUtils.staticFieldOffset(field);
        } else {
            address = WsUnsafeUtils.objectFieldOffset(field);
        }
        if (Modifier.isVolatile(field.getModifiers())) {
            WsUnsafeUtils.putDoubleVolatile(o, address, value);
        } else {
            WsUnsafeUtils.putDouble(o, address, value);
        }
        return true;
    }

    @SuppressWarnings("deprecation")
    public static void makeAccessible(Field field) {
        if ((!Modifier.isPublic(field.getModifiers()) ||
                !Modifier.isPublic(field.getDeclaringClass().getModifiers()) ||
                Modifier.isFinal(field.getModifiers())) && !field.isAccessible()) {
            ((AccessibleObject) field).setAccessible(true);
        }
    }

    /**
     * 创建javaBean模型
     * @param bClass
     * @return
     */
    public static BeanModel createBeanModel(Class<?> bClass){

        Map<String,Field> fieldMap = WsReflectUtils.getFieldMap(bClass);

        Method[] methods = bClass.getMethods();
        Map<String, Object[]> beanPropertyMap = new LinkedHashMap<>();

        for (Map.Entry<String, Field> stringFieldEntry : fieldMap.entrySet()) {
            if (Modifier.isStatic(stringFieldEntry.getValue().getModifiers())){
                continue;
            }
            Object[] objects = new Object[3];
            objects[0] = stringFieldEntry.getValue();
            beanPropertyMap.put(stringFieldEntry.getKey(),objects);
        }

        for (Method method : methods) {
            if (Modifier.isStatic(method.getModifiers())){
                continue;
            }
            String name = method.getName();
            if (name.startsWith(METHOD_NAME_SET)){
                if (name.length() == 3 || method.getParameterCount() != 1){
                    continue;
                }
                name = WsStringUtils.firstCharToLowerCase(name.substring(3));
                Object[] objects = beanPropertyMap.computeIfAbsent(name, n->new Object[3]);
                objects[2] = method;
            }else if (name.startsWith(METHOD_NAME_GET)){
                if (name.length() == 3 || method.getParameterCount() != 0){
                    continue;
                }
                name = WsStringUtils.firstCharToLowerCase(name.substring(3));
                Object[] objects = beanPropertyMap.computeIfAbsent(name, n->new Object[3]);
                objects[1] = method;
            }else if (name.startsWith(METHOD_NAME_IS)){
                if (name.length() == 2 || method.getParameterCount() != 0){
                    continue;
                }
                name = WsStringUtils.firstCharToLowerCase(name.substring(2));
                Object[] objects = beanPropertyMap.computeIfAbsent(name, n->new Object[3]);
                objects[1] = method;
            }
        }

        MethodHandles.Lookup lookup = MethodHandles.lookup();
        BeanModel beanModel = new BeanModel(bClass);
        Map<String, BeanPropertyModel> beanPropertyModelMap = beanModel.getPropertyModelMap();
        for (Map.Entry<String, Object[]> stringEntry : beanPropertyMap.entrySet()) {
            Object[] objects = stringEntry.getValue();
            if (objects[1] == null || (objects[0] == null && objects[2] == null)){
                continue;
            }
            try {
                Method getMethod = (Method) objects[1];
                MethodHandle getMethodHandle = lookup.unreflect(getMethod);
                Method setMethod = objects[2] == null ? null :  (Method) objects[2];
                MethodHandle setMethodHandle = setMethod == null ? null : lookup.unreflect(setMethod);
                beanPropertyModelMap.put(stringEntry.getKey(),new BeanPropertyModel(stringEntry.getKey(),(Field) objects[0],getMethod,getMethodHandle,setMethod,setMethodHandle));
            }catch (IllegalAccessException e){
                throw new RuntimeException(e);
            }
        }
        return beanModel;
    }
}





