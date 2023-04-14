package cn.katoumegumi.java.common.model;

/**
 * 三元组
 */
public class TripleEntity <K,V1,V2>{

    private final K key;

    private final V1 value1;

    private final V2 value2;

    public TripleEntity(K key, V1 value1, V2 value2) {
        this.key = key;
        this.value1 = value1;
        this.value2 = value2;
    }

    public K getKey() {
        return key;
    }

    public V1 getValue1() {
        return value1;
    }

    public V2 getValue2() {
        return value2;
    }
}
