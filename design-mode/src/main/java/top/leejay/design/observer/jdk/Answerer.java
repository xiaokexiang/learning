package top.leejay.design.observer.jdk;

import java.util.Observable;
import java.util.Observer;

/**
 * @author xiaokexiang
 * @date 11/11/2019
 * 
 */
public class Answerer implements Observer {
    private String name;

    public Answerer(String name) {
        this.name = name;
    }

    /**
     * @param o   发布者
     * @param arg 发布时的参数
     */
    @Override
    public void update(Observable o, Object arg) {
        Platform platform = (Platform) o;
        Question question = (Question) arg;
        System.out.println("==============");
        System.out.println(name + ": 您好！\n" + "您收到一个来自: " + platform.getPlatformName() + "的问题, 问题内容如下：" + question.getContent() + "\n提问者: " + question.getUsername());
    }
}
