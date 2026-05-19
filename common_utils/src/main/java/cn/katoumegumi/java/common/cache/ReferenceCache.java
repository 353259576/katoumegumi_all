package cn.katoumegumi.java.common.cache;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ReferenceCache<K,V>{

    private final Map<K, Reference<? extends V>> cacheMap;

    private final Map<Reference<? extends V>,K> cacheIndexMap;

    private final ReferenceQueue<V> expiredRefQueue;

    public ReferenceCache() {
        this.cacheMap = new ConcurrentHashMap<>();
        this.cacheIndexMap = new ConcurrentHashMap<>();
        this.expiredRefQueue = new ReferenceQueue<>();
    }

    public V get(K k) {
        cleanExpiredCache();
        if (k == null) {
            return null;
        }
        Reference<? extends V> reference = cacheMap.get(k);
        if (reference == null) {
            return null;
        }
        V v = reference.get();
        if (v == null) {
            return null;
        }
        cacheMap.computeIfPresent(k,(key,oldValue) ->{
            if (oldValue.get() == null) {
                cacheIndexMap.remove(oldValue);
                return null;
            }
            return oldValue;
        });
        return v;
    }

    public boolean put(K k,V v) {
        if (k == null || v == null) {
            return false;
        }
        Reference<V> newReference = createReferenceValue(v,this.expiredRefQueue);
        cacheMap.compute(k,(key,oldValue) ->{
            if (oldValue != null) {
                cacheIndexMap.remove(oldValue);
            }
            cacheIndexMap.put(newReference,key);
            return newReference;
        });
        cleanExpiredCache();
        return true;
    }


    public int size() {
        return this.cacheMap.size();
    }

    public V remove(K k) {
        if (k == null) {
            return null;
        }
        Reference<? extends V>[] hold = new Reference[1];
        cacheMap.compute(k,(key,oldValue) ->{
            if (oldValue != null) {
                cacheIndexMap.remove(oldValue);
                hold[0] = oldValue;
            }
            return null;
        });
        return hold[0] == null ? null : hold[0].get();
    }

    public int cleanExpiredCache() {
        int count = 0;
        for (Reference<? extends V> v = expiredRefQueue.poll(); v != null; v = expiredRefQueue.poll()) {
            K k = cacheIndexMap.remove(v);
            if (k == null) {
                continue;
            }
            Reference<? extends V> finalV = v;
            cacheMap.compute(k,(key, oldValue)->{
               if (oldValue == finalV) {
                   return null;
               }
               return oldValue;
            });
            count++;
        }
        return count;
    }
    protected abstract Reference<V> createReferenceValue(V v,ReferenceQueue<V> referenceQueue);
}
