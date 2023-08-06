package cn.katoumegumi.java.common;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.logging.Logger;

/**
 * 反射获取unsafe方法
 */
public class WsUnsafeUtils {

    private static final Logger log = Logger.getLogger(WsUnsafeUtils.class.getName());

    static class UnsafeUtils {
        private static Object unsafe;

        static {
            try {
                Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
                Field f = unsafeClass.getDeclaredField("theUnsafe");
                f.setAccessible(true);
                unsafe = f.get(null);
            } catch (NoSuchFieldException | IllegalAccessException | ClassNotFoundException e) {
                log.info("Unsafe Load Fail:" + e.getMessage());
                //log.info(e.getMessage());
            }
        }
    }

    private static final MethodHandles.Lookup lookup = MethodHandles.lookup();

    private static final Object unsafe = UnsafeUtils.unsafe;

    private static final MethodHandle objectFieldOffset = getMethod("objectFieldOffset", Field.class);

    private static final MethodHandle staticFieldOffset = getMethod("staticFieldOffset", Field.class);

    private static final MethodHandle staticFieldBase = getMethod("staticFieldBase", Field.class);

    private static final MethodHandle arrayBaseOffset = getMethod("arrayBaseOffset", Class.class);

    private static final MethodHandle allocateInstance = getMethod("allocateInstance", Class.class);

    private static final MethodHandle arrayIndexScale = getMethod("arrayIndexScale", Class.class);

    /**
     * object
     */
    private static final MethodHandle getObjectByObjectAndLong = getMethod("getObject", Object.class, long.class);
    private static final MethodHandle putObject = getMethod("putObject", Object.class, long.class, Object.class);
    private static final MethodHandle getObjectVolatile = getMethod("getObjectVolatile", Object.class, long.class);
    private static final MethodHandle putObjectVolatile = getMethod("putObjectVolatile", Object.class, long.class, Object.class);
    private static final MethodHandle putOrderedObject = getMethod("putOrderedObject", Object.class, long.class, Object.class);

    /**
     * int
     */
    private static final MethodHandle getIntByObjectAndLong = getMethod("getInt", Object.class, long.class);
    private static final MethodHandle putIntByObjectAndOffsetAndLong = getMethod("putInt", Object.class, long.class, int.class);
    private static final MethodHandle getIntVolatileByObjectAndLong = getMethod("getIntVolatile", Object.class, long.class);
    private static final MethodHandle putIntVolatileByObjectAndOffsetAndLong = getMethod("putIntVolatile", Object.class, long.class, int.class);

    /**
     * byte
     */
    private static final MethodHandle getByteByObjectAndLong = getMethod("getByte", Object.class, long.class);
    private static final MethodHandle putByteByObjectAndOffsetAndLong = getMethod("putByte", Object.class, long.class, byte.class);
    private static final MethodHandle getByteVolatileByObjectAndLong = getMethod("getByteVolatile", Object.class, long.class);
    private static final MethodHandle putByteVolatileByObjectAndOffsetAndLong = getMethod("putByteVolatile", Object.class, long.class, byte.class);

