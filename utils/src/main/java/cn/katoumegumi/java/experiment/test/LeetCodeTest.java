package cn.katoumegumi.java.experiment.test;

import com.alibaba.fastjson.JSON;

import java.util.*;

/**
 * @author ws
 */

public class LeetCodeTest {

    private final static int MAX = Integer.MAX_VALUE >> 1;
    /**
     * 513. 找树左下角的值
     *
     * @param root
     * @return
     */
    public static int lastDeep = 0;
    public static int leftValue;

    public static void main(String[] args) {
        //repeatedSubstringPattern("1434");
        /*int i = totalNQueens(1);
        System.out.println(i);*/
        boolean k = backspaceCompare("","");
        System.out.println(k);
    }

    /**
     * 斐波那契数列
     *
     * @param N
     * @return
     */
    public static int fib(int N) {
        if (N < 2) {
            return N;
        }
        int x0 = 0;
        int x1 = 1;
        int fibSum = 0;
        for (int i = 2; i <= N; i++) {
            fibSum = x0 + x1;
            x0 = x1;
            x1 = fibSum;
        }
        return fibSum;
    }

    public static int findBottomLeftValue(TreeNode root) {
        int deep = 0;
        leftValue = root.val;
        if (root.left != null) {

            findBottomLeftValue(root.left, deep + 1);
        }
        if (root.right != null) {
            findBottomLeftValue(root.right, deep + 1);
        }
        return leftValue;
    }

    public static void findBottomLeftValue(TreeNode root, int deep) {
        if (deep > lastDeep) {
            lastDeep = deep;
            leftValue = root.val;
        }
        if (root.left != null) {
            findBottomLeftValue(root.left, deep + 1);
        }
        if (root.right != null) {
            findBottomLeftValue(root.right, deep + 1);
        }
    }

    public static int findBottomLeftValue1(TreeNode root) {
        Queue<TreeNode> queue = new LinkedList<>();
        queue.add(root);
        int value = 0;
        while (!queue.isEmpty()) {
            value = queue.peek().val;
            int count = queue.size();
            while (count-- > 0) {
                TreeNode treeNode = queue.poll();
                if (treeNode.left != null) {
                    queue.add(treeNode.left);
                }
                if (treeNode.right != null) {
                    queue.add(treeNode.right);
                }
            }
        }
        return value;
    }

    /**
     * 514. 自由之路
     *
     * @param ring
     * @param key
     * @return
     */
    public static int findRotateSteps1(String ring, String key) {
        char[] rings = ring.toCharArray();
        List<Integer>[] rs = new ArrayList[26];
        for (int i = 0; i < 26; i++) {
            rs[i] = new ArrayList<>();
        }
        for (int i = 0; i < rings.length; i++) {
            rs[rings[i] - 'a'].add(i);
        }

        char[] keys = key.toCharArray();
        int[][] dp = new int[ring.length()][key.length()];
        for (int i = 0; i < dp.length; i++) {
            Arrays.fill(dp[i], -1);
        }
        return slove(0, 0, keys, rs, dp, ring.length());


    }

    private static int slove(int r_idx, int k_idx, char[] keys, List<Integer>[] rs, int[][] dp, final int rlen) {
        if (k_idx == keys.length) {
            return 0;
        }
        if (dp[r_idx][k_idx] != -1) {
            return dp[r_idx][k_idx];
        }
        int res = MAX;
        for (int i : rs[keys[k_idx] - 'a']) {
            int t = 1 + Math.min(Math.abs(r_idx - i), Math.min(Math.abs(i + rlen - r_idx), Math.abs(r_idx + rlen - i)));
            t += slove(i, k_idx + 1, keys, rs, dp, rlen);

            res = Math.min(res, t);
        }
        if (r_idx == 0 && r_idx == 0) {
            System.out.println(JSON.toJSONString(dp));
        }
        return dp[r_idx][k_idx] = res;
    }


