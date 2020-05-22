package top.leejay.netty;

import com.google.common.collect.Lists;
import com.google.common.primitives.Bytes;
import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.yaml.snakeyaml.util.ArrayUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
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
    void bio() throws Exception {
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


    /**
     * channel: 面对缓冲区，属于读写模式，普通的流只能读或者写。
     * byteBuffer: capacity： buffer的容量，最多能容纳的数量，一旦指定不会变
     * position： 读写指针，当前buffer读取或写到的位置
     * limit： 表示在写模式下最多能写多少数据
     * byteBuffer.flip(): 翻转buffer(写模式下触发)，limit=当前的position(即当前写了多少数据)，position = 0，开始从头开始读
     * byteBuffer.clear(): 不是真正的删除数据，可以理解成 复位（position=0, limit=capacity）
     */
    @Test
    void stringToFileByNio() throws Exception {
        List<String> list = Lists.newArrayList();
        for (int i = 1; i <= 30000000; i++) {
            list.add("1590117795737 你好呀你好呀你好呀你好呀 10.000000000000" + i + "\r\n");
        }
        FileOutputStream outputStream = new FileOutputStream("D:\\out.txt");
        FileChannel outChannel = outputStream.getChannel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(Integer.MAX_VALUE - 2);
        long start = System.currentTimeMillis();
        System.out.println("start time: " + start);
        for (int i = 1; i <= list.size(); i++) {
            // 添加数据到缓冲区
            byteBuffer.put(list.get(i - 1).getBytes());
            // 每100个元素就写一次数据
            if (i % 5000 == 0) {
                byteBuffer.flip();
                while (byteBuffer.hasRemaining()) {
                    // 将缓冲区数据写入通道
                    outChannel.write(byteBuffer);
                }
                // 清空buffer(只是设置)
                byteBuffer.clear();
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("end time: " + end);
        System.out.println("time: " + (System.currentTimeMillis() - start)); // 62450
        outChannel.close();
        outputStream.close();
        // 100000 176788
        // 10000 87755 116881 122976
        // 5000 125960 97927 122038
        // 1000 93895 112628
        // 500 162854 117830
        // 100 206634 131182
    }
}
