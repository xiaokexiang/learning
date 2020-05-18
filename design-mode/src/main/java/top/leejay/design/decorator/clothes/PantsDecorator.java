package top.leejay.design.decorator.clothes;

/**
 * @author xiaokexiang
 * @date 11/8/2019
 * 
 */
public class PantsDecorator extends AbstractClothesDecorator {

    public PantsDecorator(AbstractClothes abstractPerson) {
        super(abstractPerson);
    }

    @Override
    protected void wear() {
        super.wear();
        System.out.println("穿裤子。。。");
    }
}
