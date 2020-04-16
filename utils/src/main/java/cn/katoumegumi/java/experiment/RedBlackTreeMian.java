package cn.katoumegumi.java.experiment;

public class RedBlackTreeMian {


    public static void main(String[] args) {
        TreeNode<String> treeNodeP = new TreeNode<>();
        treeNodeP.setValue("p");
        TreeNode<String> treeNodeF = new TreeNode<>();
        treeNodeF.setValue("F");
        treeNodeP.setLeftTreeNode(treeNodeF);
        TreeNode<String> treeNodeV = new TreeNode<>();
        treeNodeV.setValue("V");
        treeNodeP.setRightTreeNode(treeNodeV);
        /*TreeNode<String> treeNodeR = new TreeNode<>();
        treeNodeR.setValue("R");
        treeNodeV.setLeftTreeNode(treeNodeR);
        TreeNode<String> treeNodeX = new TreeNode<>();
        treeNodeX.setValue("X");
        treeNodeV.setRightTreeNode(treeNodeX);*/
        RedBlackTreeMian redBlackTreeMian = new RedBlackTreeMian();
        TreeNode<String> treeNode = redBlackTreeMian.rightRotation(treeNodeP);
        //treeNode = redBlackTreeMian.leftRotation(treeNode);
        System.out.println(treeNode.getValue());
    }

    /**
     * 树右旋
     *
     * @param treeNode
     */
    public <T> TreeNode<T> rightRotation(TreeNode<T> treeNode) {
        TreeNode<T> parentTreeNode = treeNode.getParentTreeNode();
        //boolean isRoot = parentTreeNode==null;
        TreeNode<T> leftTreeNode = treeNode.getLeftTreeNode();
        TreeNode<T> leftRightTreeNode = leftTreeNode == null ? null : leftTreeNode.getRightTreeNode();
        treeNode.setLeftTreeNode(leftRightTreeNode);
        leftTreeNode.setRightTreeNode(treeNode);
        if (parentTreeNode != null) {
            //boolean isParentRight = parentTreeNode.getRightTreeNode().equals(treeNode);
            if (parentTreeNode.getRightTreeNode().equals(treeNode)) {
                parentTreeNode.setRightTreeNode(leftTreeNode);
            } else {
                parentTreeNode.setLeftTreeNode(leftTreeNode);
            }
        } else {
            leftTreeNode.setParentTreeNode(null);
        }
        return leftTreeNode;
    }

    /**
     * 树左旋
     *
     * @param treeNode
     */
    public <T> TreeNode<T> leftRotation(TreeNode<T> treeNode) {
        TreeNode<T> parentTreeNode = treeNode.getParentTreeNode();
        //boolean isRoot = parentTreeNode==null;
        TreeNode<T> rightTreeNode = treeNode.getRightTreeNode();
        TreeNode<T> rightLeftTreeNode = rightTreeNode == null ? null : rightTreeNode.getLeftTreeNode();
        treeNode.setRightTreeNode(rightLeftTreeNode);
        rightTreeNode.setLeftTreeNode(treeNode);
        if (parentTreeNode != null) {
            //boolean isParentRight = parentTreeNode.getRightTreeNode().equals(treeNode);
            if (parentTreeNode.getRightTreeNode().equals(treeNode)) {
                parentTreeNode.setRightTreeNode(rightTreeNode);
            } else {
                parentTreeNode.setLeftTreeNode(rightLeftTreeNode);
            }
        } else {
            rightTreeNode.setParentTreeNode(null);
        }
        return rightTreeNode;
    }

    public void changeColor(TreeNode treeNode) {

    }

}
