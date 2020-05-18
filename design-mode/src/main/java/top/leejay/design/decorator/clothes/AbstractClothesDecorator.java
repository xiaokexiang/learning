package top.leejay.design.decorator.clothes;

/**
 * @author xiaokexiang
 * @date 11/8/2019
 * 装饰抽象类
 */
public abstract class AbstractClothesDecorator extends AbstractClothes {
    private AbstractClothes abstractPerson;

    public AbstractClothesDecorator(AbstractClothes abstractPerson) {
        this.abstractPerson = abstractPerson;
    }

    @Override
    protected void wear() {
        abstractPerson.wear();
    }
}
