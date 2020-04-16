package cn.katoumegumi.java.experiment;

import java.io.*;
import java.util.Comparator;
import java.util.PriorityQueue;

public class HufTree {
    private static final int LEN = 256;
    private int[] byteCount = new int[LEN];// 统计各字节出现次数
    private String[] charCode = new String[LEN];// 记录各字节哈夫曼编码
    private PriorityQueue<hufNode> nodeQueue = new PriorityQueue<>(LEN, new Comparator<hufNode>() {
        @Override
        public int compare(hufNode o1, hufNode o2) {
            return o1.count - o2.count;// 按次数排序
        }
    });

    // 主函数
    public static void main(String[] args) {
        /*File file = new File("file\\003.txt");
        File file2 = new File("file\\压缩文件1.txt");
        new HufTree().compress(file, file2);// 压缩文件
        System.out.println("原文件大小：" + file.length()/1000 + "kb");
        System.out.println("压缩文件大小：" + file2.length()/1000 + "kb");*/
        String str = "世Aカ";
        byte[] bytes = str.getBytes();
        for (byte b : bytes) {
            System.out.println(b);
        }
    }

    // 压缩文件
    private void compress(File file, File file2) {
        byte[] bs = readFile(file);// 读取文件
        countChar(bs);// 统计词频
        hufNode root = createTree();// 创建哈夫曼树
        generateCode(root, "");// 生成哈夫曼编码
        printCode();// 打印哈夫曼编码
        writeFile(bs, file2);// 写入压缩文件
    }

    // 将文件转换为字节数组保存
    private byte[] readFile(File file) {
        byte[] bs = new byte[(int) file.length()];// 创建字节数组
        BufferedInputStream bis = null;// 声明字节缓冲流
        try {
            bis = new BufferedInputStream(new FileInputStream(file));
            bis.read(bs);// 将文件读取到字节数组中
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bis != null) {
                    bis.close();// 关闭输入流
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bs;
    }

    // 统计词频
    private void countChar(byte[] bs) {
        for (int i = 0, length = bs.length; i < length; i++) {


            //----------汉字判断处理部分-----------
            if (bs[i] < 0) {
                byteCount[LEN + bs[i]]++;
            } else {
                byteCount[bs[i]]++;
            }
            //----------汉字判断处理部分-----------


        }
    }

    // 创建哈夫曼树
    private hufNode createTree() {
        for (int i = 0; i < LEN; i++) {
            if (byteCount[i] != 0) {// 使用优先队列保存
                nodeQueue.add(new hufNode((char) i + "", byteCount[i]));
            }
        }
        // 构建哈夫曼树
        while (nodeQueue.size() > 1) {
            hufNode min1 = nodeQueue.poll();// 获取并移除队列头元素
            hufNode min2 = nodeQueue.poll();
            hufNode result = new hufNode(min1.str + min2.str, min1.count + min2.count);
            result.lchild = min1;
            result.rchild = min2;
            nodeQueue.add(result);// 加入左节点
        }
        return nodeQueue.peek();// 返回根节点
    }

    // 生成哈夫曼编码
    private void generateCode(hufNode root, String s) {
        // 叶子节点
        if (root.lchild == null && root.rchild == null) {
            // 保存至编码数组对应位置
            charCode[(int) root.str.charAt(0)] = s;
        }
        if (root.lchild != null) {// 左边加0
            generateCode(root.lchild, s + "0");
        }
        if (root.rchild != null) {// 右边加1
            generateCode(root.rchild, s + "1");
        }
    }

    // 写入压缩文件 ：1、各字节编码长度 2、各字节编码 3、编码后的文件
    private void writeFile(byte[] bs, File file2) {
        BufferedOutputStream bos = null;// 声明字符缓冲流
        try {
            // 创建字符缓冲流
            bos = new BufferedOutputStream(new FileOutputStream(file2));
            // 写入各字节编码长度，并获得编码的二进制文件
            String binaryCode = writeCodeLength(file2, bos);
            // 字节数组文件转码成二进制文件
            String binaryFile = transFile(bs);
            // 合并二进制编码及文件（二进制编码+二进制文件）
            String codeAndFile = binaryCode + binaryFile;
            // 生成字节并 写入合并后二进制文件文件
            generateFile(bos, codeAndFile);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bos != null) {
                    bos.close();// 关闭输入流
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 写入各字节编码长度
    private String writeCodeLength(File file2, BufferedOutputStream bos) throws IOException {
        StringBuilder sb = new StringBuilder();// 创建字符缓冲流
        for (int i = 0; i < LEN; i++) {
            if (charCode[i] == null) {
                bos.write(0);
            } else {
                sb.append(charCode[i]);// 存储哈夫曼编码
                bos.write(charCode[i].length());
            }
        }
        return sb.toString();// 返回各字节哈夫曼编码
    }

    // 文件转码
    private String transFile(byte[] bs) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0, length = bs.length; i < length; i++) {


            //----------汉字判断处理部分-----------
            if (bs[i] < 0) {
                sb.append(charCode[LEN + bs[i]]);
            } else {
                sb.append(charCode[bs[i]]);
            }
            //----------汉字判断处理部分-----------


        }
        return sb.toString();
    }

    // 生成字节文件
    private void generateFile(BufferedOutputStream bos, String codeAndFile) throws IOException {
        int lastZero = 8 - codeAndFile.length() % 8;// 补0数
        int len = codeAndFile.length() / 8 + 1;// 取商+1
        if (lastZero != 0) {
            len = len + 1;//余数不为0则补加一位
            for (int i = 0; i < lastZero; i++) {
                codeAndFile += "0";// 加0补齐8位
            }
        }
        // 创建字符数组，保存字节
        byte[] bv = new byte[len];
        bv[0] = Integer.valueOf(lastZero).byteValue();
        for (int i = 1; i < len; i++) {
            // 先将8位01字符串二进制数据转换为十进制整型数据，再转为一个byte
            byte bytes = Integer.valueOf(changeString(codeAndFile.substring(0, 8))).byteValue();
            bv[i] = bytes;
            codeAndFile = codeAndFile.substring(8);// 去除前8个字节
        }
        // 写入文件
        bos.write(bv);
    }

    // 8位01字符串转换为十进制整型数据
    private int changeString(String str) {
        return (int) (str.charAt(0) - 48) * 128 + (str.charAt(1) - 48) * 64 + (str.charAt(2) - 48) * 32
                + (str.charAt(3) - 48) * 16 + (str.charAt(4) - 48) * 8 + (str.charAt(5) - 48) * 4
                + (str.charAt(6) - 48) * 2 + (str.charAt(7) - 48);
    }

    // 打印编码
    private void printCode() {
        for (int i = 0; i < LEN; i++) {
            if (charCode[i] != null) {
                System.out.println("(" + i + "," + (char) i + "," + byteCount[i] + "," + charCode[i] + ","
                        + charCode[i].length() + ")");
            }
        }
    }

    // 成员内部类
    private static class hufNode {
        private hufNode lchild;// 左分支
        private hufNode rchild;// 右分支
        private String str;// 记录字符
        private int count;// 统计次数

        // 构造方法
        public hufNode(String str, int count) {
            super();
            this.str = str;
            this.count = count;
        }
    }
}
