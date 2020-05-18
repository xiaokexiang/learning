package top.leejay.interview.question6;

/**
 * @author xiaokexiang
 * @date 3/24/2020
 */
@SuppressWarnings("all")
public interface RequestQueue<E> {

    E getRequest() throws InterruptedException;

    void putRequest(E e) throws InterruptedException;
}
