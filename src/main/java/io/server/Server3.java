package io.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

public class Server3 {
    private int port = 8080;
    private int timeout = 1000;
    private int connectionSize = 1024;
    private int bufferSize = 1024;
    private String charset = "UTF-8";

    public static void main(String[] args) throws IOException {
        new Server3().start();
    }

    /*
     * @description: 非阻塞式IO,一个线程处理多个连接
     */
    public void start() throws IOException {
        //1.创建Selector
        Selector selector = Selector.open();
        //2.1.创建ServerSocketChannel
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        //2.2.绑定端口
        serverSocketChannel.socket().bind(new InetSocketAddress(port), connectionSize);
        //2.3.设置为非阻塞
        serverSocketChannel.configureBlocking(false);
        //3.注册ServerSocketChannel,监听连接事件
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("服务端启动成功");

        while (true) {
            int readyCount = selector.select(timeout);
            if (readyCount == 0) continue;

            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                iterator.remove();
                //处理连接事件
                if (selectionKey.isAcceptable()) {
                    handleAccept(selector, serverSocketChannel);
                }
                //处理读事件
                if (selectionKey.isReadable()) {
                    handleRead(selector, selectionKey);
                }
            }
        }
    }

    /*
     * @description: 处理客户端的连接
     */
    private void handleAccept(Selector selector, ServerSocketChannel serverSocketChannel) throws IOException {
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
        System.out.println("收到新连接: " + socketChannel.toString());
    }

    /*
     * @description: 处理客户端发送来的消息
     */
    private void handleRead(Selector selector, SelectionKey selectionKey) throws IOException {
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
        StringBuilder result = new StringBuilder();
        while (socketChannel.read(buffer) > 0) {
            buffer.flip();
            result.append(Charset.forName(charset).decode(buffer));
            buffer.compact();
        }
        socketChannel.register(selector, SelectionKey.OP_READ);
        if (result.length() > 0) {
            System.out.println("收到消息: " + result);
            broadcast(selector, socketChannel, result.toString());
        }
    }

    /*
     * @description: 广播消息给其他客户端
     */
    private void broadcast(Selector selector, SocketChannel sourceChannel, String message) {
        Set<SelectionKey> selectionKeys = selector.keys();
        selectionKeys.forEach(selectionKey -> {
            Channel targetChanel = selectionKey.channel();
            if (targetChanel instanceof SocketChannel && targetChanel != sourceChannel) {
                try {
                    ((SocketChannel) targetChanel).write(Charset.forName(charset).encode(message));
                } catch (IOException e) {

                }
            }
        });
    }
}
