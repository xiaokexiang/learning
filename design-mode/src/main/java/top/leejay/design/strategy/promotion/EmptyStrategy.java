package top.leejay.design.strategy.promotion;

/**
 * @author xiaokexiang
 * @date 11/7/2019
 * 
 */
public class EmptyStrategy implements PromotionStrategy {
    @Override
    public void doPromotion() {
        System.out.println("无优惠");
    }
}
