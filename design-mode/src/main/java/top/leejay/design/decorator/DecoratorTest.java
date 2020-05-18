package top.leejay.design.decorator;

/**
 * @author xiaokexiang
 * @date 11/8/2019
 * 
 */
public class DecoratorTest {
    public static void main(String[] args) {
        AbstractBatterCake abstractBatterCake;
        abstractBatterCake = new BaseBatterCake();
        /*加个鸡蛋*/
        abstractBatterCake = new EggDecorator(abstractBatterCake);
        System.out.println(abstractBatterCake.getMsg() + ", 总价: " + abstractBatterCake.getPrice());
    }
}
