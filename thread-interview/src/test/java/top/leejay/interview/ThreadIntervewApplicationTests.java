package top.leejay.interview;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

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
}
