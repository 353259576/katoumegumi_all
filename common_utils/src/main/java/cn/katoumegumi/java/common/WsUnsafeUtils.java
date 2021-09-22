package cn.katoumegumi.java.common;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class WsUnsafeUtils {

    private static Object unsafe;

    private static Method objectFieldOffset;

    private static Method staticFieldOffset;

    private static Method arrayBaseOffset;

    private static Method allocateInstance;

    private static Method arrayIndexScale;


    /**
     * int
     */
    private static Method getIntByObjectAndLong;

    private static Method putIntByObjectAndOffsetAndLong;

    private static Method getIntVolatileByObjectAndLong;

    private static Method putIntVolatileByObjectAndOffsetAndLong;

    /**
     * byte
     */
    private static Method getByteByObjectAndLong;

    private static Method putByteByObjectAndOffsetAndLong;

    private static Method getByteVolatileByObjectAndLong;

    private static Method putByteVolatileByObjectAndOffsetAndLong;
    /**
     * boolean
     */
    private static Method getBooleanByObjectAndLong;

    private static Method putBooleanByObjectAndOffsetAndLong;

    private static Method getBooleanVolatileByObjectAndLong;

    private static Method putBooleanVolatileByObjectAndOffsetAndLong;
    /**
     * char
     */
    private static Method getCharByObjectAndLong;

    private static Method putCharByObjectAndOffsetAndLong;

    private static Method getCharVolatileByObjectAndLong;

    private static Method putCharVolatileByObjectAndOffsetAndLong;

    /**
     * short
     */
    private static Method getShortByObjectAndLong;

    private static Method putShortByObjectAndOffsetAndLong;

    private static Method getShortVolatileByObjectAndLong;

    private static Method putShortVolatileByObjectAndOffsetAndLong;

    /**
     * long
     */
    private static Method getLongByObjectAndLong;

    private static Method putLongByObjectAndOffsetAndLong;

    private static Method getLongVolatileByObjectAndLong;

    private static Method putLongVolatileByObjectAndOffsetAndLong;

    /**
     * float
     */
    private static Method getFloatByObjectAndLong;

    private static Method putFloatByObjectAndOffsetAndLong;

    private static Method getFloatVolatileByObjectAndLong;

    private static Method putFloatVolatileByObjectAndOffsetAndLong;

    /**
     * double
     */
    private static Method getDoubleByObjectAndLong;

    private static Method putDoubleByObjectAndOffsetAndLong;

    private static Method getDoubleVolatileByObjectAndLong;

    private static Method putDoubleVolatileByObjectAndOffsetAndLong;


    /**
     * object
     */
    private static Method getObjectByObjectAndLong;

    private static Method putObject;

    private static Method getObjectVolatile;

    private static Method putObjectVolatile;

    private static Method putOrderedObject;


    /**
     * memory
     */
    private static Method allocateMemory;

    private static Method reallocateMemory;

    private static Method freeMemory;

    private static Method setMemoryByAddressAndByteAndValue;

    private static Method setMemoryByObjectAndOffsetAndBytesAndValue;

    private static Method copyMemoryBySrcAddressAndDestAddressAndBytes;

    private static Method copyMemoryBySrcBaseAndSrcAddressAndDesBaseAndDestAddressAndBytes;

    private static Method park;

    private static Method unpark;

    private static Method getLoadAverage;

    private static Method loadFence;

    private static Method storeFence;

    private static Method fullFence;

    static {
        try {
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            Field f = unsafeClass.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            unsafe = f.get(null);
        } catch (NoSuchFieldException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (unsafe != null) {

            objectFieldOffset = getMethod("objectFieldOffset", Field.class);

            staticFieldOffset = getMethod("staticFieldOffset", Field.class);

            arrayBaseOffset = getMethod("arrayBaseOffset", Class.class);

            allocateInstance = getMethod("allocateInstance", Class.class);

            arrayIndexScale = getMethod("arrayIndexScale", Class.class);

            /**
             * object
             */
            getObjectByObjectAndLong = getMethod("getObject", Object.class, long.class);
            putObject = getMethod("putObject", Object.class, long.class, Object.class);
            getObjectVolatile = getMethod("getObjectVolatile", Object.class, long.class);
            putObjectVolatile = getMethod("putObjectVolatile", Object.class, long.class, Object.class);
            putOrderedObject = getMethod("putOrderedObject", Object.class, long.class, Object.class);

            /**
             * int
             */
            getIntByObjectAndLong = getMethod("getInt", Object.class, long.class);
            putIntByObjectAndOffsetAndLong = getMethod("putInt", Object.class, long.class, int.class);
            getIntVolatileByObjectAndLong = getMethod("getIntVolatile", Object.class, long.class);
            putIntVolatileByObjectAndOffsetAndLong = getMethod("putIntVolatile", Object.class, long.class, int.class);

            /**
             * byte
             */
            getByteByObjectAndLong = getMethod("getByte", Object.class, long.class);
            putByteByObjectAndOffsetAndLong = getMethod("putByte", Object.class, long.class, byte.class);
            getByteVolatileByObjectAndLong = getMethod("getByteVolatile", Object.class, long.class);
            putByteVolatileByObjectAndOffsetAndLong = getMethod("putByteVolatile", Object.class, long.class, byte.class);

            /**
             * boolean
             */
            getBooleanByObjectAndLong = getMethod("getBoolean", Object.class, long.class);
            putBooleanByObjectAndOffsetAndLong = getMethod("putBoolean", Object.class, long.class, boolean.class);
            getBooleanVolatileByObjectAndLong = getMethod("getBooleanVolatile", Object.class, long.class);
            putBooleanVolatileByObjectAndOffsetAndLong = getMethod("putBooleanVolatile", Object.class, long.class, boolean.class);
            /**
             * char
             */
            getCharByObjectAndLong = getMethod("getChar", Object.class, long.class);
            putCharByObjectAndOffsetAndLong = getMethod("putChar", Object.class, long.class, char.class);
            getCharVolatileByObjectAndLong = getMethod("getCharVolatile", Object.class, long.class);
            putCharVolatileByObjectAndOffsetAndLong = getMethod("putCharVolatile", Object.class, long.class, char.class);
            /**
             * short
             */
            getShortByObjectAndLong = getMethod("getShort", Object.class, long.class);
            putShortByObjectAndOffsetAndLong = getMethod("putShort", Object.class, long.class, short.class);
            getShortVolatileByObjectAndLong = getMethod("getShortVolatile", Object.class, long.class);
            putShortVolatileByObjectAndOffsetAndLong = getMethod("putShortVolatile", Object.class, long.class, short.class);
            /**
             * long
             */
            getLongByObjectAndLong = getMethod("getLong", Object.class, long.class);
            putLongByObjectAndOffsetAndLong = getMethod("putLong", Object.class, long.class, long.class);
            getLongVolatileByObjectAndLong = getMethod("getLongVolatile", Object.class, long.class);
            putLongVolatileByObjectAndOffsetAndLong = getMethod("putLongVolatile", Object.class, long.class, long.class);
            /**
             * float
             */
            getFloatByObjectAndLong = getMethod("getFloat", Object.class, long.class);
            putFloatByObjectAndOffsetAndLong = getMethod("putFloat", Object.class, long.class, float.class);
            getFloatVolatileByObjectAndLong = getMethod("getFloatVolatile", Object.class, long.class);
            putFloatVolatileByObjectAndOffsetAndLong = getMethod("putFloatVolatile", Object.class, long.class, float.class);
            /**
             * double
             */
            getDoubleByObjectAndLong = getMethod("getDouble", Object.class, long.class);
            putDoubleByObjectAndOffsetAndLong = getMethod("putDouble", Object.class, long.class, double.class);
            getDoubleVolatileByObjectAndLong = getMethod("getDoubleVolatile", Object.class, long.class);
            putDoubleVolatileByObjectAndOffsetAndLong = getMethod("putDoubleVolatile", Object.class, long.class, double.class);

            /**
             * memory
             */
            allocateMemory = getMethod("allocateMemory", long.class);
            reallocateMemory = getMethod("reallocateMemory", long.class, long.class);
            freeMemory = getMethod("freeMemory", long.class);
            setMemoryByAddressAndByteAndValue = getMethod("setMemory", long.class, long.class, byte.class);
            setMemoryByObjectAndOffsetAndBytesAndValue = getMethod("setMemory", Object.class, long.class, long.class, byte.class);
            copyMemoryBySrcAddressAndDestAddressAndBytes = getMethod("copyMemory", long.class, long.class, long.class);
            copyMemoryBySrcBaseAndSrcAddressAndDesBaseAndDestAddressAndBytes = getMethod("copyMemory", Object.class, long.class, Object.class, long.class, long.class);

            park = getMethod("park", boolean.class, long.class);
            unpark = getMethod("unpark", Object.class);
            getLoadAverage = getMethod("getLoadAverage", double[].class, int.class);

            loadFence = getMethod("loadFence");
            storeFence = getMethod("storeFence");
            fullFence = getMethod("fullFence");
        }
    }

    private static Method getMethod(String methodName, Class<?>... parameterTypes) {
        try {
            return unsafe.getClass().getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Object invoke(Method method, Object... objects) {
        try {
            return method.invoke(unsafe, objects);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }


    public static long objectFieldOffset(Field f) {
        return (long) invoke(objectFieldOffset, f);
    }

    public static long staticFieldOffset(Field f) {
        return (long) invoke(staticFieldOffset, f);
    }

    public static int arrayBaseOffset(Class<?> arrayClass) {
        return (int) invoke(arrayBaseOffset, arrayClass);
    }

    public static Object allocateInstance(Class<?> cls) {
        return invoke(allocateInstance, cls);
    }

    public static int arrayIndexScale(Class<?> arrayClass) {
        return (int) invoke(arrayIndexScale, arrayClass);
    }

    /**
     * int
     *
     * @param o
     * @param offset
     * @return
     */
    public static int getInt(Object o, long offset) {
        return (int) invoke(getIntByObjectAndLong, o, offset);
    }

    public static void putInt(Object o, long offset, int x) {
        invoke(putIntByObjectAndOffsetAndLong, o, offset, x);
    }

    public static int getIntVolatile(Object o, long offset) {
        return (int) invoke(getIntVolatileByObjectAndLong, o, offset);
    }

    public static void putIntVolatile(Object o, long offset, int x) {
        invoke(putIntVolatileByObjectAndOffsetAndLong, o, offset, x);
    }

    /**
     * byte
     *
     * @param o
     * @param offset
     * @return
     */
    public static byte getByte(Object o, long offset) {
        return (byte) invoke(getByteByObjectAndLong, o, offset);
    }

    public static void putByte(Object o, long offset, byte x) {
        invoke(putByteByObjectAndOffsetAndLong, o, offset, x);
    }

    public static byte getByteVolatile(Object o, long offset) {
        return (byte) invoke(getByteVolatileByObjectAndLong, o, offset);
    }

    public static void putByteVolatile(Object o, long offset, byte x) {
        invoke(putByteVolatileByObjectAndOffsetAndLong, o, offset, x);
    }

    /**
     * boolean
     *
     * @param o
     * @param offset
     * @return
     */
    public static boolean getBoolean(Object o, long offset) {
        return (boolean) invoke(getBooleanByObjectAndLong, o, offset);
    }

    public static void putBoolean(Object o, long offset, boolean x) {
        invoke(putBooleanByObjectAndOffsetAndLong, o, offset, x);
    }

    public static boolean getBooleanVolatile(Object o, long offset) {
        return (boolean) invoke(getBooleanVolatileByObjectAndLong, o, offset);
    }

    public static void putBooleanVolatile(Object o, long offset, boolean x) {
        invoke(putBooleanVolatileByObjectAndOffsetAndLong, o, offset, x);
    }

    /**
     * char
     *
     * @param o
     * @param offset
     * @return
     */
    public static char getChar(Object o, long offset) {
        return (char) invoke(getCharByObjectAndLong, o, offset);
    }

    public static void putChar(Object o, long offset, char x) {
        invoke(putCharByObjectAndOffsetAndLong, o, offset, x);
    }

    public static char getCharVolatile(Object o, long offset) {
        return (char) invoke(getCharVolatileByObjectAndLong, o, offset);
    }

    public static void putCharVolatile(Object o, long offset, char x) {
        invoke(putCharVolatileByObjectAndOffsetAndLong, o, offset, x);
    }

    /**
     * short
     *
     * @param o
     * @param offset
     * @return
     */
    public static short getShort(Object o, long offset) {
        return (short) invoke(getShortByObjectAndLong, o, offset);
    }

    public static void putShort(Object o, long offset, short x) {
        invoke(putShortByObjectAndOffsetAndLong, o, offset, x);
    }

    public static short getShortVolatile(Object o, long offset) {
        return (short) invoke(getShortVolatileByObjectAndLong, o, offset);
    }

    public static void putShortVolatile(Object o, long offset, short x) {
        invoke(putShortVolatileByObjectAndOffsetAndLong, o, offset, x);
    }

    /**
     * long
     *
     * @param o
     * @param offset
     * @return
     */
    public static long getLong(Object o, long offset) {
        return (long) invoke(getLongByObjectAndLong, o, offset);
    }

    public static void putLong(Object o, long offset, long x) {
        invoke(putLongByObjectAndOffsetAndLong, o, offset, x);
    }

    public static long getLongVolatile(Object o, long offset) {
        return (long) invoke(getLongVolatileByObjectAndLong, o, offset);
    }

    public static void putLongVolatile(Object o, long offset, long x) {
        invoke(putLongVolatileByObjectAndOffsetAndLong, o, offset, x);
    }

    /**
     * float
     *
     * @param o
     * @param offset
     * @return
     */
    public static float getFloat(Object o, long offset) {
        return (float) invoke(getFloatByObjectAndLong, o, offset);
    }

    public static void putFloat(Object o, long offset, float x) {
        invoke(putFloatByObjectAndOffsetAndLong, o, offset, x);
    }

    public static float getFloatVolatile(Object o, long offset) {
        return (float) invoke(getFloatVolatileByObjectAndLong, o, offset);
    }

    public static void putFloatVolatile(Object o, long offset, float x) {
        invoke(putFloatVolatileByObjectAndOffsetAndLong, o, offset, x);
    }

    /**
     * double
     *
     * @param o
     * @param offset
     * @return
     */
    public static double getDouble(Object o, long offset) {
        return (double) invoke(getDoubleByObjectAndLong, o, offset);
    }

    public static void putDouble(Object o, long offset, double x) {
        invoke(putDoubleByObjectAndOffsetAndLong, o, offset, x);
    }

    public static double getDoubleVolatile(Object o, long offset) {
        return (double) invoke(getDoubleVolatileByObjectAndLong, o, offset);
    }

    public static void putDoubleVolatile(Object o, long offset, double x) {
        invoke(putDoubleVolatileByObjectAndOffsetAndLong, o, offset, x);
    }


    /**
     * object
     *
     * @param o
     * @param offset
     * @return
     */
    public static Object getObject(Object o, long offset) {
        return invoke(getObjectByObjectAndLong, o, offset);
    }

    public static Object getObjectVolatile(Object o, long offset) {
        return invoke(getObjectVolatile, o, offset);
    }

    public static void putObject(Object o, long offset, Object x) {
        invoke(putObject, o, offset, x);
    }

    public static void putObjectVolatile(Object o, long offset, Object x) {
        invoke(putObjectVolatile, o, offset, x);
    }

    public static void putOrderedObject(Object o, long offset, Object x) {
        invoke(putOrderedObject, o, offset, x);
    }

    /**
     * memory
     */
    public static long allocateMemory(long bytes) {
        return (long) invoke(allocateMemory, bytes);
    }

    public static long reallocateMemory(long address, long bytes) {
        return (long) invoke(reallocateMemory, address, bytes);
    }

    public static void freeMemory(long address) {
        invoke(freeMemory, address);
    }

    public static void setMemory(long address, long bytes, byte value) {
        invoke(setMemoryByAddressAndByteAndValue, address, bytes, value);
    }

    public static void setMemory(Object o, long offset, long bytes, byte value) {
        invoke(setMemoryByObjectAndOffsetAndBytesAndValue, o, offset, bytes, value);
    }

    public static void copyMemory(long srcAddress, long destAddress, long bytes) {
        invoke(copyMemoryBySrcAddressAndDestAddressAndBytes, srcAddress, destAddress, bytes);
    }

    public void copyMemory(Object srcBase, long srcOffset,
                           Object destBase, long destOffset,
                           long bytes) {
        invoke(copyMemoryBySrcBaseAndSrcAddressAndDesBaseAndDestAddressAndBytes, srcBase, srcOffset, destBase, destOffset, bytes);
    }

    public static void park(boolean isAbsolute, long time) {
        invoke(park, isAbsolute, time);
    }

    public static void unpark(Object thread) {
        invoke(unpark, thread);
    }

    public static int getLoadAverage(double[] loadavg, int nelems) {
        return (int) invoke(getLoadAverage, loadavg, nelems);
    }

    public static void loadFence() {
        invoke(loadFence);
    }

    public static void storeFence() {
        invoke(storeFence);
    }

    public static void fullFence() {
        invoke(fullFence);
    }

}
