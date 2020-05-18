package top.leejay.design.decorator.clothes;

/**
 * @author xiaokexiang
 * @date 11/8/2019
 * 
 *      抽象类
 *        |
 * 被装饰类    装饰抽象类
 *               |
 *          具体的装饰类
 */
public abstract class AbstractClothes {
    /**
     * 穿衣服
     */
    protected abstract void wear();
}
