package top.leejay.design.strategy.pay;

/**
 * @author xiaokexiang
 * @date 11/7/2019
 * 定义支付逻辑和支付类型
 */
public abstract class AbstractPayment {
    /**
     * 获取支付名
     *
     * @return return
     */
    public abstract String getName();

    /**
     * 获取余额
     *
     * @param uid 支付账户id
     * @return 余额
     */
    public abstract double getBalance(String uid);

    /**
     * 支付
     *
     * @param uid    账户id
     * @param amount 余额
     * @return 支付成功or失败
     */
    public PayState<String> pay(String uid, double amount) {
        if (getBalance(uid) < amount) {
            return new PayState<>(500, "支付失败", "余额不足");
        } else {
            return new PayState<>(200, "支付成功", "支付余额：" + amount);
        }
    }
}
