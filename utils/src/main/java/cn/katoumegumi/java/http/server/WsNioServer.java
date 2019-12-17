package cn.katoumegumi.java.http.server;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

public class WsNioServer {

    public static void main(String[] args) {
        try {
            Selector selector = Selector.open();
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(1996));
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            while (true) {

                while (selector.selectNow() > 0) {
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    System.out.println(selectionKeys.size());
                    Iterator<SelectionKey> selectionKeyIterator = selectionKeys.iterator();
                    while (selectionKeyIterator.hasNext()) {
                        SelectionKey selectionKey = selectionKeyIterator.next();
                        if (selectionKey.isAcceptable()) {
                            System.out.println("连接开始");
                            SocketChannel socketChannel = serverSocketChannel.accept();
                            if (socketChannel != null) {
                                socketChannel.configureBlocking(false);
                                socketChannel.register(selector, SelectionKey.OP_READ);
                            }
                            System.out.println("连接完成");
                        } else if (selectionKey.isReadable()) {
                            System.out.println("读取开始");
                            SocketChannel socketChannel = (SocketChannel) selectionKey.channel();

                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                            WritableByteChannel writableByteChannel = Channels.newChannel(byteArrayOutputStream);

                            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                            while (socketChannel.read(byteBuffer) <= 0) {
                                byteBuffer.flip();
                                while (byteBuffer.hasRemaining()) {
                                    writableByteChannel.write(byteBuffer);
                                }
                                byteBuffer.clear();
                            }
                            System.out.println(new String(byteArrayOutputStream.toByteArray()));
                            socketChannel.register(selector, SelectionKey.OP_WRITE);
                            System.out.println("读取完成");
                        } else if (selectionKey.isWritable()) {
                            System.out.println("写入开始");
                            SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                            String httpResponse = "HTTP/1.1 200 OK\r\n" +
                                    "Content-Length: 38\r\n" +
                                    "Content-Type: text/html\r\n" +
                                    "\r\n" +
                                    "<html><body>Hello World!</body></html>";
                            socketChannel.write(ByteBuffer.wrap(httpResponse.getBytes()));
                            socketChannel.close();
                            System.out.println("写入完成");
                        }
                        System.out.println("删除标志");
                        selectionKeyIterator.remove();
                    }
                }

            }



        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
