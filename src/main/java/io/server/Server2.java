package io.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server2 {
    private ExecutorService executorService = Executors.newCachedThreadPool();
    private int port = 8080;
    private String charset = "UTF-8";

    public static void main(String[] args) throws IOException {
        new Server2().start();
    }

    /*
     * @description: 使用线程池处理阻塞式IO,使服务端可以同时处理多个请求
     */
    public void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("服务器启动成功");
        while (!serverSocket.isClosed()) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("收到新连接：" + clientSocket.toString());
            executorService.submit(
                    () -> {
                        try {
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
                        } catch (IOException e) {
                        } finally {
                            try {
                                clientSocket.close();
                            } catch (IOException e) {
                            }
                        }
                    });
        }
        serverSocket.close();
    }
}
