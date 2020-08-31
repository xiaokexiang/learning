### JDK自带工具

#### jps(JVM Process Status Tool)

列出正在运行的`虚拟机进程`，并显示`虚拟机执行主类名称`及这些进程的`本地虚拟机唯一ID(LVMID)`。

```shell
jps [options]

# 只输出LVMID
jps -q

# 输出传递给main()的参数
jps -m

# 输出虚拟机进程启动的JVM参数
jps -v
```

---

#### jstat(JVM Statistics Monitoring Tool)

用`于监视虚拟机各中运行状态信息`的命令行工具。

```shell
# 查询指定LVMID进程，间隔intervalms，一共查询count次
jstat [options] [LVMID] [interval s|ms] [count]

# 间隔250ms，查询5次
jstat -gcutil 3016 250 5
jstat -gcutil 3016 0.25s 5
```

![](https://image.leejay.top/image/20200831/9AhH78eabF2f.png?imageslim)

> 从左往右分别表示：`S0使用比例`、S1使用比例、`Eden使用比例`、`老年代使用比例`、元数据区使用比例、压缩使用的比例、`Minor GC次数`、Minor GC总耗时、`Full GC次数`、Full GC总耗时、所有GC总耗时。

---

#### jinfo(Configuration Info for Java)

`实时查看和调整(不是全部)`虚拟机各种参数。`jps -v & java -XX:+PrintFlagsFinal some.class`也可以实现jinfo中参数查看的功能。

```shell
jinfo [options] [pid]
# 查看LVMID进程的参数：ParallelGCThreads

jinfo -flag ParallelGCThreads 16964
-XX：ParallelGCThreads=10

# 查看System.getProperties()参数
jinfo -sysprops [pid]
```

> 注意：类似`-Xms、-Xmx`此类参数无法通过jinfo修改，会抛出`AttachOperationFailedException`。

---

#### jmap(Memory Map for Java)

主要用于生成`堆转储快照`。还可以查询`finalize`执行队列、堆和元数据区信息。

```shell
jmap [options] [pid]

# 生成堆转储快照，有live说明只dump出活的对象
jmap dump:live,format=b,file=D://hello.bin [pid]
jmap dump,format=b,file=D://hello.bin [pid]

# 查看堆详细信息，包括垃圾回收器信息
jmap -heap [pid]

# 显示堆中对象统计信息：类、实例数量
jmap -histo [pid]
```

---

#### jhat(JVM Heap Analysis Tool)

与`jmap`搭配使用，分析`jmap`生成的`堆转储快照`。`jhat`内置一个微型web服务器，可在浏览器中查看分析结果。但是一般不使用`jhat`，因为它比较简陋，且我们一般不会在部署机器上分析。

![](https://image.leejay.top/image/20200831/g2CuA2tEqzNr.png?imageslim)

> `1. cmd -> jhat D://hello.bin`
>
> `2. open -> localhost:7000`
>
> `3. click -> Show heap histogram`

---

#### jstack(Stack Trace for Java)

用于生成虚拟机当前时刻的`线程快照`。线程快照是当前虚拟机内每一条线程`正在执行的方法堆栈`的集合。其目的多是为了定位`线程长时间停顿的原因`。

```shell
jstack [options] [pid]

# 输出堆栈快照及锁的附加信息
jstack -l [pid]
```

---

### 可视化工具

#### jconsole

`jconsole(Java Monitoring and Management Console)`是一款基于`JMX(Java Management Extensions)`的可视化监视、管理工具。

```shell
jconsole [pid]
```

- 内存监控

```java
public class JConsole {
    static class OOMObject {
        public byte[] placeHolder = new byte[64 * 1024];
    }

    /**
      * 以64kb/50ms的速度向堆中填充数据
      */
    public static void fill(int num) {
        List<OOMObject> objects = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            objects.add(new OOMObject());
        }
        System.gc();
        System.out.println("something ...");
    }

    /**
     * -Xms100m -Xmx100m -XX:+UseSerialGC
     */
    public static void main(String[] args) {
        fill(1000);
    }
}
```



![](https://image.leejay.top/image/20200831/jm8fWjPFpwhR.png?imageslim)

> 结合代码，我们可以看出`eden区`经历了`三次Minor GC`。并且显示了eden区域的内存大小为`27328kb`。

- 线程监控

```java
public class JConsoleThread {
    /**
     * 模拟 IO 等待、线程死循环、线程锁等待的状态
     */
    public static void main(String[] args) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(
            							new InputStreamReader(System.in));
        bufferedReader.readLine();
        createBusyThread();
        bufferedReader.readLine();
        createLockThread();
    }

    static void createBusyThread() {
        new Thread(() -> {
            while (true) {
                // do something forever
            }
        }, "busy_thread").start();
    }

    private static final Object LOCK = new Object();
    static void createLockThread() {
        new Thread(() -> {
            synchronized (LOCK) {
                try {
                    LOCK.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "lock_thread").start();
    }
}
```

![](https://image.leejay.top/image/20200831/g3kG1UR4h5nA.png?imageslim)

> 当Main线程等待控制台输入时，main线程处于`Runnable`状态，线程仍会被分配运行时间。
>
> busy_thread也处于`Runnable`状态，一直在死循环.
>
> lock_thread处于`waiting`状态，等待被唤醒。
>
> jconsole自带`检测死锁`的功能。

---

#### jcmd & jhsdb

`jcmd & jhsdb`是JDK提供的两个集成的多功能工具箱。能够实现前面提到的绝大部分的功能。

| 基础工具                  | JCMD                            | JHSDB(JDK10)                   |
| ------------------------- | ------------------------------- | ------------------------------ |
| jps -lm                   | jcmd                            | N/A                            |
| jmap -dump,f=<path> <pid> | jcmd <pid> GC.head_dump <path>  | jhsdb jmap --binaryheap <path> |
| jmap -histo <pid>         | jcmd <pid> GC.class_histogram   | jhsdb jmap -histo              |
| jstack <pid>              | jcmd <pid> Thread.print         | jhsdb jstack --locks           |
| jinfo -sysprops <pid>     | jcmd <pid> VM.system_properties | jhsdb info -- sysprops         |
| jinfo -flag <pid>         | jcmd <pid> VM.flags             | jhsdb jinfo --flags            |

