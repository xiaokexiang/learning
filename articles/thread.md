## Java多线程知识点

[toc]

---

#### 1. 守护线程与非守护线程

运行在JVM进程中的线程非为两类： 守护线程 & 非守护线程，我们通过如下代码将线程设置为非守护线程。

```java
void daemon() {
    Thread t = new Thread();
    t.setDaemon(true);
}
```

>  当所有的非守护线程运行退出后，整个JVM进程都会退出(包括守护线程)。

---

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

---

#### 3. 为什么wait() & notify()属于Object类？

因为wait() 和 notify()拥有锁才可以执行，其次synchronized中的锁可以是任意对象(通过对象头中的MarkWord实现)，所以他们属于Object类。

---

#### 4. 线程状态迁移图

![](https://image.leejay.top/image/20200326/3XSAP42BEbCV.png)

---

#### 5. Synchronized在对象头中的构成

![](https://image.leejay.top/image/20200603/sVQHgMaLfpgG.png?imageslim)

---

#### 6. 内存可见性问题

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

#### 7. volatile

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

  A：如果`写入变量值不依赖变量当前值(count++就是依赖当前值)`，那么就可以用volatile。

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

---

#### 8.CAS

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
            // 
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