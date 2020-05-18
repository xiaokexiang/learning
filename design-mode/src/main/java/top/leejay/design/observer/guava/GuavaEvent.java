package top.leejay.design.observer.guava;

import com.google.common.eventbus.Subscribe;

/**
 * @author xiaokexiang
 * @date 11/11/2019
 * google jar
 * <dependency>
 * <groupId>com.google.guava</groupId>
 * <artifactId>guava</artifactId>
 * <version>20.0</version>
 * </dependency>
 */
public class GuavaEvent {

    @Subscribe // 以后可能删除
    public void subscribe(String s) {
        // 业务逻辑
        System.out.println("执行subscribe方法, 传入的参数是: " + s);
    }
}
