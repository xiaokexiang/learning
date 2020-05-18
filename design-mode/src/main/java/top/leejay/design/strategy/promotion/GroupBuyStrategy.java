package top.leejay.design.strategy.promotion;

/**
 * @author xiaokexiang
 * @date 11/7/2019
 * 
 */
public class GroupBuyStrategy implements PromotionStrategy {
    @Override
    public void doPromotion() {
        System.out.println("拼团优惠");
    }
}
