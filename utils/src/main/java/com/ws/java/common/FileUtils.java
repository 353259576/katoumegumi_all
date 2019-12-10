package com.ws.java.common;

import com.ws.java.http.client.utils.WsSSLContext;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Base64;
@Slf4j
public class FileUtils {
    public static void main(String[] args) {
        //DelayQueue delayQueue = new DelayQueue();
        /*try {
            FileInputStream fileInputStream = new FileInputStream("D:/图片/1.png");
            System.out.println(fileSave(fileInputStream,"D:/图片/ws1.png"));
        }catch (Exception e){
            e.printStackTrace();
        }*/

       /* ByteBuffer byteBuffer = ByteBuffer.allocate(1000);
        byteBuffer.compact();
        System.out.println(byteBuffer);*/

        /*try {
            //Socket socket = new Socket("47.99.96.245",443);
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.connect(new InetSocketAddress("47.99.96.245", 443));
            //SocketChannel socketChannel = ((SSLSocket)SSLSocketFactory.getDefault().createSocket("47.99.96.245",443)).getChannel();
            String sb = "POST https://m.lieshouupin.com/mobile/activity/selectSign?url=https%3A%2F%2Fm.lieshouupin.com%2F HTTP/1.1\n" +
                    "Host: m.lieshouupin.com\n" +
                    "Connection: keep-alive\n" +
                    "Content-Length: 0\n" +
                    "Accept: application/json\n" +
                    "Origin: https://m.lieshouupin.com\n" +
                    "X-Requested-With: XMLHttpRequest\n" +
                    "User-Agent: Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36\n" +
                    "Referer: https://m.lieshouupin.com/\n" +
                    "Accept-Encoding: gzip, deflate, br\n" +
                    "Accept-Language: zh-CN,zh;q=0.9\n" +
                    "Cookie: UM_distinctid=1661fa1b08dc0-0d1a2057d9c28-3c604504-1fa400-1661fa1b08e27d; Hm_lvt_146c81b5845ddac67ace5e9dc6d1f0fc=1538289550; username=map; Hm_lvt_2fa6c3d7b1e995a56e8f3c4a42e57ee7=1538128967,1539239822; CNZZDATA1273417027=977168498-1538126232-https%253A%252F%252Fm.lieshouupin.com%252F%7C1539239828; Hm_lpvt_2fa6c3d7b1e995a56e8f3c4a42e57ee7=1539249754\n" +
                    "\n";
            ByteBuffer byteBuffer = ByteBuffer.wrap(sb.getBytes());
            while (byteBuffer.hasRemaining()) {
                socketChannel.write(byteBuffer);
            }
            byteBuffer.clear();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            WritableByteChannel writableByteChannel = Channels.newChannel(byteArrayOutputStream);
            while (socketChannel.read(byteBuffer) != -1) {
                byteBuffer.flip();
                while (byteBuffer.hasRemaining()) {
                    writableByteChannel.write(byteBuffer);
                }
                byteBuffer.clear();
            }
            byte[] bytes = byteArrayOutputStream.toByteArray();
            System.out.println(new String(bytes));
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        /*File file = createFile("D:\\新建文件夹\\19.jpg");
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            //FileOutputStream fileOutputStream = new FileOutputStream("D:\\新建文件夹\\19copy.jpg");
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            WsStreamUtils.inputToOutput(fileInputStream,byteArrayOutputStream);
            byte bytes[] = byteArrayOutputStream.toByteArray();
            try {
                System.out.println(new String(bytes,"ASCII"));
            }catch (UnsupportedEncodingException e){
                e.printStackTrace();
            }

        }catch (FileNotFoundException e){
            e.printStackTrace();
        }
*/

        try {
            /*File file = createFile("F://1.jpg");
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            FileChannel fileChannel = fileOutputStream.getChannel();
            byte bytes[] = Base64.getMimeDecoder().decode(str.getBytes());
            ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
            fileChannel.write(byteBuffer);
            fileChannel.close();
            fileOutputStream.flush();
            fileOutputStream.close();*/
            File file = new File("F:\\新建文件夹 (4)\\19.jpg");
            FileInputStream fileInputStream = new FileInputStream(file);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            FileChannel fileChannel = fileInputStream.getChannel();
            WritableByteChannel writableByteChannel = Channels.newChannel(byteArrayOutputStream);
            fileChannel.transferTo(0,fileChannel.size(),writableByteChannel);
            fileChannel.close();
            writableByteChannel.close();
            byte bytes[] = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.flush();
            byteArrayOutputStream.close();
            fileInputStream.close();
            String  base64Str = new String(Base64.getEncoder().encode(bytes),"utf-8");
            System.out.println(base64Str);
            File outFile = createFile("F:\\新建文件夹 (4)\\2.txt");
            FileOutputStream fileOutputStream = new FileOutputStream(outFile);
            fileOutputStream.write(base64Str.getBytes("utf-8"));
            fileOutputStream.close();
        }catch (IOException e){
            e.printStackTrace();
        }


    }


    public static File createFile(String filePath){
        File file = new File(filePath);
        if(file.exists()){
            return file;
        }else {
            if(filePath.endsWith("/")||filePath.endsWith("\\")){
                file.mkdirs();
                return file;
            }else {
                int position1 = filePath.lastIndexOf('\\');
                int position2 = filePath.lastIndexOf('/');
                int position = Math.max(position1,position2);
                String path = filePath.substring(0,position);
                File fPath = new File(path);
                fPath.mkdirs();
                try {
                    file.createNewFile();
                }catch (IOException e){
                    e.printStackTrace();
                }
                return file;
            }
        }

    }





}
