package top.leejay.design.strategy.pay;

/**
 * @author xiaokexiang
 * @date 11/7/2019
 * 
 */
public class Order {
    private String uid;
    private double amount;

    public Order(String uid, double amount) {
        this.uid = uid;
        this.amount = amount;
    }

    public PayState<String> pay() {
        return pay(PayStrategy.PayName.DEFAULT_PAY);
    }

    public PayState<String> pay(String payKey) {
        AbstractPayment payment = PayStrategy.getPay(payKey);
        System.out.println("欢迎使用: " + payment.getName());
        System.out.println("本次交易金额为: " + amount + ", 开始扣款。。。");
        return payment.pay(uid, amount);
    }
}

class OrderTest {
    public static void main(String[] args) {
        /*order -> payment -> aliPay -> payState*/
        Order order = new Order("1", 2000.00);
        PayState<String> pay = order.pay(PayStrategy.PayName.ALI_PAY);
        System.out.println(pay);
    }
}
