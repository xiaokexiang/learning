package top.leejay.interview.question6;

/**
 * @author xiaokexiang
 * @date 3/23/2020
 * Client -> putRequest -> requestQueue
 * Server -> getRequest -> requestQueue
 * 此线程设计模式：Guarded Suspension
 */
public class Request {
    private final String name;

    public Request(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Request{" +
                "name='" + name + '\'' +
                '}';
    }
}
