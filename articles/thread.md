## Java多线程知识点

#### 1. 守护线程与非守护线程

运行在JVM进程中的线程非为两类： 守护线程 & 非守护线程，我们通过如下代码将线程设置为非守护线程。

```java
void daemon() {
    Thread t = new Thread();
    t.setDaemon(true);
}
```

>  当所有的非守护线程运行退出后，整个JVM进程都会退出(包括守护线程)。

#### 2. 线程的中断

在Java多线程中，我们一般用`t.interrupt()`来实现对线程的中断，即使你调用了中断函数，线程也不一定会响应中断`(因为此时不一定拥有CPU执行权)`，该方法的本质就是修改了`中断标志位`。

只有声明了`InterruptedException()`异常的方法才会抛出中断异常，比如`sleep()、wait()和join()`，这三个方法会将线程的中断标识设置为false，并抛出中断异常。

- interrupt() 

  Thread类的实例方法，用于中断线程，但只是设置线程的中断标志位。

-  isInterrupted()

  Thread类的实例方法，用于判断当前线程是否中断，`该方法不会重置线程中断标志位`。

- Thread.interrupted()

  Thread类的静态方法，用于判断`当前线程的中断状态`，`并会重置线程中断标志位`。

```java
@Test
void interrupt() {
    Thread thread = new Thread(() -> {
        for (int i = 0; i < 1000; i++) {
			// do nothing
        }
        // Thread.interrupted() 判断当前线程中断状态并重置
        System.out.println("Thread interrupted: " + Thread.interrupted());//true
        System.out.println("Thread interrupted: " + Thread.interrupted());//false
    });
    thread.start();
    // t.interrupt() 尝试中断线程，如果是那三个方法会立即响应并抛出异常
    thread.interrupt();
    // t.isInterrupted() 判断线程中断状态
    System.out.println("thread isInterrupt: " + thread.isInterrupted());//true
}
```

#### 3. 为什么wait() & notify()属于Object类？

因为wait() 和 notify()拥有锁才可以执行，其次Synchronized中的锁可以是任意对象(通过对象头中的MarkWord实现)，所以他们属于Object类。

#### 4. 线程状态迁移图

![](https://image.leejay.top/image/20200326/3XSAP42BEbCV.png)

#### 5. Synchronized在对象头中的构成

![](https://image.leejay.top/image/20200603/sVQHgMaLfpgG.png?imageslim)

