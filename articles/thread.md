## Java多线程

### 多线程问题

#### wait() & notify()为何属于Object类？

因为wait() 和 notify()拥有锁才可以执行，其次synchronized中的锁可以是任意对象(通过对象头中的MarkWord实现)，所以他们属于Object类。

#### 何时使用notify()或notifyAll()

只有当`所有线程拥有相同的等待条件，所有线程被唤醒后执行相同的操作，最重要的是只需要唤醒一个线程`，notify最典型的应用是在`线程池`。除此之外建议使用notifyAll和signalAll(condition)。因为`使用notifyAll不会遗落等待队列中的线程，但是notifyAll会带来毕竟强的竞争。`

```java
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
            // 只会唤醒一个线程，另一个人不会被唤醒
            Lock.notify();
            // A和B都会被唤醒,但是顺序不一定
            // Lock.notifyAll();
        }
    });

    threadA.start();
    threadB.start();
    Thread.sleep(1000);
    threadC.start();
    // 保证A和B已经在等待队列中再唤醒
    threadA.join();
    threadB.join();
    threadC.join();
    System.out.println("main thread is end");
}
```

#### JVM中 long和double是原子操作吗？

我们基于Hotspot虚拟机，在32位系统下，每次能操作的最大长度是32bit，而long/double是8字节/64bit，所以对long/double的读写需要两条指令才能完，所以`对long/double的操作在32位hotspot中不是原子性操作`，而64位hotspot可以实现对long/double的原子性操作。

---

### 多线程基础

####  守护线程与非守护线程

运行在JVM进程中的线程非为两类： 守护线程 & 非守护线程，我们通过如下代码将线程设置为非守护线程。

```java
void daemon() {
    Thread t = new Thread();
    t.setDaemon(true);
}
```

>  当所有的非守护线程运行退出后，整个JVM进程都会退出(包括守护线程)。

#### 线程的中断

在Java多线程中，我们一般用`t.interrupt()`来实现对线程的中断，即使你调用了中断函数，线程也不一定会响应中断`(因为此时不一定拥有CPU执行权)`，该方法的本质就是修改了`中断标志位`。

只有声明了`InterruptedException()`异常的方法才会抛出中断异常，比如`sleep()、wait()和join()`，这三个方法会将线程的中断标识设置为false，并抛出中断异常。

- interrupt() 

  Thread类的实例方法，用于中断线程，但只是设置线程的中断标志位。

- isInterrupted()

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

#### 线程状态迁移图

