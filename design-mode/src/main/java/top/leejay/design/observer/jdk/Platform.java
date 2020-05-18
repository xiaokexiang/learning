package top.leejay.design.observer.jdk;

import java.util.Observable;

/**
 * @author xiaokexiang
 * @date 11/11/2019
 * 平台: 模拟知乎PLATFORM 用户(发布者)提问题给某个答主(观察者) 适用jdk原生api
 */
public class Platform extends Observable {
    private static final String PLATFORM = "知乎";

    public String getPlatformName() {
        return PLATFORM;
    }

    /**
     * 写个线程安全的单例
     */
    private Platform() {
    }

    public static Platform getInstance() {
        return Holder.PLATFORM;
    }

    private static class Holder {
        private static final Platform PLATFORM = new Platform();
    }

    public void publishQuestion(Question question) {
        System.out.println(question.getUsername() + "在" + PLATFORM + "上提交了一个问题。");
        setChanged();
        // 唤醒观察者, 同时将参数传递
        notifyObservers(question);
    }

}