    public static int findRotateSteps(String ring, String key) {
        Map<Integer, List<Integer>> locationMap = new HashMap<>();
        int ringLength = ring.length();
        int keyLength = key.length();
        char[] ringChars = ring.toCharArray();
        char[] keyChars = key.toCharArray();
        for (int i = 0; i < ringLength; i++) {
            int index = ringChars[i] - 'a';
            List<Integer> list = locationMap.computeIfAbsent(index, k -> new ArrayList<>());
            list.add(i);
        }
        return 0;


        /*int ringLength = ring.length();
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
            System.out.println("处理字符："+c+"****************************");
            while (!listQueue.isEmpty()) {
                List<Integer> integerList = listQueue.poll();
                int nowIndex = integerList.get(0);
                int allNum = integerList.get(1);
                System.out.println("当前总步数未："+allNum);

                if(ring.charAt(nowIndex) == c){
                    System.out.println("不变"+allNum+"+1="+(allNum+1));
                    allNum++;
                    List<Integer> unalteredList = new ArrayList<>();
                    unalteredList.add(nowIndex);
                    unalteredList.add(allNum);
                    nextListQueue.add(unalteredList);
                }else {
                    int anticlockwise;
                    int anticlockwiseIndex;
                    //逆时针
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
                    //System.out.println("顺时针："+clockwise +"步 逆时针："+anticlockwise+"步");
                    //顺时针逆时针比较
                    int anticlockwiseAllNum = allNum + anticlockwise;
                    int clockwiseAllNum = allNum + clockwise;
                    int difference = clockwiseAllNum - anticlockwiseAllNum;
                    if (difference > 0) {
                        System.out.println("从点"+nowIndex+"逆时针走：" + anticlockwise + "步");
                        List<Integer> anticlockwiseList = new ArrayList<>();
                        anticlockwiseList.add(anticlockwiseIndex);
                        anticlockwiseList.add(anticlockwiseAllNum);
                        nextListQueue.add(anticlockwiseList);
                    } else if (difference < 0) {
                        System.out.println("从点"+nowIndex+"顺时针走：" + clockwise + "步");
                        List<Integer> clockwiseList = new ArrayList<>();
                        clockwiseList.add(clockwiseIndex);
                        clockwiseList.add(clockwiseAllNum);
                        nextListQueue.add(clockwiseList);

                    } else {
                        //rotationType = 3;
                        //oldAllNum = anticlockwiseAllNum;
                        //oldIndex = anticlockwiseIndex;
                        System.out.println("相同");
                        System.out.println("从点"+nowIndex+"逆时针走：" + anticlockwise + "步");
                        System.out.println("从点"+nowIndex+"顺时针走：" + clockwise + "步");

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
        return allNum;*/
    }


    /**
     * 70.爬楼梯
     *
     * @param n
     * @return
     */
    //记忆
    public static int climbStairs(int n) {
        //f(n) = f(n-1) + f(n-2)
        int dp[] = new int[n + 1];
        dp[0] = 1;
        dp[1] = 2;
        for (int i = 2; i < n; i++) {
            dp[i] = dp[i - 1] + dp[i - 2];
        }
        return dp[n - 1];

    }


    public static int climbStairs1(int n) {
        int[] steps = new int[n + 1];
        int nowStairs = 0;
        return climbStairsRecursion(nowStairs, n, steps);
    }

    public static int climbStairsRecursion(int nowStairs, int n, int[] steps) {
        if (nowStairs > n) {
            return 0;
        }
        if (nowStairs == n) {
            return 1;
        }
        if (steps[nowStairs] > 0) {
            return steps[nowStairs];
        }
        return steps[nowStairs] = climbStairsRecursion(nowStairs + 1, n, steps) + climbStairsRecursion(nowStairs + 2, n, steps);
    }

    /**
     * 338. 比特位计数
     *
     * @param num
     * @return
     */
    public static int[] countBits(int num) {
        int[] dp = new int[num + 1];
        for (int i = 0; i <= num; i++) {
            dp[i] = (dp[i >> 1]) + (i & 1);
        }
        return dp;
    }

    /**
     * 面试题46. 把数字翻译成字符串
     *
     * @param num
     * @return
     */
    public static int translateNum(int num) {
        int i1 = 0, i2 = 0, i3 = 1;

        int lastN = -1;
        int n = num % 10;
        num = num / 10;
        while (num > 0 || n > 0) {
            i1 = i2;
            i2 = i3;
            i3 = 0;
            i3 += i2;
            if (n > 0) {
                if (lastN > -1) {
                    if ((lastN < 6 && n < 3) || (lastN > 5 && n < 2)) {
                        i3 += i1;
                    }
                }


            }
            lastN = n;
            n = num % 10;
            num = num / 10;

        }
        return i3;


    }

