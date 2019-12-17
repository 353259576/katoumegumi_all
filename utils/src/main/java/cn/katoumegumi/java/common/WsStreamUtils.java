package cn.katoumegumi.java.common;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class WsStreamUtils {



    public static ByteBuffer InputStreamToByteBuffer(InputStream inputStream){
        if(inputStream instanceof FileInputStream){
            FileInputStream fileInputStream = (FileInputStream)inputStream;
            FileChannel fileChannel = fileInputStream.getChannel();
            try {
                long size = fileChannel.size();
                if(size > Long.parseLong(Integer.MAX_VALUE + "")){
                    throw new RuntimeException("文件过大");
                }
                ByteBuffer byteBuffer = ByteBuffer.allocateDirect((int) size);
                fileChannel.read(byteBuffer);
                byteBuffer.flip();
                fileChannel.close();
                return byteBuffer;
            }catch (IOException e){
                e.printStackTrace();
                try {
                    inputStream.close();
                }catch (IOException e1){
                    e1.printStackTrace();
                }

            }

        }else {
            ReadableByteChannel readableByteChannel = Channels.newChannel(inputStream);
            try {
                long size = inputStream.available();
                if(size > Long.parseLong(Integer.MAX_VALUE + "")){
                    throw new RuntimeException("文件过大");
                }
                ByteBuffer byteBuffer = ByteBuffer.allocateDirect((int) size);
                readableByteChannel.read(byteBuffer);
                byteBuffer.flip();
                readableByteChannel.close();
                return byteBuffer;
            }catch (IOException e){
                e.printStackTrace();
                try {
                    inputStream.close();
                }catch (IOException e1){
                    e1.printStackTrace();
                }
            }

        }
        return null;
    }



    public static void inputToOutput(InputStream inputStream,OutputStream outputStream){
        if(inputStream instanceof FileInputStream && outputStream instanceof FileOutputStream){
            FileInputStream fileInputStream = (FileInputStream)inputStream;
            FileOutputStream fileOutputStream = (FileOutputStream)outputStream;
            FileChannel fileInputChannel = fileInputStream.getChannel();
            FileChannel fileOutputChannel = fileOutputStream.getChannel();
            try {
                fileInputChannel.transferTo(fileInputChannel.position(),fileInputChannel.size(),fileOutputChannel);
            }catch (IOException e){
                e.printStackTrace();
            }finally {
                try {
                    fileOutputChannel.close();
                    fileInputChannel.close();
                    outputStream.flush();
                    outputStream.close();
                    inputStream.close();
                }catch (IOException e){
                    e.printStackTrace();
                }

            }

        } else {
            ReadableByteChannel readableByteChannel = Channels.newChannel(inputStream);
            WritableByteChannel writableByteChannel = Channels.newChannel(outputStream);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
            try {
                while (readableByteChannel.read(byteBuffer) != -1){
                    byteBuffer.flip();
                    writableByteChannel.write(byteBuffer);
                    byteBuffer.clear();
                }
            }catch (IOException e){
                e.printStackTrace();
            }finally {
                try {
                    writableByteChannel.close();
                    readableByteChannel.close();
                    outputStream.flush();
                    outputStream.close();
                    inputStream.close();
                }catch (IOException e){
                    e.printStackTrace();
                }

            }
        }


    }




    public static byte[] encodeFileToZip(File...files){
        try {
            InputStream inputStreams[] = new InputStream[files.length];
            String fileNames[] = new String[files.length];
            for(int i = 0; i < files.length; i++){
                inputStreams[i] = new FileInputStream(files[i]);
                fileNames[i] = files[i].getName();
            }
            return encodeFileToZip(inputStreams,fileNames);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }

    }


    public static byte[] encodeFileToZip(InputStream inputStreams[],String fileNames[]){
        Long startTime = System.currentTimeMillis();
        ZipOutputStream zipOutputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        WritableByteChannel writableByteChannel = null;
        try {

            byteArrayOutputStream = new ByteArrayOutputStream();
            zipOutputStream = new ZipOutputStream(byteArrayOutputStream);
            zipOutputStream.setLevel(7);
            zipOutputStream.setMethod(ZipOutputStream.DEFLATED);
            zipOutputStream.setComment(new String("Java压缩".getBytes("utf-8"),"ASCII"));
            writableByteChannel = Channels.newChannel(zipOutputStream);
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            for(int i = 0; i < inputStreams.length; i++){
                ZipEntry zipEntry  = new ZipEntry(fileNames[i]);
                zipOutputStream.putNextEntry(zipEntry);
                ReadableByteChannel readableByteChannel= Channels.newChannel(inputStreams[i]);
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
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }finally {
            try {
                if(writableByteChannel != null){
                    writableByteChannel.close();
                }
            }catch (Exception e){
                e.printStackTrace();
            }

            try {
                if(zipOutputStream != null){
                    zipOutputStream.flush();
                    zipOutputStream.close();
                }
            }catch (Exception e){
                e.printStackTrace();
            }


            try {
                if(byteArrayOutputStream != null){
                    byteArrayOutputStream.flush();
                    byteArrayOutputStream.close();
                }
            }catch (Exception e){
                e.printStackTrace();
            }

            try {
                if(inputStreams != null){
                    for(int i = 0; i < inputStreams.length; i++){
                        inputStreams[i].close();
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            Long endTime = System.currentTimeMillis();
            System.out.println("系统压缩文件需要："+(endTime-startTime)+"毫秒");
        }

    }


}
