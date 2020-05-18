package top.leejay.design.decorator;

/**
 * @author xiaokexiang
 * @date 11/8/2019
 * 煎饼装饰抽象类
 */
public abstract class AbstractBatterCakeDecorator extends AbstractBatterCake {

    private AbstractBatterCake abstractBatterCake;

    public AbstractBatterCakeDecorator(AbstractBatterCake abstractBatterCake) {
        this.abstractBatterCake = abstractBatterCake;
    }

    @Override
    protected String getMsg() {
        return abstractBatterCake.getMsg();
    }

    @Override
    protected int getPrice() {
        return abstractBatterCake.getPrice();
    }
}
