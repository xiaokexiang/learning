package top.leejay.design.proxy;

/**
 * @author xiaokexiang
 * @date 11/6/2019
 * 目标对象
 */
public class Son implements Person {

    @Override
    public void findLove() {
        System.out.println("儿子要求肤白貌美大长腿");
    }
}
