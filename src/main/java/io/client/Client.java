package io.client;

import lombok.SneakyThrows;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

public class Client {
    private String address = "127.0.0.1";
    private int port = 8080;

    public static void main(String[] args) throws IOException, InterruptedException {
//        new Client().start();
        new Client().new NioClient().start();
    }


    public void start() throws IOException, InterruptedException {
        Socket socket = new Socket(address, port);
        OutputStream output = socket.getOutputStream();
        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入发送的消息:");
        String message = scanner.nextLine();
        while (!message.equals("exit")) {
            output.write(message.getBytes(Charset.forName("UTF-8")));
            output.flush();
            message = scanner.nextLine();
        }
        System.out.println("客户端退出");
        output.write(message.getBytes(Charset.forName("UTF-8")));
        output.flush();
        scanner.close();
        socket.close();
    }

    public class NioClient {
        private String name;

        public void start() throws IOException {
            Selector selector = Selector.open();
            SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress(address, port));
            socketChannel.configureBlocking(false);
            socketChannel.register(selector, SelectionKey.OP_READ);
            Scanner scanner = new Scanner(System.in);
            System.out.print("请输入您的昵称:");
            String name = scanner.nextLine();
            this.name = name;
            new Thread(new NioHandler(selector)).start();
            System.out.println("请输入发送的消息:");
            while (scanner.hasNextLine()) {
                String request = scanner.nextLine();
                if (request != null && request.length() > 0) {
                    socketChannel.write(Charset.forName("UTF-8").encode("[name]: " + request));
                }
            }
            socketChannel.close();
        }
    }

    class NioHandler implements Runnable {
        private Selector selector;

        public NioHandler(Selector selector) {
            this.selector = selector;
        }

        @SneakyThrows
        @Override
        public void run() {
            while (true) {
                int ready = selector.select();
                if (ready < 1) {
                    continue;
                }
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();
                    iterator.remove();
                    if (selectionKey.isReadable()) {
                        SocketChannel channel = (SocketChannel) selectionKey.channel();
                        String message = "";
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        while (channel.read(buffer) > 0) {
                            buffer.flip();
                            message += Charset.forName("UTF-8").decode(buffer);
                        }
                        System.out.println(message);
                        channel.register(selector, SelectionKey.OP_READ);
                    }
                }
            }
        }
    }
}
