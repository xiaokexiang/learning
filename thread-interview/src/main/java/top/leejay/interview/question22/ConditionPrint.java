package top.leejay.interview.question22;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author xiaokexiang
 * @date 7/6/2020
 * 交替打印奇偶数
 */
public class ConditionPrint {
    private static final ReentrantLock LOCK = new ReentrantLock();
    private static final Condition CONDITION = LOCK.newCondition();
    private static int count = 0;

    public static void main(String[] args) {
        new Thread(() -> {
            while (count < 100) {
               LOCK.lock();
               try {
                   if (count % 2 != 0) {
                       try {
                           CONDITION.await();
                       } catch (InterruptedException e) {
                           e.printStackTrace();
                       }
                   }
                   System.out.println("偶数: " + count);
                   count++;
                   CONDITION.signal();
               } finally {
                   LOCK.unlock();
               }
            }
        }).start();


        new Thread(() -> {
            while (count < 100) {
                LOCK.lock();
                try {
                    if (count % 2 == 0) {
                        try {
                            CONDITION.await();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    System.out.println("奇数: " + count);
                    count++;
                    CONDITION.signal();
                } finally {
                    LOCK.unlock();
                }
            }
        }).start();
    }
}
