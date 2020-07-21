package top.leejay.learning.algorithm;

import java.util.Arrays;

/**
 * 基于数组实现的队列：队首出队，队尾入队。
 * 循环队列,每次移动队首或队尾，都需要重新计算队首队尾的index
 */
public class NumberQueue {
    private int[] array;
    private int head;// head的index
    private int tail;// tail的index

    public NumberQueue(int size) {
        this.array = new int[size];
    }

    /**
     * 出队
     */
    int deQueue() {
        // 说明此时队列为空
        if (tail == head) {
            throw new IndexOutOfBoundsException("Queue is empty");
        }
        int i = array[head];
        array[head] = 0;
        head = newHead();
        return i;
    }

    /**
     * 入队
     */
    void enQueue(int value) {
        // 如果tail的下个节点就是head那么说明此时队列满了
        if (newTail() == head) {
            throw new IndexOutOfBoundsException("Queue is max");
        }
        array[tail] = value;
        tail = newTail();
    }

    @Override
    public String toString() {
        return "NumberQueue{" +
                "array=" + Arrays.toString(array) +
                ", head=" + head +
                ", tail=" + tail +
                '}';
    }

    /**
     * 计算新队尾的地址
     *
     * @return index
     */
    int newTail() {
        return (tail + 1) % array.length;
    }

    /**
     * 计算新队首的地址
     *
     * @return index
     */
    int newHead() {
        return (head + 1) % array.length;
    }

    public static void main(String[] args) {
        NumberQueue numberQueue = new NumberQueue(8);
        for (int i = 0; i < 7; i++) {
            numberQueue.enQueue(i + 5);
        }
        System.out.println(numberQueue.toString());
        System.out.println(numberQueue.deQueue());
        System.out.println(numberQueue.deQueue());
        System.out.println(numberQueue.deQueue());
        numberQueue.enQueue(8);
        numberQueue.enQueue(9);
        System.out.println(numberQueue.toString());
    }

}
