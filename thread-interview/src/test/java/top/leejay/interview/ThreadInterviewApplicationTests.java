package top.leejay.interview;

import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@SpringBootTest
class ThreadInterviewApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void interrupt() {
        Thread thread = new Thread(() -> {
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
    void interrupt2() {
        Thread thread = new Thread(() -> {
            // sleep wait join 三个api会抛出被中断异常
            try {
                Thread.sleep(10000L);
            } catch (InterruptedException e) {
                System.out.println("被打断了...");
                e.printStackTrace();
            }
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

    @Test
    void name() {
        Map<String, List<List<Long>>> convertedMap = new HashMap<>();
        // 2020-01-19 14:53:34
        List<Long> longs = Lists.newArrayList(1579416814000L, 1L);
        // 2020-02-19 14:53:34
        List<Long> longs1 = Lists.newArrayList(1582095214000L, 1L);
        // 2019-12-12 19:13:34
        List<Long> longs2 = Lists.newArrayList(1576149214000L, 1L);
        // 2020-04-19 14:53:34
        List<Long> longs3 = Lists.newArrayList(1587279214000L, 1L);
        // 2020-05-19 14:53:34
        List<Long> longs4 = Lists.newArrayList(1589871214000L, 1L);
        //2020-03-12 19:13:34
        List<Long> longs5 = Lists.newArrayList(1584011614000L, 1L);
        convertedMap.put("hello", Lists.newArrayList(longs3, longs, longs5));
        convertedMap.put("hello1", Lists.newArrayList(longs4, longs1, longs5));
        convertedMap.put("hello2", Lists.newArrayList(longs2, longs5, longs));
        convertedMap.forEach((k, v) -> convertedMap.put(k, v.stream().sorted(Comparator.comparing(x -> x.get(0))).collect(Collectors.toList())));

        // 获取每个测点值范围的开始结束时间
        List<Long> startTimes = Lists.newArrayList();
        List<Long> endTimes = Lists.newArrayList();
        convertedMap.forEach((k, v) -> {
            long start = v.get(0).get(0);
            long end = v.get(v.size() - 1).get(0);
            if (start >= end) {
                throw new RuntimeException("No intersection of sensor values");
            }
            startTimes.add(start);
            endTimes.add(end);
        });
        startTimes.sort(Long::compareTo);
        Long start = startTimes.get(startTimes.size() - 1);
        endTimes.sort(Long::compareTo);
        Long end = endTimes.get(0);
        if (startTimes.size() != endTimes.size() || start >= end) {
            throw new RuntimeException("No intersection of sensor values");
        }
        log.info("测点值区间交集开始时间： {}， 结束时间： {}", start, end);
        convertedMap.forEach((k, v) ->
                convertedMap.put(k, v.stream()
                        .filter(x -> x.get(0) <= end && x.get(0) >= start)
                        .collect(Collectors.toList())));

        System.out.println(convertedMap);
    }

    private static final Object Lock = new Object();

    @SneakyThrows
    @Test
    void notifyOrNotifyAll() {
        // 将线程A加入Lock的等待队列
        Thread threadA = new Thread(() -> {
            synchronized (Lock) {
                try {
                    System.out.println("thread a prepare to wait");
                    Lock.wait();
                    System.out.println("thread a is awakened");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        // 将线程B加入Lock的等待队列
        Thread threadB = new Thread(() -> {
            synchronized (Lock) {
                try {
                    System.out.println("thread b prepare to wait");
                    Lock.wait();
                    System.out.println("thread b is awakened");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        // 线程C唤醒等待队列中等待线程
        Thread threadC = new Thread(() -> {
            synchronized (Lock) {
                System.out.println("thread c prepare to notify");
                Lock.notify(); // 只会唤醒一个线程，另一个人不会被唤醒
                // Lock.notifyAll(); // A和B都会被唤醒,但是顺序不一定
            }
        });

        threadA.start();
        threadB.start();
        Thread.sleep(1000);
        threadC.start();

        threadA.join();
        threadB.join();
        threadC.join();
        System.out.println("main thread is end");
    }
}
