package cn.katoumegumi.java.common.model;

/**
 * 三元组
 */
public class TripleEntity <M, L, R>{

    private final M key;

    private final L left;

    private final R right;

    public TripleEntity(M key, L left, R right) {
        this.key = key;
        this.left = left;
        this.right = right;
    }

    public M getKey() {
        return key;
    }

    public L getLeft() {
        return left;
    }

    public R getRight() {
        return right;
    }
}
