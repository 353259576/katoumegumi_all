package cn.katoumegumi.java.common.cache;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;

public class SoftReferenceCache<K,V> extends ReferenceCache<K,V>{

    @Override
    public Reference<V> createReferenceValue(V v, ReferenceQueue<V> referenceQueue) {
        return new SoftReference<>(v,referenceQueue);
    }
}
