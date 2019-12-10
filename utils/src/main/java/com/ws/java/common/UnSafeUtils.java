package com.ws.java.common;



import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class UnSafeUtils {

    private volatile Integer k = 0;

    /*private static Unsafe unsafe;

    static {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            unsafe = (Unsafe) field.get(field);
            field.setAccessible(false);
        }catch (NoSuchFieldException|IllegalAccessException e){
            e.printStackTrace();
        }

    }*/

    /*public static void main(String[] args) throws Exception{
        Unsafe unsafe = UnSafeUtils.getUnsafe();
        System.out.println(unsafe);
        UnSafeUtils unSafeUtils = new UnSafeUtils();
        Field field = unSafeUtils.getClass().getDeclaredField("k");
        long address = unsafe.objectFieldOffset(field);
        //unSafeUtils.k = unSafeUtils.k+1;


        System.out.println(unSafeUtils.add(address));

        long startTime = System.nanoTime();
        long address = unsafe.allocateMemory(4);
        int addressSize = unsafe.addressSize();
        System.out.println(addressSize);
        byte bytes[] = new byte[100000];
        for(int i = 0; i < 100000; i++){
            bytes[i] = unsafe.getByte(address);
            address += 1;
        }
        System.out.println(new String(bytes,"ASCII"));
        int k = unsafe.getInt(address);
        //int k = 20;
        System.out.println(k);
        long endTime = System.nanoTime();
        System.out.println(endTime - startTime);
        unsafe.freeMemory(address);
        k = unsafe.getInt(address);
        System.out.println(k);
        File file = (File) unsafe.allocateInstance(File.class);

        System.out.println(file);
    }*/

    /*public int add(long address){
        int prev,next;
        do {
            prev = k;
            next = prev+3;
        }while (!unsafe.compareAndSwapObject(this,address,prev,next));
        return k;
    }

    public static Unsafe getUnsafe(){
        return unsafe;
    }*/
}
