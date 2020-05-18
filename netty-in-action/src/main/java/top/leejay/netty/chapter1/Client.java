package top.leejay.netty.chapter1;

import lombok.SneakyThrows;

import java.io.*;
import java.net.Socket;

/**
 * @author xiaokexiang
 * @date 4/8/2020
 * 客户端 模拟请求 1s/发送信息并接受server信息
 */
@SuppressWarnings("all")
public class Client {

    @SneakyThrows
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("127.0.0.1", 9000);
        socket.setTcpNoDelay(true);
        PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        while (true) {
            Thread.sleep(1000);
            // 输出内容到server
            writer.println("i'm client ... " + System.currentTimeMillis());
            // 获取server返回的内容
            String message = reader.readLine();
            System.out.println("get message from server: " + message);
        }
    }
}