    /**
     * boolean
     */
    private static final MethodHandle getBooleanByObjectAndLong = getMethod("getBoolean", Object.class, long.class);
    private static final MethodHandle putBooleanByObjectAndOffsetAndLong = getMethod("putBoolean", Object.class, long.class, boolean.class);
    private static final MethodHandle getBooleanVolatileByObjectAndLong = getMethod("getBooleanVolatile", Object.class, long.class);
    private static final MethodHandle putBooleanVolatileByObjectAndOffsetAndLong = getMethod("putBooleanVolatile", Object.class, long.class, boolean.class);
    /**
     * char
     */
    private static final MethodHandle getCharByObjectAndLong = getMethod("getChar", Object.class, long.class);
    private static final MethodHandle putCharByObjectAndOffsetAndLong = getMethod("putChar", Object.class, long.class, char.class);
    private static final MethodHandle getCharVolatileByObjectAndLong = getMethod("getCharVolatile", Object.class, long.class);
    private static final MethodHandle putCharVolatileByObjectAndOffsetAndLong = getMethod("putCharVolatile", Object.class, long.class, char.class);
    /**
     * short
     */
    private static final MethodHandle getShortByObjectAndLong = getMethod("getShort", Object.class, long.class);
    private static final MethodHandle putShortByObjectAndOffsetAndLong = getMethod("putShort", Object.class, long.class, short.class);
    private static final MethodHandle getShortVolatileByObjectAndLong = getMethod("getShortVolatile", Object.class, long.class);
    private static final MethodHandle putShortVolatileByObjectAndOffsetAndLong = getMethod("putShortVolatile", Object.class, long.class, short.class);
    /**
     * long
     */
    private static final MethodHandle getLongByObjectAndLong = getMethod("getLong", Object.class, long.class);
    private static final MethodHandle putLongByObjectAndOffsetAndLong = getMethod("putLong", Object.class, long.class, long.class);
    private static final MethodHandle getLongVolatileByObjectAndLong = getMethod("getLongVolatile", Object.class, long.class);
    private static final MethodHandle putLongVolatileByObjectAndOffsetAndLong = getMethod("putLongVolatile", Object.class, long.class, long.class);
    /**
     * float
     */
    private static final MethodHandle getFloatByObjectAndLong = getMethod("getFloat", Object.class, long.class);
    private static final MethodHandle putFloatByObjectAndOffsetAndLong = getMethod("putFloat", Object.class, long.class, float.class);
    private static final MethodHandle getFloatVolatileByObjectAndLong = getMethod("getFloatVolatile", Object.class, long.class);
    private static final MethodHandle putFloatVolatileByObjectAndOffsetAndLong = getMethod("putFloatVolatile", Object.class, long.class, float.class);
    /**
     * double
     */
    private static final MethodHandle getDoubleByObjectAndLong = getMethod("getDouble", Object.class, long.class);
    private static final MethodHandle putDoubleByObjectAndOffsetAndLong = getMethod("putDouble", Object.class, long.class, double.class);
    private static final MethodHandle getDoubleVolatileByObjectAndLong = getMethod("getDoubleVolatile", Object.class, long.class);
    private static final MethodHandle putDoubleVolatileByObjectAndOffsetAndLong = getMethod("putDoubleVolatile", Object.class, long.class, double.class);

    /**
     * cas
     */
    private static final MethodHandle compareAndSwapObject = getMethod("compareAndSwapObject", Object.class, long.class, Object.class, Object.class);
    private static final MethodHandle compareAndSwapInt = getMethod("compareAndSwapInt", Object.class, long.class, int.class, int.class);
    private static final MethodHandle compareAndSwapLong = getMethod("compareAndSwapLong", Object.class, long.class, long.class, long.class);

    private static final MethodHandle getAndAddInt = getMethod("getAndAddInt", Object.class, long.class, int.class);
    private static final MethodHandle getAndAddLong = getMethod("getAndAddLong", Object.class, long.class, long.class);
    private static final MethodHandle getAndSetInt = getMethod("getAndSetInt", Object.class, long.class, int.class);
    private static final MethodHandle getAndSetLong = getMethod("getAndSetLong", Object.class, long.class, long.class);
    private static final MethodHandle getAndSetObject = getMethod("getAndSetObject", Object.class, long.class, Object.class);

    /**
     * memory
     */
    private static final MethodHandle allocateMemory = getMethod("allocateMemory", long.class);
    private static final MethodHandle reallocateMemory = getMethod("reallocateMemory", long.class, long.class);
    private static final MethodHandle freeMemory = getMethod("freeMemory", long.class);
    private static final MethodHandle setMemoryByAddressAndByteAndValue = getMethod("setMemory", long.class, long.class, byte.class);
    private static final MethodHandle setMemoryByObjectAndOffsetAndBytesAndValue = getMethod("setMemory", Object.class, long.class, long.class, byte.class);
    private static final MethodHandle copyMemoryBySrcAddressAndDestAddressAndBytes = getMethod("copyMemory", long.class, long.class, long.class);
    private static final MethodHandle copyMemoryBySrcBaseAndSrcAddressAndDesBaseAndDestAddressAndBytes = getMethod("copyMemory", Object.class, long.class, Object.class, long.class, long.class);
    private static final MethodHandle addressSize = getMethod("addressSize");
    private static final MethodHandle pageSize = getMethod("pageSize");

