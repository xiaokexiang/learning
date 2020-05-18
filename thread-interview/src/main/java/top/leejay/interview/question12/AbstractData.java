package top.leejay.interview.question12;

import java.util.Arrays;

/**
 * @author xiaokexiang
 * @date 3/27/2020
 */
public abstract class AbstractData implements Data {

    private final char[] buffer;

    public AbstractData(int size) {
        this.buffer = new char[size];
        // 初始默认全部填充'*'
        Arrays.fill(buffer, '*');
    }

    @Override
    public abstract char[] read() throws InterruptedException;

    @Override
    public abstract void write(char c) throws InterruptedException;

    protected char[] doRead() {
        char[] newBuffer = new char[buffer.length];
        // char[] 拷贝 buffer[] -> newBuffer[]
        System.arraycopy(buffer, 0, newBuffer, 0, newBuffer.length);
        slowly();
        return newBuffer;
    }

    protected void doWrite(char c) {
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = c;
            slowly();
        }
        System.out.println(Thread.currentThread().getName() + " write " + String.valueOf(buffer));
    }

    private void slowly() {
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
