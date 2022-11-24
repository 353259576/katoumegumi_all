package cn.katoumegumi.java.common.image;

/**
 * 边距
 */
public class WsMargin {

    private final int top;

    private final int bottom;

    private final int left;

    private final int right;

    public WsMargin(int top, int bottom, int left, int right) {
        this.top = top;
        this.bottom = bottom;
        this.left = left;
        this.right = right;
    }

    public int getTop() {
        return top;
    }

    public int getBottom() {
        return bottom;
    }

    public int getLeft() {
        return left;
    }

    public int getRight() {
        return right;
    }
}
