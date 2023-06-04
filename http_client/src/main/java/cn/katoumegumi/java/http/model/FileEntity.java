package cn.katoumegumi.java.http.model;


import cn.katoumegumi.java.common.WsStringUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.List;

/**
 * 文件类型
 *
 * @author ws
 */
public class FileEntity extends BaseEntity {

    private String fileName;

    private String fileType;

    private byte[] fileData;

    public String getFileName() {
        return fileName;
    }

    public FileEntity setFileName(String fileName) {
        if (WsStringUtils.isNotBlank(fileName)) {
            List<String> strs = WsStringUtils.split(fileName, '.');
            if (strs.size() == 1) {
            } else {
                this.fileType = strs.get(1);
            }
        }
        this.fileName = fileName;
        return this;
    }

    public String getFileType() {
        return fileType;
    }

    public byte[] getValue() {
        return fileData;
    }

    public FileEntity setValue(File file) {
        try {
            setFileName(file.getName());
            return setValue(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return this;
    }

    public FileEntity setValue(InputStream inputStream) {
        ByteArrayOutputStream outputStream = null;
        WritableByteChannel writableByteChannel = null;
        ReadableByteChannel readableByteChannel = null;
        try {
            outputStream = new ByteArrayOutputStream();
            writableByteChannel = Channels.newChannel(outputStream);
            if (inputStream instanceof FileInputStream) {
                FileInputStream fileInputStream = (FileInputStream) inputStream;
                //FileChannel fileChannel = fileInputStream.getChannel();
                readableByteChannel = fileInputStream.getChannel();
                FileChannel fileChannel = (FileChannel) readableByteChannel;
                fileChannel.transferTo(0, fileChannel.size(), writableByteChannel);
                this.fileData = outputStream.toByteArray();
            } else {
                readableByteChannel = Channels.newChannel(inputStream);
                ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
                int i = -1;
                while ((i = readableByteChannel.read(byteBuffer)) > 0) {
                    byteBuffer.flip();
                    while (byteBuffer.hasRemaining()) {
                        writableByteChannel.write(byteBuffer);
                    }
                    byteBuffer.clear();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {


            if (writableByteChannel != null) {
                try {
                    writableByteChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (readableByteChannel != null) {
                try {
                    readableByteChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return this;

    }
}
