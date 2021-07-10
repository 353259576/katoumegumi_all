package cn.katoumegumi.java.common;


import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class WsStreamUtils {

    //public static Logger log = LoggerFactory.getLogger(WsStreamUtils.class);

    public static ByteBuffer inputStreamToByteBuffer(InputStream inputStream) {
        if (inputStream instanceof FileInputStream) {
            FileInputStream fileInputStream = (FileInputStream) inputStream;
            FileChannel fileChannel = fileInputStream.getChannel();
            try {
                long size = fileChannel.size();
                if (size > Long.parseLong(Integer.MAX_VALUE + "")) {
                    throw new RuntimeException("文件过大");
                }
                ByteBuffer byteBuffer = ByteBuffer.allocateDirect((int) size);
                fileChannel.read(byteBuffer);
                byteBuffer.flip();
                return byteBuffer;
            } catch (IOException e) {
                e.printStackTrace();
                close(fileChannel,inputStream);
            }

        } else {
            ReadableByteChannel readableByteChannel = Channels.newChannel(inputStream);
            try {
                long size = inputStream.available();
                if (size > Long.parseLong(Integer.MAX_VALUE + "")) {
                    throw new RuntimeException("文件过大");
                }
                ByteBuffer byteBuffer = ByteBuffer.allocateDirect((int) size);
                readableByteChannel.read(byteBuffer);
                byteBuffer.flip();
                return byteBuffer;
            } catch (IOException e) {
                e.printStackTrace();
                close(readableByteChannel,inputStream);
            }

        }
        return null;
    }


    public static void inputToOutput(InputStream inputStream, OutputStream outputStream) {
        if (inputStream instanceof FileInputStream && outputStream instanceof FileOutputStream) {
            FileInputStream fileInputStream = (FileInputStream) inputStream;
            FileOutputStream fileOutputStream = (FileOutputStream) outputStream;
            FileChannel fileInputChannel = fileInputStream.getChannel();
            FileChannel fileOutputChannel = fileOutputStream.getChannel();
            try {
                fileInputChannel.transferTo(fileInputChannel.position(), fileInputChannel.size(), fileOutputChannel);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                close(fileOutputChannel,fileInputChannel,outputStream,inputStream);
            }

        } else {
            ReadableByteChannel readableByteChannel = Channels.newChannel(inputStream);
            WritableByteChannel writableByteChannel = Channels.newChannel(outputStream);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
            try {
                while (readableByteChannel.read(byteBuffer) != -1) {
                    byteBuffer.flip();
                    writableByteChannel.write(byteBuffer);
                    byteBuffer.clear();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                close(writableByteChannel,readableByteChannel,outputStream,inputStream);
            }
        }
    }


    /**
     * zip压缩
     * @param files
     * @return
     */
    public static byte[] encodeFileToZip(File... files) {
        try {
            InputStream[] inputStreams = new InputStream[files.length];
            String[] fileNames = new String[files.length];
            for (int i = 0; i < files.length; i++) {
                inputStreams[i] = new FileInputStream(files[i]);
                fileNames[i] = files[i].getName();
            }
            return encodeFileToZip(inputStreams, fileNames);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public static byte[] encodeFileToZip(InputStream[] inputStreams, String[] fileNames) {
        ZipOutputStream zipOutputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        WritableByteChannel writableByteChannel = null;
        try {

            byteArrayOutputStream = new ByteArrayOutputStream();
            zipOutputStream = new ZipOutputStream(byteArrayOutputStream);
            zipOutputStream.setLevel(7);
            zipOutputStream.setMethod(ZipOutputStream.DEFLATED);
            //zipOutputStream.setComment(new String("Java压缩".getBytes("utf-8"),"ASCII"));
            writableByteChannel = Channels.newChannel(zipOutputStream);
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            for (int i = 0; i < inputStreams.length; i++) {
                ZipEntry zipEntry = new ZipEntry(fileNames[i]);
                zipOutputStream.putNextEntry(zipEntry);
                ReadableByteChannel readableByteChannel = Channels.newChannel(inputStreams[i]);
                while (readableByteChannel.read(byteBuffer) != -1) {
                    byteBuffer.flip();
                    while (byteBuffer.hasRemaining()) {
                        writableByteChannel.write(byteBuffer);
                    }
                    byteBuffer.clear();
                }
                zipOutputStream.closeEntry();
                readableByteChannel.close();
                inputStreams[i].close();
            }
            zipOutputStream.finish();
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            close(writableByteChannel,zipOutputStream,byteArrayOutputStream);
            close(inputStreams);
        }
    }




    /**
     * 关闭流
     * @param closeables
     */
    public static void close(Closeable... closeables){
        for(Closeable closeable:closeables){
            if(closeable != null) {
                try {
                    if(closeable instanceof Flushable){
                      ((Flushable) closeable).flush();
                    }
                    closeable.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
