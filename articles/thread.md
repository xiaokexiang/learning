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
      // 这里的flag就可以不同锁同步
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

ThreadLocal变量，是Thread类的成员变量，因为`类的每个实例的成员变量都是这个实例独有的`，所以在不同的Thread中有不同的副本，每个线程的副本`只能由当前线程使用，线程间互不影响`。因为一个线程可以拥有多个ThreadLocal对象，所以其内部使用`ThreadLocalMap<ThreadLocal<?>, Object>`来实现。

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

> `Thread生命周期没有结束，ThreadLocal对象被回收后没有调用过get、set或remove方法就会导致内存泄漏。`

我们可以看出内存泄漏的触发条件比较苛刻的，但确实会发生，其实`只要线程Thread的生命周期结束，那么Thread的ThreadLocalMap也不会存在强引用，那么ThreadLocalMap中的value最终也会被回收。`，所以在使用ThreadLocal时，除了需要密切关注`Thread和ThreadLocal的生命周期`，还需要在每次使用完之后调用`remove`方法，这样做还有一个好处就是，如果你使用的是线程池，那么会出现`线程复用`的情况，如果不及时清理会导致下次使用的值不符合预期。

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

1. 多个线程进入`入口等待队列enterQueue`，JVM会保证只有一个线程能进入管程内部，Syn中进入管程的线程随机。
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

- 记录当前锁的持有线程

  由AQS的父类`AbstractOwnableSynchronizer`实现记录当前锁的持有线程功能。

- state变量

  内部维护了volatile修饰的state变量，state = 0时表明没有线程获取锁，state = 1时表明有一个线程获取锁，当state > 1时，说明该线程重入了该锁。

- 线程阻塞和唤醒

  由`LockSupport`类实现，其底层是调用了Unsafe的park 和 unpark。`如果当前线程是非中断状态，调用park()阻塞，返回中断状态是false，如果当前线程是中断状态，调用park()会不起作用立即返回。也是为什么AQS要清空中断状态的原因`。

- FIFO队列

  AQS内部维护了一个基于`CLH(Craig, Landin, and Hagersten(CLH)locks。基于链表的公平的自旋锁)`变种的FIFO双向链表阻塞队列，在等待机制上由自旋改成阻塞唤醒(park/unpark)。

  ![](https://image.leejay.top/image/20200609/CHJldTlsLVp2.png?imageslim)

  > 还未初始化的时候，head = tail = null，之后初始化队列，往其中假如阻塞的线程时，会新建一个空的node，让head和tail都指向这个空node。之后加入被阻塞的线程对象。当head=tai时候说明队列为空。

- Node的waitStatus

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
  > A：如果线程A在执行use时传递的`想是自己修改的数据，那么需要锁降级`。如果希望传递的是最新的数据，那么不需要锁降级。

##### 读写锁总结

- ReetrentReadWriteLock通过将state变量分为高低16位来解决记录读锁写锁获取的总数。
- 读锁的私有变HoldCounter记录者当前线程获取读锁的次数，底层通过`ThreadLocal`实现。
- 读锁的非公平获获取，通过`apparentlyFirstQueuedIsExclusive`方法一定概率防止了写锁无限等待。
- 当线程A获取写锁时，会因为其他持有`写锁（不包括线程A）`或`读锁（包括线程A）`的线程而阻塞。
- 当线程A获取读锁时，会因为其他持有`写锁（不包括线程A)`而阻塞。

---

#### CountDownLatch

#### CyclicBarrier