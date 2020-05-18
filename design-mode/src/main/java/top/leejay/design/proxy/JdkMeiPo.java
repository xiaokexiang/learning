package top.leejay.design.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author xiaokexiang
 * @date 11/6/2019
 * 
 */
public class JdkMeiPo {


    private Object target;

    public JdkMeiPo(Object target) {
        this.target = target;
    }

    public void before() {
        System.out.println("我是媒婆，我给你物色对象，讲出你的需求");
        System.out.println("开始物色");
    }

    public void after() {
        System.out.println("合适就办事");
    }

    public Object getInstance() {
        Class<?> clazz = target.getClass();
        return Proxy.newProxyInstance(clazz.getClassLoader(), clazz.getInterfaces(), (proxy, method, args) -> {
            before();
            Object invoke = method.invoke(target, args);
            after();
            return invoke;
        });
    }
}
