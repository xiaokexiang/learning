package top.leejay.design.decorator.clothes;

/**
 * @author xiaokexiang
 * @date 11/8/2019
 * 
 */
public class ClothesTest {
    public static void main(String[] args) {
        AbstractClothes clothes;
        clothes = new Tee();
        PantsDecorator pantsDecorator = new PantsDecorator(clothes);
        pantsDecorator.wear();
    }
}
