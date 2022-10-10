package cn.katoumegumi.java.common;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

public class WsUnsafeUtils {

    static class UnsafeUtils {
        private static Object unsafe;

        static {
            try {
                Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
                Field f = unsafeClass.getDeclaredField("theUnsafe");
                f.setAccessible(true);
                unsafe = f.get(null);
            } catch (NoSuchFieldException | IllegalAccessException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private static final Object unsafe = UnsafeUtils.unsafe;

    private static final Method objectFieldOffset = getMethod("objectFieldOffset", Field.class);

    private static final Method staticFieldOffset = getMethod("staticFieldOffset", Field.class);

    private static final Method arrayBaseOffset = getMethod("arrayBaseOffset", Class.class);

    private static final Method allocateInstance = getMethod("allocateInstance", Class.class);

    private static final Method arrayIndexScale = getMethod("arrayIndexScale", Class.class);

    /**
     * object
     */
    private static final Method getObjectByObjectAndLong = getMethod("getObject", Object.class, long.class);
    private static final Method putObject = getMethod("putObject", Object.class, long.class, Object.class);
    private static final Method getObjectVolatile = getMethod("getObjectVolatile", Object.class, long.class);
    private static final Method putObjectVolatile = getMethod("putObjectVolatile", Object.class, long.class, Object.class);
    private static final Method putOrderedObject = getMethod("putOrderedObject", Object.class, long.class, Object.class);

    /**
     * int
     */
    private static final Method getIntByObjectAndLong = getMethod("getInt", Object.class, long.class);
    private static final Method putIntByObjectAndOffsetAndLong = getMethod("putInt", Object.class, long.class, int.class);
    private static final Method getIntVolatileByObjectAndLong = getMethod("getIntVolatile", Object.class, long.class);
    private static final Method putIntVolatileByObjectAndOffsetAndLong = getMethod("putIntVolatile", Object.class, long.class, int.class);

    /**
     * byte
     */
    private static final Method getByteByObjectAndLong = getMethod("getByte", Object.class, long.class);
    private static final Method putByteByObjectAndOffsetAndLong = getMethod("putByte", Object.class, long.class, byte.class);
    private static final Method getByteVolatileByObjectAndLong = getMethod("getByteVolatile", Object.class, long.class);
    private static final Method putByteVolatileByObjectAndOffsetAndLong = getMethod("putByteVolatile", Object.class, long.class, byte.class);

    /**
     * boolean
     */
    private static final Method getBooleanByObjectAndLong = getMethod("getBoolean", Object.class, long.class);
    private static final Method putBooleanByObjectAndOffsetAndLong = getMethod("putBoolean", Object.class, long.class, boolean.class);
    private static final Method getBooleanVolatileByObjectAndLong = getMethod("getBooleanVolatile", Object.class, long.class);
    private static final Method putBooleanVolatileByObjectAndOffsetAndLong = getMethod("putBooleanVolatile", Object.class, long.class, boolean.class);
    /**
     * char
     */
    private static final Method getCharByObjectAndLong = getMethod("getChar", Object.class, long.class);
    private static final Method putCharByObjectAndOffsetAndLong = getMethod("putChar", Object.class, long.class, char.class);
    private static final Method getCharVolatileByObjectAndLong = getMethod("getCharVolatile", Object.class, long.class);
    private static final Method putCharVolatileByObjectAndOffsetAndLong = getMethod("putCharVolatile", Object.class, long.class, char.class);
    /**
     * short
     */
    private static final Method getShortByObjectAndLong = getMethod("getShort", Object.class, long.class);
    private static final Method putShortByObjectAndOffsetAndLong = getMethod("putShort", Object.class, long.class, short.class);
    private static final Method getShortVolatileByObjectAndLong = getMethod("getShortVolatile", Object.class, long.class);
    private static final Method putShortVolatileByObjectAndOffsetAndLong = getMethod("putShortVolatile", Object.class, long.class, short.class);
    /**
     * long
     */
    private static final Method getLongByObjectAndLong = getMethod("getLong", Object.class, long.class);
    private static final Method putLongByObjectAndOffsetAndLong = getMethod("putLong", Object.class, long.class, long.class);
    private static final Method getLongVolatileByObjectAndLong = getMethod("getLongVolatile", Object.class, long.class);
    private static final Method putLongVolatileByObjectAndOffsetAndLong = getMethod("putLongVolatile", Object.class, long.class, long.class);
    /**
     * float
     */
    private static final Method getFloatByObjectAndLong = getMethod("getFloat", Object.class, long.class);
    private static final Method putFloatByObjectAndOffsetAndLong = getMethod("putFloat", Object.class, long.class, float.class);
    private static final Method getFloatVolatileByObjectAndLong = getMethod("getFloatVolatile", Object.class, long.class);
    private static final Method putFloatVolatileByObjectAndOffsetAndLong = getMethod("putFloatVolatile", Object.class, long.class, float.class);
    /**
     * double
     */
    private static final Method getDoubleByObjectAndLong = getMethod("getDouble", Object.class, long.class);
    private static final Method putDoubleByObjectAndOffsetAndLong = getMethod("putDouble", Object.class, long.class, double.class);
    private static final Method getDoubleVolatileByObjectAndLong = getMethod("getDoubleVolatile", Object.class, long.class);
    private static final Method putDoubleVolatileByObjectAndOffsetAndLong = getMethod("putDoubleVolatile", Object.class, long.class, double.class);

    /**
     * cas
     */
    private static final Method compareAndSwapObject = getMethod("compareAndSwapObject", Object.class, long.class, Object.class, Object.class);
    private static final Method compareAndSwapInt = getMethod("compareAndSwapInt", Object.class, long.class, int.class, int.class);
    private static final Method compareAndSwapLong = getMethod("compareAndSwapLong", Object.class, long.class, long.class, long.class);

    private static final Method getAndAddInt = getMethod("getAndAddInt", Object.class, long.class, int.class);
    private static final Method getAndAddLong = getMethod("getAndAddLong", Object.class, long.class, long.class);
    private static final Method getAndSetInt = getMethod("getAndSetInt", Object.class, long.class, int.class);
    private static final Method getAndSetLong = getMethod("getAndSetLong", Object.class, long.class, long.class);
    private static final Method getAndSetObject = getMethod("getAndSetObject", Object.class, long.class, Object.class);

    /**
     * memory
     */
    private static final Method allocateMemory = getMethod("allocateMemory", long.class);
    private static final Method reallocateMemory = getMethod("reallocateMemory", long.class, long.class);
    private static final Method freeMemory = getMethod("freeMemory", long.class);
    private static final Method setMemoryByAddressAndByteAndValue = getMethod("setMemory", long.class, long.class, byte.class);
    private static final Method setMemoryByObjectAndOffsetAndBytesAndValue = getMethod("setMemory", Object.class, long.class, long.class, byte.class);
    private static final Method copyMemoryBySrcAddressAndDestAddressAndBytes = getMethod("copyMemory", long.class, long.class, long.class);
    private static final Method copyMemoryBySrcBaseAndSrcAddressAndDesBaseAndDestAddressAndBytes = getMethod("copyMemory", Object.class, long.class, Object.class, long.class, long.class);
    private static final Method addressSize = getMethod("addressSize");
    private static final Method pageSize = getMethod("pageSize");

    private static final Method throwException = getMethod("throwException", Throwable.class);


    private static final Method park = getMethod("park", boolean.class, long.class);
    private static final Method unpark = getMethod("unpark", Object.class);
    private static final Method getLoadAverage = getMethod("getLoadAverage", double[].class, int.class);

    private static final Method loadFence = getMethod("loadFence");
    private static final Method storeFence = getMethod("storeFence");
    private static final Method fullFence = getMethod("fullFence");

    private static final Method invokeCleaner = getMethod("invokeCleaner", ByteBuffer.class);


    private static Method getMethod(String methodName, Class<?>... parameterTypes) {
        if (unsafe == null) {
            return null;
        }
        try {
            return unsafe.getClass().getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Object invoke(Method method, Object... objects) {
        assert method != null;
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
     * cas
     */

    public final boolean compareAndSwapObject(Object o, long offset,
                                              Object expected,
                                              Object x) {
        return (boolean) invoke(compareAndSwapObject, o, offset, expected, x);
    }

    public final boolean compareAndSwapInt(Object o, long offset,
                                           int expected,
                                           int x) {
        return (boolean) invoke(compareAndSwapInt, o, offset, expected, x);
    }

    public final boolean compareAndSwapLong(Object o, long offset,
                                            long expected,
                                            long x) {
        return (boolean) invoke(compareAndSwapLong, o, offset, expected, x);
    }

    public final int getAndAddInt(Object o, long offset, int delta) {
        return (int) invoke(getAndAddInt, o, offset, delta);
    }

    public final long getAndAddLong(Object o, long offset, long delta) {
        return (long) invoke(getAndAddLong, o, offset, delta);
    }

    public final int getAndSetInt(Object o, long offset, int delta) {
        return (int) invoke(getAndSetInt, o, offset, delta);
    }

    public final long getAndSetLong(Object o, long offset, long delta) {
        return (long) invoke(getAndSetLong, o, offset, delta);
    }

    public final Object getAndSetObject(Object o, long offset, Object delta) {
        return invoke(getAndSetObject, o, offset, delta);
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

    public static int addressSize() {
        return (int) invoke(addressSize);
    }

    public static int pageSize() {
        return (int) invoke(pageSize);
    }

    public static void throwException(Throwable ee) {
        invoke(throwException, ee);
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


    public void invokeCleaner(java.nio.ByteBuffer directBuffer) {
        invoke(invokeCleaner, directBuffer);
    }

}
