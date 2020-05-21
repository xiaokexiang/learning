package top.leejay.netty;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Date;
import java.util.Locale;

@SpringBootTest
class NettyApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void nio() throws Exception {
        long start = System.currentTimeMillis();
        System.out.println("start time: " + start);
        FileInputStream inputStream = new FileInputStream("D:\\work\\projects\\learning\\in.txt");
        FileOutputStream outputStream = new FileOutputStream("D:\\out.txt");
        ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
        FileChannel inputStreamChannel = inputStream.getChannel();
        FileChannel outputStreamChannel = outputStream.getChannel();
        while (inputStreamChannel.read(buffer) != -1) {
            buffer.flip();
            outputStreamChannel.write(buffer);
            buffer.clear();
        }
        long end = System.currentTimeMillis();
        System.out.println("end time: " + end);
        System.out.println("time: " + (System.currentTimeMillis() - start)); // 586ms
    }

    @Test
    void bio() throws Exception{
        long start = System.currentTimeMillis();
        System.out.println("start time: " + start);
        FileInputStream inputStream = new FileInputStream("D:\\work\\projects\\learning\\in.txt");
        FileOutputStream outputStream = new FileOutputStream("D:\\out.txt");
        byte[] bytes = new byte[1024];
        while (inputStream.read(bytes) != -1) {
            outputStream.write(bytes);
        }
        outputStream.flush();
        outputStream.close();
        inputStream.close();
        long end = System.currentTimeMillis();
        System.out.println("end time: " + end);
        System.out.println("time: " + (System.currentTimeMillis() - start)); // 12482ms
    }
}
