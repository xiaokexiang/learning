package top.leejay.design.proxy;

import org.springframework.cglib.core.DebuggingClassWriter;

/**
 * @author xiaokexiang
 * @date 11/6/2019
 * 
 */
public class ProxyTest {
    public static void main(String[] args) {
        JdkMeiPo jdkMeiPo = new JdkMeiPo(new Consumer());
        Person instance = (Person) jdkMeiPo.getInstance();
        instance.findLove();
        System.out.println("===================");
        /*将Cglib代码class文件生成到指定目录下*/
        System.setProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY, "D://");
        CglibMeiPo cglibMeiPo = new CglibMeiPo(new CglibConsumer());
        CglibConsumer cglibConsumer = (CglibConsumer) cglibMeiPo.getInstance();
        /*执行流程: this.findLove() -> intercept() -> methodProxy.invokeProxy() -> target.findLove()*/
        cglibConsumer.findLove();
    }
}
