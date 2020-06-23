## Java多线程知识点

[toc]

---

### 1. 守护线程与非守护线程

运行在JVM进程中的线程非为两类： 守护线程 & 非守护线程，我们通过如下代码将线程设置为非守护线程。

```java
void daemon() {
    Thread t = new Thread();
    t.setDaemon(true);
}
```

>  当所有的非守护线程运行退出后，整个JVM进程都会退出(包括守护线程)。

---

### 2. 线程的中断

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

---

### 3. 为什么wait() & notify()属于Object类？

因为wait() 和 notify()拥有锁才可以执行，其次synchronized中的锁可以是任意对象(通过对象头中的MarkWord实现)，所以他们属于Object类。

---

### 4.什么时候使用notify(signal)和notifyAll(signalAll)

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

---

### 5. MESA模型

在解释MESA模型之前，我们需要了解什么是`管程：又称为监视器，它是描述并实现对共享变量的管理与操作，使其在多线程下能正确执行的一个管理策略。可以理解成临界区资源的管理策略。`MESA模型是管程的一种实现策略，Java使用的就是该策略。

#### 相关术语

1. **enterQueue**：`管程的入口队列`，当线程在申请进入管程中发现管程已被占用，那么就会进入该队列并阻塞。
2. **varQueue**：`条件变量等待队列`，在线程执行过程中(已进入管程)，条件变量不符合要求，线程被阻塞时会进入该队列。
3. **condition variables**：条件变量，存在于管程中，一般由程序赋予意义，程序通过判断条件变量执行阻塞或唤醒操作。
4. **阻塞和唤醒**：wait()和await()就是阻塞操作。notify()和notifyAll()就是唤醒操作。

#### 模型概念图

