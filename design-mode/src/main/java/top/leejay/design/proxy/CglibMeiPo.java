package top.leejay.design.proxy;

import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.InvocationHandler;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * @author xiaokexiang
 * @date 11/6/2019
 * 
 */
public class CglibMeiPo implements MethodInterceptor {

    private Object target;

    public CglibMeiPo(Object target) {
        this.target = target;
    }

    public Object getInstance() {
        Class<?> clazz = target.getClass();
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clazz);
        enhancer.setCallback(this);
        return enhancer.create();
    }

    public void before() {
        System.out.println("我是媒婆，我给你物色对象，讲出你的需求");
        System.out.println("开始物色");
    }

    public void after() {
        System.out.println("合适就办事");
    }

    /**
     * @param o           目标对象
     * @param method      目标对象的方法
     * @param objects     目标对象方法的参数
     * @param methodProxy 代理对象的方法(与目标对象的方法对应)
     * @return return
     * @throws Throwable Throwable
     */
    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        before();
        /*
        父类就是目标对象
        invokeSuper方法的作用就是获取代理类对应的FastClass
        FastClass: 在第一次执行MethodProxy.invoke() or invokeSuper() 方法时生成, 包括代理类和被代理类的FastClass并放在缓存中
        这个类会为代理类或被代理类的方法生成一个index; 这个index作为入参, FastClass就可以直接定位要调用的方法进行直接调用, 免去反射调用的烦恼
        所以FastClass也是Cglib比JDK更快的原因
         */
        Object obj = methodProxy.invokeSuper(o, objects);
        after();
        return obj;
    }
}
