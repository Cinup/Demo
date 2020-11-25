package io.client;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Scanner;

public class Client1 {
    private final String EXIT_MESSAGE = "exit";
    private String address = "127.0.0.1";
    private int port = 8080;
    private String charset = "UTF-8";

    public static void main(String[] args) throws IOException {
        new Client1().start();
    }

    public void start() throws IOException {
        Socket socket = new Socket(address, port);
        System.out.println("客户端启动成功");
        OutputStream output = socket.getOutputStream();
        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入发送的消息: ");
        String message = scanner.nextLine();
        while (!EXIT_MESSAGE.equals(message)) {
            sendMessage(output, message);
            message = scanner.nextLine();
        }
        sendMessage(output, message);
        System.out.println("客户端退出");
        scanner.close();
        socket.close();
    }

    private void sendMessage(OutputStream output, String message) throws IOException {
        output.write(message.getBytes(Charset.forName(charset)));
        output.flush();
    }
}
