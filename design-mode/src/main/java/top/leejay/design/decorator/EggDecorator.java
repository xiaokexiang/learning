package top.leejay.design.decorator;

/**
 * @author xiaokexiang
 * @date 11/8/2019
 * 加个鸡蛋的装饰类
 */
public class EggDecorator extends AbstractBatterCakeDecorator {
    public EggDecorator(AbstractBatterCake abstractBatterCake) {
        super(abstractBatterCake);
    }

    @Override
    protected String getMsg() {
        return super.getMsg() + "+1个鸡蛋";
    }

    @Override
    protected int getPrice() {
        return super.getPrice() + 1;
    }
}
