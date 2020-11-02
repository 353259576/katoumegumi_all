package cn.katoumegumi.java.experiment;

public class TreeNode<T> {

    private T value;
    private Integer color;
    private TreeNode<T> parentTreeNode;
    private TreeNode<T> leftTreeNode;
    private TreeNode<T> rightTreeNode;

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public Integer getColor() {
        return color;
    }

    public void setColor(Integer color) {
        this.color = color;
    }

    public TreeNode<T> getParentTreeNode() {
        return parentTreeNode;
    }

    public void setParentTreeNode(TreeNode<T> parentTreeNode) {
        this.parentTreeNode = parentTreeNode;
    }

    public TreeNode<T> getLeftTreeNode() {
        return leftTreeNode;
    }

    public void setLeftTreeNode(TreeNode<T> leftTreeNode) {
        if (leftTreeNode != null) {
            leftTreeNode.setParentTreeNode(this);
        }
        this.leftTreeNode = leftTreeNode;
    }

    public TreeNode<T> getRightTreeNode() {
        return rightTreeNode;
    }

    public void setRightTreeNode(TreeNode<T> rightTreeNode) {
        if (rightTreeNode != null) {
            rightTreeNode.setParentTreeNode(this);
        }
        this.rightTreeNode = rightTreeNode;
    }
}
