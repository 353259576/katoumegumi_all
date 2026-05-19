package cn.katoumegumi.java.common;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WeakReferenceCache <K,V>{

    private final Map<K, Reference<? extends V>> cacheMap;

    private final Map<Reference<? extends V>,K> cacheIndexMap;

    private final ReferenceQueue<V> expiredWeekRefQueue;

    public WeakReferenceCache() {
        this.cacheMap = new ConcurrentHashMap<>();
        this.cacheIndexMap = new ConcurrentHashMap<>();
        this.expiredWeekRefQueue = new ReferenceQueue<>();
    }

    public V get(K k) {
        cleanExpiredCache();
        if (k == null) {
            return null;
        }
        Reference<? extends V> reference = cacheMap.get(k);
        V v;
        if (reference == null || (v = reference.get()) == null) {
            return null;
        }
        return v;
    }

    public boolean put(K k,V v) {
        cleanExpiredCache();
        if (k == null || v == null) {
            return false;
        }
        Reference<? extends V> reference = cacheMap.get(k);
        if (reference != null) {
            cacheIndexMap.remove(reference);
            cacheMap.remove(k);
        }
        reference = new WeakReference<>(v,this.expiredWeekRefQueue);
        cacheMap.put(k,reference);
        cacheIndexMap.put(reference,k);
        return true;
    }


    public int size() {
        cleanExpiredCache();
        return this.cacheMap.size();
    }

    public V remove(K k) {
        cleanExpiredCache();
        if (k == null) {
            return null;
        }
        Reference<? extends V> exist = cacheMap.remove(k);
        if (exist == null) {
            return null;
        }
        cacheIndexMap.remove(exist);
        return exist.get();
    }

    public int cleanExpiredCache() {
        int count = 0;
        for (Reference<? extends V> v = expiredWeekRefQueue.poll(); v != null; v = expiredWeekRefQueue.poll()) {
            K k = cacheIndexMap.remove(v);
            if (k == null) {
                continue;
            }
            cacheMap.remove(k);
            count++;
        }
        return count;
    }
}
