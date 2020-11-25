package io.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;


public class Server1 {
    private int port = 8080;
    private String charset = "UTF-8";

    public static void main(String[] args) throws IOException {
        new Server1().start();
    }

    /*
     * @description: 阻塞式IO,当一个客户端连接上服务端时,服务端会被阻塞住,在此期间不能服务端处理其他的客户端连接,直到客户端退出
     */
    public void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("服务器启动成功");
        while (!serverSocket.isClosed()) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("收到新连接：" + clientSocket.toString());

            BufferedReader bufferedReader =
                    new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), charset));
            char[] buf = new char[1024];
            for (int i = bufferedReader.read(buf); i != -1; i = bufferedReader.read(buf)) {
                String message = new String(buf, 0, i);
                if (message.equals("exit")) {
                    System.out.println("客户端退出");
                    break;
                }
                System.out.print("收到数据，来自：" + clientSocket.toString());
                System.out.println("消息内容为：" + message);
            }
            clientSocket.close();
        }
        serverSocket.close();
    }
}

