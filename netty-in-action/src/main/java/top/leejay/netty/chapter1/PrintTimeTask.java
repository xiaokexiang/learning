package top.leejay.netty.chapter1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * @author xiaokexiang
 * @date 4/8/2020
 * 执行打印任务
 */
public class PrintTimeTask implements Runnable {
    private Socket socket;

    PrintTimeTask(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        PrintWriter writer = null;
        BufferedReader reader = null;
        try {
            // 获取输入输出流
            writer = new PrintWriter(this.socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // 获取client输入的内容
            String message;
            while (null != (message = reader.readLine())) {
                Thread.sleep(1000);
                if (0 == message.length()) {
                    break;
                }
                System.out.println("get message from client: " + message);
                writer.println("i'm server ... " + System.currentTimeMillis());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            assert writer != null;
            writer.close();
            try {
                assert reader != null;
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}