![](https://image.leejay.top/image/20200326/3XSAP42BEbCV.png)

#### Synchronized结构图

![](https://image.leejay.top/image/20200603/sVQHgMaLfpgG.png?imageslim)



#### 内存可见性

CPU及JVM为了优化代码执行效率，会对代码进行重排序，其中包括：

- 编译器重排序(没有先后依赖关系的语句，编译器可以重新调整语句执行顺序)
- CPU指令重排序(让没有依赖关系的多条指令并行)
- CPU内存重排序(`CPU有自己的缓存，指令执行顺序和写入主内存顺序不一致`)

其中`CPU内存重排序`是导致`内存可见性`的主因。根据JMM内存模型，我们描述下过程：

 如果线程需要修改共享变量，那么线程A会`拷贝共享变量的副本到本地线程中并对其进行修改`，之后会将值写回共享内存中(时间不确定)，但在写回之前，线程B读取共享变量到本地准备修改，而此时`线程A修改共享变量的操作对线程B不可见`。

重排序规则：

- as-if-serial

  不管怎么重排序，`单线程程序的执行结果不能被改变`。只要操作之间没有数据依赖性，那么编译器和CPU都可以任意重排序。

- happen-before(JVM层面)

  为了明确多线程场景下那么可以重排序，哪些不可以重排序，引入了JMM内存模型，而JMM提供了`happen-before`规范，用于在开发者编写程序和系统运行之间效率找到平衡点，`它描述了两个操作之间的内存可见性，若A happen before B，如果A在B之前执行，则A的执行结果必须对B可见`。

  - 单线程的每个操作，happen-before 于该线程中任意后续操作。
  - `对volatile变量的写入，happen-before 于后续对这个变量的读取`。
  - 对于synchronized的解锁，happen-before于后续对这个锁的加锁。
  - 对final域的写(构造函数中)，happen-before于对final域所在对象的读。

- happen-before传递性

  假设线程A先调用了set()，设置了a=5，之后线程B调用了get()，返回一定是a=5。

  ```java
  class Test {
      private int a = 0;
      private volatile int c = 0;
  
      void set() {
          a = 5;// step 1
          c = 1;// step 2
      }
  
      int get() {
          int d = c;// step 3
          return a;// step 4
      }
  }
  ```

  > 因为step1和step2在同一块内存中，所以step1 happen-before step2，同理step3 happen before step4，且因为c是volatile变量，`根据volatile变量的写 happen-before volatile变量的读，以及happen-before传递性`，step1 的结果一定对step4可见。

---

#### volatile

- 作用

volatile保证了内存的可见性，对于共享变量操作会直接从共享内存中读取，修改时会直接将结果刷入共享内存，其次`禁止了volatile修饰的变量和非volatile变量之间的重排序`。

- 原理

  为了禁止编译器重排序和CPU重排序，底层原理是通过`内存屏障`指令来实现的。

  - 编译器内存屏障

    只是为了告诉编译器不要对指令进行重排序，但编译完成后，这种内存屏障就消失了，CPU不会感知到编译器中内存屏障的存在。

  - CPU内存屏障

    由CPU提供的指令(不同的CPU架构，提供的指令不同)，可以由开发者显示调用，volatile就是通过CPU内存屏障指令来实现的。

    实现流程：

    1. 在volatile写操作的前面插入一个`StoreStore屏障`。保证volatile写操作不会和之前的写操作重排序。
    2. 在volatile写操作的后面插入一个`StoreLoad屏障`。保证`volatile写操作不会和之后读操作`重排序。
    3. 在volatile读操作后面插入一个`LoadLoad`屏障 + `LoadStore`屏障。保证`volatile读操作不会和之前的读操作、写操作`重排序。

- 与synchronized关键字的异同

  多线程会产生三大问题：原子性、有序性和可见性。

  synchronized和volatile在共享变量的操作上具有相同的内存语义(`从主内存读取，立即写入主内存`)，保证了变量的可见性。但是synchronized相比volatile还具有`原子性(阻塞和排他性，同一时刻只能有一个线程执行，而volatile是非阻塞的)`，所以`volatile是弱化版的synchronized`。

  ```java
  class Test {
      // 这里的flag就可以不用锁同步
      private static volatile boolean flag = true;
      // 模拟AtomicInteger
      private static CasUnsafe UNSAFE = new CasUnsafe(0);
      
      // 按照顺序打印1-100的奇偶数
      public static void main(String[] args) {
          THREAD_POOL.execute(() -> {
              while (UNSAFE.getValue() < 100) {
                  if (flag) {
                      System.out.println(UNSAFE.incrementAndGet());
                      flag = false;
                  }
              }
          });
          THREAD_POOL.execute(() -> {
              while (UNSAFE.getValue() < 100) {
                  if (!flag) {
                      System.out.println(UNSAFE.incrementAndGet());
                      flag = true;
                  }
              }
          });
          THREAD_POOL.shutdown();
      }
  }
  ```

  > Q：什么时候用volatile而可以不用synchronized？
  >
  > A：如果`写入变量值不依赖变量当前值(count++就是依赖当前值，先去内存读取值，然后将当前值+1，将计算后的值赋给count。比如)`，那么就可以用volatile。

- DCL(Double Check Lock)

  `双重检查加锁问题简称DCL`，用于懒汉式单例的一种写法，问题如下所示：

  ```java
  public class DoubleCheckSingleton {
  
      /**
       * 为什么这个地方要使用volatile修饰?
       *
       * 首先我们需要了解JVM是存在`编译器优化重排`功能的(编译器在不改变单线程语义情况下，重新安      * 排代码的执行顺序。但是不保证多线程情况)
       * 执行如下代码
       * singleton = new DoubleCheckSingleton();
       * 在JVM是分成三步的：
       * 1. 开辟空间分配内存
       * 2. 初始化对象
       * 3. 将singleton引用指向分配的内存地址
       *
       * 在不使用volatile时，可能被JVM优化成
       * 1. 开辟空间分配内存
       * 3. 将singleton引用指向分配的内存地址
       * 2. 初始化对象
       *
       * 那么当线程A执行1&3步的时候，线程B获取了CPU执行权，去验证`null == singleton`，
       * 发现不为null，直接返回一个未初始化完成的singleton，导致程序出错。
       *
       * volatile禁止被修饰变量的 编译器重排序 和 处理器重排序(内存屏障) （JDK1.5后）
       *
       */
      private static volatile DoubleCheckSingleton singleton;
  
      private DoubleCheckSingleton() {
      }
  
      public static DoubleCheckSingleton getInstance() {
          // 不是任何线程进来都尝试去获取锁，而是先判断singleton是否为null，优化性能
          if (null == singleton) {
              // 尝试去获取锁，保证线程安全
              synchronized (DoubleCheckSingleton.class) {
                  // 获取锁后判断singleton是否为null
                  if (null == singleton) {
                      singleton = new DoubleCheckSingleton();
                  }
              }
          }
          return singleton;
      }
  }
  
  ```

#### ThreadLocal内存泄漏

ThreadLocal类，底层由`ThreadLocalMap`实现，是Thread类的成员变量，因为`类的每个实例的成员变量都是这个实例独有的`，所以在不同的Thread中有不同的副本，每个线程的副本`只能由当前线程使用，线程间互不影响`。因为一个线程可以拥有多个ThreadLocal对象，所以其内部使用`ThreadLocalMap<ThreadLocal<?>, Object>`来实现。

```java
public class Thread implements Runnable {
    ThreadLocal.ThreadLocalMap threadLocals = null;
}
public class ThreadLocal<T> {

	static class ThreadLocalMap {
        
        // 需要注意的是这里的Entry key是ThreadLocal的弱引用
        // 弱引用的特点是在下次GC的时候会被清理
        static class Entry extends WeakReference<ThreadLocal<?>> {
            // value 与 ThreadLocal关联
            Object value;

            Entry(ThreadLocal<?> k, Object v) {
                super(k);
                value = v;
            }
        }
    }
}

```

![](https://image.leejay.top/image/20200701/Y6kWCwYi46IF.png?imageslim)

> 1. 当前线程执行时(`currentThread已初始化`)，会初始化ThreadLocal对象，存储在`Heap堆`中，ThreadLocal的引用，即`ThreadLocalRef`会存储在当前线程`Stack栈`中。
> 2. 当执行ThreadLocal的get()/set()方法时，会通过`当前线程的引用找到当前线程在堆中的实例`，判断这个实例的成员变量：`ThreadLocalMap`是否已经创建(即初始化)，如果没有则初始化。
> 3. 若一个Threa中存在多个ThreadLocal，那么ThreadLocalMap会存在多个Entry，`Entry的key是弱引用的ThreadLocal`。

根据ThreadLocal堆栈示意图，我们可以推断处只要符合以下条件，ThreadLocal的使用就会出现内存泄漏：

1. `ThreadLocal没有被外部强引用`，这样在GC的时候ThreadLocal会被回收，导致key = null。
2. `key = null`后没有调用过ThreadLocalMap中的get、set或remove方法中的任意一个。`(因为这些方法会将key = null的value也置为null，便于GC回收)`
3. `Thread对象没有被回收`，Thread强引用着ThreadLocalMap，这样ThreadLocalMap也不会被回收。
4. ThreadLocalMap没有被回收，但是`它的Entry中的key已被回收，key关联的value也不能被外部访问`，所以导致了内存泄漏。

总结如下：

> `Thread生命周期还没有结束，ThreadLocal对象被回收后且没有调用过get、set或remove方法就会导致内存泄漏。`

我们可以看出内存泄漏的触发条件比较苛刻的，但确实会发生，其实`只要线程Thread的生命周期结束，那么Thread的ThreadLocalMap也不会存在强引用，那么ThreadLocalMap中的value最终也会被回收。`，所以在使用ThreadLocal时，除了需要密切关注`Thread和ThreadLocal的生命周期`，还需要在每次使用完之后调用`remove`方法，这样做还有一个问题就是：

> 如果你使用的是线程池，那么会出现`线程复用`的情况，如果`不及时清理remove()会导致下次使用的值不符合预期`。

---

### MESA模型

在解释MESA模型之前，我们需要了解什么是`管程：又称为监视器，它是描述并实现对共享变量的管理与操作，使其在多线程下能正确执行的一个管理策略。可以理解成临界区资源的管理策略。`MESA模型是管程的一种实现策略，Java使用的就是该策略。

#### 相关术语

1. **enterQueue**：`管程的入口队列`，当线程在申请进入管程中发现管程已被占用，那么就会进入该队列并阻塞。
2. **varQueue**：`条件变量等待队列`，在线程执行过程中(已进入管程)，条件变量不符合要求，线程被阻塞时会进入该队列。
3. **condition variables**：条件变量，存在于管程中，一般由程序赋予意义，程序通过判断条件变量执行阻塞或唤醒操作。
4. **阻塞和唤醒**：wait()和await()就是阻塞操作。notify()和notifyAll()就是唤醒操作。

#### 模型概念图

![](https://image.leejay.top/image/20200623/7fsvqebTy60R.png?imageslim)

>  Synchronized和Lock在MSEA监视器模型中的区别在于`前者只有一个条件变量，后者可以有多个`。

#### 执行流程

1. 多个线程进入`入口等待队列enterQueue`，JVM会保证只有一个线程能进入管程内部，Synchronized中进入管程的线程随机。
2. 进入管程后通过条件变量判断当前线程是否能执行操作，如果不能跳到step3，否则跳到step4。
3. 条件变量调用`阻塞`方法，将当前线程放入varQueue，等待其他线程唤醒，跳回step1。
4. 执行相应操作，执行完毕后调用notify/notifyAll等唤醒操作，唤醒对应varQueue中的一个或多个等待线程。
5. 被唤醒的线程会从varQueue放入enterQueue中，再次执行step1。
6. `被唤醒的线程不会立即执行，会被放入enterQueue，等待JVM下一次选择运行，而正在运行的线程会继续执行，直到程序执行完毕。`

---

### CAS

判断数据是否被修改，同时写回新值，这两个操作要合成一个原子操作，这就是CAS(compare and swap)。

之前多线程环境下，我们对变量进行计算都是对其加锁来实现，但是现在我们可以用过Atomic相关的类来实现相同的效果且性能更好。而`AtomicInteger`就是其中的一员，其底层就是通过CAS来实现的。

```
// 伪代码
class AtomicInteger {
	// 保证内存可见性
	private volatile int value;
	
	public final int getAndIncrement() {
		for(;;) {
            int current = get();
            int next = current + 1;
            // cas替换
            if (compareAndSwap(current, next)) {
            	return current;
            }
		}
	}
	
	public int get() {
		return value;
	}
}
```

> 乐观锁：读操作不上锁，等到写操作的时候，判断数据在此期间是否被修改，如果已被修改，则重复该流程，直到把值写回去。CAS就是乐观锁的体现。

CAS的相关方法都被封装在`Unsafe类`中，我们以`AtomicInteger中操作compareAndSwapInt()`为例。

```java
/**
 * var1: 这里就是AtomicInteger对象
 * var2: AotmicInteger 中的成员变量，long型整数，变量在类中的内存偏移量
 *       可以通过unsafe.objectFieldOffset(Field var1)来获得
 * var4：变量的旧值
 * var5: 变量的新值
 */
public final native boolean compareAndSwapInt(Object var1, long var2, int var4, int var5)
```

> Unsafe类提供了三种类型的CAS操作：int、long、Object，分别对应compareAndSwapInt()、compareAndSwapLong()、compareAndSwapObject()。

#### ABA问题

因为CAS是基于值来做比较的，如果线程A将变量从X改成Y，又改回X，尽管改过两次，但是线程B去修改的时候会认为这个变量没有被修改过。

#### AtomicStampedReference

`AtomicStampedReference通过引入值和版本号的概念`用于解决CAS中ABA的问题。

```java
public class AtomicStampedReference<V> {
	// 通过静态内部类构建Pair对象实现compareAndSwapObject()
    private static class Pair<T> {
        final T reference;
        final int stamp;
        private Pair(T reference, int stamp) {
            this.reference = reference;
            this.stamp = stamp;
        }
        static <T> Pair<T> of(T reference, int stamp) {
            return new Pair<T>(reference, stamp);
        }
    }

    private volatile Pair<V> pair;
    // 先判断expect、new与current是否相等，再决定是否的调用cas判断
    public boolean compareAndSet(V   expectedReference,
                                 V   newReference,
                                 int expectedStamp,
                                 int newStamp) {
        Pair<V> current = pair;
        return
            expectedReference == current.reference &&
            expectedStamp == current.stamp &&
            ((newReference == current.reference &&
              newStamp == current.stamp) ||
             casPair(current, Pair.of(newReference, newStamp)));
    }
    
    // compareAndSwapObject()
    private boolean casPair(Pair<V> cmp, Pair<V> val) {
        return UNSAFE.compareAndSwapObject(this, pairOffset, cmp, val);
    }
}
```

> 通过判断新旧引用与版本号是否相等来判断修改是否成功。

#### AtomicMarkableReference

与`AtomicStampedReference`类似，但是其内部类传入的是`引用 + boolean值`。

```java
public class AtomicMarkableReference<V> {

    private static class Pair<T> {
        final T reference;
        // 与AtomicMarkableReference相比不同点
        final boolean mark;
        private Pair(T reference, boolean mark) {
            this.reference = reference;
            this.mark = mark;
        }
        static <T> Pair<T> of(T reference, boolean mark) {
            return new Pair<T>(reference, mark);
        }
    }

    private volatile Pair<V> pair;
    ... 
}
```

> 因为Pair<T>只接受boolean值作为版本号，所以不能完全避免ABA问题，只能是降低发生的概率。

#### AtomicIntegerFieldUpdater

用于实现对`某个不能修改源代码类的、被volatile修饰的成员变量`的原子操作。

```java
public abstract class AtomicIntegerFieldUpdater<T> {
    
    // 传入需要修改的类的class对象以及对应成员变量的名字
    public static <U> AtomicIntegerFieldUpdater<U> newUpdater(Class<U> tclass,
                                                          String fieldName) {
        // 调用实现类构造参数，会判断volatile修饰的成员变量类型是否是int
        return new AtomicIntegerFieldUpdaterImpl<U>
            (tclass, fieldName, Reflection.getCallerClass());
    } 
    
    // 由AtomicIntegerFieldUpdater实现类实现
    public final boolean compareAndSet(T obj, int expect, int update) {
        // 判断obj是不是上面tclass类型
        accessCheck(obj);
        // 最终还是调用compareAndSwapInt去更新值
        return U.compareAndSwapInt(obj, offset, expect, update);
    }
}
```

> 除了AtomicIntegerFieldUpdater，同样有AtomicLongFieldUpdater和AtomicReferenceFieldUpdater。

#### AtomicIntegerArray

实现对数组元素的原子操作，并不是对整个数组，而是针对数组中的一个元素的原子操作。

```java
// 获取数组的首地址的位置
private static final int base = unsafe.arrayBaseOffset(int[].class);
// shift标识scale中1的位置(因为scale=2^*，所以scale中只会有一位是1，这个位置即shift)
private static final int shift;
static {
    // 确保scale是2的整数次方: 2^*
    int scale = unsafe.arrayIndexScale(int[].class);
    if ((scale & (scale - 1)) != 0)
        throw new Error("data type scale not a power of two");
    // 返回scale中最高位之前的0
    shift = 31 - Integer.numberOfLeadingZeros(scale);
}
/**
 * i：即为脚标，会被转换成内存偏移量
 * expect：期待的int值
 * update：更新的int值
 */
public final boolean compareAndSet(int i, int expect, int update) {
	return compareAndSetRaw(checkedByteOffset(i), expect, update);
}

private long checkedByteOffset(int i) {
    if (i < 0 || i >= array.length)
        throw new IndexOutOfBoundsException("index " + i);
    return byteOffset(i);
}
// 最终在这里转换成内存偏移量
// 数组的首地址 + 脚标(第几个) * 数组元素大小sacle
private static long byteOffset(int i) {
    // 也就是 i * scale + base
    return ((long) i << shift) + base;
}
// array即为int[]对象 offset即为内存偏移量
private boolean compareAndSetRaw(long offset, int expect, int update) {
    return unsafe.compareAndSwapInt(array, offset, expect, update);
}
```

> 除了AtomicIntegerArray还包括AtomicLongArray和AtomicReferenceArray。

#### Striped64及相关子类

JDK8之后又提供了`Striped、LongAddr、DoubleAddr、LongAccumulator和DoubleAccumulator`用于实现对`long和double`的原子性操作。

LongAddr类，其原理是`将一个Long型拆分成多份，拆成一个base变量外加多个cell，每一个cell都包装了一个Long型变量，高并发下平摊到Cell上，最后取值再将base和cell累加求sum运算`。Cell就存在于LongAddr抽象父类Striped64中。

```java
abstract class Striped64 extends Number {
    // 一个base+多个cell
	@sun.misc.Contended static final class Cell {
        // volatile修饰的long型变量
        volatile long value;
        Cell(long x) { value = x; }
        final boolean cas(long cmp, long val) {
            // CAS
            return UNSAFE.compareAndSwapLong(this, valueOffset, cmp, val);
        }

        // Unsafe mechanics
        private static final sun.misc.Unsafe UNSAFE;
        // Cell中value在内存中的偏移量
        private static final long valueOffset;
        static {
            try {
                UNSAFE = sun.misc.Unsafe.getUnsafe();
                Class<?> ak = Cell.class;
                // 获取偏移量
                valueOffset = UNSAFE.objectFieldOffset
                    (ak.getDeclaredField("value"));
            } catch (Exception e) {
                throw new Error(e);
            }
        }
    }
}
// LongAdder base 初始值是0 只能进行累加操作
public class LongAdder extends Striped64 implements Serializable {
    // 求总值
	public long sum() {
        Cell[] as = cells; Cell a;
        long sum = base;
        if (as != null) {
            // 循环读取累加，非同步
            for (int i = 0; i < as.length; ++i) {
                if ((a = as[i]) != null)
                    sum += a.value;
            }
        }
        return sum;
    }
}
// LongAccumulator与LongAdder不同点在于构造函数，LongAccumulator可以自定义操作符和初始值
public class LongAccumulator extends Striped64 implements Serializable {
	public LongAccumulator(LongBinaryOperator accumulatorFunction,
                           long identity) {
        this.function = accumulatorFunction;
        base = this.identity = identity;
    }
}
    
```

> 相比于AtomicLong，LongAddr更适合于高并发的统计场景，而不是对某个Long型变量进行严格同步的场景。

#### 伪共享与缓存行填充

JDK8中通过`@sun.misc.Contented`注解实现缓存行填充的作用。

在CPU架构中，每个CPU都有自己的缓存。`缓存与主内存进行数据交换的基本单位叫Cache Line缓存行`。在64位的X86架构中，缓存行大小是64byte(8个long型)，意味着当缓存行失效，需要刷新到主内存的时候，最少需要刷新64字节。

![](https://image.leejay.top/image/20200605/Q1vhuTbvuFNq.png?imageslim)

我们假设主内存中有x，y，z三个Long型变量，被Core1和Core2读到自己的缓存，放在同一个缓存行，当Core1对变量x进行修改，那么它需要`失效一整行Cache Line`，并通过CPU总线发消息通知Core2对应的Cache Line失效。`所以即使y和z没有被修改，因为和x处于同一个缓存行，所以x、y、z都需要失效，这就叫做伪共享问题`。

我们通过将x，y，z三个变量分布到不同的缓存行并且填充7个无用的Long型来填充缓存行，用于避免`伪共享问题`，JDK8之前都是通过下面的类似代码来实现，JDK8之后则是通过`@sun.misc.Contented`实现此功能。

```java
class Test {
    volatile long value;
    long a ,b ,c ,d ,e ,f ,g;
}

@sun.misc.Contended class demo{
    volatile long value;
}
```

---

### Lock

Lock与Synchronized都是`可重入锁`，否则会发生死锁。Lock锁核心在于`AbstractQueueSynchronizer`，又名`队列同步器(简称AQS)`。如果需要实现自定义锁，除了需要实现Lock接口外，还需要内部类继承Sync类。

#### AbstractQueueSynchronizer

![](https://image.leejay.top/image/20200608/GVtL7ztzwtCl.png?imageslim)

##### 记录当前锁的持有线程

由AQS的父类`AbstractOwnableSynchronizer`实现记录当前锁的持有线程功能（独占锁）。

##### state变量

内部维护了volatile修饰的state变量，state = 0时表明没有线程获取锁，state = 1时表明有一个线程获取锁，当state > 1时，说明该线程重入了该锁。

##### 线程阻塞和唤醒

由`LockSupport`类实现，其底层是调用了Unsafe的park 和 unpark。`如果当前线程是非中断状态，调用park()阻塞，返回中断状态是false，如果当前线程是中断状态，调用park()会不起作用立即返回。也是为什么AQS要清空中断状态的原因`。

##### FIFO队列

AQS内部维护了一个基于`CLH(Craig, Landin, and Hagersten(CLH)locks。基于链表的公平的自旋锁)`变种的FIFO双向链表阻塞队列，在等待机制上由自旋改成阻塞唤醒(park/unpark)。

![](https://image.leejay.top/image/20200609/CHJldTlsLVp2.png?imageslim)

> 还未初始化的时候，head = tail = null，之后初始化队列，往其中假如阻塞的线程时，会新建一个空的node，让head和tail都指向这个空node。之后加入被阻塞的线程对象。当head=tai时候说明队列为空。

##### Node的waitStatus

| Node状态     | 描述                                                         |
| :----------- | :----------------------------------------------------------- |
| INIT=0       | Node初始创建时默认为0                                        |
| CANCELLED=1  | 由于超时或者中断，线程获取锁的请求取消了，节点一旦变成此状态就不会再变化。 |
| SIGNAL=-1    | 表示线程已经准备好了，等待资源释放去获取锁。                 |
| CONDITION=-2 | 表示节点处于等待队列中，等待被唤醒。                         |
| PROPAGATE=-3 | 只有当前线程处于SHARED情况下，该字段才会使用，用于共享锁的获取。 |

####  ReentrentLock

我们选择`ReentrentLock`作为入口进行源码解读，自定义的获取释放锁的方法，由其内部抽象类Sync的子类FairSync和NonfairSync中的tryAcquire、tryRelease实现。

```java
class Test {
    private static final ReentrantLock LOCK = new ReentrantLock();
    
    public void run() {
        LOCK.lock();
        try {
            //dosomething
        }finally {
            LOCK.unlock();
        }  
    }
}
```

> 判断是否成功获取锁的标志，就是CAS修改volatile修饰的state变量是否成功。

##### 公平锁和非公平锁

```java
// 非公平锁实现
static final class NonfairSync extends Sync {
	final void lock() {
        // 先尝试CAS获取锁
        if (compareAndSetState(0, 1))
            setExclusiveOwnerThread(Thread.currentThread());
        else
            // 再排队
            acquire(1);
    }
    ...
}
// 公平锁实现
static final class FairSync extends Sync {
    private static final long serialVersionUID = -3000897897090466540L;
	// 去排队
    final void lock() {
        acquire(1);
    }
    ...
}
```

> 公平锁和非公平锁如何选择？
>
> 非公平锁一进来就尝试去获取锁，有效的减少了线程的上下文切换，所以为了追求`高吞吐量`建议选择非公平锁，但是会导致某些线程长时间在排队，没有机会获取锁。否则建议选择公平锁。

##### acquire

```java
// 如果第一次获取锁失败，说明此时有其他线程持有锁，所以执行acquire
public final void acquire(int arg) {
    if (!tryAcquire(arg) &&
        acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
        selfInterrupt();
}
```

##### tryAcquire

```java
// 调用非公平锁的tryAcquire，再一次尝试去获取锁
protected final boolean tryAcquire(int acquires) {
    return nonfairTryAcquire(acquires);
}
// 返回false表明没有获取到锁，true表明成功获取锁/重入锁
final boolean nonfairTryAcquire(int acquires) {
    // 获取当前线程
    final Thread current = Thread.currentThread();
    // 获取state状态
    int c = getState();
    // 如果state是0，表明当前没有线程获取锁
    if (c == 0) {
        // 尝试去获取锁，获取成功就设置独占线程为当前线程
        if (compareAndSetState(0, acquires)) {
            setExclusiveOwnerThread(current);
            return true;
        }
    }
    // 如果当前线程已经是独占线程，此时说明锁重入了
    else if (current == getExclusiveOwnerThread()) {
        // 修改state的值
        int nextc = c + acquires;
        if (nextc < 0)
            throw new Error("Maximum lock count exceeded");
        // 设置state值，因为此时的获取锁的线程就是当前线程
        setState(nextc);
        return true;
    }
    return false;
}
// 公平锁的tryAcquire实现
protected final boolean tryAcquire(int acquires) {
    ...
        if (c == 0) {
            // hasQueuedPredecessors是公平锁的主要体现
            if (!hasQueuedPredecessors() &&
                compareAndSetState(0, acquires)) {
                setExclusiveOwnerThread(current);
                return true;
            }
        }
    ...
}
```

> Q:  为什么有的地方使用setState()，有的地方使用CAS？
>
> A:  因为使用setState()方法的前提是已经获取了锁，使用了CAS的是因为此时还没有获取锁。

##### hasQueuedPredecessors

```java
// true/false 有节点在等待/无节点等待
public final boolean hasQueuedPredecessors() {
    // 这里为什么tail获取在head之前？
    // 假设第一个节点入队，根据enq()设置head和tail可知
    // 如果此处tail = null，head = null | head != null都有可能
    // 如果此处tail != null ，那么(head = tail) != null
    Node t = tail;
    Node h = head;
    Node s;
    return h != t &&
        // (s = h.next) == null 成立说明有其他线程正在初始化队列
        ((s = h.next) == null || s.thread != Thread.currentThread());
}
```

> 返回情况分析：
>
> 1. 若`h == t`说明此时队列还没有初始化或只有哨兵节点，返回false表明无等待节点。
>
> 2. 若`h != t`成立，说明此时队列有节点啊，那`((s = h.next) == null)`应该也成立啊？
>    其实不然，我们假设线程A获取锁失败，尝试加入队列，此时队列还未初始化，A执行到enq方法：
>
>    ```java
>    private Node enq(final Node node) {
>        for (;;) {
>            Node t = tail;
>            if (t == null) {
>                // 线程A准备初始化队列，它setHead(new Node())成功了
>                // 此时线程切换，线程B执行了hasQueuedPredecessors()
>                // 此时 head != null; tail = null; head.next = null
>                // 此时h != t 且 (s = h.next) = null
>                if (compareAndSetHead(new Node()))
>                    tail = head;
>            } else {
>                node.prev = t;
>                if (compareAndSetTail(t, node)) {
>                    t.next = node;
>                    return t;
>                }
>            }
>        }
>    }
>    ```
>
>    
>
> 3. 若`((s = h.next) == null)`成立，说明此时存在另一个线程执行到`compareAndSetHead(new Node())`和`tail = head`的中间状态。所以也需要返回true，表明有节点在等待。
>
> 4. 若`((s = h.next) == null)`不成立，我们继续判断队列中第一个等待线程（`s.thread != Thread.currentThread()`）是否是当前线程，是就返回true，否则返回false。
>
> 5. 方法中为什么`Node t = tail`获取在`Node h = head`之前？
>    根据上面的分析，我们知道第一个节点入队的时候会出现`head != null 但 tail = null`的情况，因为是`先设置head再设置tail`，操作非原子性。
>    我们假设`队列未初始化`，hasQueuedPredecessors方法中`tail和head代码位置互换`，线程A先执行`Node h = head;`此时`head = null`，线程切换，线程B执行enq方法初始化队列导致`（head = tail）!= null`，又切回线程A，执行`Node t = tail`，`tail != null`，判断代码`h != t`成立，继续判断`(s = h.next) == null`出问题了，`h =null,h.next会抛空指针!!!`，这就是问题所在。(再次膜拜Doug lea！！！)

##### addWaiter

```java
// 获取不到锁，将当前线程构建成node对象加入队列
private Node addWaiter(Node mode) {
    // 创建node对象(currentThread, Node.EXCLUSIVE)
    Node node = new Node(Thread.currentThread(), mode);
    Node pred = tail;
    // 如果尾节点不等于null，说明队列不为空
    if (pred != null) {
        // 设置node的prev为尾节点
        node.prev = pred;
        // 如果此时有两个线程尝试用将node设置为tail尾节点
        // 所以需要CAS保证只有一个设置成功，另一个执行下面的enq()加入队列
        if (compareAndSetTail(pred, node)) {
            // 设置成功后，添加next指针指向node
            pred.next = node;
            return node;
        }
    }
    // 尾节点为null 或 插入尾节点失败
    enq(node);
    return node;
}
// 循环执行插入操作，直到插入队尾成功
private Node enq(final Node node) {
    for (;;) {
        Node t = tail;
        // 如果尾节点是null，说明队列还没有初始化
        if (t == null) {
            // 将head设置成空node，并且tail=head(说明此时队列初始化了但还没有节点)
            if (compareAndSetHead(new Node()))
                tail = head;
        } else {
            // t!=null，设置node.prev=t
            node.prev = t;
            // CAS设置node到队尾，如果不成功继续循环获取tail直到设置成功
            if (compareAndSetTail(t, node)) {
                // CAS成功，设置t的next属性
                t.next = node;
                // 跳出循环返回node的前驱节点
                return t;
            }
        }
    }
}
```

##### acquireQueued

```java
// 至此node已经插入队列成功，并返回
final boolean acquireQueued(final Node node, int arg) {
    boolean failed = true;
    try {
        boolean interrupted = false;
        for (;;) {
            // 获取node的前继节点
            final Node p = node.predecessor();
            // 如果node的前继节点是头节点，则node尝试去获取锁
            // tryAcquire(arg)会抛出异常
            if (p == head && tryAcquire(arg)) {
                // 获取锁成功，设置头节点为node，并清空thread和prev属性
                setHead(node);
                // 方便回收前继节点p
                p.next = null;
                // 修改failed参数
                failed = false;
                // 跳出循环并返回
                return interrupted;
            }
            // 如果前继节点不是head节点 或 前继节点是head节点但获取不到锁
            // 判断是否需要挂起,如果阻塞节点被唤醒，还会继续循环获取，直到获取锁才return
            if (shouldParkAfterFailedAcquire(p, node) &&
                parkAndCheckInterrupt())
                interrupted = true;
        }
    } finally {
        // 如果跳出循环，failed=false，不跳出循环也不会执行到这里
        // 也就是只有tryAcquire(arg)发生异常了才会执行cancelAcquire()
        if (failed)
            cancelAcquire();
    }
}

final Node predecessor() throws NullPointerException {
    // 获取node的prev节点p
    Node p = prev;
    // 如果p为null则抛出异常，这里的空指针一般不会生效，只是为了帮助虚拟机
    if (p == null)
        throw new NullPointerException();
    else
    // 否则返回前继节点p
        return p;
}
// 将node节点设置为head头节点，获取锁之后都会将头节点相关信息清除
private void setHead(Node node) {
    head = node;
    node.thread = null;
    node.prev = null;
}

// 判断获取锁失败之后是否需要park
private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
    // 获取node前继节点的waitStatus，默认情况下值为0
    int ws = pred.waitStatus;
    // 如果是signal，说明前继节点已经准备就绪，等待被占用的资源释放
    if (ws == Node.SIGNAL)
        return true;
    // 如果前继节点waitStatus>0，说明是Cancel
    if (ws > 0) {
        do {
            // 获取前继节点的前继节点，直到它的状态>0(直到前继节点不是cancel节点)
            node.prev = pred = pred.prev;
        } while (pred.waitStatus > 0);
        // 将不是cancel的节点与node相连
        pred.next = node;
    } else {
        // 尝试将前继节点pred设置成signal状态，设置signal的作用是什么？
        // 在解锁的时候只有head!=null且为signal状态才会唤醒head的下个节点
        // 如果pred状态设置成功，第二次就会进入ws == Node.SIGNAL，返回true
        compareAndSetWaitStatus(pred, ws, Node.SIGNAL);
    }
    return false;
}

// 将线程挂起并检查是否被中断
private final boolean parkAndCheckInterrupt() {
    // 挂机当前线程，不会往下执行了
    LockSupport.park(this);
    // 往下执行的条件: unpark(t)或被中断
    // 返回中断状态(并清空中断状态)
  return Thread.interrupted();
}
```

> LockSupport.park()除了`能够被unpark()唤醒，还会响应interrupt()打断`，但是Lock锁不能响应中断，如果是unpark，会返回false，如果是interrupt则返回true。

##### cancelAcquire

```java
// 节点取消获取锁
  private void cancelAcquire(Node node) {
      // 忽略不存在的node
      if (node == null)
          return;
  	// 清空node的thread属性
      node.thread = null;
  
      // 获取node的不是cancel的前继节点
      Node pred = node.prev;
      while (pred.waitStatus > 0)
          node.prev = pred = pred.prev;
  
      // 获取有效前继节点的后继节点
      Node predNext = pred.next;
  
      // 设置node节点为cancel状态
      node.waitStatus = Node.CANCELLED;
  
      // 如果node是tail尾节点，将pred(非cancel节点)设置为尾节点
      if (node == tail && compareAndSetTail(node, pred)) {
          // 设置尾节点pred的next指针为null
          compareAndSetNext(pred, predNext, null);
      } else {
          int ws;
          // 如果node不是tail尾节点
          // 1.pred不是头节点
          if (pred != head &&
              // 2.如果不是则判断前继节点状态是否是signal
              ((ws = pred.waitStatus) == Node.SIGNAL ||
               // 3.如果不是signal则尝试将pred前继节点设置为signal
               (ws <= 0 && compareAndSetWaitStatus(pred, ws, Node.SIGNAL))) &&
              // 4.判断前继节点线程信息是否为null
              pred.thread != null) {
              // 1，2/3，4条件满足，获取node的后继节点
              Node next = node.next;
              // 如果后继节点不为null且waitStatus<=0
              if (next != null && next.waitStatus <= 0)
                  // 将node的前继节点的后继节点改成node的后继节点
                  compareAndSetNext(pred, predNext, next);
          } else {
              // 如果node前继不是head & 也不是tail
              unparkSuccessor(node);
          }
  		// 将node的后继节点设置为自身，方便回收
          node.next = node;
      }
  }
  // 唤醒head节点后不为cancel的非null节点
  private void unparkSuccessor(Node node) {
      int ws = node.waitStatus;
      // 如果node.waitStatus < 0 ，将其设置为0(初始状态)
      if (ws < 0)
          compareAndSetWaitStatus(node, ws, 0);
  	// 获取node的后继节点
      Node s = node.next;
      // 如果后继节点为null或是cancel，循环查找直到不符合该条件的node
      if (s == null || s.waitStatus > 0) {
          s = null;
          // 重点：从队尾往前找！！！！
          for (Node t = tail; t != null && t != node; t = t.prev)
              if (t.waitStatus <= 0)
                  s = t;
      }
      // 找到不为cancel的非null节点
      if (s != null)
          // 唤醒对应的线程
          LockSupport.unpark(s.thread);
}
```

> Q：为什么AQS的队列查找中，是从队列尾从后向前查找的？
>
> A：节点入队时，都是遵循如下范式设置tail节点：
>
> `① node.prev = tail; `
>
> `② if(compareAndSetTail(tail, node)) { `
>
> `			③ tail.next = node; }` 
>
> ②和③两行代码不是原子性的，所以就存在：线程A将nodeA成功设置为tail尾节点，如果此时线程切换，线程B执行unparkSuccessor方法唤醒尾节点，如果从前往后查询，会发现`tail.next = null`，会认为tail是尾节点，其实此时的尾节点已经被线程A改成了nodeA，doug lea在AQS的文档中也说明了`prev是务必要保证的可靠引用，而next只是一种优化。`
>
> 又比如cancelAcquire方法中，都是断开了next指针，prev指针没有断开，也是上诉理论的一种体现。

##### selfInterrupt

```java
// 当获取锁或插入node到队列的过程中发生了interrupt，那么这里需要补上打断
static void selfInterrupt() {
    Thread.currentThread().interrupt();
}
```

##### 独占锁获取执行流程

![](https://image.leejay.top/image/20200628/9RLQ683DruWq.png?imageslim)

##### unlock

```java
public final boolean release(int arg) {
    // 尝试释放锁
    if (tryRelease(arg)) {
        Node h = head;
        // 如果头节点不为null且不是初始状态
        if (h != null && h.waitStatus != 0)
            // 唤醒头节点的后继节点
            unparkSuccessor(h);
        // 唤醒的线程会重新从parkAndCheckInterrupt()方法中被unpark
        // 然后继续新一轮的获取锁或者获取不到锁park的流程
        return true;
    }
    return false;
}

protected final boolean tryRelease(int releases) {
    // 此时处于已获取锁状态，所以不需要cas获取state，这里也会处理多次重入的情况
    int c = getState() - releases;
    // 如果当前线程不是独占线程抛异常
    if (Thread.currentThread() != getExclusiveOwnerThread())
        throw new IllegalMonitorStateException();
    boolean free = false;
    // 如果state=0说明独占锁或锁重入释放准备完毕
    if (c == 0) {
        free = true;
        setExclusiveOwnerThread(null);
    }
    // 设置状态为0
    setState(c);
    // 释放锁成功
    return free;
}
```

##### lockInterruptibly

可及时响应线程中断的获取锁的API

```java
// 方法入口
public void lockInterruptibly() throws InterruptedException {
  sync.acquireInterruptibly(1);
}
// 可响应中断
public final void acquireInterruptibly(int arg)
  throws InterruptedException {
    // 如果线程被打断直接抛出异常
    if (Thread.interrupted())
        throw new InterruptedException();
    // 尝试去获取锁，获取失败将node加入队列，被中断抛出异常
    if (!tryAcquire(arg))
        doAcquireInterruptibly(arg);
}
// 与acquireQueue几乎相同
private void doAcquireInterruptibly(int arg)
    ...
    	if (shouldParkAfterFailedAcquire(p, node) &&
        	parkAndCheckInterrupt())
            // 与acquireQueue唯一的区别
        	throw new InterruptedException();
    ...
}
```

##### tryLock(time)

响应中断且非阻塞，指定时间内获取不到锁就返回。

```java
public boolean tryLock(long timeout, TimeUnit unit)
    throws InterruptedException {
    return sync.tryAcquireNanos(1, unit.toNanos(timeout));
}
// 与lockInterruptibly相同抛出中断异常切换尝试获取锁，获取锁过程中响应中断
public final boolean tryAcquireNanos(int arg, long nanosTimeout)
    throws InterruptedException {
    if (Thread.interrupted())
        throw new InterruptedException();
    // 如果获取锁失败就去执行doAcquireNanos，直到超时返回false
    return tryAcquire(arg) ||
        doAcquireNanos(arg, nanosTimeout);
}
// 获取锁的超时方法
private boolean doAcquireNanos(int arg, long nanosTimeout)
    throws InterruptedException {
    if (nanosTimeout <= 0L)
        return false;
    // 计算deadline
    final long deadline = System.nanoTime() + nanosTimeout;
    // 将node添加到队列中
    final Node node = addWaiter(Node.EXCLUSIVE);
    boolean failed = true;
    try {
        for (;;) {
            final Node p = node.predecessor();
            if (p == head && tryAcquire(arg)) {
                setHead(node);
                p.next = null; // help GC
                failed = false;
                return true;
            }
            // 计算剩余时间
            nanosTimeout = deadline - System.nanoTime();
            // 如果小于0说明计时结束，获取失败
            if (nanosTimeout <= 0L)
                return false;
            // 判断是否需要阻塞，区别在于该方法阻塞了指定了时长
            // 为什么剩余时间要大于spinForTimeoutThreshold(1000)才会阻塞
            // 说明此时剩余时间非常短，没必要再执行挂起操作了，不如直接执行下一次循环
            if (shouldParkAfterFailedAcquire(p, node) &&
                nanosTimeout > spinForTimeoutThreshold)
                // 调用lockSupport park指定时长
                LockSupport.parkNanos(this, nanosTimeout);
            // park过程中被中断直接抛出异常
            if (Thread.interrupted())
                throw new InterruptedException();
        }
    } finally {
        if (failed)
            cancelAcquire(node);
    }
}
```

> 相比lockInterruptibly方法，tryLock(time)除了响应中断外，还拥有超时控制，由LockSupport.parkNanos()实现。

#### Condition

Condition是一个接口，其实现在Lock内，需要配合Lock锁使用。其内部构建了一个单向队列，操作时不需要使用CAS来保证同步。

```java
final ConditionObject newCondition() {
    return new ConditionObject();
}
public class ConditionObject implements Condition {
    /** First node of condition queue. */
    private transient Node firstWaiter;
  /** Last node of condition queue. */
    private transient Node lastWaiter;
    public ConditionObject() { }
}
```

##### await

```java
// 执行await时肯定已经获取了锁，所以不需要CAS操作
public final void await() throws InterruptedException {
    // 如果当前线程已中断就抛出中断异常
    if (Thread.interrupted())
        throw new InterruptedException();
    // 将当前线程添加到等待队列
    Node node = addConditionWaiter();
    // 线程阻塞之前必须要先释放锁，否则会死锁，这里是全部释放，包括重入锁
    int savedState = fullyRelease(node);
    int interruptMode = 0;
    // 判断node是否在AQS同步队列里面，初始是在条件队列里面
    while (!isOnSyncQueue(node)) {
        // signal后会在此处唤醒
        LockSupport.park(this);
        // 此处用于检测是被unpark还是被中断唤醒
        if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
            // 被中断直接退出，说明await是可以响应中断的
            break;
    }
    // 如果被唤醒或中断，node尝试加入AQS同步队列，在此过程中被中断修改状态为REINTERRUPT
    if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
        interruptMode = REINTERRUPT;
    // 清除cancel节点
    if (node.nextWaiter != null)
        unlinkCancelledWaiters();
    // 被中断唤醒，抛出被中断异常
    if (interruptMode != 0)
        reportInterruptAfterWait(interruptMode);
}

// 添加线程到条件队列
private Node addConditionWaiter() {
    Node t = lastWaiter;
    // 如果lastWaiter不是条件节点，删除非条件节点
    if (t != null && t.waitStatus != Node.CONDITION) {
        unlinkCancelledWaiters();
        t = lastWaiter;
    }
    // 将当前线程创建node
    Node node = new Node(Thread.currentThread(), Node.CONDITION);
    // 队列中没有其他node，当前node就是first
    if (t == null)
        firstWaiter = node;
    else
        // 否则将当前node加入到last后
        t.nextWaiter = node;
    // 并修改last为当前node
    lastWaiter = node;
    return node;
}

// 移除非条件节点
private void unlinkCancelledWaiters() {
    // 获取头节点，从前往后移除(和Node队列从后往前不同)
    Node t = firstWaiter;
    Node trail = null;
    // 当头节点不为null时
    while (t != null) {
        // 获取头节点的下个节点
        Node next = t.nextWaiter;
        // 如果t节点不是条件节点
        if (t.waitStatus != Node.CONDITION) {
            t.nextWaiter = null;
            if (trail == null)
                firstWaiter = next;
            else
                trail.nextWaiter = next;
            if (next == null)
                lastWaiter = trail;
        }
        else
            trail = t;
        // t指向下个节点继续判断
        t = next;
    }
}

// 判断是否在AQS的同步队列中
final boolean isOnSyncQueue(Node node) {
    // 如果waitStatus还是condition或者前驱节点为null，说明是条件队列队首，肯定不再同步队列
    if (node.waitStatus == Node.CONDITION || node.prev == null)
        return false;
    // 因为同步队列才会维护next指针，所以不为null，肯定已经在了
    if (node.next != null) // If has successor, it must be on queue
        return true;
    // 从队尾开始查找node看是否在同步队列中
    return findNodeFromTail(node);
}
```

> Q：condition的await()、signal()和Object中wait()、notify()的区别？
>
> A：首先是基于不同的锁：Lock和Synchronized，其次condition可以存在不同的条件队列，每个条件队列之间互不影响，而Synchronized只会有一个条件队列(或条件变量，根据Synchronized修饰位置不同，分别为this、class类和代码块中内容)。
>
> await()方法是响应中断的，这与lock()是不相同的，并且await()会将锁释放。

##### signal

```java
public final void signal() {
    // 线程必须持有锁才能够调用该方法
    if (!isHeldExclusively())
        throw new IllegalMonitorStateException();
    // 获取条件队列头节点
    Node first = firstWaiter;
    if (first != null)
        // 唤醒头节点
        doSignal(first);
}

private void doSignal(Node first) {
    do {
        //条件队列往后移动一位，新队头为null，将队尾也设为null
        if ( (firstWaiter = first.nextWaiter) == null)
            lastWaiter = null;
        // 清空队首next引用，此时就不在条件队列了
        first.nextWaiter = null;
        // 如果signal失败，那么就移一位获取新队头，直到signal成功
    } while (!transferForSignal(first) &&
             (first = firstWaiter) != null);
}
// 唤醒节点
final boolean transferForSignal(Node node) {
    // 尝试将node的waitStatus设为0，恢复默认状态，如果不能更新说明节点被中断，执行了cancelAcquire
    if (!compareAndSetWaitStatus(node, Node.CONDITION, 0))
        return false;
	// 将队首的node添加到AQS的同步队列，返回node的前驱节点！！
    Node p = enq(node);
    int ws = p.waitStatus;
    // 如果前驱节点是cancel或不是signal，那么直接唤醒当前node，这里会将node在isSyncQueue()中唤醒
    // 假设退出循环，执行acquireQueue()，该方法里面还是会继续判断能否获取锁，不能就尝试设置前驱节点为siganl
    if (ws > 0 || !compareAndSetWaitStatus(p, ws, Node.SIGNAL))
        // 唤醒node节点，
        LockSupport.unpark(node.thread);
    return true;
}
```

> signal只会将条件队列中第一个符合的节点移到AQS的等待队列

##### signalAll

```java
public final void signalAll() {
    // 必须持有锁才能signalAll
    if (!isHeldExclusively())
        throw new IllegalMonitorStateException();
    // 获取头节点
    Node first = firstWaiter;
    if (first != null)
        doSignalAll(first);
}
// signalAll与signal略微不同
private void doSignalAll(Node first) {
    // 因为要将整个条件队列移到同步队列，所以清空首尾标志，只能通过first查找
    lastWaiter = firstWaiter = null;
    do {
        // 循环查找first的符合条件的nextWaiter节点并将它移入同步队列
        Node next = first.nextWaiter;
        first.nextWaiter = null;
        transferForSignal(first);
        first = next;
    } while (first != null);
}
```

> signal和signalAll执行的流程中都不会释放锁，这点与await不同。

##### await总结

- 将当前节点构建成条件节点加入条件队尾，一个AQS同步队列可以对应着多个条件队列。
- `释放全部的锁`，特别是重入锁，如果不释放锁会导致死锁。
- 判断是否在AQS的同步队列中，如果不在就park当前线程，否则就尝试执行获取锁的流程，进而阻塞线程或者获取锁。

##### signal/signalAll总结

- signal会清空头节点在条件队列的引用，头节点还存在，只是队列中引用不在了。
- signal尝试将`条件队列的头节点添加到AQS同步队列的队尾`，如果头节点在同步队列中的前驱节点状态不符合条件，会唤醒头节点。
- signalAll会清空队列首尾标识，并`通过first节点依次将条件队列中的节点移入同步队列中`，若符合相关条件就唤醒相关节点。
- 线程await中isOnSyncQueue()被唤醒，进而执行await的相关逻辑。
- `signal和signalAll不会释放锁`，这与await不同

#### Semaphore

##### acquire

```java
// 共享锁可以立即响应中断异常
public void acquire() throws InterruptedException {
    sync.acquireSharedInterruptibly(1);
}
public final void acquireSharedInterruptibly(int arg)
    throws InterruptedException {
    // 如果线程被中断立即抛出异常
    if (Thread.interrupted())
        throw new InterruptedException();
    if (tryAcquireShared(arg) < 0)
        doAcquireSharedInterruptibly(arg);
}
```

共享锁tryAcquireShared()与独占锁tryAcquire()的不同在于。前者的返回值存在三种情况，后者只有两种情况(true/false)。

| tryAcquireShared 值 |             是否获取锁             |
| :-----------------: | :--------------------------------: |
|          0          | 获取共享锁成功，后续获取可能不成功 |
|         < 0         |           获取共享锁失败           |
|         > 0         |  获取共享锁成功，后续获取可能成功  |

##### tryAcquireShared

```java
protected int tryAcquireShared(int acquires) {
    return nonfairTryAcquireShared(acquires);
}
// 默认是采用了非公平获取锁的方式
final int nonfairTryAcquireShared(int acquires) {
    for (;;) {
        int available = getState();
        int remaining = available - acquires;
        // 如果remaining>=0时就一直自旋CAS修改state状态
        if (remaining < 0 ||
            compareAndSetState(available, remaining))
            return remaining;
    }
}
```

> 为什么remaining=0的时候也要尝试去修改状态，因为这个时候可能有其他线程释放了共享锁，所以有概率能获取到锁。
>
> 如果tryAcquireShared的返回值小于0，说明此时没有锁可以获取，执行入队等相关操作。

##### doAcquireSharedInterruptibly

```java
private void doAcquireSharedInterruptibly(int arg)
          throws InterruptedException {
      // 封装共享节点添加到同步队列队尾
    final Node node = addWaiter(Node.SHARED);
    boolean failed = true;
    try {
        for (;;) {
            // 获取前驱节点
            final Node p = node.predecessor();
            // 如果前驱节点是head节点
            if (p == head) {
                // 尝试获取共享锁
                int r = tryAcquireShared(arg);
                // 注意这里是r>=0
                if (r >= 0) {
                    // 与独占锁不同之处，独占锁是setHead()
                    // 除了当前线程获取锁，后面的线程也有可能获取共享锁
                    setHeadAndPropagate(node, r);
                    p.next = null; // help GC
                    failed = false;
                    return;
                }
            }
            // 判断是否需要中断及中断步骤 与独占锁相同
            if (shouldParkAfterFailedAcquire(p, node) &&
                parkAndCheckInterrupt())
                // 共享锁及时响应中断
                throw new InterruptedException();
        }
    } finally {
        // 如果抛出中断异常，此处就会执行该逻辑
        if (failed)
            cancelAcquire(node);
    }
}
```

##### setHeadAndPropagate

```java
private void setHeadAndPropagate(Node node, int propagate) {
    // 记录老的head用于下面的对比校验
    Node h = head; 
    // 和独占锁一致，将获取锁的node设为新head，清空thread属性
    // 此时node=new head，h=old head
    setHead(node);
    // 此时h = old head
    if (propagate > 0 || h == null || h.waitStatus < 0 ||
        // 此时h = new node
        (h = head) == null || h.waitStatus < 0) {
        Node s = node.next;
        if (s == null || s.isShared())
            doReleaseShared();
    }
}
```

> doReleaseShared()可以理解成unparkSuccessor的升级方法，不止获取锁的过程中被调用，释放锁的过程中也会被调用。
>
> 1. h == null 和 ((h = head) == null) 不会成立，因为之前代码执行过addWaiter，所以队列肯定已初始化，已经初始化那么肯定不为null(head节点中只是thread = null)。
> 2. 条件判断只剩 `propagate > 0 || h.waitStatus < 0 || h.waitStatus < 0 `，需要注意此处的h不是同一个，前面的h是旧head，后面的h是新head。
> 3. 根据外层方法要求 propagate >= 0，那么`propagate > 0`时，获取node的next节点，如果node是tail尾节点，那么 `s == null`成立，执行`doReleaseShared`方法，如果`s == null`不成立，则判断 `s.nextWaiter == SHARED`，添加共享节点时会设置此参数，用于判断是否是共享节点。
> 4. 那么如果`propagate  = 0`时，继续判断`h.waitStatus < 0`，从之前独占锁的唤醒我们知道在`unparkSuccessor`会将`head头节点的waitStatus设为0`，那么此处的条件何时会发生呢？我们需要先查看`doReleaseShared`中的代码，它在`compareAndSetWaitStatus(h, 0, Node.PROPAGATE)`处将head头节点设置为`PROPAGATE`，那么我们也知道`release`方法中也会调用`doReleaseShared`去释放共享锁，所以此处很有可能是其他线程释放了锁，进入下一层判断，所以此时也可能去执行`doReleaseShared`去尝试获取锁。当然此情况比较凑巧，但确实会发生。
> 5. 接上段，如果`旧h.waitStatus  < 0`不成立，那么`新h.waitStatus < 0`条件何时成立呢？在`shouldParkAfterFailedAcquire`中会将前驱节点设置为`SIGNAL`状态后去park当前节点，所以只要先执行过`shouldParkAfterFailedAcquire`方法，后获取锁，那么`新h.waitStatus < 0`肯定成立，进入下一层判断，所以这里也可能会执行`doReleaseShared`方法尝试唤醒后继节点。
> 6. `setHeadAndPropagate`的注释中说明了此方法确实会导致`不必要的唤醒操作`。

##### doReleaseShared

```java
// 唤醒后继节点并确认传播
private void doReleaseShared() {
    // 循环执行
    for (;;) {
        // 获取头节点，接上文，此时的头节点是node，不是老的head节点了
        Node h = head;
        // h != null ，只要队列初始化过，就一直成立
        // h != tail 如果队列中添加过节点，就一直成立
        // 这两个条件保证了队列至少有两个node，其中一个哨兵节点
        if (h != null && h != tail) {
            int ws = h.waitStatus;
            // 如果head是SIGNAL，就执行unparkSuccessor()
            if (ws == Node.SIGNAL) {
                if (!compareAndSetWaitStatus(h, Node.SIGNAL, 0))
                    continue;
                // 修改成功就唤醒头节点的有效后继节点
                unparkSuccessor(h);
            }
            // 如果ws == 0说明h的后继节点已经或即将被唤醒
            // CAS设置为PROPAGATE
            else if (ws == 0 &&
                     !compareAndSetWaitStatus(h, 0, Node.PROPAGATE))
                continue;                // loop on failed CAS
        }
        // 如果waitStatus是PROPAGATE直接判断
        // 跳出循环的关键: 只要新老head相等就跳出循环
        if (h == head)
            break;
    }
}
```

> 只要有线程获取锁设置了`新head`，`h == head`就会不成立导致再次循环，其目的是为了执行`unparkSuccessor(head)来唤醒有效后继节点`。

##### release

```java
public final boolean releaseShared(int arg) {
    // 调用semaphore的内部实现去释放锁
    if (tryReleaseShared(arg)) {
        // 如果成功就尝试唤醒后继节点且传播
        doReleaseShared();
        return true;
    }
    return false;
}
// 释放共享锁
protected final boolean tryReleaseShared(int releases) {
    for (;;) {
        // 获取当前state
        int current = getState();
        // 将 state + 1
        int next = current + releases;
        if (next < current) // overflow
            throw new Error("Maximum permit count exceeded");
        // CAS修改state成功返回true
        if (compareAndSetState(current, next))
            return true;
    }
}
```

> 1. `doReleaseShared`方法在此不再赘述，它保证了`多线程情况下的后继节点能够正常被唤醒`。
>
> 2. `tryReleaseShared`目的就是为了恢复`共享变量state`。便于后面的新线程获取锁。
>
> 3. Sempahore释放锁的时候，`不校验你是否持有共享锁的，所以可以理解成任意线程都可以释放锁。`那么就会出现你的`permit设置为2，当你调用了三次release，你的state为3的情况。`

>    即使调用多次release方法也不会产生影响，因为在`unparkSuccessor`方法中，会去获取next节点，如果没有就`从后往前查找有效节点`再唤醒，没有有效节点就不会唤醒。

##### 共享锁总结

- 共享锁相比独占锁最大的不同在于`setHeadAndPropagate` 和 `doReleaseShared`。
- `setHeadAndPropagate` 用于设置新head，及一定条件下调用`doReleaseShared`，且调用`doReleaseShared`会导致线程不必要的唤醒。
- `doReleaseShared`在获取锁和释放锁的时候都可能被调用，因为是共享锁，即便你获取了锁，后继节点也有可能获取锁。
- `PROPAGATE`与`SIGNAL`的意义相同，都为了让唤醒线程能检测到状态变化，区别在于前者`只作用于共享锁`。
- 共享锁操作共享变量肯定会出现`原子性和有序性`的情况(`permit = 1除外,此时是特殊的独占锁`)。

#### ReadWriteLock

ReadWriteLock是接口，它定义了两个方法：`ReadLock`和`WriteLock`，读写锁的具体实现在`ReentrantReadWriteLock`中。读写锁是之前分析的`独占锁`和`共享锁`两个特性的集合体，具有如下规定：

1. 允许多个线程同时读取变量。
2. 某时刻只允许一个线程写变量。
3. 如果有写线程正在执行写操作，那么禁止其他读线程读取变量。

ReadWriteLock的默认实现类`ReentrantReadWriteLock`

```java
public class ReentrantReadWriteLock  implements ReadWriteLock {
	// 读锁和写锁都是ReentrantReadWriteLock的内部类
    private final ReentrantReadWriteLock.ReadLock readerLock;
    private final ReentrantReadWriteLock.WriteLock writerLock;

    final Sync sync;
    // 读写锁默认是非公平锁
    public ReentrantReadWriteLock() {
        this(false);
    }
    
    // ReadLock和WriteLock都是继承了同一个抽象类Lock，所以他们属于同一个AQS队列
    public ReentrantReadWriteLock(boolean fair) {
        sync = fair ? new FairSync() : new NonfairSync();
        readerLock = new ReadLock(this);
        writerLock = new WriteLock(this);
    }
}
```

> 相比于`Semaphore`，`ReentrantReadWriteLock`采用共享和独占结合的方法。Semaphore就像是一个令牌桶，谁都可以拿取令牌执行任务，谁都可以归还令牌。它不会记录是哪个线程获取了锁，而`ReentrantReadWriteLock`会记录，只有持有相关锁才能来释放锁。

##### state

与独占锁、共享锁的state的使用不同，因为需要表示两种状态，所以对`int型state`做了`高低位切割`，分别表示不同的状态。已知`int=4byte= 32bit`，所以`高16位表示读，低16位表示写`。他们的取值范围在`[0 ~ 2^16 - 1]`，进而我们可以得出，最多有`2^16 -1`个线程可以获取读锁。

![](https://image.leejay.top/image/20200630/iU956J9I2D6U.png?imageslim)

```java
abstract static class Sync extends AbstractQueuedSynchronizer {
      
    static final int SHARED_SHIFT   = 16;
    static final int SHARED_UNIT    = (1 << SHARED_SHIFT);
    // 读锁和写锁的count不能超过MAX_COUNT
    static final int MAX_COUNT      = (1 << SHARED_SHIFT) - 1;
    static final int EXCLUSIVE_MASK = (1 << SHARED_SHIFT) - 1;

    // 返回读锁的count
    static int sharedCount(int c) { 
        return c >>> SHARED_SHIFT; 
    }
    // 返回写锁的count
    static int exclusiveCount(int c) { 
        return c & EXCLUSIVE_MASK; 
    }
}
```

> 1. 读锁和写锁的count不能超过`MAX_COUNT即2^16-1`。
>
> 2. int 型 state 无符号右移16位，得到的即为高16位的count。
>
> 3. int 型 state 与 `1111 1111 1111 1111`做`与运算`，它的结果也不会超过`1111 1111 1111 1111`，因为`与运算`的规则是`都1为1，否则为0`。
>
>    ```java
>    如int state = 3，那么它的二进制如下
>        
>      0000 0000 0000 0000 0000 0000 0000 0011
>    &                     1111 1111 1111 1111
>    ——————————————————————————————————————————    
>                          0000 0000 0000 0011
>    ```
>
> 4. 如果读count + 1，那么就等于 `c  =  c + 1 << 16`，如果count + 1，那么`c = c + 1`。
>
> 5. `如果c 不为 0，当写count = 0时，读count > 0成立`。即读锁已经获取。

##### ThreadLocalHoldCounter

除了需要记录锁被拿取的总次数，还需要记录每个线程分别拿走多少，所以我们使用[ThreadLocal](#7. ThreadLocal内存泄漏问题)，将记录的工作交给线程自己。

```java
abstract static class Sync extends AbstractQueuedSynchronizer {
    // 当前线程持有的读锁数量，当为0的时候置为null
    private transient ThreadLocalHoldCounter readHolds;
    
    // 保存成功获取读锁的线程的count 注意非volatile
    private transient HoldCounter cachedHoldCounter;
    // 用于读锁，表明第一个获取读锁的线程是谁
    private transient Thread firstReader = null;
    // 用于读锁，表明第一个获取读锁的线程重入了几次
    private transient int firstReaderHoldCount;
	Sync() {
        readHolds = new ThreadLocalHoldCounter();
        setState(getState()); // ensures visibility of readHolds
    }
    // 每个线程都拥有HoldCounter类的实例，互不干预
    static final class HoldCounter {
        int count = 0;
        // 这里线程id直接使用id，不使用reference，便于GC
        final long tid = getThreadId(Thread.currentThread());
    }

    static final class ThreadLocalHoldCounter
        extends ThreadLocal<HoldCounter> {
        public HoldCounter initialValue() {
            return new HoldCounter();
        }
    }
}
```

> 1. 这里使用线程ID而不是用线程对象的原因时避免`HoldCounter和ThreadLocal互相绑定导致GC难以释放(可以释放只是需要代价)`，目的就是帮助GC快速回收对象。
> 2. 定义三个成员变量：cachedHoldCounter、firstReader和firstReaderHoldCount原因是为了`快速判断当前线程是否持有读锁`。

##### WriteLock

我们从写锁开始入手，写锁就是独占锁的体现。

- tryLock

  ```java
  // 写锁入口
  public void lock() {
      sync.acquire(1);
  }
  
  public final void acquire(int arg) {
      if (!tryAcquire(arg) &&
          acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
          selfInterrupt();
  }
  // 与独占锁不同的子类实现
  protected final boolean tryAcquire(int acquires) {
      // 获取当前线程
      Thread current = Thread.currentThread();
      int c = getState();
      // 获取写锁的count
      int w = exclusiveCount(c);
      // c!=0说明有锁，但不知道是什么锁
      if (c != 0) {
          // state != 0 且 w = 0说明此时存在读锁，返回false
          // w != 0 说明存在写锁，必须要求当前线程是持有写锁的线程，否则返回false
          if (w == 0 || current != getExclusiveOwnerThread())
              return false;
          // 判断写锁count是否超过最大count
          if (w + exclusiveCount(acquires) > MAX_COUNT)
              throw new Error("Maximum lock count exceeded");
          // 到此处说明是当前线程，所以无竞争，直接set改变写锁count
          setState(c + acquires);
          return true;
      }
      // c=0说明说明无锁
      // 如果不需要阻塞就尝试CAS修改state
      if (writerShouldBlock() ||
          !compareAndSetState(c, c + acquires))
          return false;
      // 设置当前线程为独占线程
      setExclusiveOwnerThread(current);
      return true;
  }
  // 非公平锁实现
  static final class NonfairSync extends Sync {
      // 非公平直接返回false尝试去获取锁
      final boolean writerShouldBlock() {
          return false;
      }
  }
  
  // 公平锁实习那
  static final class FairSync extends Sync {
      // 如果返回true说明前面有节点等待
      final boolean writerShouldBlock() {
          // 判断队列中是否有前驱节点在等待
          return hasQueuedPredecessors();
      }
  }
  
  ```

> 写锁获取成功的情况：
>
> 1. 写锁的线程持有者重入了写锁。
> 2. 写锁不被任何线程持有，当前线程竞争得到了锁。
>
> 写锁获取失败的情况：
>
> 1. 当前线程不是写锁的持有者。
>
> 2. `当前只有读锁没有写锁`，不能将读锁升级为写锁。
>
> 3. 公平锁判断当前线程排在了队列中其他线程后面。
>
> 4. 尝试CAS修改state失败了。

- tryWriteLock

```java
final boolean tryWriteLock() {
    Thread current = Thread.currentThread();
    int c = getState();
    if (c != 0) {
        int w = exclusiveCount(c);
        if (w == 0 || current != getExclusiveOwnerThread())
            return false;
        // 如果等于最大值就抛溢出异常
        if (w == MAX_COUNT)
            throw new Error("Maximum lock count exceeded");
    }
    // 即使是写锁的持有线程还是通过CAS设置state
    if (!compareAndSetState(c, c + 1))
        return false;
    setExclusiveOwnerThread(current);
    return true;
}
```

> 与tryAcquire类似，是`非公平、一次性`的获取写锁，写锁计数默认加1。

- release

  写锁的释放流程与独占锁释放类似，只是tryRelease不同，我们只需要关注AQS的子类实现即可

  ```java
  // ReentrentReadWriteLock
  public void unlock() {
    sync.release(1);
  }
  //AQS.release
  public final boolean release(int arg) {
      if (tryRelease(arg)) {
          Node h = head;
          if (h != null && h.waitStatus != 0)
              unparkSuccessor(h);
          return true;
      }
      return false;
  }
  // boolean/false 释放/不是反
  protected final boolean tryRelease(int releases) {
      // 如果不是写锁持有线程则抛出异常
      if (!isHeldExclusively())
          throw new IllegalMonitorStateException();
      // 计算释放后新值
      int nextc = getState() - releases;
      // 判断写锁count == 0 写锁是否存在重入
      boolean free = exclusiveCount(nextc) == 0;
      // 为true说明已经全部释放
      if (free)
          // 清空当前独占线程
          setExclusiveOwnerThread(null);
      // 设置state
      setState(nextc);
      return free;
  }
  ```

  > 只有`state中低16位值 = 0`的时候才表明写锁释放完毕。

##### ReadLock

读锁就是共享锁的体现，我们直接查看ReentrantReadWriteLock中AQS的子类的`tryAcquireShared`和`tryReleaseShared`实现即可。

- tryAcquireShared

  ```java
  // 首先我们知道int返回值的不同代表了共享锁获取的不同情况(和semaphore一致)
  protected final int tryAcquireShared(int unused) {
  // 获取当前线程
      Thread current = Thread.currentThread();
      // 获取state
      int c = getState();
      // 1. 写锁count = 0，说明此时没有写锁。继续执行
      // 2. 写锁count != 0 ,此时有写锁，但写锁持有者是当前线程。继续执行
      // 3. 写锁count != 0,此时有写锁，但写锁持有者不是当前线程，获取失败，执行中断。
      if (exclusiveCount(c) != 0 &&
          getExclusiveOwnerThread() != current)
          return -1;
      // 计算读锁count，无符号右移16位
      int r = sharedCount(c);
      // 1. readerShouldBlock 判断是否需要阻塞，false/true 不阻塞/阻塞
      // 2. 读锁count < MAX_COUNT
      // 3. 将读锁加1，即state高16位加1
      if (!readerShouldBlock() &&
          r < MAX_COUNT &&
          compareAndSetState(c, c + SHARED_UNIT)) {
          // 执行到这里说明已经成功将读锁加1
          // 如果 r = 0说明当前线程是第一个获取读锁的线程，此时不存在竞争
          if (r == 0) {
              // 设置firstReader 和 firstReaderHoldCount属性
              firstReader  = current;
              firstReaderHoldCount = 1;
            // 如果r != 0 但 firstReader == current说明
            // 第一个读锁线程又重入了，那么只要修改firstReaderHoldCount即可
          } else if (firstReader == current) {
              // 这里不需要加锁，因为当前线程就是第一个读锁线程，不会有其他线程来操作
              firstReaderHoldCount++;
          } else {
              // 此处说明当前线程不是第一个来获取读锁的线程
              // 定义局部变量rh = 成员变量cachedHoldCounter
              HoldCounter rh = cachedHoldCounter;
              // 该条件能够保证rh持有的是当前线程的HoldCounter
              if (rh == null || rh.tid != getThreadId(current))
                  // a.将rh 和 cachedHoldCounter 指向ThreadLocal中的HoldCounter
                  cachedHoldCounter = rh = readHolds.get();
              // 执行到这里说明是之前设置过cachedHoldCounter的线程来获取读锁
              // 运气很好。这里尝试获取结果发现cachedHoldCounter就是当前线程
              // 如果rh.count = 0就说明当前线程释放了读锁，且没有获取读锁的线程HC=null
              // 所以这里当rh.count=0时需要设置rh到当前线程ThreadLocal中
              else if (rh.count == 0)
                  readHolds.set(rh);
              // 不论如何rh.count都会+1，注意cacheHoldCounter = rh,所cHC.count也+1
              rh.count++;
          }
          // 返回1表明获取成功
          return 1;
      }
      // 如果不能获取读锁执行下面逻辑
      return fullTryAcquireShared(current);
  }
  // 公平锁实现
  static final class FairSync extends Sync {
      // 判断当前线程是否是队列第一个节点
      final boolean readerShouldBlock() {
          return hasQueuedPredecessors();
      }
  }
  // 非公平锁实现
  static final class NonfairSync extends Sync {
      final boolean readerShouldBlock() {
          // 第一个节点是shared就返回false否则返回true
          return apparentlyFirstQueuedIsExclusive();
      }
  }
  ```

  > 1. 当`!readerShouldBlock() && r < MAX_COUNT && compareAndSetState(c, c + SHARED_UNIT)`为true时，此时共享锁已经获取成功，大括号中的代码都是在设置相关参数。
  >
  > 2. `r(share read count) = 0`说明此时暂无共享锁，进入该分支的只能是`第一个`来获取共享锁的线程，所以这个分支设置属性时不需要进行同步操作。
  >
  > 3. 若`r != 0`，进入`else if 分支`，此时`firstReader != null`必成立，若`firstReader = current`，说明当前线程是第一个获取共享锁的线程，它重入了，所以这里只需要将`firstReaderHoldCount`加1即可。
  >
  > 4. 进入最后一个else分支，到这个地方的线程必是`非第一个获取共享锁的线程`，首先我们需要明白此处的代码是没有同步操作的，且`cachedHoldCounter没有用volatile`修饰的，也就是`下个线程可能看不到上个线程对cachedHoldCounter的操作`。
  >
  > 5. ①若`rh = null`，那么跳转③。
  >
  >    ② 若`rh != null 但 HC.tid != currentTid`，说明`上个获取读锁的线程和当前线程不同`，跳转③。
  >
  >    ③ 获取当前线程的`HoldCounter(简称HC)`，跳转⑥。
  >
  >    ④ 若 `rh != null 但 rh.tid = currentTid`，说明`上个获取读锁的线程和当前线程相同`，读锁重入了，跳转⑤。
  >
  >    ⑤ 若 `rh.count = 0`，设置`当前线程的HC = rh`，跳转⑥。
  >
  >    ⑥ 至此`rh = 当前线程的HC`，将`rh++的同时cachedHoldCounter++`，然后` return 1`，tryAcquireShared方法结束。
  >
  > 6. step5 中比较疑惑的是何时`else if(rh.count == 0)`成立？这里需要涉及到读锁释放的代码来帮助理解。
  >
  >    ```java
  >    HoldCounter rh = cachedHoldCounter;
  >    if (rh == null || rh.tid != getThreadId(current))
  >        rh = readHolds.get();
  >    int count = rh.count;
  >    if (count <= 1) {
  >        readHolds.remove();
  >        if (count <= 0)
  >            throw unmatchedUnlockException();
  >    }
  >    --rh.count;
  >    ```
  >
  >    > 首先我们需要明确，在`读锁释放过程中，它只是清空了线程的貌私有HC，并没有处理cHC`。
  >    >
  >    > 我们假设线程A（非第一个获取读锁的线程）获取了读锁，释放读锁后再一次获取读锁这个流程来分析：
  >    >
  >    > 1. 线程A获取读锁成功进入else分支后，它会设置`threadA.HoldCounter.count = cacheHoldCounter = 1`，进线程A读锁释放，`rh = cacheHoldCounter != null`且此时count = 1，执行`readHolds.remove()`，然后`--rh.count`，这样`cacheHoldCounter.count也要减1，变成了0`。
  >    > 2. 此时线程A继续获取读锁成功，进入读锁else判断流程发现`rh = cacheHoldCounter != null`，且`rh.tid = currentTid`，所以执行`else if (rh.count == 0)`判断，此时该条件成立，并且当前线程的HC = null，所以这里需要设置当前线程的HC。代码的目的就是为了保证`读锁的获取-释放之后再获取读锁时，不会因为之前读锁的释放导致当前线程的HC为null`。
  >
  > 7. 获取共享锁成功后的代码，都是在处理
  >
  >    `firstReader`：第一个获取读锁的线程。
  >
  >    `firstReaderHoldCount`：第一个获取读锁的线程获取读锁次数。
  >
  >    `cachedHoldCounter`：获取最新获取读锁的线程的HoldCounter。
  >
  >    `HoldCounter.count`：将每个线程获取的读锁次数记录在本地线程中。

- apparentlyFirstQueuedIsExclusive

  ```java
  // 一定概率防止读锁非公平获取锁，让它去排队，让写锁不要无限等待。
  final boolean apparentlyFirstQueuedIsExclusive() {
      Node h, s;
      return (h = head) != null &&
          (s = h.next)  != null &&
          !s.isShared()         &&
          s.thread != null;
  }
  ```

  > 1. 如果head的next节点不存在（队列中第一个等待的节点）直接返回false；
  > 2. 如果队列第一个等待节点是`Shared读`节点，那么返回false，当前线程就可以获取读锁。
  > 3. 如果队列的第一个等待节点是`EXCLUSIVE写锁`，那么返回true，当前线程就不能获取读锁。
  > 4. 方法目的：`一定概率阻止读锁非公平获取动作，如果第一个节点是写锁，让读锁去排队，防止写锁无限等待（注意是一定概率，如果第一个是读锁，第二个是写锁，就不会排队），是非完全不公平读锁。`

- fullTryAcquireShared

  ```java
  // 第一次尝试获取共享锁失败就会进入此方法
  final int fullTryAcquireShared(Thread current) {
      // 定义局部变量rh
      HoldCounter rh = null;
      // 循环获取共享锁
      for (;;) {
          // 获取state
          int c = getState();
          // 如果存在写锁，当前线程不是写锁的持有线程，直接抛错。
          if (exclusiveCount(c) != 0) {
              if (getExclusiveOwnerThread() != current)
                  return -1;
              else 
                  // do nothing
                  // 如果在这个else分支中return -1，会导致死锁
                  // 因为写锁的持有线程获取共享锁失败，会被阻塞，那就没人唤醒它了
                  
          // 执行到此处说明不存在写锁 
          // 非公平锁需要判断队列第一个节点是否是写锁在等待
          // 公平锁需要查看队列是否有head后继节点在等待
          } else if (readerShouldBlock()) {
              // 执行至此说明暂无写锁，但队列中有head后继节点在等待（公平）
              // 一般有等待的节点直接返回-1，继续执行代码的原因是因为需要判断读锁是否重入
              // 查看第一个获取共享锁是否是当前线程
              if (firstReader == current) {
                  // 执行到此处说明当前线程是持有读锁的，且是第一次获取读锁的线程
              } else {
                  if (rh == null) {
                      rh = cachedHoldCounter;
                      // 判断当前是否获取了读锁，若没有则将当前线程的HC置为null
                      if (rh == null || rh.tid != getThreadId(current)) {
                          rh = readHolds.get();
                          // 执行到这说明当前线程第一次获取读锁
                          if (rh.count == 0)
                              readHolds.remove();
                      }
                  }
                  // 1. rh=cachedHoldCounter=!=null 是当前线程的HC，rh.count!=0
                  // 2. rh=当前线程HC，cachedHoldCounter=null， rh.count=0
                  if (rh.count == 0)
                      return -1;
              }
          }
          // 至此当前线程可以获取读锁
          // 判断读锁count是否超过MAX_COUNT
          if (sharedCount(c) == MAX_COUNT)
              throw new Error("Maximum lock count exceeded");
          // CAS修改state
          if (compareAndSetState(c, c + SHARED_UNIT)) {
              // 这里的逻辑与tryAcquireShared类似
              if (sharedCount(c) == 0) {
                  firstReader = current;
                  firstReaderHoldCount = 1;
              } else if (firstReader == current) {
                  firstReaderHoldCount++;
              } else {
                  // 这部分就是设置每个线程获取读锁的count
                  if (rh == null)
                      rh = cachedHoldCounter;
                  if (rh == null || rh.tid != getThreadId(current))
                      rh = readHolds.get();
                  else if (rh.count == 0)
                      readHolds.set(rh);
                  rh.count++;
                  // 设置cachedHoldCounter = 最新获取读锁成功的线程的HC
                  cachedHoldCounter = rh;
              }
              return 1;
          }
      }
  }
  ```

  > 1. 如果`存在写锁，但当前线程不是写锁的持有线程`，直接返回-1，获取共享锁失败。若当前线程是写锁的持有线程，那么直接尝试获取读锁。
  >
  > 2. 与之前`readerShouldBlock`方法返回true直接共享锁获取失败不同，这里需要继续`判断是否读锁重入`的情况。如果当前线程已获取过读锁，那么直接获取共享锁，否则返回-1去排队。
  >
  > 3. `firstReader = current`说明当前线程是第一个获取共享锁的线程，并且当前线程准备重入锁，所以直接准备获取读锁。
  >
  > 4. `firstReader != current`就无法快速判断了，根据之前tryAcquireShared中类似的代码，执行到第一个`if (rh.count == 0)`代码前，说明`rh = 当前线程的HC = new HoldCounter`，说明当前线程是第一次获取读锁，此时第一个`if (rh.count == 0)`必定成立，需要移除`当前线程的HoldCounter`（为什么？`因为当线程没有读锁的时候，当前线程的HoldCounter = null`），这样继续执行到第二个`rh.count == 0`成立，返回-1并退出。
  >
  > 5. 如果不执行第一个而是执行到`if (rh.count == 0)`前，说明当前线程是重入锁，那么`rh.count != 0`必定成立，所以继续执行准备获取共享锁。step4和step5的作用就是：`先判断当队列第一个节点是写锁时（非公平），再判断如果是重入读锁可以获取，如果是第一次获取读锁则不能获取`
  > 6. 下面获取共享锁的代码逻辑与`tryAcquirShared`作用类似，都是获取读锁成功的善后工作。

- tryLock

  ```java
  final boolean tryReadLock() {
      Thread current = Thread.currentThread();
      for (;;) {
          int c = getState();
          if (exclusiveCount(c) != 0 &&
              getExclusiveOwnerThread() != current)
              return false;
          int r = sharedCount(c);
          if (r == MAX_COUNT)
              throw new Error("Maximum lock count exceeded");
          if (compareAndSetState(c, c + SHARED_UNIT)) {
              if (r == 0) {
                  firstReader = current;
                  firstReaderHoldCount = 1;
              } else if (firstReader == current) {
                  firstReaderHoldCount++;
              } else {
                  HoldCounter rh = cachedHoldCounter;
                  if (rh == null || rh.tid != getThreadId(current))
                      cachedHoldCounter = rh = readHolds.get();
                  else if (rh.count == 0)
                      readHolds.set(rh);
                  rh.count++;
              }
              return true;
          }
      }
  }
  ```

  > tryLock和我们分析的tryAcquireShared类似，返回值不同tryLock是boolean值，同时tryLock采用的是`自旋`直到成功获取，或者`写锁被其他线程获取则返回false，获取失败。`

- tryReleaseShared

  ```java
  // AQS.unlock
  public void unlock() {
      sync.releaseShared(1);
  }
  public final boolean releaseShared(int arg) {
      if (tryReleaseShared(arg)) {
          doReleaseShared();
          return true;
      }
      return false;
  }
  // 我们只分析ReentrentReadWriteLock-tryReleaseShared具体实现
  protected final boolean tryReleaseShared(int unused) {
      // 获取当前线程
      Thread current = Thread.currentThread();
      // 如果当前线程是第一个获取读锁的线程
      if (firstReader == current) {
          // 若firstReaderHoldCount = 1成立。说明该线程只获取了一次共享锁
          if (firstReaderHoldCount == 1)
              firstReader = null;
          else
              // 说明第一个获取读锁的线程重入了读锁
              firstReaderHoldCount--;
      } else {
          // 执行到这说明当前线程不是第一个获取读锁的线程
          HoldCounter rh = cachedHoldCounter;
          // 与读锁获取代码类似
          if (rh == null || rh.tid != getThreadId(current))
              rh = readHolds.get();
          // 执行到此rh = 当前线程的HC
          // 如果rh != null 且 rh.tid = getThreadId(current)
          // 说明cachedHoldCounter恰好是当前线程的HC
          int count = rh.count;
          // 如果rh.count > 1说明该线程的读锁重入了
          // 如果rh.count = 1说明该线程的读锁获取了一次
          if (count <= 1) {
              // 清空当前线程的HoldCounter，但是不处理cachedHoldCounter
              readHolds.remove();
              // 如果rh.count = 0说明当前线程没有持有过读锁，抛异常
              if (count <= 0)
                  throw unmatchedUnlockException();
          }
          // 将rh.count减1的同时，如果cachedHoldCounter!= null
          // cachedHoldCounter.count 也要减1
          // 因为他们指向了都是当前线程私有的HoldCounter
          --rh.count;
      }
      //虽然读锁减1了，但是关键变量state还没有修改
      for (;;) {
          int c = getState();
          int nextc = c - SHARED_UNIT;
          if (compareAndSetState(c, nextc))
              // 只有当读写锁全部释放了，才会返回true，否则一直是false
              return nextc == 0;
      }
  }
  ```

   > 1. 读锁的释放容易理解，就是判断当前线程的读锁是否是重入锁，以及将每个线程中的`HoldCounter`中的`count-1`。
   > 2. 需要注意执行到`--rh.count;`，如果`cachedHoldCounter != null（说明cachedHoldCounter 恰好是当前线程的HC）`那么除了`rh.count，cachedHoldCounter.count`也需要减1。
   > 3. `if (count <= 1) `何时`count = 0`？说明当前线程没有持有过读锁，就调用了释放读锁的方法。
   > 4. 只有读写锁完全释放，tryReleaseShared才返回true，继而调用`doReleaseShared`方法。

##### 锁升级与降级

读锁线程多个线程共享的，而写锁单个线程独占的，所以写锁的并发限制比读锁高。

基于以上定义：

1. 同一个线程中，`在释放读锁的前，获取了写锁`，这种情况叫做`锁升级`（读写锁不支持）。

   我们知道获取写锁的前提条件是`读锁释放完毕`，假设此时有两个读锁线程都想获取写锁，这两个线程都想释放除自己以外的读锁，但是他们都在等对方释放，那么会导致`死锁`。究其原因：读锁是多线程共享的，大家都有读锁，凭啥我要让着你去释放我自己的读锁，都不让那就死锁了。

2. 同一个线程中，`在释放写锁的前，获取了读锁`，这种情况叫做`锁降级`（读写锁支持）。

   那为什么支持锁降级呢？因为`写锁是独占的`，此刻只有我一个人持有写锁，所以我想获取读锁就获取，不会有其他人和我抢读锁（除非这个读锁本身，但只是读锁重入而已不会产生竞争）。

总结：

- 如果有一线程持有读锁，那么此时其他线程（包括已持有读锁线程）无法获取写锁`（获取写锁的前提条件是所有的读锁释放完毕）`。

- 如果有一线程持有读锁，那么其他线程（包括已持有读锁线程）是可以获取读锁的，读写互斥，读读不互斥。

- 如果有一线程持有写锁，（除非是持有写锁线程本身）否则其他线程都不能获取读锁/写锁。写读/写写互斥。

  ```java
  public class CachedDate {
      Object data;
      volatile boolean cacheValid;
      final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
  
      void processCachedData() {
          // 先获取读锁
          rwl.readLock().lock();
          // 判断cacheValid即缓存是否可用
          if (!cacheValid) {
              // 到这里说明cache可用准备写值
              // 需要先释放读锁在获取写锁
              rwl.readLock().unlock();
              rwl.writeLock().lock();
              try {
                  // 需要再次简要cacheValid，防止其他线程在此期间改过该值
                  // 在use方法之前获取写锁写入data值及修改cacheValid状态
                  if (!cacheValid) {
                      data = System.currentTimeMillis();
                      cacheValid = true;
                  }
                  // 这里就是锁降级。在写锁释放之前先获取读锁。
                  rwl.readLock().lock();
              } finally {
                  // 释放写锁
                  rwl.writeLock().unlock();
              }
          }
          // 模拟执行use前的耗时操作
  		Thread.sleep(1000L);
          try {
              // 对缓存数据进行打印
              use(data);
          } finally {
              // 最终释放读锁
              rwl.readLock().unlock();
          }
      }
      // 只是打印缓存值
      void use(Object data) {
          System.out.println("use cache data " + data);
      }
  }
  ```

  > Q：为什么要在写锁释放前，获取读锁呢？
  >
  > A：如果线程A修改了值V，在释放写锁前没有获取读锁，那么在调用use()方法前，线程B获取了写锁，并修改了值V，这个修改`对线程A是不可见的`。最终打印的data可能是线程B修改的值。
  >
  > Q：锁降级是否是必要的？
  >
  > A：如果线程A在执行use时传递的`想是自己修改的数据，那么需要锁降级`。如果希望`传递的是最新的数据，那么不需要锁降级`。

##### 读写锁总结

- ReetrentReadWriteLock通过将state变量分为高低16位来解决记录读锁写锁获取的总数。
- 读锁的私有变HoldCounter记录者当前线程获取读锁的次数，底层通过`ThreadLocal`实现。
- 读锁的非公平获获取，通过`apparentlyFirstQueuedIsExclusive`方法一定概率防止了写锁无限等待。
- 当线程A获取写锁时，会因为其他持有`写锁（不包括线程A）`或`读锁（包括线程A）`的线程而阻塞。
- 当线程A获取读锁时，会因为其他持有`写锁（不包括线程A)`而阻塞。

---

#### CountDownLatch

描述`一个或一组线程任务需要等到条件满足之后才能继续执行`的场景。

常见于主线程开启多个子线程执行任务，主线程需等待所有子线程执行完毕才能继续执行的情况。

又比如车间组装产品，你必须要等到其他同事把配件组装好全交给你，你才可以最终组装。

```java
public class CountDownLatchTest {
	// 显示传入计数器值
    private static final CountDownLatch LATCH = new CountDownLatch(2);
    public static void main(String[] args) {
        new Thread(() -> {
            try {
                Thread.sleep(2000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // 子线程执行完毕就需要显式调用该方法
            LATCH.countDown();
        }).start();
        new Thread(() -> {
            try {
                Thread.sleep(2000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            LATCH.countDown();
        }).start();
        System.out.println("等待子线程结束任务 ...");
        try {
            // 主线程阻塞直到计数器=0
            LATCH.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("主线程被唤醒继续执行 ...");
    }
}
```

> 1. CountDownLatch维护一个计数器值，由使用者传入`一个大于0的数值N`来指定。
> 2. 执行await()方法的线程A会阻塞，直到`N = 0`，线程A继续执行。
> 3. 执行子任务的线程B，执行完毕需要显示的`调用countDown()方法`。

##### CountDownLatch

```java
public CountDownLatch(int count) {
    // 需要传入一个大于0的值
    if (count < 0) throw new IllegalArgumentException("count < 0");
    // 本质就是定义state的值
    this.sync = new Sync(count);
}

Sync(int count) {
    setState(count);
}
```

> CountDownLatch的构造函数，需要传入一个`大于0的值N`表明执行`countDown`线程数量。
>
> 如果`N = 0`表明条件符合，表明子线程已经全部执行完毕，主线程可以继续执行。

##### await

```java
public final void acquireSharedInterruptibly(int arg) throws InterruptedException {
    // 响应线程中断，抛出终端异常
    if (Thread.interrupted())
        throw new InterruptedException();
    // 根据tryAcquireShared返回值决定是否将当前线程加入队列。
    if (tryAcquireShared(arg) < 0)
        // 共享锁逻辑
        doAcquireSharedInterruptibly(arg);
}
// 判断是否需要将线程加入队列
protected int tryAcquireShared(int acquires) {
    return (getState() == 0) ? 1 : -1;
}
```

> 1. await方法是响应中断的。
> 2. 判断`共享变量state 是否等于 0`，等于说明子线程执行完毕，否则加入同步队列。
> 3. 如果主线程加入了同步队列，进入`共享锁的自旋（获取锁 -> 阻塞）的流程`，因为只有`state = 0`时`tryAcquireShared > 0`，所以只要子线程没有全部执行完毕，那么主线程就无法获取锁，那么主线程就会阻塞在`parkAndCheckInterrupt`中（共享锁逻辑，将来也会在此处被唤醒）。

##### countDown

```java
// countDown方法不支持传参，每次只能减1
public void countDown() {
    sync.releaseShared(1);
}
public final boolean releaseShared(int arg) {
    if (tryReleaseShared(arg)) {
        // 共享锁逻辑，唤醒后继等待的线程（唤醒主线程）
        doReleaseShared();
        return true;
    }
    return false;
}
// 只有当前方法返回true的时候才会执行doReleaseShared()
protected boolean tryReleaseShared(int releases) {
   // 自旋CAS修改state
    for (;;) {
        int c = getState();
        // 如果state已经是0直接返回，不然state为负数了
        if (c == 0)
            return false;
        int nextc = c-1;
        if (compareAndSetState(c, nextc))
            // 只有CAS修改stsate成功且state=0时才会返回true
            return nextc == 0;
    }
}
```

> countDown方法的本质就是将state变量减1，直到`state = 0`才会执行`doReleaseShared`唤醒阻塞在`await`处的线程。

##### await(time)

```java
// 传入时间及时间单位
public boolean await(long timeout, TimeUnit unit)
    throws InterruptedException {
    return sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
}
// 响应中断
public final boolean tryAcquireSharedNanos(int arg, long nanosTimeout)
            	throws InterruptedException {
    // 线程被中断抛出中断异常
    if (Thread.interrupted())
        throw new InterruptedException();
    return tryAcquireShared(arg) >= 0 ||
        doAcquireSharedNanos(arg, nanosTimeout);
}
protected int tryAcquireShared(int acquires) {
    return (getState() == 0) ? 1 : -1;
}
```

>  await(long timeout, TimeUnit unit)：`方法返回boolean值，主线程阻塞指定时常后被唤醒，查看state = 0是否成立，成立返回true主线程继续执行，否则执行失败。`

##### 总结

- 使用场景：当`某个量化为数字的条件被满足后`，调用await的线程才可以继续开始执行

- CountDownLatch的构造函数需要`显式的传入计数器的值`。
- 调用`await`方法的线程能继续执行的条件就是`state = 0（也是获取共享锁的条件）`，否则继续阻塞。
- 调用`countDown`方法才能修改state的值，且每次调用只能将`state - 1`。

---

#### CyclicBarrier

基于`CountDownLatch`的特性：`计数器为0时，即使调用await，该线程也不会等待其他线程执行完毕而被阻塞`。

`CyclicBarrier`的出现是为了解决复杂场景`CountDownLatch`使用的劣势。

> CountDownLatch中存在两种类型的线程：分别是`调用await方法和调用countDown方法的线程`。
>
> 而CyclicBarrier中只存在一种线程：`调用await的线程扮演了上述两种角色，即先countDown后await`。

`CyclicBarrier`拆分成两部分来理解：

- Cyclic（回环）：当所有等待线程执行完毕后，会重置状态，使其能够重用。
- Barrier（屏障）：线程调用await方法就会阻塞，这个阻塞点就是`屏障点`，等到`所有线程调用await方法`后，线程就会穿过屏障继续往下执行。

> 相比`CountDownLatch`只使用一次，`CyclicBarrier`更强调循环使用。

```java
@Slf4j
public class CyclicBarrierTest {

    // 传入每次屏障之前需要等待的线程数量
    private static final CyclicBarrier BARRIER = new CyclicBarrier(2, () -> {
        // 不能保证每代执行该语句的都是同一个线程
        log.info("doSomenthing before the last thread signal other threads")
    });
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(2);

    public static void main(String[] args) {
        EXECUTOR.execute(() -> {
            try {
                //CyclicBarrier 保证await
                log.info("doSomething ... ");
                BARRIER.await();
                log.info("continue exec ...");
                BARRIER.await();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        EXECUTOR.execute(() -> {
            try {
                log.info("doSomething ... ");
                BARRIER.await();
                log.info("continue exec ...");
                // 如果中断线程，那么会抛出异常
                // Thread.currentThread().interrupt();
                BARRIER.await();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        EXECUTOR.shutdown();
    }
}
// doSomething
// doSomething
// continue exec
// continue exec
```

> 为什么结果不是随机打印日志，而是先打印完`doSomething`，再打印`continue exec`?

##### CyclicBarrier

```java
// 使用ReentrantLock和Condition
private final ReentrantLock lock = new ReentrantLock();
private final Condition trip = lock.newCondition();

// 注意这里是final修饰，代表线程总数，当count=0时重置使用
private final int parties;
// 表明还需要多少个线程到达屏障
private int count;

// 表明每一代线程通过屏障之前需要完成的事情（并不是通过新起线程来实现）
private final Runnable barrierCommand;
// 每一组一起通过屏障的线程叫做一代Generation，不同代之间通过==比较
private Generation generation = new Generation();

public CyclicBarrier(int parties, Runnable barrierAction) {
    // paries的值必须大于0
    if (parties <= 0) throw new IllegalArgumentException();
    this.parties = parties;
    this.count = parties;
    this.barrierCommand = barrierAction;
}

// Generation只有一个属性：broken
private static class Generation {
    // false 表明线程是全部到达后一起穿过屏障
    // true表明线程没有全部到达前，就有线程穿过屏障了
    // 线程监测到会抛出BrokenBarrierException
    boolean broken = false;
}
```

> 1. `CyclicBarrier`的需要借助`Condition`来实现，执行`await的线程`需要加入条件队列等待唤醒。
> 2. `parties`是final修饰的变量，作用于count = 0时的`重新复位计数器`。
> 3. `Generation`表示一组一起通过屏障的线程，不同代之间通过`==`来比较。
> 4. `barrierCommand`用于每代线程通过屏障之前需要完成的事情（不会另起线程执行）。
> 5. 每代都包含一定`parties`的线程，通过属性`broken = true`来表明当代线程全部作废。

##### nextGeneration

```java
private void nextGeneration() {
    // 唤醒上一代的线程（表明此时是有锁的）
    trip.signalAll();
    // 将count重置为parties
    count = parties;
    // new 生成新一代
    generation = new Generation();
}
```

> 该方法的目的是为了`唤醒上一代的线程，并重置count及通过创建对象开启下一代`。

##### breakBarrier

```java
private void breakBarrier() {
    // 修改Generation对象参数
    generation.broken = true;
    // 重置计数器
    count = parties;
    // 唤醒上一代等地的线程
    trip.signalAll();
}
```

> 与`nextGeneration`不同点在于：修改`Generation对象`参数，以及`没有创建下一代Generation`。

##### reset

```java
public void reset() {
    final ReentrantLock lock = this.lock;
    lock.lock();
    try {
        breakBarrier();   // break the current generation
        nextGeneration(); // start a new generation
    } finally {
        lock.unlock();
    }
}
```

> reset方法在获取锁的前提下调用了`breakBarrier 和 nextGeneration`方法，除了`修改这一代Generation的broken、重置计数器外，还创建了下一代Generation`（虽然代码有些重复）。

##### await

```java
// 同一个线程可能多次调用await方法
// 返回值表明还需要多少个线程到达屏障处
public int await() throws InterruptedException, BrokenBarrierException {
    try {
        // false表明不需要判断超时
        return dowait(false, 0L);
    } catch (TimeoutException toe) {
        // await()中的dowait按道理不会抛出该异常
        throw new Error(toe);
    }
}

private int dowait(boolean timed, long nanos) throws InterruptedException, 										BrokenBarrierException, TimeoutException {
    final ReentrantLock lock = this.lock;
    // 获取独占锁
    lock.lock();
    try {
        // 获取这一代的Generation
        final Generation g = generation;
		// 之前breakBarrier方法会修改broken参数为true，如果线程监测到会抛出异常
        if (g.broken)
            throw new BrokenBarrierException();
        // 该方法响应中断，抛出中断异常前会调用breakBarrier
        if (Thread.interrupted()) {
            breakBarrier();
            throw new InterruptedException();
        }
		// 此中代码不需要考虑竞争
        // 将count-1类似countDownLatch中的countDown
        int index = --count;
        if (index == 0) { 
            // index = 0说明除当前线程外的其他线程都执行了await方法
            // 当前需要准备带领其他线程一起冲破屏障了
            boolean ranAction = false;
            try {
                // 执行冲破屏障前的任务
                final Runnable command = barrierCommand;
                // 这里可以看出，没有另起线程去执行，就是当前线程处理的
                if (command != null)
                    command.run();
                // 修改ranAction参数
                ranAction = true;
                // 调用nextGeneration唤醒所有等待线程、重置count并创建下一代
                nextGeneration();
                return 0;
            } finally {
                // 如果当前线程执行command任务失败
                if (!ranAction)
                    // 调用breakBarrier
                    breakBarrier();
            }
        }

        // 执行到此说明当前线程不是当代最后一个线程
        // 自旋直到被中断、await超时、broken=true或count=0
        for (;;) {
            try {
                // 进行await(time)方法的处理
                if (!timed)
                    // 当前count!=0，所以将当前线程放入条件队列等待唤醒
                    // 唤醒后从此处继续执行
                    trip.await();
                else if (nanos > 0L)
                    // Condition.await(指定时长)
                    // 返回的是deadline - currentTime的差值
                    nanos = trip.awaitNanos(nanos);
            } catch (InterruptedException ie) {
                // 如果当前线程被中断执行此处逻辑
                // 判断当前线程的generation是否改变，如果没有改变且g.broken的值是false
                // 执行breakBarrier方法并抛出中断异常
                if (g == generation && ! g.broken) {
                    breakBarrier();
                    throw ie;
                } else {
                    // 执行到此处
                    // 要么是Generation已经更新了，那么不能执行breakBarrier影响这一代
                    // 要么是g.broken = true，说明已经执行过breakBarrier，那就不再执行
                    // 最终修改当前线程中断位位true
                    Thread.currentThread().interrupt();
                }
            }
			// 此时还在循环中，继续判断broken
            if (g.broken)
                throw new BrokenBarrierException();
			// 执行到此如果不是同一代，那么此时只有两种可能
            // 1. 当前线程await后被唤醒，发现代已经更新，即最后一个线程已执行过。直接返回即可
            // 2. reset方法被调用，它其中的nextGeneration创建了新一代。
            if (g != generation)
                return index;
			
            // 执行到此说明broken = false 且 代没有更新，最后一个线程还没来
            // 继续判断是否超时，如果超时调用breakBarrier并抛出异常
            if (timed && nanos <= 0L) {
                breakBarrier();
                throw new TimeoutException();
            }
        }
    } finally {
        // 最终释放锁
        lock.unlock();
    }
}
```

> 1. await方法是响应中断的，并且如果`Generation.broken = true`则会抛出指定异常。
> 2. 若当前线程恰好是执行`当前代执行await方法的最后一个线程`，那么它会执行`barrierCommand`。
> 3. 若不是当前代的最后一个线程，那么会进入`自旋`，加入条件队列阻塞`直到被最后一个线程唤醒`。
> 4. CyclicBarrier暴露了`reset`方法，只有通过这个方法才能`显式中断这一代、重置count和开启下一代`。

##### await(time)

```java
public int await(long timeout, TimeUnit unit)
throws InterruptedException, BrokenBarrierException,TimeoutException {
    return dowait(true, unit.toNanos(timeout));
}

// 下面是与await方法唯二不同的地方
private int dowait(boolean timed, long nanos){
    ...
    for(;;) {
        if (!timed)
            trip.await();
        else if (nanos > 0L)
            // 执行的是Condition.await(time)方法
            nanos = trip.awaitNanos(nanos);
        ...
    }
    ...
    if (timed && nanos <= 0L) {
       breakBarrier();
        // await并不会抛出此异常
       throw new TimeoutException();
    }
}
```

> await(time)方法与await大部分是相同的，区别在于：
>
> 1. await(time)执行的是Condition.await(time)方法，到时`自动唤醒（底层LockSupprt.parkNanos）`来实现的。
> 2. await(time)会`抛出TimeoutException`异常。

##### 总结

- CyclicBarrier和CountDownLatch类似，都要传入`int值来设置计数器（区别：前者>0，后者>=0）`。
- CyclicBarrier的countDown和await都`由同一个线程实现`，而CountDownLatch由两种线程分别实现。
- CyclicBarrier实现了循环利用，每有`parties`个线程到达屏障，就`生成新一代并唤醒老一代线程从await处退出`继续执行各自线程中的代码，直到代码执行完毕或下一个await。

---

#### StampedLock

`JDK1.8`新增的并发工具，回顾之前的`ReentrentReadWriteLock`，它是悲观锁的实现：`只要有线程获取了读锁，获取写锁的线程就需要等待，但有可能导致写锁无限等待（其中使用了apparentlyFirstQueuedIsExclusive方法一定概率降低了写锁无限等待的问题）`。

而`StampedLock`是`乐观锁`的实现，`乐观读`的时候不加锁，读取后`发现数据改变了再升级为悲观读，此时与写互斥`。

```java
@Slf4j
public class StampedLockTest {
    private static final StampedLock LOCK = new StampedLock();
    private static int x, y;

    static void add() {
        long stamp = LOCK.writeLock();
        try {
            x += 1;
            y += 1;
        } finally {
            LOCK.unlockWrite(stamp);
        }
    }

    static void print() {
        // 尝试乐观读
        long stamp = LOCK.tryOptimisticRead();
        int currentX = x， currentY = y;
        // 如果stamp修改了，这时再加悲观读锁
        if (!LOCK.validate(stamp)) {
            log.info("value has changed ...");
            stamp = LOCK.readLock();
            try {
                currentX = x;
                currentY = y;
            } finally {
                LOCK.unlockRead(stamp);
            }
        }
        log.info("x: {}, y: {}", currentX, currentY);
    }

    public static void main(String[] args) throws InterruptedException {

        for (int i = 0; i < 10; i++) {
            new Thread(StampedLockTest::add).start();
            Thread.sleep(new Random().nextInt(2) * 1000);
            new Thread(StampedLockTest::print).start();
        }
    }
}
```

> 如上述代码所示：
>
> 1. 相比读写锁，`StampedLock`引入了乐观锁概念，只有变量发生改变才去加读锁。
> 2. 除此之外`StampedLock`的方法都会返回一个`版本号：stamp（state），用来代表此时此刻的版本`。

##### StampedLock

```java
// 移位基数
private static final int LG_READERS = 7;
// 读锁个数每次增加的单位
private static final long RUNIT = 1L;
// 第8位表示写锁
private static final long WBIT  = 1L << LG_READERS;
// 低7位表示读锁
private static final long RBITS = WBIT - 1L;
// 最大读线程个数
private static final long RFULL = RBITS - 1L;
// 写锁和读锁个数的二进制码
private static final long ABITS = RBITS | WBIT;
// 对读线程个数取反
private static final long SBITS = ~RBITS;
// 将写线程左移一位
private static final long ORIGIN = WBIT << 1;
public StampedLock() {
    state = ORIGIN;
}
```

> 初始状态`state = ORIGIN = 1L << 8`，state类型是`long`。
>
> 我们可以看出`第8位表示写锁的状态`，只有0/1两种情况，这样`写锁就不支持重入`了。`低7位表示读锁被获取的次数`。剩下的其他位是用来表示版本号的，他们共同构成了state。
>
> 

| 变量（long）               | 二进制(64bit，省略为0)  | 十进制 |
| :------------------------- | :---------------------- | :----- |
| RUNIT = 1L                 | 0000 ... 0000 0001      | 1      |
| **WBIT =  1L << 7**        | 0000 ... 1000 0000      | 128    |
| **RBITS = WBIT - 1L**      | 0000 ... 0111 1111      | 127    |
| RFULL = RBITS - 1L         | 0000 ... 0111 1110      | 126    |
| **ABIT =  RBITS \|  WBIT** | 0000 ... 1111 1111      | 255    |
| SBITS = ~RBITS             | 1111 ... 1000 0000      | -128   |
| **ORIGIN =  WBIT << 1**    | 0000 ... 0001 0000 0000 | 256    |

##### Wait Node

```java
static final class WNode {
    // 前驱节点
    volatile WNode prev;
    // 后继节点
    volatile WNode next;
    // 读线程使用，类似栈结构的链表连接读线程
    volatile WNode cowait;
    // 节点持有的线程
    volatile Thread thread;
    // 节点的状态： 0、WAITING、CANCELED
    volatile int status;
    // RMODE WMODE
    final int mode;
    // 构造函数 需要传入mode和当前节点前驱节点
    WNode(int m, WNode p) { mode = m; prev = p; }
}
```

> StampedLock内部维护了和`AQS的Node类似`的节点，但有几点不同：
>
> 1. WNode中的`cowait属性用于将读节点通过链表的形式`进行连接。
> 2. WNode中的status只有三种状态：`0、WAITING、CANCELED`。
> 3. WNode中的mode属性用于表示当前的节点是：`RMODE(读) or WMODE(写)`。

![](https://image.leejay.top/image/20200707/oyfmhKphdfR2.png?imageslim)

##### writeLock

```java
public long writeLock() {
    long s, next;
    return ((((s = state) & ABITS) == 0L &&
             U.compareAndSwapLong(this, STATE, s, next = s + WBIT)) ?
            next : acquireWrite(false, 0L));
}
```

> 我们将return拆分成三个部分来看：
>
> ```java
> ① ((s = state) & ABITS) == 0L
> ② U.compareAndSwapLong(this, STATE, s, next = s + WBIT)
> ③ ? next : acquireWrite(false, 0L)
> ```
>
> 1. ① 与 ② 同时成立时，返回`next`。若有一个不成立，返回`acquireWrite(false, 0L)`。
> 2. 默认情况下`state第9位是1，其余位都是0`，而``ABIT低8位都是1(final)`。所以进行`&`运算，可以推导出`只要&运算的结果不为0，说明此时有写锁或者读锁。`若结果不为0，执行`acquireWrite`。
>
> ```java
>   1 0000 0000
> & 0 1111 1111
> ——————————————  == 0 成立，state的低8位中有一位不为0，那么这个公式的结果肯定不为0
>   0 0000 0000     
> ```
>
> 3. 假设state是初始状态，`((s = state) & ABITS) == 0L`成立，那么执行CAS方法，尝试将`state`的值由`初始状态s改为s + WBIT(1000 0000)`，即`1 1000 0000`，表明获取写锁成功。那么返回`next`作为版本号。
> 4. 若CAS修改失败，那么说明有另外一个线程获取了写锁，那么执行`acquireWrite`方法。

##### acquireWrite

```java
// 获取CPU核心数
private static final int NCPU = Runtime.getRuntime().availableProcessors();
// 加入队列前的最大自旋次数 64次
private static final int SPINS = (NCPU > 1) ? 1 << 6 : 0;
// 前驱节点是head时的自旋次数，大于加入队列的自旋，说明要到我获取锁了，激动的自旋次数也多了
private static final int HEAD_SPINS = (NCPU > 1) ? 1 << 10 : 0;
private long acquireWrite(boolean interruptible, long deadline) {
    // 定义此次线程的wnode节点和前驱节点p
    WNode node = null, p;
    // 第一次自旋，spins = -1，通过自旋加入队列尾部
    for (int spins = -1;;) {
        long m, s, ns;
        // 如果(s = state) & ABITS) == 0L说明恰巧写锁被释放了，那么直接去CAS获取锁
        if ((m = (s = state) & ABITS) == 0L) {
            // 若获取成功直接返回ns = 新的版本号
            if (U.compareAndSwapLong(this, STATE, s, ns = s + WBIT))
                return ns;
        }
        // 如果自旋次数小于0，重新计算自旋次数
        else if (spins < 0)
            // 1. 何时m == WBIT，只有写锁没有读锁的才会成立
            // 2. wtail = whead的情况和AQS的一致，就是
            // 队列未初始化（wtail = whead = null）或
            // 只有一个哨兵节点还没有其他节点的情况(wtail = whead = new WNode())
            // 1和2同时成立说明队列没有等待节点，且写锁会被释放（时间不确定），继续自旋
            spins = (m == WBIT && wtail == whead) ? SPINS : 0;
        else if (spins > 0) {
            // 获取当前线程的随机数（基于ThreadLocalRandom）>= 0时自旋次数减1
            if (LockSupport.nextSecondarySeed() >= 0)
                --spins;
        }
        // 如果wtail = null说明队列还未初始化
        // 无论p是否为null，它都代表了队列的尾节点
        else if ((p = wtail) == null) {
            // 创建写模式节点
            WNode hd = new WNode(WMODE, null);
            // 通过CAS将hd设置为头节点
            if (U.compareAndSwapObject(this, WHEAD, null, hd))
                // 设置成功将hd也设置为wtail（非原子性，此时可能线程切换）
                wtail = hd;
        }
        // 如果当前线程的WNode = null，那么创建当前线程的WNode，此时 p = whead
        else if (node == null)
            node = new WNode(WMODE, p);
        // 如果node前驱不是p，那么设置为p，因为p代表了队列的tail节点
        // 因为存在另一个线程比当前线程早加入了队列
        else if (node.prev != p)
            node.prev = p;
        // 通过CAS修改tail尾节点，和AQS一样，prev是务必要保证的
        else if (U.compareAndSwapObject(this, WTAIL, p, node)) {
            // CAS成功，设置next指针，非原子性，所以next非可靠，此时线程切换会有影响
            p.next = node;
            // 只有成功将当前线程加入队列尾部，那么才会退出第一次的自旋
            break;
        }
    }

    // 第二次自旋
    for (int spins = -1;;) {
        WNode h, np, pp; int ps;
        // p代表了当前节点的前驱节点，也代表了此刻的尾节点wtail
        // 如果p = head说明当前线程的前驱结点是head节点，快要到自己
        if ((h = whead) == p) {
            // 如果自旋次数小于0赋予新的自旋次数
            if (spins < 0)
                spins = HEAD_SPINS;
            // 如果自旋次数小于最大自旋次数那么将当前自旋次数翻倍
            else if (spins < MAX_HEAD_SPINS)
                spins <<= 1;
            // 第三个自旋，自旋中又套了一个自旋
            for (int k = spins;;) {
                long s, ns;
                // 先判断写锁是否释放并CAS修改state
                if (((s = state) & ABITS) == 0L) {
                    if (U.compareAndSwapLong(this, STATE, s,
                                             ns = s + WBIT)) {
                        // 获取成功，设置node为头节点
                        whead = node;
                        // 清空prev指针
                        node.prev = null;
                        // 返回state
                        return ns;
                    }
                }
                // 说明此时写锁还没释放或者CAS失败（此时其他线程获取了锁）
                // 随机将自旋次数k减1，直到k <= 0时退出第三次自旋
                // 这里的退出说明head自旋次数已经用完了
                else if (LockSupport.nextSecondarySeed() >= 0 &&
                         --k <= 0)
                    break;
            }
        }
        // 执行到此：
        // 1. 当前线程的前驱节点不是头节点，
        // 2.第三次自旋获取写锁一直没成功（有个线程持有的写锁未释放）
        else if (h != null) {
            // 协助唤醒head头节点下面的cowait下的读节点
            WNode c; Thread w;
            // 循环执行，只要节点的cowait属性不为null
            while ((c = h.cowait) != null) {
                // 只有CAS设置当前读节点的下一个读节点成功才能唤醒当前读节点的线程
                if (U.compareAndSwapObject(h, WCOWAIT, c, c.cowait) &&
                    (w = c.thread) != null)
                    // 唤醒读线程
                    U.unpark(w);
            }
        }
        // 若whead = h，说明此间头节点没有发生变化
        if (whead == h) {
            // 若当前线程的前驱节点不是尾节点p，说明尾节点改变了
            if ((np = node.prev) != p) {
                // 更新尾节点p并设置尾节点p的next指针为当前线程的node
                if (np != null)
                    (p = np).next = node;   // stale
            }
            // 如果p节点的状态为0，CAS修改为WAITING
            else if ((ps = p.status) == 0)
                U.compareAndSwapInt(p, WSTATUS, 0, WAITING);
            // 如果p节点是CANCELLED，将p的前驱额和node相连
            else if (ps == CANCELLED) {
                if ((pp = p.prev) != null) {
                    node.prev = pp;
                    pp.next = node;
                }
            }
            else {
                // 处理超时问题
                long time; 
                // 如果deadline = 0L就设置time = 0L
                if (deadline == 0L)
                    time = 0L;
                // 判断deadline -currenttime 是否小于 0，小于0需要将节点cancel
                else if ((time = deadline - System.nanoTime()) <= 0L)
                    return cancelWaiter(node, node, false);
                // 获取当前线程
                Thread wt = Thread.currentThread();
                // 添加当前线程到parkBlocker属性
                U.putObject(wt, PARKBLOCKER, this);
                // 修改node的thread属性
                node.thread = wt;
                // 如果前驱节点状态小于0 且 队列已初始化 且 无法获取写锁
                // 且 头节点没有发生变化 且 node的前驱节点没有变化
                if (p.status < 0 && (p != h || (state & ABITS) != 0L) &&
                    whead == h && node.prev == p)
                    // 就去阻塞当前线程
                    U.park(false, time);
                node.thread = null;
                U.putObject(wt, PARKBLOCKER, null);
                // 如果被中断了那么就取消当前节点
                if (interruptible && Thread.interrupted())
                    return cancelWaiter(node, node, true);
            }
        }
    }
}
```

> 与AQS的写锁获取区别：`StampedLock的锁获取的自旋是有次数限制的，并且不同情况自旋次数不同`。
>
> 写锁的获取总体分为三个部分，分别对应着`三次自旋`：
>
> 1. 第一次自旋：尝试获取写锁，若成功则返回state，失败就继续判断`spins是否需要重新赋值（若队列刚初始化且写锁还没被释放，spins = 0）`，若`spins = 0`则将`当前线程构造成node添加到队尾（此过程可能包括队列初始化）`，否则自旋加入队尾，最终只有加入成功才会跳出自旋。
> 2. 第二次自旋上：先判断`当前线程前驱节点是否是head节点`，如果是那么加大自旋次数，并开启第三次自旋去获取写锁，若成功则设置node为新head且清空prev属性。
> 3. 第二次自旋下：若当前节点不是head的后继节点，那么`尝试唤醒头节点中cowait连接的读线程`。最后处理节点的状态并处理超时问题（包括清理无效节点），若最终还是无法获取写锁，那么就会阻塞当前线程。
>
> 获取写锁的标志：将变量`state`的第8位设为1。反之为0表示没有获取写锁。

##### unlockWrite

```java
public void unlockWrite(long stamp) {
    WNode h;
    // 若state != stamp 说明当前传入的版本号不对
    // 若stamp & WBIT = 0L说明写锁没被获取，那就无需释放
    // 上述条件成立一个抛出异常
    if (state != stamp || (stamp & WBIT) == 0L)
        throw new IllegalMonitorStateException();
    // 将stamp加1同时判断版本号是否为0，为0就设为初始值否则设为当前值
    state = (stamp += WBIT) == 0L ? ORIGIN : stamp;
    // 如果头节点不为null，且头节点不为0
    if ((h = whead) != null && h.status != 0)
        release(h);
}
// 释放头节点的后继节点
private void release(WNode h) {
    if (h != null) {
        WNode q; Thread w;
        // 将头节点status改为WAITING
        U.compareAndSwapInt(h, WSTATUS, WAITING, 0);
        // 如果head的后继无效，那么从后往前查找（和AQS一致），直到找到
        if ((q = h.next) == null || q.status == CANCELLED) {
            for (WNode t = wtail; t != null && t != h; t = t.prev)
                if (t.status <= 0)
                    q = t;
        }
        // 唤醒符合条件的后继节点
        if (q != null && (w = q.thread) != null)
            U.unpark(w);
    }
}
```

> 1. `state = (stamp += WBIT) == 0L ? ORIGIN : stamp`：
>
>    ```java
>    已知代码执行到此，当前线程必有写锁，那么当前stamp必符合如下个规则：
>    xxxx ... xxxx 1xxx xxxx 第8位是1
>    那么加上WBIT： 1000 0000 变为
>    xxxx ... xxx1 0000 0000，即会向高位进1，也就是将版本号 + 1
>    ```
>
>    > 何时`stamp += WBIT = 0L`呢？
>    >
>    > 当`stamp = 0b1111...1000 0000（64bit）`即除了低7位是0，其他位全是1位，加上`WBIT = 1000 0000`就会为0，说明此时stamp版本号已全部用完，需要重置。
>
> 2. 写锁的释放与AQS的类似，除了`判断是否已有写锁的`不同，剩下的比如`唤醒头节点的有效后继节点、从队尾往前查找`都是相同的。

##### readLock

```java
public long readLock() {
    long s = state, next;  // bypass acquireRead on common uncontended case
    return ((whead == wtail && (s & ABITS) < RFULL &&
        U.compareAndSwapLong(this, STATE, s, next = s + RUNIT)) ?
            next : acquireRead(false, 0L));
}
```

> 又称为`悲观读`，和写锁互斥，流程分成三个部分：
>
> ```java
> ① whead == wtail
> ② (s & ABITS) < RFULL 
> ③ U.compareAndSwapLong(this, STATE, s, next = s + RUNIT)
> ```
>
> 1. `whead = wtail `说明队列中还没有过节点（非哨兵节点）。
> 2. `(s & ABITS) < RFULL `判断当前读锁数量是否超过最大数量。
> 3. 若①、②成立则尝试CAS修改`STATE`状态，将读线程数量 + 1。
> 4. 若①、②、③全成立返回新的state，否则返回`acquireRead`的返回值

##### acquireRead

```java
private long acquireRead(boolean interruptible, long deadline) {
    WNode node = null, p;
    // 第一个自旋
    for (int spins = -1;;) {
        WNode h;
        // 判断队列是否初始化且有无除当前线程外的其他节点
        if ((h = whead) == (p = wtail)) {
            // 第二个自旋 尝试获取读锁
            for (long m, s, ns;;) {
                // 若相同则继续判断读线程是否超过最大数量
                if ((m = (s = state) & ABITS) < RFULL ?
                    // true执行CAS修改并返回state
                    U.compareAndSwapLong(this, STATE, s, ns = s + RUNIT) :			  				// false 此时读线程溢出，重置RBITS并返回0L
                    (m < WBIT && (ns = tryIncReaderOverflow(s)) != 0L))
                    return ns;
                
                else if (m >= WBIT) {
                    if (spins > 0) {
                        // 和写锁类似，有概率将spins-1
                        if (LockSupport.nextSecondarySeed() >= 0)
                            --spins;
                    }
                    else {
                        // 自旋为0需要判断是否跳出循环
                        if (spins == 0) {
                            WNode nh = whead, np = wtail;
                            if ((nh == h && np == p) || (h = nh) != (p = np))
                                break;
                        }
                        // 重置spins
                        spins = SPINS;
                    }
                }
            }
        }
        
       // 无尾节点那么初始化队列
        if (p == null) { 
            WNode hd = new WNode(WMODE, null);
            // 和写锁相同设置head节点
            if (U.compareAndSwapObject(this, WHEAD, null, hd))
                wtail = hd;
        }
        // 和写锁类似，只是mode变为RMODE
        else if (node == null)
            node = new WNode(RMODE, p);
        // 如果头尾相同或尾节点不是读模式节点
        else if (h == p || p.mode != RMODE) {
            // 将当前节点入队尾
            if (node.prev != p)
                node.prev = p;
            // 直到添加成功退出自旋
            else if (U.compareAndSwapObject(this, WTAIL, p, node)) {
                p.next = node;
                // 这里的break会跳到第五个自旋处
                break;
            }
        }
        // 这里说明尾节点是读模式节点 CAS 将当前节点挂到cowait属性下
        else if (!U.compareAndSwapObject(p, WCOWAIT,
                                         node.cowait = p.cowait, node))
            // CAS失败就设为null
            node.cowait = null;
        else {
            // 第三段自旋 用于阻塞当前线程
            for (;;) {
                WNode pp, c; Thread w;
                // 若头节点不为空且cowait不为空，那么唤醒其中等待的读线程
                if ((h = whead) != null && (c = h.cowait) != null &&
                    U.compareAndSwapObject(h, WCOWAIT, c, c.cowait) &&
                    (w = c.thread) != null) 
                    U.unpark(w);
                // 若头节点等于tail的前驱节点，说明快到自己获取锁了
                if (h == (pp = p.prev) || h == p || pp == null) {
                    long m, s, ns;
                    // 第四段自旋 尝试获取锁
                    do {
                        // 代码和第一段自旋中的获取锁相同，判断读锁数量同时获取读锁
                        if ((m = (s = state) & ABITS) < RFULL ?
                            U.compareAndSwapLong(this, STATE, s,
                                                 ns = s + RUNIT) :
                            (m < WBIT &&
                             (ns = tryIncReaderOverflow(s)) != 0L))
                            return ns;
                    } while (m < WBIT);
                }
                // 如果头节点没有变过且前驱节点没有改变，那么需要阻塞当前线程了
                if (whead == h && p.prev == pp) {
                    long time;
                    // 如果前置节点的前节点为null或头节点等于前继节点或前置节点状态是cancel
                    if (pp == null || h == p || p.status > 0) {
                        // 置为null
                        node = null;
                        // 退出自旋从第一个自旋重试
                        break;
                    }
                    // 以下是执行超时机制的代码，和写锁相同
                    if (deadline == 0L)
                        time = 0L;
                    else if ((time = deadline - System.nanoTime()) <= 0L)
                        return cancelWaiter(node, p, false);
                    Thread wt = Thread.currentThread();
                    U.putObject(wt, PARKBLOCKER, this);
                    // 设置当前线程到节点中
                    node.thread = wt;
                    // 若前驱节点不是头节点 且 头节点和前驱节点没变过
                    if ((h != pp || (state & ABITS) == WBIT) &&
                        whead == h && p.prev == pp)
                        // 当条件符合的时候阻塞当前线程
                        U.park(false, time);
                    node.thread = null;
                    U.putObject(wt, PARKBLOCKER, null);
                    if (interruptible && Thread.interrupted())
                        return cancelWaiter(node, p, true);
                }
            }
        }
    }
	//第五段自旋，处理第一个加入队尾的读线程
    for (int spins = -1;;) {
        WNode h, np, pp; int ps;
        // 这其中的逻辑和之前的代码类似，都是判断是否前继节点是头节点，然后尝试获取读锁
        if ((h = whead) == p) {
            if (spins < 0)
                spins = HEAD_SPINS;
            else if (spins < MAX_HEAD_SPINS)
                spins <<= 1;
            // 第六段自旋
            for (int k = spins;;) { // spin at head
                long m, s, ns;
                // 获取读锁且判断是否超过最大读锁限制
                if ((m = (s = state) & ABITS) < RFULL ?
                    U.compareAndSwapLong(this, STATE, s, ns = s + RUNIT) :
                    (m < WBIT && (ns = tryIncReaderOverflow(s)) != 0L)) {
                    WNode c; Thread w;
                    whead = node;
                    node.prev = null;
                    // 协助唤醒当前节点中的挂在cowait属性上的读节点
                    while ((c = node.cowait) != null) {
                        if (U.compareAndSwapObject(node, WCOWAIT,
                                                   c, c.cowait) &&
                            (w = c.thread) != null)
                            U.unpark(w);
                    }
                    return ns;
                }
                // 若其他线程占有写锁，随机将spins-1且若没有自旋次数就break
                else if (m >= WBIT &&
                         LockSupport.nextSecondarySeed() >= 0 && --k <= 0)
                    break;
            }
        }
        else if (h != null) {
            WNode c; Thread w;
            while ((c = h.cowait) != null) {
                if (U.compareAndSwapObject(h, WCOWAIT, c, c.cowait) &&
                    (w = c.thread) != null)
                    U.unpark(w);
            }
        }
        // 如果头节点没变化
        if (whead == h) {
            // 更新前置节点状态
            if ((np = node.prev) != p) {
                if (np != null)
                    (p = np).next = node;   // stale
            }
            // 将等待的节点状态设为WAITING
            else if ((ps = p.status) == 0)
                U.compareAndSwapInt(p, WSTATUS, 0, WAITING);
            // 如果节点已取消，那么移除队列
            else if (ps == CANCELLED) {
                if ((pp = p.prev) != null) {
                    node.prev = pp;
                    pp.next = node;
                }
            }
            else {
                // 和之前逻辑相同，处理超时问题
                long time;
                if (deadline == 0L)
                    time = 0L;
                else if ((time = deadline - System.nanoTime()) <= 0L)
                    return cancelWaiter(node, node, false);
                Thread wt = Thread.currentThread();
                U.putObject(wt, PARKBLOCKER, this);
                node.thread = wt;
                if (p.status < 0 &&
                    (p != h || (state & ABITS) == WBIT) &&
                    whead == h && node.prev == p)
                    U.park(false, time);
                node.thread = null;
                U.putObject(wt, PARKBLOCKER, null);
                if (interruptible && Thread.interrupted())
                    return cancelWaiter(node, node, true);
            }
        }
    }
}
```

> 1. 一进来如果无写锁、当前队列没有其他节点或队列未初始化，那么尝试获取读锁，成功就返回。
> 2. 若无法获取读锁，那么和写锁一样，会尝试把当前线程加入队列，但这里分为两种：
>    1. 如果当前线程是`连续几个读线程中第一个加入的读线程`，那么直接`加入队尾`。
>    2. 若不是连续几个读线程第一个加入的读线程，会`进入到首个读节点的cowait属性中，形成链表结构`。
> 3. 和写锁相同，如果长时间无法获取读锁，那么会阻塞当前线程，直到被唤醒继续自旋获取锁。

##### unlockRead

```java
public void unlockRead(long stamp) {
    long s, m; WNode h;
    // 循环执行
    for (;;) {
        // 若版本号不同 或 不存在写锁 或 只有写锁无读锁
        // 上述条件符合一条就抛异常
        if (((s = state) & SBITS) != (stamp & SBITS) ||
            (stamp & ABITS) == 0L || (m = s & ABITS) == 0L || m == WBIT)
            throw new IllegalMonitorStateException();
        // 只有当前读锁次数小于最大读锁次数尝试释放锁
        if (m < RFULL) {
            // CAS修改state
            if (U.compareAndSwapLong(this, STATE, s, s - RUNIT)) {
                // 直到读锁全部释放且头节点不为null且头节点状态不为0
                if (m == RUNIT && (h = whead) != null && h.status != 0)
                    // 唤醒头节点的后继节点并break
                    release(h);
                break;
            }
        }
        // 读线程数量溢出如果
        else if (tryDecReaderOverflow(s) != 0L)
            break;
    }
}
```

> 循环释放读锁节点直到为0，然后唤醒头节点的下一个有效后继节点。

##### tryOptimisticRead

```java
// 锁的乐观读
public long tryOptimisticRead() {
    long s;
    // 判断是否存在写锁，存在就返回0L，不存在就返回 state的高56位
    return (((s = state) & WBIT) == 0L) ? (s & SBITS) : 0L;
}
```

> 又称为`乐观读`，此处的代码是没有加锁的，所以需要配合`validate`方法使用。

##### validate

```java
// 校验版本号
public boolean validate(long stamp) {
    // Unsafe的内存屏障api 
    // 为什么使用内存屏障，因为tamp变量没有被volatile修饰
    U.loadFence();
    // 返回state和stamp是否相同
    return (stamp & SBITS) == (state & SBITS);
}

void print() {
	long stamp = LOCK.tryOptimisticRead();
    // 如果stamp修改了，这时再加悲观读锁
    int currentX = x, currentY = y;
    if (!LOCK.validate(stamp)) {
        ...
    }   
}
```

> 我们需要保障`tryOptimisticRead`和`validate`设计的三行代码`不能被重排序`，因为state已经被volatile修饰，但stamp不是volatile，所以在validate中加入`内存屏障`。

##### 总结

- `StampedLock`不是基于AQS来实现的，但是其内部实现和AQS类似。
- `StampedLock`不支持锁的重入、不支持条件变量且只有非公平实现。
- `StampedLock`的`允许一个线程在存在多个读线程的时候获取写锁`。
- `StampedLock`的悲观读和`ReentrentReadWriteLock`相同，都会因为写锁存在而阻塞。
- `StampedLock`的乐观读，是线程不安全的，但读写不互斥。
- `StampedLock`支持`锁的升级和降级`，而`ReentrentReadWriteLock`只支持`锁降级`。
- `StampedLock`唤醒线程是一次性唤醒连续的读锁，并且其他线程还会协助唤醒。

---

### Queue

#### BlockingQueue

##### 概念

`BlockingQueue`带`阻塞`功能的`线程安全`队列，但队列已满时会阻塞添加者，当队列为空时会阻塞获取者。它本身是一个接口，具体的功能由它的实现类来完成。

![](https://image.leejay.top/image/20200707/w6jIy7hJTtxv.png?imageslim)

##### 接口方法

```java
public interface BlockingQueue<E> extends Queue<E> {
    // 添加元素到队列中返回boolean，队列满抛出异常
    boolean add(E e);
    // 添加元素到队列中，无返回值，抛出中断异常，队列满就阻塞
    void put(E e) throws InterruptedException;
    // 添加元素返回boolea 队列满就返回false，非阻塞
    boolean offer(E e);
    // 添加元素返回boolean，等待指定时间直到队列有空间可插入
    boolean offer(E e, long timeout, TimeUnit unit)
        throws InterruptedException;
	// 从队首获取元素并删除，阻塞，支持等待时中断异常
    E take() throws InterruptedException;
	// 获取队首元素并删除，若无元素等待执行时长，时间到还没有就返回null
    E poll(long timeout, TimeUnit unit) 
        					throws InterruptedException;
    // 返回理想状态下队列不阻塞可加入的元素数量，如果队列没有最大限制就返回
    // Integer.max_value
    int remainingCapacity();
	// 移除指定元素（1个或多个）若它存在(equals比较)
    // 若元素存在（或队列改变）返回true
    boolean remove(Object o);
	// 判断队列是否至少包含一个某元素
    public boolean contains(Object o);
	// 移除队列中全部可用元素，添加到指定集合中，若元素添加失败可能导致元素不在
    // 移除前和添加后的集合中
    int drainTo(Collection<? super E> c);
	// 移除指定数量元素并添加到集合中
    int drainTo(Collection<? super E> c, int maxElements);
}

```

##### 方法对比

| 方法        | 作用                     | 返回值  | 队列已满                      |
| ----------- | ------------------------ | ------- | ----------------------------- |
| add(E e)    | 添加元素到队列           | boolean | 抛出异常，不阻塞              |
| offer(E e)  | 添加元素到队列           | boolean | 返回false，不阻塞             |
| put(E e)    | 添加元素到队列           | void    | 会阻塞直到队列空，支持中断    |
| offer(time) | 指定时长内添加元素到队列 | boolean | 阻塞，超时返回false，支持中断 |

| 方法       | 作用                     | 返回值  | 队列为空                     |
| ---------- | ------------------------ | ------- | ---------------------------- |
| take()     | 获取并删除队首元素       | E       | 阻塞等待直到有元素可以获取   |
| poll()     | 获取并删除队首元素       | E/null  | 不阻塞等待，返回null         |
| remove()   | 移除指定的一个或多个元素 | boolean | 不阻塞等待                   |
| peek()     | 获取队首元素但不删除     | E/null  | 不阻塞等待，返回null         |
| poll(time) | 指定时长内获取并删除元素 | E/null  | 阻塞等待，超时返回null，中断 |

#### ArrayBlockingQueue

##### 构造

`有界阻塞队列`，我们将从类变量、构造函数、添加与获取角度来解析`ArrayBlockingQueue`的实现。

```java
public class ArrayBlockingQueue<E> extends AbstractQueue<E>
        implements BlockingQueue<E>, java.io.Serializable {
    // 底层使用数组实现
    final Object[] items;
    // 元素获取的所以
    int takeIndex;
	// 元素添加的索引
    int putIndex;
	// 队列中元素个数
    int count;
	// 采用独占锁
    final ReentrantLock lock;
	// 等待获取的条件队列(不为空就可以获取)
    private final Condition notEmpty;
	// 等待添加的队列(不满就可以添加)
    private final Condition notFull;
    // 默认实现 需要指定队列大小，默认非公平锁
    public ArrayBlockingQueue(int capacity) {
        this(capacity, false);
    }

    public ArrayBlockingQueue(int capacity, boolean fair) {
        // 容量小于0抛异常
        if (capacity <= 0)
            throw new IllegalArgumentException();
        // 初始化数组（堆中）
        this.items = new Object[capacity];
        // 初始化独占锁和它的两个条件队列
        lock = new ReentrantLock(fair);
        notEmpty = lock.newCondition();
        notFull =  lock.newCondition();
    }
}  
```

> 1. ArrayBlockingQueue创建时需要指定`容量大小（因为是int，最大2^31-1）`。
> 2. 使用`一个独占锁和它的两个Condition队列`实现同步，默认`非公平锁`实现。
> 3. ArrayBlockingQueue是`"有序的(非逻辑有序)"`，遵循`FIFO先进先出`的执行顺序。

##### 添加

- put

```java
public void put(E e) throws InterruptedException {
    // 元素不能为null否则报NPE
    checkNotNull(e);
    // 获取独占锁
    final ReentrantLock lock = this.lock;
    // 可中断的获取锁
    lock.lockInterruptibly();
    try {
        // 判断队列是否满了
        while (count == items.length)
            // 若已满 将当前线程加入 等待添加的条件队列
            // 等待被下次唤醒
            notFull.await();
        // 将元素加入队列
        enqueue(e);
    } finally {
        lock.unlock();
    }
}

private void enqueue(E x) {
    // 获取数组
    final Object[] items = this.items;
    // 将元素添加到指定index
    items[putIndex] = x;
    // putIndex + 1的同时判断队列是否满了
    if (++putIndex == items.length)
        // 如果满了就将put的index置为0，防止指针溢出
        putIndex = 0;
    // 将队列的大小+1
    count++;
    // 唤醒获取条件队列的节点
    notEmpty.signal();
}
```

> 1. put方法`获取独占锁的时候可以响应中断`。
> 2. 获取独占锁后，如果队列已满，会将当前线程加入`notFull等待添加条件队列`。
> 3. 若队列没有满，那么会调用`enqueue`将元素加入数组并修改相关变量。

- offer

```java
public boolean offer(E e) {
    checkNotNull(e);
    final ReentrantLock lock = this.lock;
    lock.lock();
    try {
        // 区别在此，队列满返回false
        if (count == items.length)
            return false;
        else {
            enqueue(e);
            return true;
        }
    } finally {
        lock.unlock();
    }
}
```

> offer()方法在队列满的时候直接返回`false`，而put则是调用`await阻塞等待`。
>
> offer(time)方法区别在于`awaitNanos阻塞一定时间，超时了队列仍满再返回false`。

- add

```java
public boolean add(E e) {
    return super.add(e);
}
// 抽象类AbstractQueue
public boolean add(E e) {
    // 本质还是调用offer方法，只是如果队列满就返回异常
    if (offer(e))
        return true;
    else
        throw new IllegalStateException("Queue full");
}
```

> add()底层是调用的offer()，只是处理队列满的手段不同，`add在队列满时会抛出异常`。

##### 获取

- take

```java
public E take() throws InterruptedException {
    final ReentrantLock lock = this.lock;
    lock.lockInterruptibly();
    try {
        // 与take方法类似
        // 当队列为空的时候
        while (count == 0)
            // 将当前线程加入 等待获取条件队列
            notEmpty.await();
        // 不为空时调用dequeue获取数据
        return dequeue();
    } finally {
        lock.unlock();
    }
}

private E dequeue() {
    final Object[] items = this.items;
    @SuppressWarnings("unchecked")
    // 获取数组中指定index数据并清除该数据
    E x = (E) items[takeIndex];
    items[takeIndex] = null;
    // 如果takeIndex+1超过数组长度
    if (++takeIndex == items.length)
        // 将takeIndex重置
        takeIndex = 0;
    // 将数组数量减1
    count--;
    // 若迭代器不为null
    if (itrs != null)
        // 需要处理迭代器
        // 若队列为空就清空所有迭代器，不为空就清空takeIndex的迭代器
        itrs.elementDequeued();
    // 唤醒等待添加的条件队列
    notFull.signal();
    return x;
}
```

> 1. take()整体流程与put类似，当队列没有元素时，会添加到`notEmpty`条件队列。
> 2. 如果队列有元素就调用`dequeue`获取元素、唤醒`等待添加条件队列`的节点。

- poll

```java
public E poll() {
    final ReentrantLock lock = this.lock;
    lock.lock();
    try {
        return (count == 0) ? null : dequeue();
    } finally {
        lock.unlock();
    }
}
```

> poll()与take()区别在于队列为空时，`前者返回null`，后者阻塞等待。
>
> poll(time)与take()区别在于`awaitNanos`阻塞等待指定时长，`若队列仍为空返回null`。

- peek

```java
public E peek() {
    final ReentrantLock lock = this.lock;
    lock.lock();
    try {
        // 直接返回指定索引的数据，队列为空时返回null
        return itemAt(takeIndex); // null when queue is empty
    } finally {
        lock.unlock();
    }
}

final E itemAt(int i) {
    return (E) items[i];
}
```

> peek()与take()区别在于`返回数据时并不删除数据`，peek()`在队列为空时返回null`。

##### 总结

- `ArrayBlockingQueue`是有界（需要指定初始队列大小）的阻塞队列，最大容量不超过`Integer.MAX_VALUE`。
- `ArrayBlockingQueue`遵循`FIFO先进先出的顺序规则`。
- `ArrayBlockingQueue`中的方法是线程安全的，是通过`独占锁`实现的。
- `ArrayBlockingQueue`因为只有一把锁，所以并不是真正的`同时添加和获取`。

---

#### LinkedBlockingQueue

##### 构造

```java
public class LinkedBlockingQueue<E> extends AbstractQueue<E>
        implements BlockingQueue<E>, java.io.Serializable {
    // 内部维护了Node对象，加入队列的元素被封装成node对象通过链表的形式链接
    static class Node<E> {
        // 节点的data
        E item;
        // 节点的next指针
        Node<E> next;
        Node(E x) { item = x; }
    }
    // 队列容量
    private final int capacity;
	// 因为是两把锁，所以共享count需要同步，使用atmoicInteger
    private final AtomicInteger count = new AtomicInteger();
    // 链表的队首，它的item = null(不变)
    transient Node<E> head;
	// 链表的队尾，它的next = null(不变)
    private transient Node<E> last;
	// take、poll之类获取元素的锁（注意是非公平锁）
    private final ReentrantLock takeLock = new ReentrantLock();
	// takeLock的等待获取的条件队列
    private final Condition notEmpty = takeLock.newCondition();
	// put offer之类的获取元素的锁（注意是非公平锁）
    private final ReentrantLock putLock = new ReentrantLock();
	// putLock的等待添加的条件队列
    private final Condition notFull = putLock.newCondition();
    // 默认构造函数，默认队列大小是2^31-1
	public LinkedBlockingQueue() {
        this(Integer.MAX_VALUE);
    }
    // 指定队列的大小
    public LinkedBlockingQueue(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException();
        this.capacity = capacity;
         // 初始化链表队首和队尾 = 空的node节点
        last = head = new Node<E>(null);
    }  
}
```

> 和ArrayBlockingQueue相比，`LinkedBlockingQueue`有三处不同：
>
> 1. 底层实现：前者是数组，后者是通过`静态内部类构建的节点组成的链表`。
> 2. 锁实现：前者支持公平/非公平锁，后者只`支持非公平锁`。
> 3. 锁数量：前者是`一把锁和它的两个Condition`，后者是`两个(一把锁和他的一个Condition)`，因为有两把锁，所以采用了`AtomicInteger来表示count变量`。

##### 添加

- put

```java
public void put(E e) throws InterruptedException {
    // 判断是否为null
    if (e == null) throw new NullPointerException();
    // 这里为什么创建局部变量c=-1？如果是0，那么每次都会
    int c = -1;
    // 将当前元素构建成node对象
    Node<E> node = new Node<E>(e);
    final ReentrantLock putLock = this.putLock;
    final AtomicInteger count = this.count;
    // 可中断的获取put锁
    putLock.lockInterruptibly();
    try {
        // 判断队列是否已满
        while (count.get() == capacity) {
            // 如果已满，添加到等待添加条件队列
            notFull.await();
        }
        // 将node入队
        enqueue(node);
        // 将队列容量count+1的同时将count赋值给c
        // 第一次put时: c = 0,count = 1
        c = count.getAndIncrement();
        // 判断是否超过最大容量
        if (c + 1 < capacity)
            // 没有超过就唤醒添加元素队列继续添加
            notFull.signal();
    } finally {
        // 释放put锁
        putLock.unlock();
    }
    // 如果c = 0那么唤醒等待获取的条件队列中的节点
    // 当队列中只有一个node节点时c=0成立
    if (c == 0)
        signalNotEmpty();
}

private void enqueue(Node<E> node) {
    // 将node加入队列，并成为新队尾，老队尾的next指针指向node
    last = last.next = node;
}
// 只能由put/offer调用
private void signalNotEmpty() {
    // 获取take锁
    final ReentrantLock takeLock = this.takeLock;
    takeLock.lock();
    try {
        // 唤醒等待获取的条件队列的节点
        notEmpty.signal();
    } finally {
        takeLock.unlock();
    }
}
```

> `LinkedBlockingQueue`put的流程与`ArrayBlockingQueue`有些不同：
>
> 1. 当前线程获取`put锁`后，如果队列已满，那么会加入`等待添加的条件队列`，如果队列未满，那么会封装node加入队尾。
> 2. 加入成功后会将`count + 1`，如果`count < capacity`，那么就唤醒`等待添加的条件队列中的节点`，最后释放put锁。
> 3. 因为是两把锁，理论上`添加和获取的操作是可以同时进行的`，所以代码最后还需要判断下`count == 0`，如果成立说明此时恰好有一个数据，唤醒`等待获取队列中线程`来获取。

- offer

```java
public boolean offer(E e) {
    // 判断是否为null
    if (e == null) throw new NullPointerException();
    final AtomicInteger count = this.count;
    // 如果队列已满，那么直接返回false
    if (count.get() == capacity)
        return false;
    int c = -1;
    // 构建新node
    Node<E> node = new Node<E>(e);
    final ReentrantLock putLock = this.putLock;
    // 获取put锁
    putLock.lock();
    try {
        // 如果count小于队列容量
        if (count.get() < capacity) {
            // 加入队列
            enqueue(node);
            // c = count, count = count+1
            c = count.getAndIncrement();
            // 继续判断是否超过队列容量
            if (c + 1 < capacity)
                // 没超过就唤醒等待添加元素条件队列中的线程
                notFull.signal();
        }
    } finally {
        putLock.unlock();
    }
    // c = 0说明此时队列中恰好一个节点
    if (c == 0)
        // 唤醒 等待获取元素条件队列中的线程
        signalNotEmpty();
    // 如果c<0说明队列已满无法添加了
    return c >= 0;
}
```

> 与put()不同点在于返回值，offer()返回boolean值，当`队列满的时候返回false`。
>
> offer(time)在队列满的时候`等待指定时长`，如果`唤醒后队列还没有空间就返回false`。

##### 获取

- take

```java
public E take() throws InterruptedException {
    E x;
    int c = -1;
    final AtomicInteger count = this.count;
    // 获取take锁
    final ReentrantLock takeLock = this.takeLock;
    // 可中断获取take锁
    takeLock.lockInterruptibly();
    try {
        // 判断队列是否为空
        while (count.get() == 0) {
            // 为空就加入 等待获取元素条件队列
            notEmpty.await();
        }
        // 队列不为空获取链表中的头节点中元素
        x = dequeue();
        // c = count, count = count - 1
        c = count.getAndDecrement();
        // 如果c > 1说明此时数据堆积
        if (c > 1)
            // 唤醒 等待获取元素条件队列中的线程
            notEmpty.signal();
    } finally {
        takeLock.unlock();
    }
    // 如果 c = capacity时，count 肯定是小于capacity的
    if (c == capacity)
        // 唤醒 等待添加元素条件队列
        signalNotFull();
    return x;
}

// 获取链表中node节点中元素并返回
private E dequeue() {
    // 获取头节点
    Node<E> h = head;
    // 获取头节点的next节点
    Node<E> first = h.next;
    // 将头节点的next设为自己方便gc
    h.next = h;
    // 设置first为head节点
    head = first;
    // 返回first中的元素并将其置为null
    E x = first.item;
    first.item = null;
    return x;
}
```

> 1. 如果队列为空，那么就将当前线程加入`等待获取元素条件队列`。
> 2. `dequeue()`会将head的next节点设为新的head，`返回并清空新head的item属性`。
> 3. `c == capacity`时为何要唤醒`等待添加元素条件队列中的线程`？因为此时的`c = count + 1`，所以还缺一个节点队列才满，所以唤醒添加节点的条件队列。
> 4. take()当`队列为空的时候会阻塞`，直到不为空获取元素。

- poll

```java
public E poll() {
    final AtomicInteger count = this.count;
    if (count.get() == 0)
        return null;
    // 赋值null 与take不同
    E x = null;
    int c = -1;
    final ReentrantLock takeLock = this.takeLock;
    takeLock.lock();
    try {
        // 与take的区别之处
        if (count.get() > 0) {
            x = dequeue();
            c = count.getAndDecrement();
            // 如果 c > 1说明队列存在很多数据需要take
            if (c > 1)
                // 继续唤醒线程获取
                notEmpty.signal();
        }
    } finally {
        takeLock.unlock();
    }
    if (c == capacity)
        signalNotFull();
    return x;
}
```

> 和take()明显的区别在于：`poll()在队列满的时候返回null，并且不会阻塞`。
>
> poll(time)：`阻塞指定时长，唤醒后如果队列仍为空，那么返回null`。

- peek

```java
public E peek() {
    // 如果队列为空返回null
    if (count.get() == 0)
        return null;
    final ReentrantLock takeLock = this.takeLock;
    takeLock.lock();
    try {
        // 返回head的next节点，并不会删除next节点
        Node<E> first = head.next;
        // 如果队列刚初始化那么head = last = new Node(null)
        // head.next = null
        // 此时也返回null
        if (first == null)
            return null;
        else
            // 不为null就返回item
            return first.item;
    } finally {
        takeLock.unlock();
    }
}
```

> peek()相比take()：除了队列为空时返回null外，还不会阻塞等待。

##### 总结

- `LinkedBlockingQueue`是无界（可以不传递初始队列大小）队列，不指定容量时默认`Integer.MAX_VALUE`。
- `LinkedBlockingQueue`的底层是由`链表组成的`，它`head.item = null, last.next  = null`是永远成立的。并且它也符合`FIFO`规则。
- `LinkedBlockingQueue`拥有两把锁，分别对应着put和take，所以count变量需要同步。
- `LinkedBlockingQueue`可以实现`逻辑上真正的同时take和put`，所以性能更强。

---

#### Other Queue

##### PriorityBlockingQueue

- `PriorityBlockingQueue`是不符合`FIFO`规则的队列，它是按照`元素的优先级从小到大出队列的`，是由元素实现`Comparator`接口来实现的。

- 队列`默认容量11，最大容量Integer.MAX_VALUE - 8`，底层通过`独占锁和Condition条件队列`实现，但只有`notEmpty`条件队列。

- 当队列大小不够时会扩容（不超过MAX_SIZE），扩容规则如下

  ```java
  int newCap=oldCap+((oldCap < 64)?(oldCap + 2):(oldCap >> 1));
  ```

- 底层是数组，但是用`数组实现了二叉堆`，排在`堆顶`的就是要出队的元素。

##### DelayQueue

- `延迟队列`，一个按照`延迟时间从小到大出队的PriorityBlockingQueue`。
- DelayQueue中的元素必须要实现`Delayed`接口，复写`getDelay和compareTo`方法。
- `未来时间 - 当前时间 `，值越小就越先出队，但前提是`时间差 <= 0`。

##### SynchronousQueue

- `SynchronousQueue`队列本身并没有容量的概念，`先调用put的线程会阻塞，直到另一个线程调用了take`。如果调用多次put，那么也需要掉哦那个同样次数的take，才能全部解锁。
- `SynchronousQueue`支持公平和非公平实现，假设调用三次put，公平锁的情况下，`第一个take的线程对应着第一个put的线程`，非公平锁情况下，`第一个take的线程对应着第三个put的线程`。

---

### Deque

`双端队列，支持在队列的头尾出增加或获取数据`，`Deque`接口中定义了相关的方法

```java
public interface Deque<E> extends Queue<E> {
	// 添加到队首
	void addFirst(E e);
    // 添加到队尾
    void addLast(E e);
    // 获取队首
    boolean offerFirst(E e);
    // 获取队尾
    boolean offerLast(E e);
	...
}	
```

> 相比`BlockingQueue的父接口Queue`，`Deque`中定义了头尾操作数据的方法。

#### BlockingDeque

```java
public interface BlockingDeque<E> extends BlockingQueue<E>, Deque<E> {
    void putFirst(E e) throws InterruptedException;
    void putLast(E e) throws InterruptedException;
    E takeFirst() throws InterruptedException;
    E takeLast() throws InterruptedException;
   ...
}
```

> `BlockingQueue`继承了`BlockingQueue`和`Deque`接口。添加了一些抛出中断的方法。

#### LinkedBlockingDeque

我们以`LinkedBlockingDeque`为切入点了解`双端队列`的实现。

##### 构造

```java
public class LinkedBlockingDeque<E>
    extends AbstractQueue<E>
    implements BlockingDeque<E>, java.io.Serializable {
    // 内部维护的静态内部类是双向节点
	static final class Node<E> {
        E item;
        Node<E> prev;// 区别
        Node<E> next;
        Node(E x) {
            item = x;
        }
    }
    // (first == null && last == null) || 
    // (first.prev == null && first.item != null) 规则不变
	transient Node<E> first;
    // (first == null && last == null) ||
    // (last.next == null && last.item != null) 规则不变
    transient Node<E> last;
    private transient int count;
    private final int capacity;
    // 内部仍是一把锁 两个条件队列
    final ReentrantLock lock = new ReentrantLock();
    // 等待获取 条件队列
    private final Condition notEmpty = lock.newCondition();
    // 等待添加  条件队列
    private final Condition notFull = lock.newCondition();
    public LinkedBlockingDeque() {
        this(Integer.MAX_VALUE);
    }
    public LinkedBlockingDeque(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException();
        this.capacity = capacity;
    }
}
```

> 与`LinkedBlockingQueue`构造区别：
>
> 1. `内部维护是双链表节点，拥有prev和next指针`。
> 2. 队列初始化后，first和last节点的item != null，

##### 添加

- putFirst

```java
// 添加元素到队首，队列满就阻塞等待
public void putFirst(E e) throws InterruptedException {
    // 判断是否为null
    if (e == null) throw new NullPointerException();
    // 封装节点
    Node<E> node = new Node<E>(e);
    final ReentrantLock lock = this.lock;
    // 这里是lock不是lockInterruptibly
    lock.lock();
    try {
        // 如果队列满返回false
        while (!linkFirst(node))
            // 将线程加入 等待添加条件队列
            notFull.await();
    } finally {
        lock.unlock();
    }
}
// 将元素添加到队首，队列满返回false
private boolean linkFirst(Node<E> node) {
    // 队列满返回false
    if (count >= capacity)
        return false;
    // 获取first节点
    Node<E> f = first;
    // 将当前节点的next指向first
    node.next = f;
    // 将node设为first
    first = node;
    // 如果last=null，说明当前是第一个加入队列的节点
    if (last == null)
        // first = last = Node(prev=null, next=null, item != null)
        last = node;
    else
        // last != null 维护 node和first的prev和next，不处理last
        f.prev = node;
    // 队列大小+1
    ++count;
    // 唤醒等待获取条件队列中的线程
    notEmpty.signal();
    // 返回true
    return true;
}
```

> 1. 将元素添加到队首，如果队列满，`阻塞`等待直到将元素添加到队列中。
> 2. 当添加节点是队列中的第一个节点时，`first = last = Node(prev = next = null, item != null)`，之后`linkFirst`就只会维护node和fist之间的关系，不会维护last节点。

- putLast

```java
// 将元素添加到队尾
public void putLast(E e) throws InterruptedException {
    // 判断NPE
    if (e == null) throw new NullPointerException();
    // 创建节点
    Node<E> node = new Node<E>(e);
    final ReentrantLock lock = this.lock;
    lock.lock();
    try {
        // 将元素加入队尾，如果队列满返回false
        while (!linkLast(node))
            // 将线程加入 等待添加条件队列
            notFull.await();
    } finally {
        lock.unlock();
    }
}
// 将元素添加到队尾，队列满返回false
private boolean linkLast(Node<E> node) {
    // 如果队列满返回false
    if (count >= capacity)
        return false;
    // 获取last节点
    Node<E> l = last;
    // 将node的前驱设为last
    node.prev = l;
    // 将node设为last
    last = node;
    // 同理，说明当前节点第一个加入队列的节点
    if (first == null)
        // first = last = node(prev=null, last=null, item!=null)
        first = node;
    else
        // 不是第一个加入队列的节点，维护last和node之间的关系
        l.next = node;
    // 将队列数量加1
    ++count;
    // 唤醒 等待获取条件队列线程
    notEmpty.signal();
    return true;
}
```

> 1. 将元素添加到队尾，如果队列满，`阻塞`等待直到将元素添加到队列中。
> 2. 和`putFirst`同理，第一个加入队列的节点需要特殊处理下，`linkLast`只会维护node和last之间的关系，不会维护first节点。

##### 获取

- takeFirst

```java
// 获取队首元素
public E takeFirst() throws InterruptedException {
    final ReentrantLock lock = this.lock;
    // 获取独占锁
    lock.lock();
    try {
        E x;
        // 队列为空返回false，就阻塞等待拿到头元素
        while ( (x = unlinkFirst()) == null)
            // 将线程加入 等待获取条件队列
            notEmpty.await();
        // 否则返回元素
        return x;
    } finally {
        lock.unlock();
    }
}
// 队列为空返回null，否则返回头节点并处理链表
private E unlinkFirst() {
    /// 若first = null，说明队列没有初始化，直接返回null
    Node<E> f = first;
    if (f == null)
        return null;
    // 获取first的next节点
    Node<E> n = f.next;
    // 获取next节点的item
    E item = f.item;
    // 将first.item = null
    f.item = null;
    // 去掉f的引用，便于GC
    f.next = f;
    // 将next节点设为first节点
    first = n;
    // 成立条件：1. 队列刚初始化还没有节点进入 2. 节点被清空了。
    // 两个条件都需要处理下last节点
    if (n == null)
        // first = last = null
        last = null;
    else
        // 如果队列中还有其他元素，不处理last，将first.prev=null
        n.prev = null;
    // 将队列大小-1
    --count;
    // 唤醒等待添加队列中线程
    notFull.signal();
    return item;
}
```

> 1. takeFirst会获取队首元素，当队列为空时，`阻塞`等待直到队列不为空获取到元素。
> 2. 获取队首元素的同时，会删除原来的first的节点。如果`删除后队列没有其他节点或队列刚初始化`，都需要处理last节点。否则只需要维护first节点，不用处理last节点。

- takeLast

```java
public E takeLast() throws InterruptedException {
    final ReentrantLock lock = this.lock;
    lock.lock();
    try {
        E x;
        // 队列为空返回null，会加入等待获取条件队列
        while ( (x = unlinkLast()) == null)
            notEmpty.await();
        return x;
    } finally {
        lock.unlock();
    }
}
// 队列为空返回null，否则返回last节点
private E unlinkLast() {
    // 如果last = null那么说明队列为空（或被清空了），返回null
    Node<E> l = last;
    if (l == null)
        return null;
    // 获取last的前驱节点
    Node<E> p = l.prev;
    // 获取前驱节点的item，并设为null
    E item = l.item;
    l.item = null;
    // 去除last节点引用，便于回收
    l.prev = l;
    // 前驱节点设为last
    last = p;
    // 和takeFirst一致，处理队列被清空或刚初始化时first节点
    if (p == null)
        first = null;
    else
        // 不需要处理first节点，维护node和last关系
        p.next = null;
    // 队列大小-1
    --count;
    // 唤醒 等待添加条件队列线程
    notFull.signal();
    return item;
}
```

> 1. takeLast会获取队尾元素，当队列为空时，`阻塞`等待直到队列不为空获取到元素。
> 2. 获取队尾元素的同时，会删除原来的last的节点。如果`删除后队列没有其他节点或队列刚初始化`，都需要处理first节点。否则只需要维护last节点，不用处理first节点。

##### 总结

- 除了内部维护的是`双向链表队列、一把独占锁和两个条件队列`外，其实现原理和`LinkedBlockingQueue`相同。
- `LinkedBlockingDeque`无论操作队首还是队尾，都要考虑`队列内无节点`的情况。
- `LinkedBlockingDeque`中非空队列是不存在哨兵节点的，是`直接返回头或尾`，而`LinkedBlockingQueue`返回的是`头节点的next节点`。
- 因为`一把锁的设计，存在头尾操作都需要竞争锁`的问题，所以`LinkedBlockingDeque`效率要低于`LinkedBlockingQueue`。

---

### Collection

#### CopyOnWriteArrayList

> `CopyOnWrite`思想是计算机程序设计领域的一种优化策略。若有多个调用者同时要求相同的资源，他们会获得`共同的指针`指向相同的资源，直到某个调用者试图修改资源的时候，才会`复制一份副本`给该调用者，但`其他调用者见到的最初资源不改变`，此过程`对其他调用者透明`。
>
> `CopyOnWriteArrayList`是ArrayList的线程安全变体，通过`生成新的副本`来实现。

##### 构造

```java
public class CopyOnWriteArrayList<E>
    implements List<E>, RandomAccess, Cloneable, java.io.Serializable {
    
  	// 内部独占锁
    final transient ReentrantLock lock = new ReentrantLock();
	// volatile 修饰的数组，只能getArray和setArray操作
    private transient volatile Object[] array;
	// 返回当前数组
    final Object[] getArray() {
        return array;
    }
	// 设置数组
    final void setArray(Object[] a) {
        array = a;
    }
	// 构造函数 创建一个空数组
    public CopyOnWriteArrayList() {
        setArray(new Object[0]);
    }
}
```

> 底层是通过数组实现，数组使用`volatile`修饰保证了多线程之间的可见性。

##### add

```java
public boolean add(E e) {
    // 获取独占锁
    final ReentrantLock lock = this.lock;
    lock.lock();
    try {
        // 获取当前的数组，此时不会存在其他线程修改了数组
        // 只是add期间若有其他线程查询，那么查到的是旧的数据
        Object[] elements = getArray();
        // 获取数组长度
        int len = elements.length;
        // copy数组并将数组扩大1
        Object[] newElements = Arrays.copyOf(elements, len + 1);
        // 将元素插入数组的最后
        newElements[len] = e;
        // 设置数组到成员变量array中
        setArray(newElements);
        return true;
    } finally {
        lock.unlock();
    }
}
```

> add操作需要获取独占锁，在执行add操作期间，若有其他线程执行查询操作，那么它获得将会是旧的数据。在add操作之后查询，获得会是最新的数据。
>
> 底层调用的时`System.arraycopy`实现数组的拷贝，需要注意：
>
> 1. 此方法属于`浅拷贝（复制的是对象的引用）`，如果是数组类型是对象，那么`拷贝后的数组持有的是原数组的引用`。所以`拷贝后的数组修改会影响原数组`。
> 2. 如果数组类型是基本数据类型（值位于常量池），那么`拷贝只是修改数组中元素的指向`，并不是在常量池中又复制了一份。
>
> ![](https://image.leejay.top/image/20200709/BlpbAEFkBDBa.png?imageslim)

##### remove

```java
// 移除指定index元素
public E remove(int index) {
    final ReentrantLock lock = this.lock;
    // 获取独占锁
    lock.lock();
    try {
        // 获取数组
        Object[] elements = getArray();
        // 获取数组length
        int len = elements.length;
        // 获取old数组[index]数据
        E oldValue = get(elements, index);
        // index + 1 = length 
        int numMoved = len - index - 1;
		// numMoved = 0说明移除的是数组的最后一个元素
        if (numMoved == 0)
            // 直接将长度减1直接copy即可。
            setArray(Arrays.copyOf(elements, len - 1));
        else {
            // 否则说明移除的是中间的元素
            // 创建小1的数组
            Object[] newElements = new Object[len - 1];
            System.arraycopy(elements, 0, newElements, 0, index);
            System.arraycopy(elements, index + 1, newElements, index, numMoved);
            // 设置拷贝后的新数组
            setArray(newElements);
        }
        // 返回被删除的旧值
        return oldValue;
    } finally {
        lock.unlock();
    }
}
```

> remove方法的难度在于`如何移除oldValue并将原有的数据平移到新的数组中`。
>
> 我们假设`Object[] objs = [2, 3, 5, 7, 9];length = 5,index<=length-1`：
>
> 1. 如果我们移除的是`index = 4`的元素（即最后一个元素），那么直接创建`length = 4`的数组，将数据直接拷贝过去就行，变成`[2,3,5,7]`。
> 2. 如果我们移除是第三个元素(index = 2)，那么按照源码中的方法：第一次拷贝后，`newElements = [2,3]`，此时`numMoved = 2`，那么执行第二个拷贝之后，`newElements = [2,3,7,9]`。 

##### set

```java
// 将数组的指定index改成指定值
public E set(int index, E element) {
    final ReentrantLock lock = this.lock;
    // 获取独占锁
    lock.lock();
    try {
        // 获取数组
        Object[] elements = getArray();
        // 获取指定index的值
        E oldValue = get(elements, index);
		// 判断新旧值是否相同，相同就不需要更改
        if (oldValue != element) {
            // 计算数组长度
            int len = elements.length;
            // 创建新数组
            Object[] newElements = Arrays.copyOf(elements, len);
            // 将数组的指定indexiu该
            newElements[index] = element;
            // 设置新数组
            setArray(newElements);
        } else {
            // 并非完全禁止操作；确保可变的写语义
            setArray(elements);
        }
        return oldValue;
    } finally {
        lock.unlock();
    }
}
```

> 基于`CopyOnWrite`原理，set方法也需要重新copy一份数组。

##### get

```java
// 获取某个index元素
public E get(int index) {
    // get的数据有可能不是最新的，因为读写不互斥
    // 此时一个线程已经复制了数据，还没有setArray，get到的就不是最新的
    return get(getArray(), index);
}
// 就是返回数组的index的数据
private E get(Object[] a, int index) {
    return (E) a[index];
}
```

> get方法能够保证每次获取到的数据都是`当时最新`的数据（基于volatile）。

##### 总结

- `CopyOnWriteArrayList`适用于`读多写少`的并发场景，它允许`null且可以重复`。
- `CopyOnWriteArrayList`添加元素时建议使用`批量添加`，因为每次添加都要复制。
- `CopyOnWriteArrayList`的是通过`写时数组copy`来实现，在写操作的时候，内存中会同时具有两个对象的内存，如果这个数组对象过大，会导致`内存占用`问题。

- `CopyOnWriteArrayList`只能保证`数据的最终一致性`，不保证数据实时一致性（读写不互斥，有线程修改数据已经复制了副本，还未执行setArray时，你读到的就是旧数据）。

> 如果需要使用不重复的`CopyOnWrite`框架，推荐`CopyOnWriteArraySet`。它能够实现不重复，核心原理就是添加的时候通过`addIfAbsent`判断元素是否已存在。

#### ConcurrentLinkedQueue

##### 特性

- 基于链表的无界线程安全队列。
- 队列顺序是`FIFO`先进先出的顺序。队首是插入最久的元素，队尾是最新的元素。
- 使用场景：`许多线程将共享对一个公共集合的访问，不支持null`。
- 内部的并发操作通过`自旋 + CAS`实现。与`LinkedBlockingQueue`独占锁不同。

##### 构造

```java
public class ConcurrentLinkedQueue<E> extends AbstractQueue<E>
        implements Queue<E>, java.io.Serializable {
    // head头节点
    private transient volatile Node<E> head;
	// tail尾节点
    private transient volatile Node<E> tail;
	// 不用传递初始容量
    public ConcurrentLinkedQueue() {
        // 初始化head和tail，哨兵节点
        head = tail = new Node<E>(null);
    }
    // 私有静态内部类，用于构成链表的节点（单向链表）
    // 核心是通过CAS来实现并发操作
    private static class Node<E> {
        volatile E item;
        // 标记next节点 volatile修饰的
        volatile Node<E> next;
		// 构造
        Node(E item) {
            // CAS添加item
            UNSAFE.putObject(this, itemOffset, item);
        }
		// CAS修改item（把cmp设置成val）
        boolean casItem(E cmp, E val) {
            return UNSAFE.
                compareAndSwapObject(this, itemOffset, cmp, val);
        }
		// CAS设置next指针
        void lazySetNext(Node<E> val) {
            UNSAFE.putOrderedObject(this, nextOffset, val);
        }
		// CAS修改next节点
        boolean casNext(Node<E> cmp, Node<E> val) {
            return UNSAFE
                .compareAndSwapObject(this, nextOffset, cmp, val);
        }
        private static final sun.misc.Unsafe UNSAFE;
        // Node节点中item偏移量
        private static final long itemOffset;
        // Node节点中next的偏移量
        private static final long nextOffset;

        static {
            try {
                UNSAFE = sun.misc.Unsafe.getUnsafe();
                Class<?> k = Node.class;
                itemOffset = UNSAFE.objectFieldOffset
                    (k.getDeclaredField("item"));
                nextOffset = UNSAFE.objectFieldOffset
                    (k.getDeclaredField("next"));
            } catch (Exception e) {
                throw new Error(e);
            }
        }
    }
}
```

> 1. Node是私有静态内部类，其中定义了`item和next的CAS方法`。
> 2. 因为不是阻塞队列，所以`不存在容量字段`，也`不需要指定大小`。

##### 提示

> 如果使用的是idea，会出现`head莫名奇妙被修改，节点引用指向自己的问题`。
>
> 解决方案：https://blog.csdn.net/AUBREY_CR7/article/details/106331490

##### offer

```java
// 添加节点到队列中
public boolean offer(E e) {
    // 老一套，判断是否为空，为空抛出NPE
    checkNotNull(e);
    // 初始化节点
    final Node<E> newNode = new Node<E>(e);
	// 自旋从队尾开始,这里只有初始化条件，没有循环结束条件
    for (Node<E> t = tail, p = t;;) {
        // p被认为是真正的尾节点,获取p.next节点
        // 因为此时有可能有其他线程成为tail
        Node<E> q = p.next;
        // q = null 说明此刻p就是tail尾节点
        if (q == null) {
            // CAS将newNode设为p的next节点，失败就继续自旋
            if (p.casNext(null, newNode)) {
                // p = t = tail = Node(next = newNode)
                if (p != t)
                    // CAS设置tail尾节点，即使失败了，
                    casTail(t, newNode);
                return true;
            }
        }
        else if (p == q)
            // 如果tail此时被其他线程改变了，那么p = t成立
            // 没改变 t = head
            p = (t != (t = tail)) ? t : head;
        else
            // 此行代码用于找到真正的尾节点，赋予p，
            // 因为tail更新不及时，每添加两个才会更新tail
            p = (p != t && t != (t = tail)) ? t : q;
    }
}
// 测试代码
class Test {
    private static ConcurrentLinkedQueue<Integer> QUEUE = 
        						new ConcurrentLinkedQueue<>();
    public static void main(String[] args) {
        QUEUE.offer(11);
        QUEUE.offer(22);
        QUEUE.offer(33);
    }
}
```

>推论：`每插入两个节点，tail指针才会移动，指向第二个插入的节点`。
>
>1. `t表示刚进入代码时的尾节点，p用来表示队列真正的尾节点`，当`p.next = null`成立时说明p此时指向真正的尾节点，如果不成立说明p此时不是真正的尾节点，需要查找真正的尾节点并将它赋予p，保证每次新增的节点都在队尾。
>2. `p = (p != t && t != (t = tail)) ? t : q;`，针对这个代码，我们假设一个场景，队列中已经有第一个节点了（此时tail指针还没修改），此时线程A和线程B同时进入该段自旋代码准备执行：
>   1. 线程A判断`p.next != null`，执行else中代码，此时`p != t`不成立，所以`p = q`后继续循环执行，线程A继续判断`p.next = q = null`成立，所以执行`p.casNext`，此时线程A的值加入了队列，此时`p != t`成立，准备执行casTail。
>   2. 此时`切换为线程B`，线程B判断`p.next != null`，执行else中`p != t`不成立，所以`p = q`后继续循环，因为线程A的值加入了队列，所以`q = p.next != null`，执行else中代码，此时`p != t`成立，准备执行`t != (t = tail)`。
>   3. 切换回线程A，`线程A执行casTai，tail指针被修改`，线程A返回true退出循环，切换到线程B，判断`t != (t = tail)`成立，此时`p = t = 更改后的tail`，继续循环执行`p.next = q = null`成立，执行casNext，将线程A的值也加入队列中。
>3. `p == q`需要结合`poll`方法去解析(一些线程offer，一些poll)，当它成立的时候说明`p.next = p = q`，说明这种节点是哨兵节点，表示为需要删除或者过滤的节点。

##### offer执行流程

![](https://image.leejay.top/image/20200710/XWBJx3hhhzX3.jpg?imageslim)

##### poll

```java
// 删除链表的头节点并返回该节点的item
public E poll() {
    restartFromHead:
    // 自旋
    for (;;) {
        // head = h = p
        for (Node<E> h = head, p = h, q;;) {
            E item = p.item;
			// 如果item不为null，那么CAS修改为null
            if (item != null && p.casItem(item, null)) {
                // CAS成功后会执行到这里
                // head也是每两个节点更新一次
                if (p != h) 
                    // p != h 说明此时需要更新head标识
                    updateHead(h, ((q = p.next) != null) ? q : p);
                // 直接返回item
                return item;
            }
            // 如果p.item = null 且 p.next= null
            else if ((q = p.next) == null) {
                // 更新head节点
                updateHead(h, p);
                return null;
            }
            else if (p == q)
                continue restartFromHead;
            else
                p = q;
        }
    }
}

final void updateHead(Node<E> h, Node<E> p) {
    // 如果要更新的节点和当前节点不同，那么尝试更新head头节点，注意h节点不会变
    if (h != p && casHead(h, p))
        // 将原head的节点next指针指向自己，便于GC
        h.lazySetNext(h);
}
```

> 推论：`每移除两个节点，head指针会移动一次`。
>
> 1. 和offer方法一样，`h为刚进入代码的头节点，p节点用来表示为真正要删除的头节点`。
>
> 2. 只有当当前head节点的`item!=null`时才会尝试去CAS修改，若`item = null`的节点会通过`q = p.next`去查找。找到后执行`updateHead`，移除h节点并设置新的head节点。
>
> 3. `p == q`何时成立：线程A和线程B同时获取队列中的元素，假设线程B移除了节点并将其设为`哨兵节点（h.next = h）`，此时线程A判断`item != null`不成立，继续判断`p == q`成立。
>
> 4. 再结合offer方法中何时`p == q`：
>
>    1. `此时队列中head=tail(item=null, next=node1)，node1=(item!=null,next=null)`，此时线程A尝试offer数据，线程B尝试poll数据，线程A先进入循环，切换为线程B，此时`h = head = p`，继续执行`p.item = null`，判断`q = p.next != null`且`p != q`，所以执行else：`p = q`，继续循环，`p.item != null`，尝试CAS修改，且`p != h`，所以执行`updateHead将h改为哨兵节点`。
>    2. 此时线程切换回A，线程执行`q = p.next（此时p已经是哨兵节点了）`判断`q != null`，继续判断`p = q`成立，执行`p = (t != (t = tail)) ? t : head;`，此时的`p = head`，继续从头节点开始循环插入尾节点。至此两个线程都执行完毕。
>
>    ![](https://image.leejay.top/image/20200710/qsxlf6aucyH7.png?imageslim)

##### poll执行流程

![](https://image.leejay.top/image/20200710/8Pn7oKJw2R07.jpg?imageslim)

##### 总结

- `ConcurrentlinkedQueue`是`非阻塞队列`，底层使用`自旋和CAS`来实现，`FIFO`且不允许`null`值。
- `ConcurrentlinkedQueue`元素入队和出队操作都是线程安全的，但`遍历不是的线程安全的`，并且在判断元素是否为空的时候建议使用`isEmpty`而不是`sze == 0（遍历队列）`
- `ConcurrentlinkedQueue`中的`head和tail`节点都是延迟更新，采用的是`HOPS`策略，如果每次节点入队都更新头尾节点，确实代码更好理解，当时执行大量CAS操作对性能也是损耗，`采用大量读的操作来替代每次节点入队都写的操作，以此提升性能。`
- 相比`LinkedBlockingQueue`阻塞队列，`ConcurrentlinkedQueue`非阻塞队列的并发性能更好些。当时具体使用场景选择不同的

---

#### ConcurrentHashMap

`JDK1.8`之后采用的是`数组 + 链表 + 红黑树`的结构，通过`Synchronized + CAS`实现线程安全，而`JDK1.7`采用的是`将一个HashMap分成多个Segment`的方式，通过`继承ReentrentLock的Segment分段锁`实现线程安全。

##### Node

```java
// Node数组，组成ConcurrentHashMap的主要结构
transient volatile Node<K,V>[] table;
// 扩容期间不为null，因为存在协助扩容的机制，所以需要设置volatile保证线程间可见性
private transient volatile Node<K,V>[] nextTable;
static class Node<K,V> implements Map.Entry<K,V> {
    final int hash;
    final K key;
    volatile V val;
    volatile Node<K,V> next;

    Node(int hash, K key, V val, Node<K,V> next) {
        this.hash = hash;
        this.key = key;
        this.val = val;
        this.next = next;
    }
}
// 如果一个index下所有的节点全部转移完后会放置ForwardingNode节点，防止put插入错误位置
// 如果正在扩容但是put插入的位置不是ForwardingNode还是可以继续put的，支持两者并发
// 如果是get的方法，那么就需要获取nextTable属性(新的chm的引用)，用于返回新的值
static final class ForwardingNode<K,V> extends Node<K,V> {
    final Node<K,V>[] nextTable;
    ForwardingNode(Node<K,V>[] tab) {
        super(MOVED, null, null, null);
        this.nextTable = tab;
    }
}
// 红黑树的根节点使用的TreeNode，不存储key-value
static final class TreeBin<K,V> extends Node<K,V> {
    TreeNode<K,V> root;
    volatile TreeNode<K,V> first;
    volatile Thread waiter;
    volatile int lockState;
    // values for lockState
    static final int WRITER = 1; // set while holding write lock
    static final int WAITER = 2; // set when waiting for write lock
    static final int READER = 4; // increment value for setting read lock
}
// 构建成红黑树树的节点结构
static final class TreeNode<K,V> extends Node<K,V> {
    TreeNode<K,V> parent;  // red-black tree links
    TreeNode<K,V> left;
    TreeNode<K,V> right;
    TreeNode<K,V> prev;    // needed to unlink next upon deletion
    boolean red;

    TreeNode(int hash, K key, V val, Node<K,V> next,
             TreeNode<K,V> parent) {
        super(hash, key, val, next);
        this.parent = parent;
    }
}
```

> 1. `Node`的定义与HashMap类似，只是用`volatile修饰value和next`，用于`保证线程间的可见性`。
>
> 2. `ForwardingNode`节点用于表示扩容期间，指定数组位置下的`所有节点`全部转移后，会`使用该节点占据指定位置`，防止put插入错误的位置。
> 3. `TreeBin`用于表示红黑树结构根节点的TreeNode，`不存储key-value数据`。
> 4. `TreeNode`表示组成红黑树节点的结构，`存储key-value数据`。
> 5. 成员变量`nextTable`在`扩容期间不为null`，表示扩容中下个需要使用的table。因为线程协助扩容的机制的存在，所以用`volatile`修饰，保证线程间的可见性。

##### 构造

```java
// 负数时表示为正在初始化或扩容：-1表示初始化或-(1+活动resize线程数量)
// 当table为null时，持有初始table size直到table创建(默认为0)
// 初始化后持有下个大小直到扩容table
private transient volatile int sizeCtl;
public ConcurrentHashMap(int initialCapacity,
                             float loadFactor, int concurrencyLevel) {
    if (!(loadFactor > 0.0f) || initialCapacity < 0 || concurrencyLevel <= 0)
        throw new IllegalArgumentException();
    if (initialCapacity < concurrencyLevel)
        initialCapacity = concurrencyLevel;
    long size = (long)(1.0 + (long)initialCapacity / loadFactor);
    // tableSizeFor方法用于保证容量必须是2次幂
    int cap = (size >= (long)MAXIMUM_CAPACITY) ?
        MAXIMUM_CAPACITY : tableSizeFor((int)size);
    // sizeCtl的初始值就是cap
    this.sizeCtl = cap;
}
```

> `构造方法只是定义了属性，并没有真正的开辟空间创建对象`。
>
> initialCapacity：初始容量，默认是`16`。
>
> loadFactor： 扩容因子，默认是`0.75f`。
>
> concurrencyLevel：并发级别，并发更新线程的数量。
>
> sizeCtl：用于控制在初始化或者并发扩容时的线程数，默认为0，否则为初始容量大小cap。在`initTable`初始化后`sizeCtl = 0.75 * 数组大小`。当`sizeCtl < 0`时存在：`-1表示正在初始化，-(1 + 活动resize线程）表示正在resize`两种情况的值。

##### put

```java
public V put(K key, V value) {
    return putVal(key, value, false);
}

static final int HASH_BITS = 0x7fffffff;
// 计算hash值，通过高低16交互避免hash冲突，并通过&运算保证最高位是0
static final int spread(int h) {
    return (h ^ (h >>> 16)) & HASH_BITS;
}


// onlyIfAbsent：  false/true 允许覆盖/不允许覆盖
final V putVal(K key, V value, boolean onlyIfAbsent) {
    // key和value都不允许为null，hashmap允许
    if (key == null || value == null) throw new NullPointerException();
    // 计算key的hash值
    int hash = spread(key.hashCode());
    int binCount = 0;
    // 定义局部变量 tab = Node[]
    for (Node<K,V>[] tab = table;;) {
        Node<K,V> f; int n, i, fh;
        // 如果table = null 或 table.length = 0，说明table没有初始化
        if (tab == null || (n = tab.length) == 0)
            // 初始化table
            tab = initTable();
        // 如果table不为null说明已经初始化过
        // 计算当前key在table[]对应位置是否为null
        else if ((f = tabAt(tab, i = (n - 1) & hash)) == null) {
            // cas设置Node到指定index，成功就退出
            // 失败说明有同样index的key刚操作成功
            if (casTabAt(tab, i, null,
                         new Node<K,V>(hash, key, value, null)))
                break;
        }
        // 如果不为null判断当前节点的hash == MOVED(-1)，表示当前正在对数组进行扩容
        else if ((fh = f.hash) == MOVED)
            // 协助进行扩容，扩容下面再分析
            tab = helpTransfer(tab, f);
        // 已经初始化，且不在扩容，那么调用synchronized进行元素的添加
        else {
            V oldVal = null;
            // 加锁
            synchronized (f) {
                // 判断有没有线程对table[i]进行修改
                if (tabAt(tab, i) == f) {
                    // fh >= 0说明是链表结构
                    if (fh >= 0) {
                        binCount = 1;
                        for (Node<K,V> e = f;; ++binCount) {
                            K ek;
                            // 如果hash key都相同，替换旧值
                            if (e.hash == hash &&
                                ((ek = e.key) == key ||
                                 (ek != null && key.equals(ek)))) {
                                oldVal = e.val;
                                // onlyIfAbsent = false 才能替换
                                if (!onlyIfAbsent)
                                    e.val = value;
                                break;
                            }
                            // 否则找到链表最后的节点，将当前节点加入链接
                            Node<K,V> pred = e;
                            if ((e = e.next) == null) {
                                pred.next = new Node<K,V>(hash, key,
                                                          value, null);
                                break;
                            }
                        }
                    }
                    // 如果不是链表判断是不是红黑树
                    else if (f instanceof TreeBin) {
                        Node<K,V> p;
                        binCount = 2;
                        // 调用红黑树的put方法，返回不是null说明之前有过这个key
                        if ((p = ((TreeBin<K,V>)f)
                          .putTreeVal(hash, key,value)) != null) {
                            oldVal = p.val;
                            if (!onlyIfAbsent)
                                p.val = value;
                        }
                    }
                }
            }
            // 判断binCount >= 8
            if (binCount != 0) {
                if (binCount >= TREEIFY_THRESHOLD)
                    // 成立就转换成红黑树或扩容
                    treeifyBin(tab, i);
                if (oldVal != null)
                    return oldVal;
                break;
            }
        }
    }
    // 进行数量统计或扩容
    addCount(1L, binCount);
    return null;
}

private final Node<K,V>[] initTable() {
    Node<K,V>[] tab; int sc;
    // CAS + 自旋,老搭档，table不为空就退出自旋
    while ((tab = table) == null || tab.length == 0) {
        // 如果sizeCtl < 0说明有其他线程正在初始化或扩容
        if ((sc = sizeCtl) < 0)
            // 交出线程执行权，只是自旋
            Thread.yield(); // lost initialization race; just spin
        // 不是，则CAS修改sizeCtl为-1，表示正在初始化
        else if (U.compareAndSwapInt(this, SIZECTL, sc, -1)) {
            try {
                // 继续判断一次table==null
                // 确实会出现：执行到此处线程切换，别的线程执行了初始化
                if ((tab = table) == null || tab.length == 0) {
                    // 如果sizeCtl > 0说明构造函数设置了sizeCtl，否则默认cap=16
                    int n = (sc > 0) ? sc : DEFAULT_CAPACITY;
                    @SuppressWarnings("unchecked")
                    // 定义数组的大小
                    Node<K,V>[] nt = (Node<K,V>[])new Node<?,?>[n];
                    // table成员变量指向nt刚创建的数组
                    table = tab = nt;
                    // 计算新的sizeCtl，表示下一次扩容的阈值0.75n
                    sc = n - (n >>> 2);
                }
            } finally {
                sizeCtl = sc;
            }
            break;
        }
    }
    return tab;
}

static final int MIN_TREEIFY_CAPACITY = 64;
// 判断转成红黑树还是扩容
private final void treeifyBin(Node<K,V>[] tab, int index) {
    Node<K,V> b; int n, sc;
    // table不能为null
    if (tab != null) {
        // 判断数组长度是否小于64
        if ((n = tab.length) < MIN_TREEIFY_CAPACITY)
            // 扩容的核心方法，下面再分析，扩大一倍
            tryPresize(n << 1);
        // 转成红黑树
        else if ((b = tabAt(tab, index)) != null && b.hash >= 0) {
            synchronized (b) {
                if (tabAt(tab, index) == b) {
                    TreeNode<K,V> hd = null, tl = null;
                    // 将链表转换成红黑树
                    for (Node<K,V> e = b; e != null; e = e.next) {
                        // 遍历每个节点，创建对应的TreeNode
                        TreeNode<K,V> p =
                            new TreeNode<K,V>(e.hash, e.key, e.val,
                                              null, null);
                        // 建立树之间的关系
                        if ((p.prev = tl) == null)
                            hd = p;
                        else
                            tl.next = p;
                        tl = p;
                    }
                    // 将第一个树节点放到TreeBin容器中
                    setTabAt(tab, index, new TreeBin<K,V>(hd));
                }
            }
        }
    }
}
```

> 1. `ConcurrentHashMap`的 put方法中不允许`key和value的值为null`，这与HashMap不同。
>
> 2. `ConcurrentHashMap`的`table(Node[])在put方法中才会初始化`，构造函数中并不会初始化。
>
> 3. `initTable`方法中通过`自旋+CAS`实现线程安全的table初始化。
>
> 4. `sizeCtl`成员变量在`初始化后就不再等于数组长度`，而是用于表示`扩容阈值(0.75n)`。
>
> 5. `treeifyBin`若`当前table.length < 64时会变成原来的2倍，否则会转换成红黑树`。
>
> 6. `addCount`方法用于添加计数，如果table太小且还未调整大小，则调用transfer扩容。 如果已经调整了大小，那么需要帮助扩容。
>
> 7. 执行流程：
>
>    ①判断队列是否为空，为空就先初始化队列。
>
>    ②不为空就查看数组当前位置是否为null，如果为null直接创建Node放在此位置。
>
>    ③判断当前数组是否在`扩容(f.hash == MOVED)`，如果是正在扩容，那么当前线程协助扩容。
>
>    ④如果①②③都不成立，那么使用`synchronized`加锁准备执行⑤⑥。
>
>    ⑤如果当前节点存放的是链表，那么将链表中的节点依次比较，如果相同就替换，如果没有相同的那就添加到`链表尾部`。
>
>    ⑥如果当前节点存放的是红黑树，调用putTreeVal添加到树上，如果同一个位置下`节点超过8个`，且`数组大小超过64`，那么会将链表转成红黑树，否则会`扩容成原来数组两倍`。
>
>    ⑦最后执行`addCount`计数并判断是否需要扩容。

##### resize

```java
private static final int MIN_TRANSFER_STRIDE = 16;
// 扩容期间表示下一个数组的index
private transient volatile int transferIndex;
// 扩容的核心方法，参数原有的table和扩容后的table
private final void transfer(Node<K,V>[] tab, Node<K,V>[] nextTab) {
    // 定义数组长度和步长
    int n = tab.length, stride;
    // 如果CPU核数大于1，计算 n/(8*NCPU) < 16成立 步长stride = 16
    // 单核情况下，默认一个线程执行扩容
    if ((stride = (NCPU > 1) ? (n >>> 3) / NCPU : n) < MIN_TRANSFER_STRIDE)
        stride = MIN_TRANSFER_STRIDE;
    // 如果nextTable为null，那么创建扩容后的table[]，默认是2倍
    if (nextTab == null) {
        try {
            @SuppressWarnings("unchecked")
            // 初始化创建大小为2倍原有数组长度的数组
            Node<K,V>[] nt = (Node<K,V>[])new Node<?,?>[n << 1];
            nextTab = nt;
        } catch (Throwable ex) {
            sizeCtl = Integer.MAX_VALUE;
            return;
        }
        // 将创建的数组赋予nextTable成员变量，在此处被赋值，只有扩容期间此参数不为null
        // 说明是第一个扩容的线程,此后如果有其他线程调用put，那么也会进来帮忙
        nextTable = nextTab;
        // 默认是旧table的大小，用于表示数组扩容进度
        // [0, transferIndex-1]表示还未分配到线程扩容的部分
        // [transferIndex， n-1]表示已经分配给某个线程正在扩容或已经扩容完成的部分
        transferIndex = n;
    }
    int nextn = nextTab.length;
    // 创建ForwardingNode，并赋值nextTab
    ForwardingNode<K,V> fwd = new ForwardingNode<K,V>(nextTab);
    // 结合下文：advance表示从i(transferIndex - 1)到bound位置过程中是否一直继续
    boolean advance = true;
    // 表示扩容是否结束
    boolean finishing = false;
    // i 表示为 遍历的下标，bound为遍历的边界
    // i = nextIndex - 1;bound = nextIndex - stride，拿不到任务两者都为0
    for (int i = 0, bound = 0;;) {
        Node<K,V> f; int fh;
        while (advance) {
            // 以下三个分支，一个成功就会退出while循环
            int nextIndex, nextBound;
            if (--i >= bound || finishing)
                advance = false;
            else if ((nextIndex = transferIndex) <= 0) {
                i = -1;
                advance = false;
            }
            // 因为扩容分配给多个线程需要同步，使用CAS操作transferIndex
            // 尝试为当前线程分配步长，CAS操作成功就表示拿到步长了。
            else if (U.compareAndSwapInt
                     (this, TRANSFERINDEX, nextIndex,
                      nextBound = (nextIndex > stride ?
                                   nextIndex - stride : 0))) {
                // 分配成功就修改bound和i，advance退出循环
                bound = nextBound;
                i = nextIndex - 1;
                advance = false;
            }
        }
        // 至此i为负数，整个hashmap已经遍历完成了，准备扩容
        // 如果 i<0 或i >= 旧数组大小n 或 i + n >= 新数组大小
        if (i < 0 || i >= n || i + n >= nextn) {
            int sc;
            // 如果finishing = true说明扩容完成
            if (finishing) {
                // 将nextTable置为null，将
                nextTable = null;
                table = nextTab;
                sizeCtl = (n << 1) - (n >>> 1);
                return;
            }
            if (U.compareAndSwapInt(this, SIZECTL, sc = sizeCtl, sc - 1)) {
                if ((sc - 2) != resizeStamp(n) << RESIZE_STAMP_SHIFT)
                    return;
                finishing = advance = true;
                i = n; // recheck before commit
            }
        }
        // table[i]迁移完毕，此位置放个ForwardingNode
        else if ((f = tabAt(tab, i)) == null)
            advance = casTabAt(tab, i, null, fwd);
        // 说明这个位置已经在迁移中了 fh = f.hash
        else if ((fh = f.hash) == MOVED)
            advance = true;
        else {
            // 对table[i]开始迁移
            synchronized (f) {
                // 先判断此位置有没有被其他线程修改
                if (tabAt(tab, i) == f) {
                    Node<K,V> ln, hn;
                    // 如果fh > 0说明是链表
                    if (fh >= 0) {
                        int runBit = fh & n;
                        Node<K,V> lastRun = f;
                        for (Node<K,V> p = f.next; p != null; p = p.next) {
                            int b = p.hash & n;
                            // 当b!=runBit时表明节点p后的全部节点的hashcode都相同
                            if (b != runBit) {
                                runBit = b;
                                lastRun = p;
                            }
                        }
                        // 和hashmap一致 hashcode & n = 0就不平移，不等于就平移
                        if (runBit == 0) {
                            ln = lastRun;
                            hn = null;
                        }
                        else {
                            hn = lastRun;
                            ln = null;
                        }
                        for (Node<K,V> p = f; p != lastRun; p = p.next) {
                            int ph = p.hash; K pk = p.key; V pv = p.val;
                            // 下面是将链表中的节点平移到新的数组中
                            // 这点hashmap是一致的，通过hashcode & 数组长度来判断
                            // 如果需要平移，那么平移后的index = oldIndex + n
                            if ((ph & n) == 0)
                                ln = new Node<K,V>(ph, pk, pv, ln);
                            else
                                hn = new Node<K,V>(ph, pk, pv, hn);
                        }
                        setTabAt(nextTab, i, ln);
                        setTabAt(nextTab, i + n, hn);
                        setTabAt(tab, i, fwd);
                        advance = true;
                    }
                    // 下面是处理红黑树的迁移
                    else if (f instanceof TreeBin) {
                        TreeBin<K,V> t = (TreeBin<K,V>)f;
                        TreeNode<K,V> lo = null, loTail = null;
                        TreeNode<K,V> hi = null, hiTail = null;
                        int lc = 0, hc = 0;
                        for (Node<K,V> e = t.first; e != null; e = e.next) {
                            int h = e.hash;
                            TreeNode<K,V> p = new TreeNode<K,V>
                                (h, e.key, e.val, null, null);
                            if ((h & n) == 0) {
                                if ((p.prev = loTail) == null)
                                    lo = p;
                                else
                                    loTail.next = p;
                                loTail = p;
                                ++lc;
                            }
                            else {
                                if ((p.prev = hiTail) == null)
                                    hi = p;
                                else
                                    hiTail.next = p;
                                hiTail = p;
                                ++hc;
                            }
                        }
                        ln = (lc <= UNTREEIFY_THRESHOLD) ? untreeify(lo) :
                        (hc != 0) ? new TreeBin<K,V>(lo) : t;
                        hn = (hc <= UNTREEIFY_THRESHOLD) ? untreeify(hi) :
                        (lc != 0) ? new TreeBin<K,V>(hi) : t;
                        setTabAt(nextTab, i, ln);
                        setTabAt(nextTab, i + n, hn);
                        setTabAt(tab, i, fwd);
                        advance = true;
                    }
                }
            }
        }
    }
}
// tryPersize可以扩容指定大小
private final void tryPresize(int size) {
    // 判断size是否超过MAXIMUM_CAPACITY >>> 1，是size就是MAXIMUM_CAPACITY
    // 否就调用tableSizeFor生成大于size的最小2此幂
    int c = (size >= (MAXIMUM_CAPACITY >>> 1)) ? MAXIMUM_CAPACITY :
    tableSizeFor(size + (size >>> 1) + 1);
    int sc;
    // sizeCtl < 0表示正在初始化或扩容
    while ((sc = sizeCtl) >= 0) {
        Node<K,V>[] tab = table; int n;
        // 如果table = null，sizeCtl表示初始容量
        if (tab == null || (n = tab.length) == 0) {
            // 选择sizeCtl和cap较大的作为数组大小
            n = (sc > c) ? sc : c;
            // 尝试将sizeCtl设为-1表示正在初始化
            if (U.compareAndSwapInt(this, SIZECTL, sc, -1)) {
                try {
                    if (table == tab) {
                        @SuppressWarnings("unchecked")
                        Node<K,V>[] nt = (Node<K,V>[])new Node<?,?>[n];
                        table = nt;
                        //sizeCtl = 0.75n，即扩容阈值
                        sc = n - (n >>> 2);
                    }
                } finally {
                    sizeCtl = sc;
                }
            }
        }
        // 如果c小于等于sc或数组大小超过max，则break
        else if (c <= sc || n >= MAXIMUM_CAPACITY)
            break;
        else if (tab == table) {
            int rs = resizeStamp(n);
            // sc < 0说明正在扩容，那么帮助扩容
            if (sc < 0) {
                Node<K,V>[] nt;
                if ((sc >>> RESIZE_STAMP_SHIFT) != rs || sc == rs + 1 ||
                    sc == rs + MAX_RESIZERS || (nt = nextTable) == null ||
                    transferIndex <= 0)
                    break;
                // 将sc加1，sc表示正在进行扩容帮忙的线程数量
                if (U.compareAndSwapInt(this, SIZECTL, sc, sc + 1))
                    transfer(tab, nt);
            }
            // 如果没有初始化或者正在扩容，那么开启第一次扩容
            else if (U.compareAndSwapInt(this, SIZECTL, sc,
                                         (rs << RESIZE_STAMP_SHIFT) + 2))
                transfer(tab, null);
        }
    }
}
```

> 1. transfer方法需要传入`扩容前数组table和扩容后数组nextTable`，如果nextTable=null，会读nextTable进行初始化，大小时table的2倍。
>
> 2. `stride`表示为`步长`，代表每个线程处理扩容的长度，通过公式：`(stride = (NCPU > 1) ? (n >>> 3) / NCPU : n)`计算得出，一般是16。
>
> 3. transferIndex用于表示整个数组扩容的进度，其扩容的范围不同分别表示为：`[0，transfer - 1] 表示还未分配线程扩容的部分，[transfer， n(原数组长度)]表示为已分配线程进行扩容，有可能正在扩容或扩容已完成`，如果当前线程CAS修改transferIndex成功，说明它可以在`指定步长范围内进行扩容操作`。
>
>    ![](https://image.leejay.top/image/20200713/Jns0xdIrB1m6.png?imageslim)
>
> 4. 假设扩容还未完成之前，有的table[i]已经转移到新的table中了，有的还在旧的table中，此时有get()线程访问旧table[]，我们会新建一个`ForwardingNode`用于存放新的table的引用，保证get到的是新的table中的数据。那如果是put线程呢？会调用`helpTransfer`来帮助最早扩容的线程来进行扩容。
>
> 5. 与Hashmap中关于链表的扩容一致，会通过`hashcode & length == 0`判断是否需要移位，如果需要移位，那么移位后的`index = oldIndex + oldCap`。
>
> 6. 总体流程：
>
>    ① 计算`stride步长`，一般值为16，如果扩容后数组`nextTable = null`，则初始化nextTable，且大小是扩容前table的2倍。
>
>    ② 当前线程`基于stride步长和transferIndex(即old table大小)`开始获取扩容任务，直到CAS修改`transferIndex`成功即视为获取任务成功，准备执行扩容。
>
>    ③ 如果finishing = true表明扩容任务完成，如果当前`table[i] = null，说明table[i]`迁移完成，那么会放置`FowardingNode`用于将get线程`请求转发(nextTable记录新table引用)`去查询新的table。
>
>    ④ 最终进行扩容，根据链表或红黑树分开扩容，链表使用了链表平移的优化方法(`扩容后链表顺序非绝对倒序`)，直到所有线程分别扩容结束，扩容流程才结束。

##### get

```java
public V get(Object key) {
    Node<K,V>[] tab; Node<K,V> e, p; int n, eh; K ek;
    // 计算key的hashcode
    int h = spread(key.hashCode());
    // 如果table不为空且table[i]的值不为null
    if ((tab = table) != null && (n = tab.length) > 0 &&
        (e = tabAt(tab, (n - 1) & h)) != null) {
        // 如果hashcode相同
        if ((eh = e.hash) == h) {
            // 并且key相同
            if ((ek = e.key) == key || (ek != null && key.equals(ek)))
                // 返回该位置的value
                return e.val;
        }
        // eh=-1说明当前节点时ForwardingNode节点
        // eh=-2说明时TreeBin
     	// 不同类型调用各自的find方法
        else if (eh < 0)
            return (p = e.find(h, key)) != null ? p.val : null;
        // eh>=0,说明该节点下挂的是链表，直接遍历链表
        while ((e = e.next) != null) {
            if (e.hash == h &&
                ((ek = e.key) == key || (ek != null && key.equals(ek))))
                return e.val;
        }
    }
    // 如果查不到就返回null
    return null;
}
```

> 为什么get()方法不需要加锁？
>
> 因为Node类的`属性value被volatile修饰`，保证线程间的可见性。因为是无锁的，所以性能能够大幅提升。
>
> 但是`ConcurrentHashMap`和`CopyOnWriteArrayList`一样，都是保证了`数据最终一致性，不能保证实时一致性`。因为`读写不互斥`，所以线程获取某个key的时候是看不到另一个线程正在添加或修改该key的。

##### 扩容时机

- 执行put()方法中如果`同一位置下节点数超过8个且数组长度小于64时`，会调用treeifyBin()方法进行扩容。
- 执行put()方法中如果检测到节点的`hash值 = MOVED`，那么会调用`helpTransfer`进行协助扩容。
- 执行put()方法中的`addCount`方法，如果数组元素发生改变有可能调用扩容。
- 执行putAll()时如果`当前数组大小超过了扩容阈值`，会进行扩容。

##### 扩容时的读写操作

- 当数组正在扩容时，某线程调用了`get`方法，那么如果对应的table[i]已经全部迁移，那么只需要通过table[i]位置中的`FowardingNode.nextTable`属性获取新的table的引用。
- 当数组正在扩容时，某线程调用了`put`方法，那么当前线程会调用`helpTransfer`方法协助进行扩容。

---

### ThreadPool

