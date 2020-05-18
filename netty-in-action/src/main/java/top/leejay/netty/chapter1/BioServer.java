package top.leejay.netty.chapter1;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author xiaokexiang
 * @date 4/8/2020
 * Bio Server服务(通过多线程实现伪异步)
 * 1s/发送信息并接受client信息
 */
@SuppressWarnings("all")
public class BioServer {
    // 创建线程池，这样在接收到请求的时候实现伪异步
    private static final Executor THREAD_POOL = Executors.newFixedThreadPool(4);

    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        BufferedWriter writer = null;
        BufferedReader reader = null;

        try {
            // 启动socket server 端口号是 9000
            serverSocket = new ServerSocket(9000);
            while (true) {
                // 如果没有连接会一直阻塞在这里, 直到有连接
                Socket socket = serverSocket.accept();
                System.out.println("client connected ...");
                // 一个请求进来就让线程池去执行任务
                THREAD_POOL.execute(new PrintTimeTask(socket));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                serverSocket.close();
                writer.close();
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}