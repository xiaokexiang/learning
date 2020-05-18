package top.leejay.interview.question12;

/**
 * @author xiaokexiang
 * @date 3/27/2020
 */
@SuppressWarnings("all")
public interface Data {

    char[] read() throws InterruptedException;

    void write(char c) throws InterruptedException;
}
