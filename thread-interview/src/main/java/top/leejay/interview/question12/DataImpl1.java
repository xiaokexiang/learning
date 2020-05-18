package top.leejay.interview.question12;

/**
 * @author xiaokexiang
 * @date 3/27/2020
 * 数据类 通过read-write lock进行读写data操作
 */
public class DataImpl1 extends AbstractData {
    /**
     * 使用synchronized实现的read-write lock
     */
    private final ReadWriteLocks lock = new ReadWriteLocks();

    public DataImpl1(int size) {
        super(size);
    }

    /**
     * 和ReentrantLock相同
     */
    @Override
    public char[] read() throws InterruptedException {
        lock.readLock();
        try {
            return doRead();
        } finally {
            lock.readUnlock();
        }
    }

    @Override
    public void write(char c) throws InterruptedException {
        lock.writeLock();
        try {
            doWrite(c);
        } finally {
            lock.writeUnlock();
        }
    }
}
