package top.leejay.design.proxy;

/**
 * @author xiaokexiang
 * @date 11/6/2019
 * 媒婆的客户
 */
public class Consumer implements Person {
    @Override
    public void findLove() {
        System.out.println("高富帅");
    }
}
