package top.leejay.interview;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

@Slf4j
@SpringBootTest
class ThreadIntervewApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void interrupt() {
        Thread thread = new Thread(() -> {
//            try {
//                Thread.sleep(10000L);
//            } catch (InterruptedException e) {
//                System.out.println("被打断了...");
//                e.printStackTrace();
//            }
            for (int i = 0; i < 1000; i++) {

            }
            // Thread.interrupted()
            System.out.println("Thread interrupted: " + Thread.interrupted());
            System.out.println("Thread interrupted: " + Thread.interrupted());
        });
        thread.start();
        // t.interrupt()
        thread.interrupt();
        // t.isInterrupted()
        boolean interrupted = thread.isInterrupted();
        System.out.println("thread isInterrupt: " + interrupted);
    }

    @Test
    public void advance() {
        List<BigDecimal> list1 = Lists.newArrayList();
        List<BigDecimal> list2 = Lists.newArrayList();
        List<BigDecimal> list3 = Lists.newArrayList();
        for (int i = 0; i < 1000000; i++) {
            list1.add(BigDecimal.valueOf(i));
        }

        for (int i = 500000; i < 1000000; i++) {
            list2.add(BigDecimal.valueOf(i));
        }

        for (int i = 400000; i < 600000; i++) {
            list3.add(BigDecimal.valueOf(i));
        }

        List<List<BigDecimal>> bigDecimals = Lists.newArrayList();
        bigDecimals.add(list1);
        bigDecimals.add(list2);
        bigDecimals.add(list3);
        log.info("Take intersection start time: {}", new Date().toString());
        // 将code对应的List<Time>取交集
        HashSet<BigDecimal> p = new HashSet<>();
        for (List<BigDecimal> decimal : bigDecimals) {
            HashSet<BigDecimal> value = new HashSet<>(decimal);
            if (CollectionUtils.isEmpty(p)) {
                p = value;
                continue;
            }
            p.retainAll(value);
            // =0 说明无交集
            if (p.size() <= 0) {
                // 说明两者无交集，直接报错，外层捕获落库
                throw new RuntimeException("No intersection of sensor values");
            }
        }
        log.info("Take intersection end time: {}", new Date().toString());
        System.out.println(p.size());
    }

    @Test
    public void advance1() {
        List<BigDecimal> list1 = Lists.newArrayList();
        List<BigDecimal> list2 = Lists.newArrayList();
        List<BigDecimal> list3 = Lists.newArrayList();
        for (int i = 0; i < 1000000; i++) {
            list1.add(BigDecimal.valueOf(i));
        }

        for (int i = 500000; i < 1000000; i++) {
            list2.add(BigDecimal.valueOf(i));
        }

        for (int i = 400000; i < 600000; i++) {
            list3.add(BigDecimal.valueOf(i));
        }

        List<List<BigDecimal>> bigDecimals = Lists.newArrayList();
        bigDecimals.add(list1);
        bigDecimals.add(list2);
        bigDecimals.add(list3);
        log.info("Take intersection start time: {}", new Date().toString());
        // 将code对应的List<Time>取交集
        List<BigDecimal> p = new ArrayList<>();
        for (List<BigDecimal> value : bigDecimals) {
            if (CollectionUtils.isEmpty(p)) {
                p = value;
                continue;
            }
            if (p.size() > value.size()) {
                p.retainAll(value);
            } else {
                value.retainAll(p);
                p = value;
            }
            // =0 说明无交集
            if (p.size() <= 0) {
                // 说明两者无交集，直接报错，外层捕获落库
                throw new RuntimeException("No intersection of sensor values");
            }
        }
        log.info("Take intersection end time: {}", new Date().toString());
        System.out.println(p);
    }

    @Test
    void sqrt() {
        List<Double> doubles = Lists.newArrayList(1.23, 4.56, 7.89);
        double sqrt = Math.sqrt(doubles.stream().mapToDouble(s -> Math.pow(s, 2)).sum() / doubles.size());
        BigDecimal bigDecimal = new BigDecimal(String.valueOf(sqrt));
        bigDecimal = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP);
        System.out.println(bigDecimal);
    }

}
