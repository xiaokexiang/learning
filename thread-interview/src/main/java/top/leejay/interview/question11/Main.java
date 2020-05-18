package top.leejay.interview.question11;

import java.util.concurrent.Exchanger;

/**
 * @author xiaokexiang
 * @date 3/26/2020
 */
public class Main {
    public static void main(String[] args) {
        Exchanger<char[]> exchanger = new Exchanger<>();
        char[] buffer1 = new char[10];
        char[] buffer2 = new char[10];
        new ProducerThread("producer", exchanger, buffer1).start();
        new ConsumerThread("consumer", exchanger, buffer2).start();
    }
}
