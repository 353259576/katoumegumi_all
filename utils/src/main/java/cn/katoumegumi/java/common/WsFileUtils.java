package cn.katoumegumi.java.common;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WsFileUtils {
    public static void main(String[] args) {

    }


    public static String adjustFilePath(String oldFilePath) {
        oldFilePath = oldFilePath.replaceAll("\\\\", "/");
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
        return WsListUtils.arrayToList(files);
    }


    public static List<String> getListDirectory(String path) {

        List<File> listFile = getListFile(path);
        if (listFile == null || listFile.size() == 0) {
            return null;
        }
        List<String> folders = new ArrayList<>();
        for (File f1 : listFile) {
            if (f1.isDirectory()) {
                folders.add(f1.getName());
            }
        }
        return folders;
    }


    public static File createFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            file.setReadable(true);
            file.setWritable(true);
            file.setExecutable(true);
            return file;
        } else {
            if (filePath.endsWith("/") || filePath.endsWith("\\")) {
                file.mkdirs();
                return file;
            } else {
                int position1 = filePath.lastIndexOf('\\');
                int position2 = filePath.lastIndexOf('/');
                int position = Math.max(position1, position2);
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