    /**
     * 62.不同路径
     *
     * @param m
     * @param n
     * @return
     */
    public int uniquePaths(int m, int n) {
        int[][] dp = new int[n][m];
        //f(m,n) = f(n-1,m) + f(n,m-1);
        for (int i = 0; i < m; i++) {
            dp[0][i] = 1;
        }
        for (int i = 0; i < n; i++) {
            dp[i][0] = 1;
        }
        for (int i = 1; i < m; i++) {
            for (int j = 1; j < n; j++) {
                dp[j][i] = dp[j - 1][i] + dp[j][i - 1];
            }
        }
        return dp[n - 1][m - 1];
    }

    /**
     * 5.最长回文子串
     */
    /*public static String longestPalindrome(String s) {
    }*/

    /**
     * 64. 最小路径和
     *
     * @param grid
     * @return
     */
    public int minPathSum(int[][] grid) {
        //f(m,n) = min{f(m-1,n),f(m,n-1)}
        int n = grid.length;
        int m = grid[0].length;
        int dp[][] = new int[n][m];
        dp[0][0] = grid[0][0];
        for (int i = 1; i < n; i++) {
            dp[i][0] = dp[i - 1][0] + grid[i][0];
        }

        for (int i = 1; i < m; i++) {
            dp[0][i] = dp[0][i - 1] + grid[0][i];
        }

        for (int i = 1; i < n; i++) {
            for (int j = 1; j < m; j++) {
                dp[i][j] = Math.min(dp[i - 1][j], dp[i][j - 1]) + grid[i][j];
            }
        }
        return dp[n - 1][m - 1];
    }

    /**
     * 95. 不同的二叉搜索树 II
     * @param n
     * @return
     */
    public static List<TreeNode> generateTrees(int n) {

        if(n == 0){
            return new LinkedList<TreeNode>();
        }

        return generateTrees(1,n);
    }


    public static List<TreeNode> generateTrees(int start,int end) {
        List<TreeNode> treeNodeList = new LinkedList<>();
        if(start > end){
            treeNodeList.add(null);
            return treeNodeList;
        }

        for(int i = start; i <= end; i++){

            List<TreeNode> leftList = generateTrees(start,i - 1);
            //leftList.add(null);

            List<TreeNode> rightList = generateTrees(i + 1,end);

            for(TreeNode left:leftList){
                for(TreeNode right:rightList){
                    TreeNode treeNode = new TreeNode(i);
                    treeNode.left = left;
                    treeNode.right = right;
                    treeNodeList.add(treeNode);
                }

            }

        }
        return treeNodeList;
    }

    /**
     * 459. 重复的子字符串
     * @param s
     * @return
     */
    public static boolean repeatedSubstringPattern(String s) {
        int n = s.length();
        for(int i = 1; i <= n/2; ++i){
            if(n % i == 0){
                boolean k = true;
                for(int j = i; j < n; ++j){
                    if(s.charAt(j) != s.charAt(j-i)){
                        k = false;
                        break;
                    }
                }
                if(k){
                    return k;
                }
            }
        }

        return false;
    }


    /**
     * 530. 二叉搜索树的最小绝对差
     * @param root
     * @return
     */
    public int minimumDifference = Integer.MAX_VALUE;
    public TreeNode preMinimumDifferenceTreeNode = null;
    public int getMinimumDifference(TreeNode root) {
        getMinimumDifference(root,null);
        return minimumDifference;
    }


    public void getMinimumDifference(TreeNode local,TreeNode prev){
        if(local == null){
            return;
        }
        getMinimumDifference(local.left,local);
        if(preMinimumDifferenceTreeNode != null){
            minimumDifference = Math.min(minimumDifference,Math.abs(preMinimumDifferenceTreeNode.val - local.val));
            preMinimumDifferenceTreeNode = local;
        }else {
            preMinimumDifferenceTreeNode = local;
        }
        getMinimumDifference(local.right,local);
    }


    /**
     * 977. 有序数组的平方
     * @param A
     * @return
     */
    public int[] sortedSquares(int[] A) {
        int[] returnArray = new int[A.length];
        int max = A.length - 1;
        int start = 0;
        for(int i = 0; i < A.length; i++){
            start = i;
            if(A[i] >= 0){
                break;
            }
        }
        int left = start - 1;
        int index = 0;
        while (left >= 0 || start <= max){
            int l,r;
            if(left < 0){
                l = Integer.MAX_VALUE;
            }else {
                l = A[left];
                l *= l;
            }
            if(start > max){
                r = Integer.MAX_VALUE;
            }else {
                r = A[start];
                r *= r;
            }
            if(l < r){
                returnArray[index] = l;
                left--;
            }else {
                returnArray[index] = r;
                start++;
            }
            index++;

        }
        return returnArray;

    }

