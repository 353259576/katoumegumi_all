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
                this.fileName = strs.get(0);
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
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            WritableByteChannel writableByteChannel = Channels.newChannel(outputStream);
            if (inputStream instanceof FileInputStream) {
                FileInputStream fileInputStream = (FileInputStream) inputStream;
                FileChannel fileChannel = fileInputStream.getChannel();
                fileChannel.transferTo(0, fileChannel.size(), writableByteChannel);
                this.fileData = outputStream.toByteArray();
                writableByteChannel.close();
                outputStream.close();
                fileChannel.close();
                inputStream.close();
            } else {
                ReadableByteChannel readableByteChannel = Channels.newChannel(inputStream);
                ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
                int i = -1;
                while ((i = readableByteChannel.read(byteBuffer)) > 0) {
                    byteBuffer.flip();
                    while (byteBuffer.hasRemaining()) {
                        writableByteChannel.write(byteBuffer);
                    }
                    byteBuffer.clear();
                }
                writableByteChannel.close();
                readableByteChannel.close();
                outputStream.close();
                inputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;

    }
}
