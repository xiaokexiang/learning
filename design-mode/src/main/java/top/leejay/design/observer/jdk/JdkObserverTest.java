package top.leejay.design.observer.jdk;

/**
 * @author xiaokexiang
 * @date 11/11/2019
 * 
 */
public class JdkObserverTest {
    public static void main(String[] args) {
        Platform instance = Platform.getInstance();
        // 构建问题
        Question question = new Question("小明", "为什么地球是⚪的");

        // 构建观察者
        Answerer answerer1 = new Answerer("韩梅梅");
        Answerer answerer2 = new Answerer("李雷");

        // 给发布者添加观察者
        instance.addObserver(answerer1);
        instance.addObserver(answerer2);

        // 发布问题
        instance.publishQuestion(question);
    }
}
