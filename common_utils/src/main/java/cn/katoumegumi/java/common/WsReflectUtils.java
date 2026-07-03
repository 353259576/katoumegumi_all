package cn.katoumegumi.java.common;

import cn.katoumegumi.java.common.cache.SoftReferenceCache;
import cn.katoumegumi.java.common.model.BeanModel;
import cn.katoumegumi.java.common.model.BeanPropertyModel;
import cn.katoumegumi.java.common.model.GenericsType;

import java.io.ObjectStreamClass;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.SerializedLambda;
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

    private static final SoftReferenceCache<String, String> FIELD_NAME_CACHE = new SoftReferenceCache<>();

    private static final SoftReferenceCache<Class<?>, Map<String, Field>> FIELD_MAP_CACHE = new SoftReferenceCache<>();

    private static final SoftReferenceCache<Class<?>, BeanModel> BEAN_MODEL_CACHE = new SoftReferenceCache<>();

    /**
     * 类 -> 非静态字段列表的稳定缓存。{@link #getFieldMap} 返回的 Map 包含静态、非静态全部字段，
     * 这里单独缓存过滤后的列表，避免每次 {@link #getFieldList} / {@link #getFieldAll}
     * 都重新分配 ArrayList 并逐字段判断修饰符。
     */
    private static final ConcurrentHashMap<Class<?>, Field[]> NON_STATIC_FIELDS_CACHE = new ConcurrentHashMap<>();


    /**
     * 缓存多泛型实参列表
     */
    private static final ConcurrentHashMap<Type, List<Class<?>>> GENERIC_LIST_CACHE = new ConcurrentHashMap<>();

    private static final Field[] EMPTY_FIELD_ARRAY = new Field[0];

    /**
     * 复用单个 Lookup，避免每次 {@link #createBeanModel} 都重新通过反射获取
     */
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    /**
     * Field → 预计算的访问元数据。缓存 offset / modifiers / type / staticBase，
     * 使 {@link #getValue} / {@link #setValue} 的热路径从"每次 3 次 Unsafe 反射 +
     * 多次 Modifier 调用"降为"一次 ConcurrentHashMap 查找 + 一次 Unsafe 读写"。
     */
    private static final ConcurrentHashMap<Field, FieldAccess> FIELD_ACCESS_CACHE = new ConcurrentHashMap<>();

    /**
     * 预缓存的字段访问元数据
     */
    private static final class FieldAccess {
        final long offset;
        final int modifiers;
        final Class<?> type;
        /**
         * static 字段的 base 对象；instance 字段为 null
         */
        final Object staticBase;
        final boolean volatileField;
        final boolean primitive;
        final boolean isStatic;

        FieldAccess(Field f) {
            this.modifiers = f.getModifiers();
            this.type = f.getType();
            this.isStatic = Modifier.isStatic(this.modifiers);
            this.volatileField = Modifier.isVolatile(this.modifiers);
            this.primitive = this.type.isPrimitive();
            if (this.isStatic) {
                this.offset = WsUnsafeUtils.staticFieldOffset(f);
                this.staticBase = WsUnsafeUtils.staticFieldBase(f);
            } else {
                this.offset = WsUnsafeUtils.objectFieldOffset(f);
                this.staticBase = null;
            }
        }

        /**
         * 返回 Unsafe 读写的 base 对象
         */
        Object base(Object instance) {
            return isStatic ? staticBase : instance;
        }
    }

    private static FieldAccess getFieldAccess(Field field) {
        FieldAccess cached = FIELD_ACCESS_CACHE.get(field);
        if (cached != null) {
            return cached;
        }
        FieldAccess resolved = new FieldAccess(field);
        FieldAccess prev = FIELD_ACCESS_CACHE.putIfAbsent(field, resolved);
        return prev != null ? prev : resolved;
    }

    private static final Field WRITE_REPLACE_METHOD_FIELD = WsReflectUtils.getFieldByName(ObjectStreamClass.class, "writeReplaceMethod");

    public static void main(String[] args) {
        Field field = getFieldByName(GenericsType.class, "genericsTypeList");
        System.out.println(getGenericClass(field.getGenericType()).get(0).getTypeName());

        String str = "List< Map< String, List<Map<String,>>>>";
        List<GenericsType> list = getGenericsType(str.toCharArray(), new int[]{0, str.length()});
        System.out.println(list);


        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("123213123123");
        stringBuilder.setLength(stringBuilder.length() - 1);
        System.out.println(stringBuilder);
        stringBuilder.append("123");
        System.out.println(stringBuilder);
    }

    public static Field getFieldForObject(String name, Object object) {
        Class<?> clazz = object.getClass();
        return getFieldForClass(name, clazz);
    }

    public static Field getFieldForClass(String name, Class<?> clazz) {
        Map<String, Field> fieldMap = getFieldMap(clazz);
        return fieldMap.get(name);
    }

    /**
     * 获取所有非静态的字段数组（缓存）
     *
     * @param clazz
     * @return 已经过滤掉静态字段的字段数组，永不为 null
     */
    public static Field[] getFieldAll(Class<?> clazz) {
        if (clazz == null) {
            return EMPTY_FIELD_ARRAY;
        }
        Field[] cached = NON_STATIC_FIELDS_CACHE.get(clazz);
        if (cached != null) {
            return cached.clone();
        }
        cached = NON_STATIC_FIELDS_CACHE.computeIfAbsent(clazz, c -> {
            Map<String, Field> fieldMap = getFieldMap(c);
            if (fieldMap.isEmpty()) {
                return EMPTY_FIELD_ARRAY;
            }
            List<Field> list = new ArrayList<>(fieldMap.size());
            for (Field field : fieldMap.values()) {
                if (!Modifier.isStatic(field.getModifiers())) {
                    list.add(field);
                }
            }
            return list.toArray(EMPTY_FIELD_ARRAY);
        });
        if (cached.length == 0) {
            return cached;
        }
        return cached.clone();
    }

    public static List<Field> getFieldList(Class<?> clazz) {
        return Arrays.asList(getFieldAll(clazz));
    }

    /**
     * 获取所有field
     *
     * @param clazz
     * @return
     */
    public static Map<String, Field> getFieldMap(Class<?> clazz) {
        if (clazz == null) {
            return Collections.emptyMap();
        }
        Map<String, Field> fieldNameAndFieldMap = FIELD_MAP_CACHE.get(clazz);
        if (fieldNameAndFieldMap != null) {
            return fieldNameAndFieldMap;
        }
        fieldNameAndFieldMap = new LinkedHashMap<>();
        Class<?> originalClass = clazz;
        for (; clazz != Object.class && clazz != null; clazz = clazz.getSuperclass()) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field value : fields) {
                String name = value.getName();
                fieldNameAndFieldMap.putIfAbsent(name, value);
            }
        }
        FIELD_MAP_CACHE.put(originalClass, fieldNameAndFieldMap);
        return fieldNameAndFieldMap;
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
        Set<Method> existSet = new HashSet<>();
        for (Method declaredMethod : clazz.getDeclaredMethods()) {
            if (!existSet.add(declaredMethod)) {
                continue;
            }
            if (declaredMethod.getName().equals(methodName)) {
                methodList.add(declaredMethod);
            }
        }
        for (Method method : clazz.getMethods()) {
            if (!existSet.add(method)) {
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
        if (child == parent || parent == Object.class) {
            return true;
        }
        if (child == Object.class) {
            return false;
        }
        return parent.isAssignableFrom(child);
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
        String str = FIELD_NAME_CACHE.get(n);
        if (str != null) {
            return str;
        }
        try {
            Method method = supplierFunc.getClass().getDeclaredMethod(WRITE_REPLACE_FUNCTION_NAME);
            method.setAccessible(true);
            SerializedLambda serializedLambda = (SerializedLambda) method.invoke(supplierFunc);
            String name = serializedLambda.getImplMethodName();
            String value = methodToFieldName(name);
            FIELD_NAME_CACHE.put(n, value);
            return value;
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return null;
        }
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
        String str = FIELD_NAME_CACHE.get(n);
        if (str != null) {
            return str;
        }
        SerializedLambda serializedLambda = getSerializedLambda(sFunction);
        String name = serializedLambda.getImplMethodName();
        String value = methodToFieldName(name);
        FIELD_NAME_CACHE.put(n, value);
        return value;
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

//    /**
//     * 获取泛型
//     *
//     * @param field
//     * @param <T>
//     * @return
//     */
//    public static <T> Class<?> getClassTypeof(Field field) {
//        Class<?> tClass = field.getType();
//        if (tClass.isArray() || WsReflectUtils.classCompare(tClass, Collection.class)) {
//            return getGenericsType(field.getGenericType());
//        } else {
//            return tClass;
//        }
//    }

    /**
     * 获取属性类型上的所有顶级泛型实参类型，按声明顺序返回。
     * 例如 {@code List<Foo>} 返回 [Foo]；{@code Map<String, Bar>} 返回 [String, Bar]；
     * 无泛型或无法解析的元素以 null 占位。结果列表不可变且按 Type 缓存。
     *
     * @param type 字段/getter/setter 上的 {@link Type}
     * @return 不可变列表，永不为 null
     */
    public static List<Class<?>> getGenericClass(Type type) {
        if (type == null) {
            return Collections.emptyList();
        }
        List<Class<?>> cached = GENERIC_LIST_CACHE.get(type);
        if (cached != null) {
            return cached;
        }

        return GENERIC_LIST_CACHE.computeIfAbsent(type,t->{
            GenericsType genericsType = getGenericsType(t.getTypeName());
            List<GenericsType> genericsTypeList = genericsType.getGenericsTypeList();
            if (WsCollectionUtils.isEmpty(genericsTypeList)) {
                return Collections.emptyList();
            }
            List<Class<?>> result = new ArrayList<>(genericsTypeList.size());
            for (GenericsType model : genericsTypeList) {
                result.add(loadClassOrNull(model.getClassName()));
            }
            return Collections.unmodifiableList(result);

        });
    }

    /**
     * 缓存泛型类型
     */
    private static final Map<String, GenericsType> GENERICS_TYPE_CACHE = new ConcurrentHashMap<>();

    public static GenericsType getGenericsType(Type type) {
        if (type == null) {
            throw new IllegalArgumentException("type is null");
        }
        return getGenericsType(type.getTypeName());
    }

    public static GenericsType getGenericsType(String typeName) {
        if (WsStringUtils.isBlank(typeName)) {
            throw new IllegalArgumentException("type name is null");
        }
        return GENERICS_TYPE_CACHE.computeIfAbsent(typeName, name -> {
            List<GenericsType> list = getGenericsType(name.toCharArray(), new int[]{0, typeName.length()});
            if (WsCollectionUtils.isEmpty(list)) {
                throw new IllegalArgumentException("Unable to resolve type:" + name);
            }
            return list.get(0);
        });
    }

    private static final char GENERIC_START = '<';
    private static final char GENERIC_END = '>';
    private static final char GENERIC_SEPARATOR = ',';

    private static List<GenericsType> getGenericsType(char[] typeName, int[] startAndEndIndex) {
        List<GenericsType> list = new ArrayList<>();
        GenericsType genericsType = new GenericsType();
        StringBuilder sb = new StringBuilder();
        trimBlank(startAndEndIndex, typeName);
        int startIndex = startAndEndIndex[0];
        int endIndex = startAndEndIndex[1];
        for (; startIndex < endIndex; startIndex++) {
            char c = typeName[startIndex];
            if (c == GENERIC_START) {
                startAndEndIndex[0] = startIndex + 1;
                genericsType.setGenericsTypeList(getGenericsType(typeName, startAndEndIndex));
                startIndex = startAndEndIndex[0] - 1;
            } else if (c == GENERIC_END) {
                startIndex++;
                break;
            } else if (c == GENERIC_SEPARATOR) {
                genericsType.setClassName(sb.toString());
                list.add(genericsType);
                genericsType = new GenericsType();
                if (sb.length() != 0) {
                    sb.setLength(0);
                    //sb = new StringBuilder();
                }
                startAndEndIndex[0] = startIndex + 1;
                trimBlank(startAndEndIndex, typeName);
                startIndex = startAndEndIndex[0] - 1;
            } else {
                sb.append(c);
            }
        }
        startAndEndIndex[0] = startIndex;
        genericsType.setClassName(sb.toString());
        list.add(genericsType);
        return list;
    }

    /**
     * 去除字符数组中指定范围的前后空白字符，并更新索引数组
     *
     * @param startAndEndIndex 长度为 2 的整型数组，[0] 为起始索引，[1] 为结束索引（不包含）
     * @param typeName         待处理的字符数组
     */
    private static void trimBlank(int[] startAndEndIndex, char[] typeName) {
        int sIndex = startAndEndIndex[0];
        int eIndex = startAndEndIndex[1];
        if (sIndex < 0) {
            sIndex = 0;
        }
        if (eIndex > typeName.length) {
            eIndex = typeName.length;
        }
        if (sIndex < eIndex) {
            for (; sIndex < eIndex; sIndex++) {
                char c = typeName[sIndex];
                if (Character.isWhitespace(c)) {
                    continue;
                }
                break;
            }
            for (; eIndex > sIndex; eIndex--) {
                char c = typeName[eIndex - 1];
                if (Character.isWhitespace(c)) {
                    continue;
                }
                break;
            }
        }
        startAndEndIndex[0] = sIndex;
        startAndEndIndex[1] = eIndex;
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
     * 加载类，如果不存在则返回 null
     * @param className
     * @return
     */
    public static Class<?> loadClassOrNull (String className) {
        if (WsStringUtils.isBlank(className)) {
            return null;
        }
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * 获得值。使用预缓存的 {@link FieldAccess} 避免 per-call 的
     * {@code getModifiers}/{@code getType}/{@code objectFieldOffset} 开销。
     *
     * @param o
     * @param field
     * @return
     */
    public static Object getValue(Object o, Field field) {
        FieldAccess fa = getFieldAccess(field);
        Object base = fa.base(o);
        long address = fa.offset;
        Class<?> t = fa.type;
        if (fa.primitive) {
            if (fa.volatileField) {
                if (t == int.class) {
                    return WsUnsafeUtils.getIntVolatile(base, address);
                }
                if (t == long.class) {
                    return WsUnsafeUtils.getLongVolatile(base, address);
                }
                if (t == double.class) {
                    return WsUnsafeUtils.getDoubleVolatile(base, address);
                }
                if (t == boolean.class) {
                    return WsUnsafeUtils.getBooleanVolatile(base, address);
                }
                if (t == float.class) {
                    return WsUnsafeUtils.getFloatVolatile(base, address);
                }
                if (t == short.class) {
                    return WsUnsafeUtils.getShortVolatile(base, address);
                }
                if (t == byte.class) {
                    return WsUnsafeUtils.getByteVolatile(base, address);
                }
                if (t == char.class) {
                    return WsUnsafeUtils.getCharVolatile(base, address);
                }
                throw new RuntimeException("不支持的类型");
            } else {
                if (t == int.class) {
                    return WsUnsafeUtils.getInt(base, address);
                }
                if (t == long.class) {
                    return WsUnsafeUtils.getLong(base, address);
                }
                if (t == double.class) {
                    return WsUnsafeUtils.getDouble(base, address);
                }
                if (t == boolean.class) {
                    return WsUnsafeUtils.getBoolean(base, address);
                }
                if (t == float.class) {
                    return WsUnsafeUtils.getFloat(base, address);
                }
                if (t == short.class) {
                    return WsUnsafeUtils.getShort(base, address);
                }
                if (t == byte.class) {
                    return WsUnsafeUtils.getByte(base, address);
                }
                if (t == char.class) {
                    return WsUnsafeUtils.getChar(base, address);
                }
                throw new RuntimeException("不支持的类型");
            }
        } else {
            if (fa.volatileField) {
                return WsUnsafeUtils.getObjectVolatile(base, address);
            } else {
                return WsUnsafeUtils.getObject(base, address);
            }
        }
    }

    /**
     * 设置值（Object 路径）。使用预缓存的 {@link FieldAccess}。
     */
    public static boolean setValue(Object o, Object value, Field field) {
        FieldAccess fa = getFieldAccess(field);
        if (o == null && !fa.isStatic) {
            return false;
        }
        Object base = fa.base(o);
        if (fa.volatileField) {
            WsUnsafeUtils.putObjectVolatile(base, fa.offset, value);
        } else {
            WsUnsafeUtils.putObject(base, fa.offset, value);
        }
        return true;
    }

    // ---- 以下为按基本类型分派的 setValue 重载，均通过 FieldAccess 跳过 per-call 反射 ----

    public static boolean setValue(Object o, int value, Field field) {
        FieldAccess fa = getFieldAccess(field);
        Object base = fa.base(o);
        if (fa.volatileField) {
            WsUnsafeUtils.putIntVolatile(base, fa.offset, value);
        } else {
            WsUnsafeUtils.putInt(base, fa.offset, value);
        }
        return true;
    }

    public static boolean setValue(Object o, boolean value, Field field) {
        FieldAccess fa = getFieldAccess(field);
        Object base = fa.base(o);
        if (fa.volatileField) {
            WsUnsafeUtils.putBooleanVolatile(base, fa.offset, value);
        } else {
            WsUnsafeUtils.putBoolean(base, fa.offset, value);
        }
        return true;
    }

    public static boolean setValue(Object o, char value, Field field) {
        FieldAccess fa = getFieldAccess(field);
        Object base = fa.base(o);
        if (fa.volatileField) {
            WsUnsafeUtils.putCharVolatile(base, fa.offset, value);
        } else {
            WsUnsafeUtils.putChar(base, fa.offset, value);
        }
        return true;
    }

    public static boolean setValue(Object o, byte value, Field field) {
        FieldAccess fa = getFieldAccess(field);
        Object base = fa.base(o);
        if (fa.volatileField) {
            WsUnsafeUtils.putByteVolatile(base, fa.offset, value);
        } else {
            WsUnsafeUtils.putByte(base, fa.offset, value);
        }
        return true;
    }

    public static boolean setValue(Object o, short value, Field field) {
        FieldAccess fa = getFieldAccess(field);
        Object base = fa.base(o);
        if (fa.volatileField) {
            WsUnsafeUtils.putShortVolatile(base, fa.offset, value);
        } else {
            WsUnsafeUtils.putShort(base, fa.offset, value);
        }
        return true;
    }

    public static boolean setValue(Object o, long value, Field field) {
        FieldAccess fa = getFieldAccess(field);
        Object base = fa.base(o);
        if (fa.volatileField) {
            WsUnsafeUtils.putLongVolatile(base, fa.offset, value);
        } else {
            WsUnsafeUtils.putLong(base, fa.offset, value);
        }
        return true;
    }

    public static boolean setValue(Object o, float value, Field field) {
        FieldAccess fa = getFieldAccess(field);
        Object base = fa.base(o);
        if (fa.volatileField) {
            WsUnsafeUtils.putFloatVolatile(base, fa.offset, value);
        } else {
            WsUnsafeUtils.putFloat(base, fa.offset, value);
        }
        return true;
    }

    public static boolean setValue(Object o, double value, Field field) {
        FieldAccess fa = getFieldAccess(field);
        Object base = fa.base(o);
        if (fa.volatileField) {
            WsUnsafeUtils.putDoubleVolatile(base, fa.offset, value);
        } else {
            WsUnsafeUtils.putDouble(base, fa.offset, value);
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
     *
     * @param bClass
     * @return
     */
    public static BeanModel createBeanModel(Class<?> bClass) {

        BeanModel cacheBeanModel = BEAN_MODEL_CACHE.get(bClass);
        if (cacheBeanModel != null) {
            return cacheBeanModel;
        }

        Map<String, Field> fieldMap = WsReflectUtils.getFieldMap(bClass);

        Method[] methods = bClass.getMethods();
        Map<String, Object[]> beanPropertyMap = new LinkedHashMap<>();

        for (Map.Entry<String, Field> stringFieldEntry : fieldMap.entrySet()) {
            if (Modifier.isStatic(stringFieldEntry.getValue().getModifiers())) {
                continue;
            }
            Object[] objects = new Object[3];
            objects[0] = stringFieldEntry.getValue();
            beanPropertyMap.put(stringFieldEntry.getKey(), objects);
        }

        for (Method method : methods) {
            if (Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            String name = method.getName();
            if (name.length() < 3) {
                continue;
            }
            if (name.startsWith(METHOD_NAME_SET)) {
                if (name.length() == 3 || method.getParameterCount() != 1) {
                    continue;
                }
                name = WsStringUtils.firstCharToLowerCase(name.substring(3));
                Object[] objects = beanPropertyMap.computeIfAbsent(name, n -> new Object[3]);
                objects[2] = method;
            } else if (name.startsWith(METHOD_NAME_GET)) {
                if (name.length() == 3 || method.getParameterCount() != 0) {
                    continue;
                }
                name = WsStringUtils.firstCharToLowerCase(name.substring(3));
                Object[] objects = beanPropertyMap.computeIfAbsent(name, n -> new Object[3]);
                objects[1] = method;
            } else if (name.startsWith(METHOD_NAME_IS)) {
                if (method.getParameterCount() != 0) {
                    continue;
                }
                name = WsStringUtils.firstCharToLowerCase(name.substring(2));
                Object[] objects = beanPropertyMap.computeIfAbsent(name, n -> new Object[3]);
                objects[1] = method;
            }
        }

        MethodHandles.Lookup lookup = LOOKUP;
        BeanModel beanModel = new BeanModel(bClass);
        Map<String, BeanPropertyModel> beanPropertyModelMap = beanModel.getPropertyModelMap();
        for (Map.Entry<String, Object[]> stringEntry : beanPropertyMap.entrySet()) {
            Object[] objects = stringEntry.getValue();
            if (objects[1] == null || (objects[0] == null && objects[2] == null)) {
                continue;
            }
            try {
                Method getMethod = (Method) objects[1];
                MethodHandle getMethodHandle = lookup.unreflect(getMethod);
                Method setMethod = objects[2] == null ? null : (Method) objects[2];
                MethodHandle setMethodHandle = setMethod == null ? null : lookup.unreflect(setMethod);
                beanPropertyModelMap.put(stringEntry.getKey(), new BeanPropertyModel(stringEntry.getKey(), (Field) objects[0], getMethod, getMethodHandle, setMethod, setMethodHandle));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        BEAN_MODEL_CACHE.put(bClass, beanModel);
        return beanModel;
    }
}





