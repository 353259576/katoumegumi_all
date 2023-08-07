package cn.katoumegumi.java.common;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 文件工具类
 *
 * @author 星梦苍天
 */
public class WsFileUtils {
    public static void main(String[] args) {

    }

    /**
     * 将文件路径的 \\ // 替换成 /
     *
     * @param oldFilePath
     * @return
     */
    public static String adjustFilePath(String oldFilePath) {
        oldFilePath = oldFilePath.replaceAll("\\\\", "/");
        oldFilePath = oldFilePath.replaceAll("//", "/");
        return oldFilePath;
    }


    public static List<File> getListFile(String filePath) {
        filePath = adjustFilePath(filePath);
        File file = new File(filePath);
        return getListFile(file);
    }


    public static List<File> getListFile(File file) {
        if (file == null) {
            return null;
        }
        if (!file.exists()) {
            return null;
        }
        if (file.isFile()) {
            return null;
        }
        File[] files = file.listFiles();
        return WsCollectionUtils.arrayToList(files);
    }

    /**
     * 创建文件
     *
     * @param filePath 文件路径
     * @return
     */
    public static File createFile(String filePath) {
        filePath = adjustFilePath(filePath);
        File file = new File(filePath);
        if (file.exists()) {
            file.setReadable(true);
            file.setWritable(true);
            file.setExecutable(true);
            return file;
        } else {
            if (filePath.endsWith("/")) {
                file.mkdirs();
                return file;
            } else {
                //int position1 = filePath.lastIndexOf('\\');
                int position = filePath.lastIndexOf('/');
                //int position = Math.max(position1, position2);
                String path = filePath.substring(0, position);
                File fPath = new File(path);
                fPath.mkdirs();
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                file.setWritable(true);
                file.setReadable(true);
                file.setExecutable(true);
                return file;
            }
        }

    }
}
