package top.leejay.interview.question14;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

/**
 * @author xiaokexiang
 * @date 3/28/2020
 * 模拟小型服务器 ServerSocket
 */
@SuppressWarnings("all")
public class MiniServer {

    private final int port;

    public MiniServer(int port) {
        this.port = port;
    }


    public void execute() throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Listening on " + serverSocket);

        try {
            while (true) {
                System.out.println("Accepting");
                // 阻塞直到一个连接
                Socket client = serverSocket.accept();
                // 改成伪异步(和netty不同)
                System.out.println("Connected to " + client);
                new Thread(() -> {
                    try {
                        Task.service(client);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            serverSocket.close();
        }
    }
}

class Task {
    public static void service(Socket client) throws IOException {
        System.out.println(Thread.currentThread().getName() + ": Task.service(" + client + ") BEGIN");
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(client.getOutputStream());
            dataOutputStream.writeBytes("HTTP/1.0 200 OK\r\n");
            dataOutputStream.writeBytes("Content-type: text/html\r\n");
            dataOutputStream.writeBytes("\r\n");
            dataOutputStream.writeBytes("<html><head><title>CountDown</title></head><body>");
            dataOutputStream.writeBytes("<h1>CountDown Start!</h1>");
            for (int i = 10; i >= 0; i--) {
                System.out.println(Thread.currentThread().getName() + ": CountDown i = " + i);
                dataOutputStream.writeBytes("<h1>" + i + "</h1>");
                dataOutputStream.flush();
                try {
                    Thread.sleep(new Random().nextInt(2000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                dataOutputStream.writeBytes("</body></html>");
            }
        } finally {
            client.close();
        }
        System.out.println(Thread.currentThread().getName() + ": Task.do(" + client + ") END");
    }
}
