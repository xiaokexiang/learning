package top.leejay.design.strategy.promotion;

/**
 * @author xiaokexiang
 * @date 11/7/2019
 * 促销活动方案类(多态)
 */
public class PromotionActivity {
    private PromotionStrategy promotionStrategy;

    public PromotionActivity(PromotionStrategy promotionStrategy) {
        this.promotionStrategy = promotionStrategy;
    }

    public void exec() {
        promotionStrategy.doPromotion();
    }
}

class PromotionStrategyTest {
    public static void main(String[] args) {
        CashBackStrategy cashBackStrategy = new CashBackStrategy();
        PromotionActivity promotionActivity = new PromotionActivity(cashBackStrategy);
        promotionActivity.exec();

        System.out.println("-------------------------");

        PromotionActivity activity = new PromotionActivity(PromotionStrategyFactory.getPromotionStrategy("COUPON"));
        activity.exec();
    }
}
