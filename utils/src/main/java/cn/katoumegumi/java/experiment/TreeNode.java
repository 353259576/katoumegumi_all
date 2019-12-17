package cn.katoumegumi.java.experiment;

import lombok.Data;

@Data
public class TreeNode<T> {

    private T value;
    private Integer color;
    private TreeNode<T> parentTreeNode;
    private TreeNode<T> leftTreeNode;
    private TreeNode<T> rightTreeNode;


    public void setLeftTreeNode(TreeNode<T> leftTreeNode) {
        if(leftTreeNode != null){
            leftTreeNode.setParentTreeNode(this);
        }
        this.leftTreeNode = leftTreeNode;
    }

    public void setRightTreeNode(TreeNode<T> rightTreeNode) {
        if(rightTreeNode != null){
            rightTreeNode.setParentTreeNode(this);
        }
        this.rightTreeNode = rightTreeNode;
    }
}