    /**
     * 52. N皇后 II
     * @param n
     * @return
     */
    public static int totalNQueensNum = 0;
    public static int totalNQueens(int n) {
        totalNQueensNum = 0;
        //已占用的格子
        int[] columns = new int[n];
        Arrays.fill(columns,0);
        int[] lefts = new int[2*n-1];
        Arrays.fill(lefts,0);
        int[] rights = new int[2*n -1];
        Arrays.fill(rights,0);
        totalNQueens(columns,lefts,rights,0,n);
        return totalNQueensNum;
    }

    public static void totalNQueens(int[] columns,int[] lefts,int[] rights,int rowNum,int columnEnd){
        if(rowNum >= columnEnd){
            totalNQueensNum++;
            return;
        }
        for(int i = 0; i < columnEnd; i++) {
            if (columns[i] == 0 && lefts[i - rowNum + columnEnd - 1] == 0 && rights[rowNum + i] == 0) {
                columns[i] = 1;
                lefts[i - rowNum + columnEnd - 1] = 1;
                rights[rowNum + i] = 1;
                totalNQueens(columns, lefts, rights, rowNum + 1, columnEnd);
                columns[i] = 0;
                lefts[i - rowNum + columnEnd - 1] = 0;
                rights[rowNum + i] = 0;
            }
        }
    }


    public static boolean backspaceCompare(String S, String T) {
        char[] sc = S.toCharArray();
        char[] tc = T.toCharArray();
        LinkedList<Character> sq = new LinkedList<>();
        LinkedList<Character> tq = new LinkedList<>();
        for(Character c:sc){
            if(c.equals('#')){
                if(sq.size() > 0) {
                    sq.removeLast();
                }
            }else {
                sq.add(c);
            }
        }
        for(Character c:tc){
            if(c.equals('#')){
                if(tq.size() > 0) {
                    tq.removeLast();
                }
            }else {
                tq.add(c);
            }
        }
        StringBuilder s = new StringBuilder();
        for (Character c:sq){
            s.append(c);
        }
        StringBuilder t = new StringBuilder();
        for(Character c:tq){
            t.append(c);
        }
        if(s.length() != t.length()){
            return false;
        }else {
            if(s.length() == 0){
                return true;
            }else {
                return s.toString().equals(t.toString());
            }
        }
    }


    /**
     * 143. 重排链表
     * @param head
     */
    public void reorderList(ListNode head) {
        LinkedList<ListNode> listNodeLinkedList = new LinkedList<>();
        ListNode start = head;
        while (start != null){
            listNodeLinkedList.add(start);
            start = start.next;
        }
        ListNode prev = null;
        ListNode node = null;
        boolean k = true;
        while (listNodeLinkedList.size() > 0){
            if(k){
                node = listNodeLinkedList.pollFirst();
            }else {
                node = listNodeLinkedList.pollLast();
            }
            if(prev != null){
                prev.next = node;
            }
            prev = node;
            k = !k;
        }
        if(prev != null) {
            prev.next = null;
        }

    }

    /**
     * 876. 链表的中间结点(快慢指针)
     * @param head
     * @return
     */
    public ListNode middleNode(ListNode head) {
        ListNode fast = head;
        ListNode slow = head;
        while (fast.next != null && fast.next.next != null){
            fast = fast.next.next;
            slow = slow.next;
        }
        if(fast.next != null){
            slow = slow.next;
        }
        return slow;
    }

    /**
     * 925. 长按键入
     * @param name
     * @param typed
     * @return
     */
    public boolean isLongPressedName(String name, String typed) {

        char[] nameCh = name.toCharArray();
        char[] typedCh = typed.toCharArray();
        int nIndex = 0;
        int tIndex = 0;
        int nLength = nameCh.length;
        int tLength = typedCh.length;
        while (tIndex < tLength){
            if(nIndex < nLength && nameCh[nIndex] == typedCh[tIndex]){
                nIndex++;
                tIndex++;
            }else if(nIndex > 0 && nameCh[nIndex - 1] == typedCh[tIndex]){
                tIndex++;
            }else {
                return false;
            }
        }
        return nIndex == nLength;




    }

}
