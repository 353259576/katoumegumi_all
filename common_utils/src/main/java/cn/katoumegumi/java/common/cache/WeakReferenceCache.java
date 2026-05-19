package cn.katoumegumi.java.common.cache;

import cn.katoumegumi.java.common.model.KeyValue;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Map;

public class WeakReferenceCache <K,V> extends ReferenceCache<K,V>{

    @Override
    public Reference<V> createReferenceValue(V v, ReferenceQueue<V> referenceQueue) {
        return new WeakReference<>(v,referenceQueue);
    }

    public static void main(String[] args) throws InterruptedException {
        WeakReferenceCache<String, KeyValue<Integer,Integer>> weakReferenceCache = new WeakReferenceCache<>();
        weakReferenceCache.put("1",new KeyValue<>(1,1));
        weakReferenceCache.put("2",new KeyValue<>(2,2));
        weakReferenceCache.put("3",new KeyValue<>(3,3));
        System.gc();
        Thread.sleep(10000);
        System.gc();
        System.gc();
        System.out.println(weakReferenceCache.get("1"));
        System.out.println(weakReferenceCache.get("2"));
        System.out.println(weakReferenceCache.get("3"));
        weakReferenceCache.put("1",new KeyValue<>(111,111));
        System.out.println(weakReferenceCache.get("1"));
    }
}
