package cn.katoumegumi.java.experiment.test;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author ws
 */

public class LeetCodeTest {

    public static void main(String[] args) {
        /*TreeNode treeNode = new TreeNode(0);
        TreeNode treeNode1 = new TreeNode(-1);
        treeNode.left = treeNode1;
        int k = findBottomLeftValue1(treeNode);
        System.out.println(k);*/
        System.out.println(findRotateSteps1("caotmcaataijjxi","oatjiioicitatajtijciocjcaaxaaatmctxamacaamjjx"));
    }

    /**
     * 斐波那契数列
     * @param N
     * @return
     */
    public static int fib(int N){
        if(N < 2){
            return N;
        }
        int x0 = 0;
        int x1 = 1;
        int fibSum = 0;
        for(int i = 2;i <= N; i++){
            fibSum = x0 + x1;
            x0 = x1;
            x1 = fibSum;
        }
        return fibSum;
    }

    /**
     * 513. 找树左下角的值
     * @param root
     * @return
     */
    public static int lastDeep = 0;
    public static int leftValue;
    public static int findBottomLeftValue(TreeNode root) {
        int deep = 0;
        leftValue = root.val;
        if(root.left != null){

            findBottomLeftValue(root.left,deep + 1);
        }
        if(root.right != null){
            findBottomLeftValue(root.right,deep + 1);
        }
        return leftValue;
    }
    public static void findBottomLeftValue(TreeNode root,int deep) {
        if(deep > lastDeep){
            lastDeep = deep;
            leftValue = root.val;
        }
        if(root.left != null){
            findBottomLeftValue(root.left,deep + 1);
        }
        if(root.right != null){
            findBottomLeftValue(root.right,deep + 1);
        }
    }

    public static int findBottomLeftValue1(TreeNode root){
        Queue<TreeNode> queue = new LinkedList<>();
        queue.add(root);
        int value = 0;
        while (!queue.isEmpty()){
            value = queue.peek().val;
            int count = queue.size();
            while (count -- > 0){
                TreeNode treeNode = queue.poll();
                if(treeNode.left != null){
                    queue.add(treeNode.left);
                }
                if(treeNode.right != null){
                    queue.add(treeNode.right);
                }
            }
        }
        return value;
    }

    /**
     * 514. 自由之路
     * @param ring
     * @param key
     * @return
     */
    public static int findRotateSteps1(String ring, String key) {
        char[] rings=ring.toCharArray();
        List<Integer>[] rs=new List[26];
        for (int i=0;i<26;i++)rs[i]=new ArrayList<>();
        for (int i=0;i<rings.length;i++)
        {
            rs[rings[i]-'a'].add(i);
        }

        char[] keys=key.toCharArray();
        int[][] dp=new int[ring.length()+1][key.length()+1];
        for (int i=0;i<dp.length;i++) Arrays.fill(dp[i],-1);
        return slove(0,0,keys,rs,dp,ring.length());


    }

    private final static int MAX=Integer.MAX_VALUE>>1;
    private static int slove(int r_idx, int k_idx, char[] keys, List<Integer>[] rs, int[][] dp,final int rlen) {
        if(k_idx==keys.length){
            return 0;
        }
        if(dp[r_idx][k_idx]!=-1){
            return dp[r_idx][k_idx];
        }
        int res=MAX;
        for (int i:rs[keys[k_idx]-'a'])
        {
            int t=1+Math.min(Math.abs(r_idx-i),Math.min(Math.abs(i+rlen-r_idx),Math.abs(r_idx+rlen-i)));
            t+=slove(i,k_idx+1,keys,rs,dp,rlen);

            res=Math.min(res,t);
        }
        return dp[r_idx][k_idx]=res;
    }


    public static int findRotateSteps(String ring, String key) {
        int ringLength = ring.length();
        int keyLength = key.length();
        Queue<Queue<List<Integer>>> list = new LinkedList<>();
        Queue<List<Integer>> queue = new LinkedList<>();
        List<Integer> integers = new ArrayList<>();
        integers.add(0);
        integers.add(0);
        queue.add(integers);
        list.add(queue);
        for(int i = 0; i < keyLength; i++){
            char c = key.charAt(i);
            Queue<List<Integer>> listQueue = list.poll();
            Queue<List<Integer>> nextListQueue = new LinkedList<>();
            while (!listQueue.isEmpty()) {
                List<Integer> integerList = listQueue.poll();
                int nowIndex = integerList.get(0);
                int allNum = integerList.get(1);

                //逆时针
                int anticlockwise;
                int anticlockwiseIndex;
                anticlockwise = ring.indexOf(c, nowIndex);
                if (anticlockwise == -1) {
                    anticlockwise = ring.indexOf(c);
                    anticlockwiseIndex = anticlockwise;
                    anticlockwise = ringLength - nowIndex + anticlockwise;
                    anticlockwise++;
                } else {
                    anticlockwiseIndex = anticlockwise;
                    anticlockwise = anticlockwise - nowIndex;
                    anticlockwise++;
                }

                //顺时针
                int clockwise;
                int clockwiseIndex;
                clockwise = ring.substring(0, nowIndex).lastIndexOf(c);
                if (clockwise == -1) {
                    clockwise = ring.lastIndexOf(c);
                    clockwiseIndex = clockwise;
                    clockwise = nowIndex + ringLength - clockwise;
                    clockwise++;
                } else {
                    clockwiseIndex = clockwise;
                    clockwise = nowIndex - clockwise;
                    clockwise++;
                }
                System.out.println("顺时针："+clockwise +"步 逆时针："+anticlockwise+"步");
                //顺时针逆时针比较
                int anticlockwiseAllNum = allNum + anticlockwise;
                int clockwiseAllNum = allNum + clockwise;
                int difference = clockwiseAllNum - anticlockwiseAllNum;
                if (difference > 0) {
                    System.out.println("逆时针走：" + anticlockwise + "步");
                    //nowIndex = anticlockwiseIndex;
                    //allNum = anticlockwiseAllNum;
                    List<Integer> anticlockwiseList = new ArrayList<>();
                    anticlockwiseList.add(anticlockwiseIndex);
                    anticlockwiseList.add(anticlockwiseAllNum);
                    nextListQueue.add(anticlockwiseList);
                } else if (difference < 0) {
                    System.out.println("顺时针走：" + clockwise + "步");
                    //nowIndex = clockwiseIndex;
                    //allNum = clockwiseAllNum;
                    List<Integer> clockwiseList = new ArrayList<>();
                    clockwiseList.add(clockwiseIndex);
                    clockwiseList.add(clockwiseAllNum);
                    nextListQueue.add(clockwiseList);

                } else {
                    //rotationType = 3;
                    //oldAllNum = anticlockwiseAllNum;
                    //oldIndex = anticlockwiseIndex;
                    System.out.println("相同");

                    List<Integer> anticlockwiseList = new ArrayList<>();
                    anticlockwiseList.add(anticlockwiseIndex);
                    anticlockwiseList.add(anticlockwiseAllNum);
                    nextListQueue.add(anticlockwiseList);

                    List<Integer> clockwiseList = new ArrayList<>();
                    clockwiseList.add(clockwiseIndex);
                    clockwiseList.add(clockwiseAllNum);
                    nextListQueue.add(clockwiseList);
                }
            }
            list.add(nextListQueue);
        }
        queue = list.poll();
        int allNum = -1;
        while (!queue.isEmpty()){
            integers = queue.poll();
            if(allNum == -1){
                allNum = integers.get(1);
            }else {
                if(allNum > integers.get(1)){
                    allNum = integers.get(1);
                }
            }

        }
        return allNum;
    }




}
