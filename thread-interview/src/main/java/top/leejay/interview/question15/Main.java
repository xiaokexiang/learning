package top.leejay.interview.question15;

/**
 * @author xiaokexiang
 * @date 3/28/2020
 * WorkerThread 工人线程会取回工作进行处理，当所有工作全部完成后，工人线程会等待新的工作到来
 * ClientThread 向Channel类发送工作请求
 * Channel 雇佣了五个工人线程进行工作，接受工作请求并将工作交给工人
 * Request 表示工作请求的类
 * <p>
 * ClientThread <------> Channel <-------> WorkerThread
 * ⬇
 * Request
 */
public class Main {
    public static void main(String[] args) {
        Channel channel = new Channel(5);
        channel.startWorkers();
        new ClientThread("Alice", channel).start();
        new ClientThread("Bobby", channel).start();
        new ClientThread("Chris", channel).start();
    }
}
