package top.leejay.interview.question12;

/**
 * @author xiaokexiang
 * @date 3/27/2020
 * 设计: read & read 不冲突；read & write 冲突； write & write 冲突
 * 获取读取锁的时候： 1.如果有线程正在执行写入则等待。 2.如果有线程正在执行读取则无需等待。
 * 获取写入锁的时候： 1.如果有线程正在执行写入则等待。 2.如果有线程正在执行读读取则等待。
 */
public class ReadWriteLocks {
    /**
     * 实际正在读取中的线程个数
     */
    private int readingReaders = 0;

    /**
     * 正在等待写入的线程个数
     */
    private int waitingWriters = 0;

    /**
     * 实际正在写入的线程个数
     */
    private int writingWriters = 0;

    /**
     * 写入优先 该字段的作用就是为了保证read执行完后尽量执行write，write执行完后尽量执行read
     */
    private boolean preferWriter = true;

    public synchronized void readLock() throws InterruptedException {
        while (writingWriters > 0 || (preferWriter && waitingWriters > 0)) {
            wait();
        }
        readingReaders++;
    }

    public synchronized void readUnlock() {
        readingReaders--;
        preferWriter = true;
        notifyAll();
    }

    public synchronized void writeLock() throws InterruptedException {
        waitingWriters++;
        try {
            while (readingReaders > 0 || writingWriters > 0) {
                wait();
            }
        } finally {
            waitingWriters--;
        }
        writingWriters++;
    }

    public synchronized void writeUnlock() {
        writingWriters--;
        preferWriter = false;
        notifyAll();
    }
}