    private static final MethodHandle throwException = getMethod("throwException", Throwable.class);


    private static final MethodHandle park = getMethod("park", boolean.class, long.class);
    private static final MethodHandle unpark = getMethod("unpark", Object.class);
    private static final MethodHandle getLoadAverage = getMethod("getLoadAverage", double[].class, int.class);

    private static final MethodHandle loadFence = getMethod("loadFence");
    private static final MethodHandle storeFence = getMethod("storeFence");
    private static final MethodHandle fullFence = getMethod("fullFence");

    private static final MethodHandle invokeCleaner = getMethod("invokeCleaner", ByteBuffer.class);


    private static MethodHandle getMethod(String methodName, Class<?>... parameterTypes) {
        if (unsafe == null) {
            return null;
        }
        try {
            Method method = unsafe.getClass().getMethod(methodName, parameterTypes);
            return lookup.unreflect(method).bindTo(unsafe);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            log.info("Unsafe Method Load Fail:" + e.getMessage());
            return null;
        }
    }

    /*private static Object invoke(Method method, Object... objects) {
        assert method != null;
        try {
            return method.invoke(unsafe, objects);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }*/


    public static long objectFieldOffset(Field f) {
        //return (long) invoke(objectFieldOffset, f);
        assert objectFieldOffset != null;
        try {
            return (long) objectFieldOffset.invokeExact(f);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static long staticFieldOffset(Field f) {
        //return (long) invoke(staticFieldOffset, f);
        assert staticFieldOffset != null;
        try {
            return (long) staticFieldOffset.invokeExact(f);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static Object staticFieldBase(Field f){
        assert  staticFieldBase != null;
        try {
            return staticFieldBase.invokeExact(f);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static int arrayBaseOffset(Class<?> arrayClass) {
        //return (int) invoke(arrayBaseOffset, arrayClass);
        assert arrayBaseOffset != null;
        try {
            return (int) arrayBaseOffset.invokeExact(arrayClass);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static Object allocateInstance(Class<?> cls) {
        //return invoke(allocateInstance, cls);
        assert allocateInstance != null;
        try {
            return allocateInstance.invokeExact(cls);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static int arrayIndexScale(Class<?> arrayClass) {
        //return (int) invoke(arrayIndexScale, arrayClass);
        assert arrayIndexScale != null;
        try {
            return (int) arrayIndexScale.invokeExact(arrayClass);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * int
     *
     * @param o
     * @param offset
     * @return
     */
    public static int getInt(Object o, long offset) {
        //return (int) invoke(getIntByObjectAndLong, o, offset);
        assert getIntByObjectAndLong != null;
        try {
            return (int) getIntByObjectAndLong.invokeExact(o, offset);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void putInt(Object o, long offset, int x) {
        //invoke(putIntByObjectAndOffsetAndLong, o, offset, x);
        assert putIntByObjectAndOffsetAndLong != null;
        try {
            putIntByObjectAndOffsetAndLong.invokeExact(o, offset, x);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static int getIntVolatile(Object o, long offset) {
        //return (int) invoke(getIntVolatileByObjectAndLong, o, offset);
        assert getIntVolatileByObjectAndLong != null;
        try {
            return (int) getIntVolatileByObjectAndLong.invokeExact(o, offset);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void putIntVolatile(Object o, long offset, int x) {
        //invoke(putIntVolatileByObjectAndOffsetAndLong, o, offset, x);
        assert putIntVolatileByObjectAndOffsetAndLong != null;
        try {
            putIntVolatileByObjectAndOffsetAndLong.invokeExact(o, offset, x);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * byte
     *
     * @param o
     * @param offset
     * @return
     */
    public static byte getByte(Object o, long offset) {
        //return (byte) invoke(getByteByObjectAndLong, o, offset);
        assert getByteByObjectAndLong != null;
        try {
            return (byte) getByteByObjectAndLong.invokeExact(o, offset);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void putByte(Object o, long offset, byte x) {
        //invoke(putByteByObjectAndOffsetAndLong, o, offset, x);
        assert putByteByObjectAndOffsetAndLong != null;
        try {
            putByteByObjectAndOffsetAndLong.invokeExact(o, offset, x);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static byte getByteVolatile(Object o, long offset) {
        //return (byte) invoke(getByteVolatileByObjectAndLong, o, offset);
        assert getByteVolatileByObjectAndLong != null;
        try {
            return (byte) getByteVolatileByObjectAndLong.invokeExact(o, offset);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void putByteVolatile(Object o, long offset, byte x) {
        //invoke(putByteVolatileByObjectAndOffsetAndLong, o, offset, x);
        assert putByteVolatileByObjectAndOffsetAndLong != null;
        try {
            putByteVolatileByObjectAndOffsetAndLong.invokeExact(o, offset, x);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * boolean
     *
     * @param o
     * @param offset
     * @return
     */
    public static boolean getBoolean(Object o, long offset) {
        //return (boolean) invoke(getBooleanByObjectAndLong, o, offset);
        assert getBooleanByObjectAndLong != null;
        try {
            return (boolean) getBooleanByObjectAndLong.invokeExact(o, offset);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void putBoolean(Object o, long offset, boolean x) {
        //invoke(putBooleanByObjectAndOffsetAndLong, o, offset, x);
        assert putBooleanByObjectAndOffsetAndLong != null;
        try {
            putBooleanByObjectAndOffsetAndLong.invokeExact(o, offset, x);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean getBooleanVolatile(Object o, long offset) {
        //return (boolean) invoke(getBooleanVolatileByObjectAndLong, o, offset);
        assert getBooleanVolatileByObjectAndLong != null;
        try {
            return (boolean) getBooleanVolatileByObjectAndLong.invokeExact(o, offset);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void putBooleanVolatile(Object o, long offset, boolean x) {
        //invoke(putBooleanVolatileByObjectAndOffsetAndLong, o, offset, x);
        assert putBooleanVolatileByObjectAndOffsetAndLong != null;
        try {
            putBooleanVolatileByObjectAndOffsetAndLong.invokeExact(o, offset, x);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * char
     *
     * @param o
     * @param offset
     * @return
     */
    public static char getChar(Object o, long offset) {
        //return (char) invoke(getCharByObjectAndLong, o, offset);
        assert getCharByObjectAndLong != null;
        try {
            return (char) getCharByObjectAndLong.invokeExact(o, offset);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void putChar(Object o, long offset, char x) {
        //invoke(putCharByObjectAndOffsetAndLong, o, offset, x);
        assert putCharByObjectAndOffsetAndLong != null;
        try {
            putCharByObjectAndOffsetAndLong.invokeExact(o, offset, x);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static char getCharVolatile(Object o, long offset) {
        //return (char) invoke(getCharVolatileByObjectAndLong, o, offset);
        assert getCharVolatileByObjectAndLong != null;
        try {
            return (char) getCharVolatileByObjectAndLong.invokeExact(o, offset);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void putCharVolatile(Object o, long offset, char x) {
        //invoke(putCharVolatileByObjectAndOffsetAndLong, o, offset, x);
        assert putCharVolatileByObjectAndOffsetAndLong != null;
        try {
            putCharVolatileByObjectAndOffsetAndLong.invokeExact(o, offset, x);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * short
     *
     * @param o
     * @param offset
     * @return
     */
    public static short getShort(Object o, long offset) {
        //return (short) invoke(getShortByObjectAndLong, o, offset);
        assert getShortByObjectAndLong != null;
        try {
            return (short) getShortByObjectAndLong.invokeExact(o, offset);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void putShort(Object o, long offset, short x) {
        //invoke(putShortByObjectAndOffsetAndLong, o, offset, x);
        assert putShortByObjectAndOffsetAndLong != null;
        try {
            putShortByObjectAndOffsetAndLong.invokeExact(o, offset, x);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static short getShortVolatile(Object o, long offset) {
        //return (short) invoke(getShortVolatileByObjectAndLong, o, offset);
        assert getShortVolatileByObjectAndLong != null;
        try {
            return (short) getShortVolatileByObjectAndLong.invokeExact(o, offset);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void putShortVolatile(Object o, long offset, short x) {
        //invoke(putShortVolatileByObjectAndOffsetAndLong, o, offset, x);
        assert putShortVolatileByObjectAndOffsetAndLong != null;
        try {
            putShortVolatileByObjectAndOffsetAndLong.invokeExact(o, offset, x);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * long
     *
     * @param o
     * @param offset
     * @return
     */
    public static long getLong(Object o, long offset) {
        //return (long) invoke(getLongByObjectAndLong, o, offset);
        assert getLongByObjectAndLong != null;
        try {
            return (long) getLongByObjectAndLong.invokeExact(o, offset);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void putLong(Object o, long offset, long x) {
        //invoke(putLongByObjectAndOffsetAndLong, o, offset, x);
        assert putLongByObjectAndOffsetAndLong != null;
        try {
            putLongByObjectAndOffsetAndLong.invokeExact(o, offset, x);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static long getLongVolatile(Object o, long offset) {
        //return (long) invoke(getLongVolatileByObjectAndLong, o, offset);
        assert getLongVolatileByObjectAndLong != null;
        try {
            return (long) getLongVolatileByObjectAndLong.invokeExact(o, offset);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void putLongVolatile(Object o, long offset, long x) {
        //invoke(putLongVolatileByObjectAndOffsetAndLong, o, offset, x);
        assert putLongVolatileByObjectAndOffsetAndLong != null;
        try {
            putLongVolatileByObjectAndOffsetAndLong.invokeExact(o, offset, x);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * float
     *
     * @param o
     * @param offset
     * @return
     */
    public static float getFloat(Object o, long offset) {
        //return (float) invoke(getFloatByObjectAndLong, o, offset);
        assert getFloatByObjectAndLong != null;
        try {
            return (float) getFloatByObjectAndLong.invokeExact(o, offset);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void putFloat(Object o, long offset, float x) {
        //invoke(putFloatByObjectAndOffsetAndLong, o, offset, x);
        assert putFloatByObjectAndOffsetAndLong != null;
        try {
            putFloatByObjectAndOffsetAndLong.invokeExact(o, offset, x);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static float getFloatVolatile(Object o, long offset) {
        //return (float) invoke(getFloatVolatileByObjectAndLong, o, offset);
        assert getFloatVolatileByObjectAndLong != null;
        try {
            return (float) getFloatVolatileByObjectAndLong.invokeExact(o, offset);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void putFloatVolatile(Object o, long offset, float x) {
        //invoke(putFloatVolatileByObjectAndOffsetAndLong, o, offset, x);
        assert putFloatVolatileByObjectAndOffsetAndLong != null;
        try {
            putFloatVolatileByObjectAndOffsetAndLong.invokeExact(o, offset, x);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * double
     *
     * @param o
     * @param offset
     * @return
     */
    public static double getDouble(Object o, long offset) {
        //return (double) invoke(getDoubleByObjectAndLong, o, offset);
        assert getDoubleByObjectAndLong != null;
        try {
            return (double) getDoubleByObjectAndLong.invokeExact(o, offset);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void putDouble(Object o, long offset, double x) {
        //invoke(putDoubleByObjectAndOffsetAndLong, o, offset, x);
        assert putDoubleByObjectAndOffsetAndLong != null;
        try {
            putDoubleByObjectAndOffsetAndLong.invokeExact(o, offset, x);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static double getDoubleVolatile(Object o, long offset) {
        //return (double) invoke(getDoubleVolatileByObjectAndLong, o, offset);
        assert getDoubleVolatileByObjectAndLong != null;
        try {
            return (double) getDoubleVolatileByObjectAndLong.invokeExact(o, offset);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void putDoubleVolatile(Object o, long offset, double x) {
        //invoke(putDoubleVolatileByObjectAndOffsetAndLong, o, offset, x);
        assert putDoubleVolatileByObjectAndOffsetAndLong != null;
        try {
            putDoubleVolatileByObjectAndOffsetAndLong.invokeExact(o, offset, x);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * object
     *
     * @param o
     * @param offset
     * @return
     */
    public static Object getObject(Object o, long offset) {
        //return invoke(getObjectByObjectAndLong, o, offset);
        assert getObjectByObjectAndLong != null;
        try {
            return getObjectByObjectAndLong.invokeExact(o, offset);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static Object getObjectVolatile(Object o, long offset) {
        //return invoke(getObjectVolatile, o, offset);
        assert getObjectVolatile != null;
        try {
            return getObjectVolatile.invokeExact(o, offset);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void putObject(Object o, long offset, Object x) {
        //invoke(putObject, o, offset, x);
        assert putObject != null;
        try {
            putObject.invokeExact(o, offset, x);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void putObjectVolatile(Object o, long offset, Object x) {
        //invoke(putObjectVolatile, o, offset, x);
        assert putObjectVolatile != null;
        try {
            putObjectVolatile.invokeExact(o, offset, x);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void putOrderedObject(Object o, long offset, Object x) {
        //invoke(putOrderedObject, o, offset, x);
        assert putOrderedObject != null;
        try {
            putOrderedObject.invokeExact(o, offset, x);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * cas
     */

    public final boolean compareAndSwapObject(Object o, long offset,
                                              Object expected,
                                              Object x) {
        //return (boolean) invoke(compareAndSwapObject, o, offset, expected, x);
        assert compareAndSwapObject != null;
        try {
            return (boolean) compareAndSwapObject.invokeExact(o, offset, expected, x);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public final boolean compareAndSwapInt(Object o, long offset,
                                           int expected,
                                           int x) {
        //return (boolean) invoke(compareAndSwapInt, o, offset, expected, x);
        assert compareAndSwapInt != null;
        try {
            return (boolean) compareAndSwapInt.invokeExact(o, offset, expected, x);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public final boolean compareAndSwapLong(Object o, long offset,
                                            long expected,
                                            long x) {
        //return (boolean) invoke(compareAndSwapLong, o, offset, expected, x);
        assert compareAndSwapLong != null;
        try {
            return (boolean) compareAndSwapLong.invokeExact(o, offset, expected, x);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public final int getAndAddInt(Object o, long offset, int delta) {
        //return (int) invoke(getAndAddInt, o, offset, delta);
        assert getAndAddInt != null;
        try {
            return (int) getAndAddInt.invokeExact(o, offset, delta);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public final long getAndAddLong(Object o, long offset, long delta) {
        //return (long) invoke(getAndAddLong, o, offset, delta);
        assert getAndAddLong != null;
        try {
            return (long) getAndAddLong.invokeExact(o, offset, delta);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public final int getAndSetInt(Object o, long offset, int delta) {
        //return (int) invoke(getAndSetInt, o, offset, delta);
        assert getAndSetInt != null;
        try {
            return (int) getAndSetInt.invokeExact(o, offset, delta);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public final long getAndSetLong(Object o, long offset, long delta) {
        //return (long) invoke(getAndSetLong, o, offset, delta);
        assert getAndSetLong != null;
        try {
            return (long) getAndSetLong.invokeExact(o, offset, delta);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public final Object getAndSetObject(Object o, long offset, Object delta) {
        //return invoke(getAndSetObject, o, offset, delta);
        assert getAndSetObject != null;
        try {
            return getAndSetObject.invokeExact(o, offset, delta);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * memory
     */
    public static long allocateMemory(long bytes) {
        //return (long) invoke(allocateMemory, bytes);
        assert allocateMemory != null;
        try {
            return (long) allocateMemory.invokeExact(bytes);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static long reallocateMemory(long address, long bytes) {
        //return (long) invoke(reallocateMemory, address, bytes);
        assert reallocateMemory != null;
        try {
            return (long) reallocateMemory.invokeExact(address, bytes);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void freeMemory(long address) {
        //invoke(freeMemory, address);
        assert freeMemory != null;
        try {
            freeMemory.invokeExact(address);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void setMemory(long address, long bytes, byte value) {
        //invoke(setMemoryByAddressAndByteAndValue, address, bytes, value);
        assert setMemoryByAddressAndByteAndValue != null;
        try {
            setMemoryByAddressAndByteAndValue.invokeExact(address, bytes, value);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void setMemory(Object o, long offset, long bytes, byte value) {
        //invoke(setMemoryByObjectAndOffsetAndBytesAndValue, o, offset, bytes, value);
        assert setMemoryByObjectAndOffsetAndBytesAndValue != null;
        try {
            setMemoryByObjectAndOffsetAndBytesAndValue.invokeExact(o, offset, bytes, value);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void copyMemory(long srcAddress, long destAddress, long bytes) {
        //invoke(copyMemoryBySrcAddressAndDestAddressAndBytes, srcAddress, destAddress, bytes);
        assert copyMemoryBySrcAddressAndDestAddressAndBytes != null;
        try {
            copyMemoryBySrcAddressAndDestAddressAndBytes.invokeExact(srcAddress, destAddress, bytes);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public void copyMemory(Object srcBase, long srcOffset,
                           Object destBase, long destOffset,
                           long bytes) {
        //invoke(copyMemoryBySrcBaseAndSrcAddressAndDesBaseAndDestAddressAndBytes, srcBase, srcOffset, destBase, destOffset, bytes);
        assert copyMemoryBySrcBaseAndSrcAddressAndDesBaseAndDestAddressAndBytes != null;
        try {
            copyMemoryBySrcBaseAndSrcAddressAndDesBaseAndDestAddressAndBytes.invokeExact(srcBase, srcOffset, destBase, destOffset, bytes);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static int addressSize() {
        //return (int) invoke(addressSize);
        assert addressSize != null;
        try {
            return (int) addressSize.invokeExact();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static int pageSize() {
        //return (int) invoke(pageSize);
        assert pageSize != null;
        try {
            return (int) pageSize.invokeExact();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void throwException(Throwable ee) {
        //invoke(throwException, ee);
        assert throwException != null;
        try {
            throwException.invokeExact(ee);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void park(boolean isAbsolute, long time) {
        //invoke(park, isAbsolute, time);
        assert park != null;
        try {
            park.invokeExact(isAbsolute, time);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void unpark(Object thread) {
        //invoke(unpark, thread);
        assert unpark != null;
        try {
            unpark.invokeExact(thread);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static int getLoadAverage(double[] loadavg, int nelems) {
        //return (int) invoke(getLoadAverage, loadavg, nelems);
        assert getLoadAverage != null;
        try {
            return (int) getLoadAverage.invokeExact(loadavg, nelems);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void loadFence() {
        //invoke(loadFence);
        assert loadFence != null;
        try {
            loadFence.invokeExact();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void storeFence() {
        //invoke(storeFence);
        assert storeFence != null;
        try {
            storeFence.invokeExact();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void fullFence() {
        //invoke(fullFence);
        assert fullFence != null;
        try {
            fullFence.invokeExact();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }


    public void invokeCleaner(java.nio.ByteBuffer directBuffer) {
        //invoke(invokeCleaner, directBuffer);
        assert invokeCleaner != null;
        try {
            invokeCleaner.invokeExact(directBuffer);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

}
