package cn.katoumegumi.java.experiment.test;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;

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
        System.out.println(predictPartyVictory("RD"));
        System.out.println(canPlaceFlowers(new int[]{1,0,0,0,0,1},2));
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



        //用队列的方式慢了
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

    /**
     * 763. 划分字母区间
     * @param S
     * @return
     */
    public static List<Integer> partitionLabels(String S) {
        int[] locationArray = new int[26];
        char[] chars = S.toCharArray();
        int length = chars.length;
        for(int i = 0; i < length; ++i){
            locationArray[chars[i] - 'a'] = i;
        }

        int end = 0;
        int prevEnd = 0;
        List<Integer> list = new ArrayList<>();
        for(int i = 0; i < length; ++i) {
            end = Math.max(locationArray[chars[i] - 'a'], end);
            if (end == i) {
                list.add(end - prevEnd + 1);
                prevEnd = end + 1;
            }
        }
        return list;


        //速度慢还占内存
        /*int length = S.length();
        Map<Character, Integer> locationMap = new HashMap<>();
        for(int i = 0; i < length; i++){
            locationMap.put(S.charAt(i),i);
        }
        List<Integer> integerList = new ArrayList<>();
        Character character = null;
        int end = 0;
        int prevEnd = 0;
        for(int i = 0; i < length; i++){
            character = S.charAt(i);
            int cEnd = locationMap.get(character);
            end = Math.max(cEnd,end);
            if(end == i){
                integerList.add(end - prevEnd);
                prevEnd = end;
            }
        }
        if(prevEnd != end){
            integerList.add(end - prevEnd);
        }
        return integerList;*/
    }

    /**
     * 206. 反转链表
     * @param head
     * @return
     */
    public ListNode reverseList(ListNode head) {
        if(head == null){
            return null;
        }
        return reverseList(head,head.next);
    }

    public ListNode reverseList(ListNode first,ListNode next) {
        if(next != null){
            ListNode node = reverseList(next,next.next);
            next.next = first;
            first.next = null;
            return node;
        }else {
            return first;
        }
    }

    /**
     * 234. 回文链表
     * @param head
     * @return
     */
    public boolean isPalindrome(ListNode head) {
        if(head == null){
            return true;
        }
        ListNode fast = head;
        ListNode slow = head;
        ListNode prevSlow = null;
        while (fast.next != null && fast.next.next != null){
            fast = fast.next.next;
            ListNode next = slow.next;
            slow.next = prevSlow;
            prevSlow = slow;
            slow = next;
        }
        ListNode leftStart;
        ListNode rightStart;
        if(fast.next == null){
            rightStart = slow.next;
            leftStart = prevSlow;
        }else {
            leftStart = slow;
            rightStart = slow.next;
            slow.next = prevSlow;
        }
        while (rightStart != null){
            if(!(leftStart.val == rightStart.val)){
                return false;
            }
            leftStart = leftStart.next;
            rightStart = rightStart.next;
        }
        return true;


    }

    /**
     * 1365. 有多少小于当前数字的数字
     * @param nums
     * @return
     */
    public int[] smallerNumbersThanCurrent(int[] nums) {
        List<Integer> list = new ArrayList<>();
        for(int i:nums){
            list.add(i);
        }
        list.sort(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1-o2;
            }
        });
        for(int i = 0; i < nums.length; i++){
            nums[i] = list.indexOf(nums[i]);
        }
        return nums;
    }

    /**
     * 144. 二叉树的前序遍历(递归解法)
     * @param root
     * @return
     */
    public List<Integer> preorderTraversal(TreeNode root) {
        List<Integer> returnList = new ArrayList<>();
        preorderTraversal(root,returnList);
        return returnList;
    }

    public void preorderTraversal(TreeNode root,List<Integer> list) {
        if (root != null) {
            list.add(root.val);
            preorderTraversal(root.left, list);
            preorderTraversal(root.right, list);
        }
    }

    /**
     * 1207. 独一无二的出现次数
     * @param arr
     * @return
     */
    public static boolean uniqueOccurrences(int[] arr) {
        Arrays.sort(arr);
        int prev = arr[0];
        int prevIndex = 0;
        Set<Integer> set = new HashSet<>();
        int length = arr.length;
        int v;
        int j;
        int i = 1;
        for(; i < length; ++i){
            v = arr[i];
            if(v != prev){
                prev = v;
                j = i - prevIndex;
                prevIndex = i;
                if(set.contains(j)){
                    return false;
                }else {
                    set.add(j);
                }
            }
        }
        j = i - prevIndex;
        if(set.contains(j)){
            return false;
        }

        return true;


    }

    /**
     * 129. 求根到叶子节点数字之和(深度优先算法)
     * @param root
     * @return
     */
    public int sumNumbers(TreeNode root) {
        return sumNumbers(root,0);
    }

    public int sumNumbers(TreeNode root,Integer value) {
        if(root != null){
            if(root.left == null && root.right == null){
                return value*10 + root.val;
            }else if(root.right == null) {
                return sumNumbers(root.left,value*10 + root.val);
            }else if(root.left == null){
                return sumNumbers(root.right,value* 10 + root.val);
            }else {
                return sumNumbers(root.left,value*10 + root.val) + sumNumbers(root.right,value*10 + root.val);
            }
        }else {
            return value;
        }
    }

    /**
     * 129. 求根到叶子节点数字之和(广度优先算法)
     * @param root
     * @return
     */
    public int sumNumbersBfs(TreeNode root){
        if(root == null){
            return 0;
        }
        int sum = 0;
        Queue<TreeNode> queue = new ArrayDeque<>();
        Queue<Integer> numQueue = new ArrayDeque<>();
        queue.offer(root);
        numQueue.offer(root.val);
        while (queue.size() > 0){
            TreeNode node = queue.poll();
            Integer value = numQueue.poll();

            if(node.left == null && node.right == null){
                sum += value;
            }

            if(node.left != null){
                queue.offer(node.left);
                numQueue.offer(value*10 + node.left.val);
            }
            if(node.right != null){
                queue.add(node.right);
                numQueue.offer(value * 10 + node.right.val);
            }
        }
        return sum;
    }

    /**
     * 349. 两个数组的交集
     * @param nums1
     * @param nums2
     * @return
     */
    public int[] intersection(int[] nums1, int[] nums2) {
        Arrays.sort(nums1);
        Arrays.sort(nums2);

        int index1 = 0;
        int index2 = 0;

        int num1Length = nums1.length;
        int num2Length = nums2.length;

        Set<Integer> set = new HashSet<>();

        while (index1 < num1Length && index2 < num2Length) {
            if (nums1[index1] == nums2[index2]) {
                set.add(nums1[index1]);
                index1++;
                index2++;
            } else if (nums1[index1] > nums2[index2]) {
                index2++;
            } else {
                index1++;
            }

        }

        int[] ints = new int[set.size()];
        int index = 0;
        for (Integer i : set) {
            ints[index] = i;
            index++;
        }
        return ints;
    }

    /**
     * 140. 单词拆分 II
     * @param s
     * @param wordDict
     * @return
     */
    public static List<String> wordBreak(String s, List<String> wordDict) {
        Map<Character,Word> wordMap = new HashMap<>();
        for(String str:wordDict){
            char[] chars = str.toCharArray();
            Word word = null;
            Map<Character,Word> map = wordMap;
            for(Character c:chars){
                if(map == null){
                    word.wordMap = new HashMap<>();
                    map = word.wordMap;
                }
                word = map.computeIfAbsent(c,k->new Word());
                map = word.wordMap;
            }
            word.end = true;
        }
        List<String> lists = new ArrayList<>();
        Map<Integer,List<List<String>>> listMap = new HashMap<>();
        List<List<String>> list = wordBreak(0,s,wordMap,listMap);
        for(List<String> stringList:list){
            lists.add(String.join(" ",stringList));
        }
        return lists;
    }

    private static List<List<String>> wordBreak(Integer index,String s,Map<Character,Word> wordMap,Map<Integer,List<List<String>>> listMap){

        List<List<String>> list = listMap.get(index);
        if(list == null){
            list = new ArrayList<>();
            List<String> strings = new LinkedList<>();
            //list.add(strings);
            int i = index;
            Map<Character,Word> localMap = wordMap;
            char c;
            Word word;
            int prevIndex = index;
            for(; i < s.length(); i++){
                c = s.charAt(i);
                word = localMap.get(c);
                if(word == null){
                    listMap.put(index,list);
                    return list;
                }
                if(word.end){
                    if(word.wordMap == null){
                        localMap = wordMap;
                        strings.add(s.substring(prevIndex,i + 1));
                        prevIndex = i + 1;
                        if(prevIndex == s.length()){
                            list.add(strings);
                        }
                    }else {
                        if(i + 1 < s.length()) {
                            List<List<String>> nextList = wordBreak(i + 1, s, wordMap, listMap);
                            if (nextList != null) {
                                for (List<String> stringList : nextList) {
                                    List<String> newList = new LinkedList<>(strings);
                                    newList.add(s.substring(prevIndex, i + 1));
                                    newList.addAll(stringList);
                                    list.add(newList);
                                }
                            }
                        }else {
                            strings.add(s.substring(prevIndex,i + 1));
                            list.add(strings);
                        }
                        localMap = word.wordMap;
                    }
                }else {
                    localMap = word.wordMap;
                }
            }

            listMap.put(index,list);
            return list;
        }else {
            return list;
        }

    }


    private static class Word{
        public char c;
        public boolean end;
        public Map<Character,Word> wordMap;
    }


    /**
     * 973. 最接近原点的 K 个点
     * @param points
     * @param K
     * @return
     */
    public static int[][] kClosest(int[][] points, int K) {
        double[] distance = new double[points.length];

        for(int i = 0; i < distance.length; i++){
            distance[i] = Math.sqrt(Math.pow(points[i][0],2)+Math.pow(points[i][1],2));
        }
        int[][] r = new int[K][];

        int minIndex = 0;
        for(int i = 0; i < K; i++){
            minIndex = i;
            for(int j = i + 1; j < distance.length; j++){
                if(distance[j] < distance[minIndex]){
                    minIndex = j;
                    //distance[i] = distance[j];
                }
            }
            double d = distance[minIndex];
            distance[minIndex] = distance[i];
            distance[i] = d;
            int[] ints = points[minIndex];
            points[minIndex] = points[i];
            points[i] = ints;
        }
        return Arrays.copyOf(points,K);
    }

    /**
     * 767. 重构字符串
     * @param S
     * @return
     */
    public static String reorganizeString(String S) {
        int length = S.length();
        if(length < 2){
            return S;
        }
        int checkNum = (length + 1) / 2;
        int num = 97;
        int[] array = new int[26];
        Arrays.fill(array,0);
        char[] chars = S.toCharArray();
        for(char c:chars){
            array[c-num]++;
            if(array[c-num] > checkNum){
                return "";
            }
        }
        PriorityQueue<Character> queue = new PriorityQueue<>((o1, o2) -> array[o2 - num] - array[o1 - num]);
        for(int i = 0; i < array.length; i++){
            if(array[i] > 0){
                queue.offer((char) (i+97));
            }
        }
        StringBuilder sb = new StringBuilder();
        while (queue.size() > 1){
            char c1 = queue.poll();
            char c2 = queue.poll();
            sb.append(c1);
            sb.append(c2);
            array[c1 - 97]--;
            array[c2 - 97]--;
            if(array[c1 - 97] > 0){
                queue.offer(c1);
            }
            if(array[c2 - 97] > 0){
                queue.offer(c2);
            }
        }
        if (queue.size() > 0){
            sb.append(queue.poll());
        }
        return sb.toString();
    }

    /**
     * 34. 在排序数组中查找元素的第一个和最后一个位置(二分法)
     * @param nums
     * @param target
     * @return
     */
    public static int[] searchRange(int[] nums, int target) {
        int[] returnValue = new int[]{-1,-1};
        if(nums == null || nums.length == 0){
            return returnValue;
        }
        if(nums[0] > target){
            return returnValue;
        }

        int left = 0;
        int right = nums.length - 1;
        int mid = 0;
        while (left <= right){
            mid = (right + left)/2;
            if(nums[mid] > target){
                right = mid - 1;
            }else if(nums[mid] < target){
                left = mid + 1;
            }else {
                right = mid - 1;
            }
        }
        if(left >= nums.length){
            return returnValue;
        }
        if(nums[left] != target){
            return returnValue;
        }
        returnValue[0] = left;

        left = 0;
        right = nums.length - 1;
        mid = 0;

        while (left <= right){
            mid = (right + left)/2;
            if(nums[mid] > target){
                right = mid - 1;
            }else if(nums[mid] < target){
                left = mid + 1;
            }else {
                left = mid + 1;
            }
        }
        returnValue[1] = right;
        return returnValue;



    }


    /**
     * 649. Dota2 参议院
     * @param senate
     * @return
     */
    public static String predictPartyVictory(String senate) {
        int n = senate.length();
        Queue<Integer> rq = new ArrayDeque<>(senate.length());
        Queue<Integer> dq = new ArrayDeque<>(senate.length());
        char c;
        for(int i = 0; i < senate.length(); i++) {
            c = senate.charAt(i);
            if (c == 'R') {
                rq.offer(i);
            } else {
                dq.offer(i);
            }
        }
        int rIndex;
        int dIndex;
        while (!rq.isEmpty() && !dq.isEmpty()){
            rIndex = rq.poll();
            dIndex = dq.poll();
            if(rIndex < dIndex){
                rq.offer(rIndex + n);
            }else {
                dq.offer(dIndex + n);
            }
        }


        return rq.isEmpty()?"Dire":"Radiant";
    }

    /**
     * 605. 种花问题
     * @param flowerbed
     * @param n
     * @return
     */
    public static boolean canPlaceFlowers(int[] flowerbed, int n) {
        if(n <= 0){
            return true;
        }
        if(flowerbed.length == 0){
            return false;
        }
        int prev = 0;
        int current = flowerbed[0];
        int next = 0;
        for(int i = 1; i < flowerbed.length; i++){
            prev = current;
            current = flowerbed[i - 1];
            next = flowerbed[i];
            if(prev == 0 && current == 0 && next == 0){
                n--;
                flowerbed[i - 1] = 1;
                current = 1;
                if(n <= 0){
                    return true;
                }
            }
        }
        prev = current;
        current = next;
        if(prev == 0 && current == 0){
            n--;
            if(n <= 0){
                return true;
            }
        }
        return false;
    }

    /**
     * 239. 滑动窗口最大值
     * @param nums
     * @param k
     * @return
     */
    public int[] maxSlidingWindow(int[] nums, int k) {
        return null;
    }

}
