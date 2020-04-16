package cn.katoumegumi.java.experiment;

import com.alibaba.fastjson.JSON;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class LittlePractice {

    public static AtomicInteger atomicInteger = new AtomicInteger(0);
    public static int maxLeftBracke = 0;
    public static List<String> list = new ArrayList<>();
    public static char chars[];
    public static char nums[] = new char[]{'1', '2', '3', '4', '5', '6', '7', '8', '9'};
    public static boolean success = false;
    public static int queens[];
    public static int columns[];
    public static int rights[];
    public static int lefts[];
    public static int num;
    public static int allNum = 0;
    public static List<List<String>> strs;

    public static void main(String[] args) throws Exception {
        List<String> list = new ArrayList<>();
        //var k = "fdsf";
        //var z = 12;
        //System.out.println(k);
        //System.out.println(k.getClass());
        /*HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder().GET().uri(new URI("https://www.baidu.com")).build();
        HttpResponse<String> httpResponse = httpClient.send(httpRequest,HttpResponse.BodyHandlers.ofString());
        System.out.println(httpResponse.body());*/
        //hanoi(2,"A","B","C");
        //System.out.println(atomicInteger.get());
        //System.out.println(factorialCirculation(10l));

        /*Integer integers[][] = new Integer[][]{
                {1,2,3,4},
                {5,6,7,8},
                {9,10,11,12},
                {13,14,15,16}
        };
        integers = rotateImage(integers);
        for(Integer is[]:integers){
            for(Integer i:is){
                System.out.print(i);
            }
            System.out.println();
        }*/

        /*int nums1[] = new int[]{1,2};
        int nums2[] = new int[]{0,0};
        System.out.println(findMedianSortedArrays(nums1,nums2));

        System.out.println(jump(nums1));*/
        //System.out.println(reverse(1056389759));
        /*N_Queen( 4);
        System.out.println(allNum);
        String str = "[[\"5\",\"3\",\".\",\".\",\"7\",\".\",\".\",\".\",\".\"],[\"6\",\".\",\".\",\"1\",\"9\",\"5\",\".\",\".\",\".\"],[\".\",\"9\",\"8\",\".\",\".\",\".\",\".\",\"6\",\".\"],[\"8\",\".\",\".\",\".\",\"6\",\".\",\".\",\".\",\"3\"],[\"4\",\".\",\".\",\"8\",\".\",\"3\",\".\",\".\",\"1\"],[\"7\",\".\",\".\",\".\",\"2\",\".\",\".\",\".\",\"6\"],[\".\",\"6\",\".\",\".\",\".\",\".\",\"2\",\"8\",\".\"],[\".\",\".\",\".\",\"4\",\"1\",\"9\",\".\",\".\",\"5\"],[\".\",\".\",\".\",\".\",\"8\",\".\",\".\",\"7\",\"9\"]]";
        List list1 = JSON.parseArray(str,List.class);
        char[][] chars = new char[9][9];
        for(int i = 0; i < 9; i++){
            List list2 = (List) list1.get(i);
            for(int j = 0; j < 9; j++){
                char c = ((String)list2.get(j)).toCharArray()[0];
                chars[i][j] = c;
            }
        }
        solveSudoku(chars);
        System.out.println(JSON.toJSONString(chars));*/
        /*int nums1[] = new int[]{10,10,10};
        System.out.println(numRabbits(nums1));

        ListNode node = new ListNode(1);
        ListNode node1 = new ListNode(2);
        node.next = node1;
        System.out.println(JSON.toJSONString(removeNthFromEnd(node,1)));*/
        System.out.println(JSON.toJSONString(generateParenthesis(3)));
    }

    public static List<String> generateParenthesis(int n) {
        maxLeftBracke = n;
        chars = new char[2 * n];
        huishuo(0, 0, 0, 0);
        return list;
    }

    public static void huishuo(int num, int rightBracke, int leftBracket, int balance) {
        if (maxLeftBracke == rightBracke && maxLeftBracke == leftBracket) {
            String str = new String(chars);
            //if(!list.contains(str)){
            list.add(str);
            //}

        }
        if (leftBracket < maxLeftBracke) {
            chars[num] = '(';
            huishuo(num + 1, rightBracke, leftBracket + 1, balance + 1);
        }
        if (rightBracke < maxLeftBracke && balance > 0) {
            chars[num] = ')';
            huishuo(num + 1, rightBracke + 1, leftBracket, balance - 1);
        }

    }

    public static ListNode removeNthFromEnd(ListNode head, int n) {
        ListNode first = head;
        ListNode second = null;
        ListNode oldHead = head;
        int i = 1;
        while (head.next != null) {


            if (i >= n) {
                second = first;
                first = first.next;
            }
            head = head.next;
            i++;
        }
        if (second == null) {
            return first.next;
        } else {
            second.next = first.next;
            return oldHead;
        }


    }

    public static int numRabbits(int[] answers) {
        Map<Integer, Integer> map = new HashMap<>();
        for (int num : answers) {
            Integer size = map.get(num);
            if (size == null) {
                map.put(num, 1);
            } else {
                size++;
                map.put(num, size);
            }
        }
        Iterator<Map.Entry<Integer, Integer>> iterator = map.entrySet().iterator();
        Integer allRabbits = 0;
        Map.Entry<Integer, Integer> entry;
        while (iterator.hasNext()) {
            entry = iterator.next();
            int i = entry.getValue() / (entry.getKey() + 1);
            int k = entry.getValue() % (entry.getKey() + 1);
            if (k > 0) {
                i++;
            }
            allRabbits += i * (entry.getKey() + 1);
        }
        return allRabbits;
    }

    public static void solveSudoku(char[][] board) {
        transmigration(board, 0, 0);
    }

    public static void transmigration(char[][] board, int col, int raw) {
        System.out.println("第" + raw + "行，第" + col + "列");
        System.out.println(JSON.toJSONString(board));
        if (board[raw][col] == '.') {
            for (int i = 0; i < 9; i++) {
                if (raw == 4 && col == 1) {
                    System.out.println("断点");
                }
                if (checkNum(nums[i], board, col, raw)) {
                    board[raw][col] = nums[i];
                    nextNumGrid(board, col, raw);
                    if (!success) {
                        board[raw][col] = '.';
                    }
                }
            }
        } else {
            nextNumGrid(board, col, raw);
        }


    }

    public static void nextNumGrid(char[][] board, int col, int raw) {
        System.out.println(raw);
        int nextCol = col;
        int nextRaw = raw;
        if (nextCol == 8 && nextRaw == 8) {
            success = true;
            return;
        }
        if (col < 8) {
            nextCol = col + 1;
        } else {
            if (raw < 9) {
                nextRaw = raw + 1;
                nextCol = 0;
            }
        }

        transmigration(board, nextCol, nextRaw);


    }

    public static boolean checkNum(char num, char[][] board, int col, int raw) {
        char bNum = board[raw][col];
        if (bNum != '.') {
            return false;
        }
        for (int i = 0; i < board.length; i++) {
            if (board[i][col] == num) {
                return false;
            }
            if (board[raw][i] == num) {
                return false;
            }
        }
        int rawStart = raw / 3 * 3;
        int colStart = col / 3 * 3;
        for (int i = rawStart; i < rawStart + 3; i++) {
            for (int k = colStart; k < colStart + 3; k++) {
                if (board[i][k] == num) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void initQueen(int n) {
        strs = new ArrayList<>();
        queens = new int[n];
        columns = new int[n];
        rights = new int[2 * n - 1];
        lefts = new int[2 * n - 1];
        num = n;
    }


    public static void N_Queen(int n) {
        initQueen(n);
        backtracking(0);
    }


    public static void backtracking(int column) {
        if (column >= num) {
            List<String> list = createQueenPosition();
            System.out.println(JSON.toJSONString(list));
        } else {
            for (int i = 0; i < num; i++) {
                //System.out.println("("+column+","+i+")");
                if (canPutQueen(column, i)) {
                    putQueen(column, i);
                    backtracking(column + 1);
                    removeQueen(column, i);
                }
            }
        }
    }

    public static List<String> createQueenPosition() {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            StringBuffer stringBuffer = new StringBuffer();
            for (int k = 0; k < num; k++) {
                if (queens[k] == i) {
                    stringBuffer.append('Q');
                } else {
                    stringBuffer.append('.');
                }
            }
            list.add(stringBuffer.toString());
        }
        return list;
    }

    public static boolean canPutQueen(int column, int line) {
        if (columns[line] == 0 && rights[line + column] == 0 && lefts[column - line + num - 1] == 0) {
            return true;
        }
        return false;
    }

    public static void putQueen(int column, int line) {
        columns[line] = 1;
        queens[column] = line;
        rights[column + line] = 1;
        lefts[column - line + num - 1] = 1;
    }

    public static void removeQueen(int column, int line) {
        columns[line] = 0;
        queens[column] = 0;
        rights[column + line] = 0;
        lefts[column - line + num - 1] = 0;

    }


    public static long reverse(int x) {
        boolean isMinus = false;
        if (x < 0) {
            isMinus = true;
            x = 0 - x;
        }
        Deque<Integer> deque = new ArrayDeque<>();
        while (x > 0) {
            deque.push(x % 10);
            x = x / 10;
        }
        long i = 1;
        long value = 0;
        while (deque.size() > 0) {
            value += deque.pop() * i;
            i *= 10;
        }
        if (value > (1 << 31) - 1) {
            return 0;
        }
        int ival = (int) value;
        return isMinus == true ? 0 - ival : ival;
    }

    public static int jump(int[] nums) {
        int end = nums.length - 1;
        int num = 0;
        int index = 0;
        int step = nums[0];
        while ((index + step) < end) {
            num++;
            int max = nextStep(nums, index + 1, index + step);
            index = max;
            step = nums[max];
        }
        if (index != end) {
            num++;
        }
        return num;
    }

    public static int nextStep(int nums[], int start, int end) {
        int max = start;
        for (int i = start + 1; i <= end && i < nums.length; i++) {
            if (nums[i] + i >= nums[max] + max) {
                max = i;
            }
        }
        return max;
    }


    //有序数组寻找中值
    public static double findMedianSortedArrays(int[] nums1, int[] nums2) {
        int n1Length = nums1.length;
        int n2Length = nums2.length;
        if (n1Length > n2Length) {
            int tmps[] = nums2;
            nums2 = nums1;
            nums1 = tmps;
            int tmp = n1Length;
            n1Length = n2Length;
            n2Length = tmp;
        }
        boolean isDou;
        int startP = 0;
        int endP = (n1Length + n2Length) / 2;
        if ((n1Length + n2Length) % 2 == 0) {
            isDou = true;
        } else {
            endP++;
            isDou = false;
        }
        int n1P = 0;
        int n2p = 0;
        double value = 0;
        while (startP < endP) {
            if (n1P >= n1Length) {
                value = nums2[n2p];
                n2p++;
                startP++;
                continue;
            }
            if (nums1[n1P] > nums2[n2p]) {
                value = nums2[n2p];
                n2p++;
            } else {
                value = nums1[n1P];
                n1P++;

            }
            startP++;
        }
        if (!isDou) {
            return value;
        } else {
            if (n1P >= n1Length) {
                return (nums2[n2p] + value) / 2;
            }
            if (n2p >= n2Length) {
                n2p--;
                return (nums1[n1P] + value) / 2;
            }
            if (nums1[n1P] < nums2[n2p]) {
                return (nums1[n1P] + value) / 2;
            } else {
                return (nums2[n2p] + value) / 2;
            }
        }
    }

    public static Integer[][] rotateImage(Integer[][] integers) {
        for (int i = 0; i < integers.length; i++) {
            for (int j = i; j < integers[i].length; j++) {
                Integer r = integers[i][j];
                integers[i][j] = integers[j][i];
                integers[j][i] = r;
            }
        }
        for (int i = 0; i < integers.length; i++) {
            for (int j = 0; j < integers[i].length / 2; j++) {
                Integer r = integers[i][j];
                integers[i][j] = integers[i][integers[i].length - 1 - j];
                integers[i][integers[i].length - 1 - j] = r;
            }
        }
        return integers;
    }

    public static void hanoi(int n, String start, String middle, String end) {

        if (n == 1) {
            atomicInteger.getAndAdd(1);
            System.out.println("起始：" + start + " 缓冲：" + middle + " 目标：" + end);
            System.out.println(n + "号从" + start + "移动到" + end);
        } else {
            hanoi(n - 1, start, end, middle);
            hanoi(1, start, middle, end);
            hanoi(n - 1, middle, start, end);

        }
    }

    public static Long factorial(Long num, Long end) {
        if (num.equals(1L)) {
            return end * num;
        } else {
            end = end * (num);
            return factorial(num - 1, end);
        }
    }

    public static Long factorialCirculation(Long num) {
        Long value = 1L;
        while (true) {
            if (num.equals(1L)) {
                break;
            }
            value *= num;
            num--;
        }
        return value;
    }

    public int[] twoSum(int[] nums, int target) {
        int num1;
        int num2;
        for (int i = 0; i < nums.length; i++) {
            num1 = nums[i];
            num2 = target - num1;
            for (int j = 0; j < nums.length; j++) {
                if (j == i) {
                    continue;
                }
                if (num2 == nums[j]) {
                    return new int[]{i, j};
                }
            }
        }
        return null;
    }
}

class ListNode {
    int val;
    ListNode next;

    ListNode(int x) {
        val = x;
    }
}
