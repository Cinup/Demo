package io.client;

import lombok.SneakyThrows;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

public class Client2 {
    private final String EXIT_MESSAGE = "exit";
    private final String TEXT = "[%s]: %s";
    private String address = "127.0.0.1";
    private int port = 8080;
    private String charset = "UTF-8";

    public static void main(String[] args) throws IOException {
        new Client2().start();
    }

    public void start() throws IOException {
        Selector selector = Selector.open();
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress(address, port));
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
        Scanner scanner = new Scanner(System.in);
        System.out.print("请输入您的昵称: ");
        String name = scanner.nextLine();
        new Thread(new ReadHandler(selector)).start();
        System.out.println("请输入发送的消息: ");
        while (scanner.hasNextLine()) {
            String message = scanner.nextLine();
            if (!EXIT_MESSAGE.equals(message)) {
                socketChannel.write(Charset.forName(charset).encode(String.format(TEXT, name, message)));
            }
        }
        socketChannel.close();
    }

    class ReadHandler implements Runnable {
        private Selector selector;

        public ReadHandler(Selector selector) {
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
                        StringBuilder message = new StringBuilder();
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        while (channel.read(buffer) > 0) {
                            buffer.flip();
                            message.append(Charset.forName(charset).decode(buffer));
                        }
                        System.out.println(message);
                        channel.register(selector, SelectionKey.OP_READ);
                    }
                }
            }
        }
    }
}