![](https://image.leejay.top/image/20200623/7fsvqebTy60R.png?imageslim)

	> Synchronized和Lock在MSEA监视器模型中的区别在于`前者只有一个条件变量，后者可以有多个`。

#### 执行流程

1. 多个线程进入`入口等待队列enterQueue`，JVM会保证只有一个线程能进入管程内部，Syn中进入管程的线程随机。
2. 进入管程后通过条件变量判断当前线程是否能执行操作，如果不能跳到step3，否则跳到step4。
3. 条件变量调用`阻塞`方法，将当前线程放入varQueue，等待其他线程唤醒，跳回step1。
4. 执行相应操作，执行完毕后调用notify/notifyAll等唤醒操作，唤醒对应varQueue中的一个或多个等待线程。
5. 被唤醒的线程会从varQueue放入enterQueue中，再次执行step1。
6. `被唤醒的线程不会立即执行，会被放入enterQueue，等待JVM下一次选择运行，而正在运行的线程会继续执行，直到程序执行完毕。`

---

### 5. 线程状态迁移图

![](https://image.leejay.top/image/20200326/3XSAP42BEbCV.png)

---

### 6. Synchronized在对象头中的构成

![](https://image.leejay.top/image/20200603/sVQHgMaLfpgG.png?imageslim)

---

### 7.内存可见性问题

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
  - 对volatile变量的写入，happen-before 于后续对这个变量的读取。
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

### 8. volatile

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

  Q：什么时候用volatile而可以不用synchronized？

  A：如果`写入变量值不依赖变量当前值(count++就是依赖当前值，先去内存读取值，然后将当前值+1，将计算后的值赋给count。比如)`，那么就可以用volatile。

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

### 9.JVM对 long和double是原子操作吗？

我们基于Hotspot虚拟机，在32位系统下，每次能操作的最大长度是32bit，而long/double是8字节/64bit，所以对long/double的读写需要两条指令才能完，所以`对long/double的操作在32位hotspot中不是原子性操作`，而64位hotspot可以实现对long/double的原子性操作。

### 10.CAS

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

- ABA问题

  因为CAS是基于值来做比较的，如果线程A将变量从X改成Y，又改回X，尽管改过两次，但是线程B去修改的时候会认为这个变量没有被修改过。

- AtomicStampedReference

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

- AtomicMarkableReference

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

- AtomicIntegerFieldUpdater

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

- AtomicIntegerArray

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

- Striped64及相关子类

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

- 伪共享与缓存行填充

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

### 11.Lock

Lock与Synchronized都是`可重入锁`，否则会发生死锁。Lock锁核心在于`AbstractQueueSynchronizer`，又名`队列同步器(简称AQS)`。如果需要实现自定义锁，除了需要实现Lock接口外，还需要内部类继承Sync类。

- AQS结构

  ![](https://image.leejay.top/image/20200608/GVtL7ztzwtCl.png?imageslim)

  - 记录当前锁的持有线程

    由AQS的父类`AbstractOwnableSynchronizer`实现记录当前锁的持有线程功能。

  - state变量

    内部维护了volatile修饰的state变量，state = 0时表明没有线程获取锁，state = 1时表明有一个线程获取锁，当state > 1时，说明该线程重入了该锁。

  - 线程阻塞和唤醒

    由`LockSupport`类实现，其底层是调用了Unsafe的park 和 unpark。如果当前线程是非中断状态，调用park()阻塞，返回中断状态是false，如果当前线程是中断状态，调用park()会不起作用立即返回。也是为什么AQS要清空中断状态的原因。

  - FIFO队列

    AQS内部维护了一个基于`CLH(Craig, Landin, and Hagersten(CLH)locks。基于链表的公平的自旋锁)`变种的FIFO双向链表阻塞队列，在等待机制上由自旋改成阻塞唤醒(park/unpark)。

    ![](https://image.leejay.top/image/20200609/CHJldTlsLVp2.png?imageslim)

    > 还未初始化的时候，head = tail = null，之后初始化队列，往其中假如阻塞的线程时，会新建一个空的node，让head和tail都指向这个空node。之后加入被阻塞的线程对象。当head=tai时候说明队列为空。

- 方法入口

  我们选择`ReentrentLock`作为入口进行源码解读

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

- 公平锁和非公平锁

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

- acquire()

  ```java
  public final void acquire(int arg) {
      if (!tryAcquire(arg) &&
          acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
          selfInterrupt();
  }
  ```

  - tryAcquire()

    ```java
    // 调用非公平锁的tryAcquire()
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
            // 设置state值
            setState(nextc);
            return true;
        }
        return false;
    }
    ```

    > Q:  为什么有的地方使用setState()，有的地方使用CAS？
    >
    > A:  因为使用setState()方法的前提是已经获取了锁，使用了CAS的是因为此时还没有获取锁。

  - addWaiter()

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
            // 尝试用CAS将node设置为tail尾节点
            if (compareAndSetTail(pred, node)) {
                // 设置成功，将node插入到队列尾部并且tail=node
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
                // CAS设置node到队尾，如果不成功继续循环设置直到成功
                if (compareAndSetTail(t, node)) {
                    // CAS成功，设置t的next属性
                    t.next = node;
                    // 跳出循环
                    return t;
                }
            }
        }
    }
    ```

  - acquireQueued()

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
    // 将node节点设置为head头节点
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
            // 如果pred状态设置成功，第二次就会进入signal，返回true
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

  - cancelAcquire()

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
  
    > Q：为什么当node的后继节点是null的时候，从队尾开始往前找？
    >
    > A：在enq()方法中，if (compareAndSetTail(t, node))  和   t.next = node 不是原子性的，那么就存在将node设置为tail，还没有设置 t.next = node之前，另一个线程正好执行unparkSuccessor()的查找逻辑，从前往后找，此时的t.next = null，他就错误的认为t是尾节点，实际此时尾节点已经是node了。而prev属性赋值是在CAS操作之前，此时的tail尾节点还没有改变，所以prev比next更可靠。也符合CLH队列的特性：**prev 引用是务必要保证可靠的**。
- selfInterrupt()
  
    ```java
    // 当获取锁或插入node到队列的过程中发生了interrupt，那么这里需要补上
    static void selfInterrupt() {
        Thread.currentThread().interrupt();
  }
    ```
  
- unlock()

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
          return true;
      }
      return false;
  }
  
  protected final boolean tryRelease(int releases) {
      // 此时处于已获取锁状态，所以不需要cas获取state
      int c = getState() - releases;
      // 如果当前线程不是独占线程抛异常
      if (Thread.currentThread() != getExclusiveOwnerThread())
          throw new IllegalMonitorStateException();
      boolean free = false;
      // 如果state=0说明此时无锁
      if (c == 0) {
          free = true;
          setExclusiveOwnerThread(null);
      }
      // 设置状态
      setState(c);
      return free;
  }
  ```

- Condition

  ```java
  final ConditionObject newCondition() {
      return new ConditionObject();
  }
  ```

  > Q：condition的await()、signal()、signalAll()作用和wait()、notify()、notifyAll()区别？
  >
  > A：condition的await()、signal()、signalAll()作用和wait()、notify()、notifyAll()类似，但是两者存在区别。首先是基于不同的锁：Lock和Synchronized，其次condition可以存在不同的条件队列，每个条件队列之间互不影响，而syn只会有一个条件队列(或条件变量，根据syn修饰位置不同，分别为this、class类和代码块中内容)。

  ![图片来源微信公众号: 日拱一兵](https://image.leejay.top/image/20200623/YuqTOjdHO8eR.png?imageslim)