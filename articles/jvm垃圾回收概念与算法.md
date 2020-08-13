### 垃圾回收概述
在JVM`运行期间`，会对内存中`不再被使用的对象`进行`分配和管理`。若不及时对内存中的垃圾进行清理，会导致被保留的空间无法被其他对象使用，从而导致`内存溢出`。

> 内存溢出：系统无法分配给程序所`需要的指定大小内存`。
>
> 内存泄漏：当`对象不再使用或无法继续使用时`，因为强引用的存在导致`本该会回收的内存无法被回收`，常见于：`Map用对象作为key不重写hashcode & equals & ThreadLocal内存泄漏`。 

### 对象是否存活

JVM垃圾回收器会对对象中`不再使用(死去)`的对象进行回收，那么垃圾回收器是如何进行判断的呢。

#### 1. 引用计数法

对于一个对象A，只要有一个对象引用了A，那么A的计数器增加1，当引用失效的时候就减1。该算法会产生`对象之间循环引用`问题，会导致`内存泄漏`。

#### 2. 可达性算法

通过一系列称为`"GC Roots"`的根对象作为起点，根据引用关系向下搜索，搜索过程走过的路称为`"引用链"`。如果某个对象到`"GC Roots"`没有任何引用链相连，就说明该对象需要被回收。

![](https://image.leejay.top/image/20200813/PK6PSNTuG2W8.png?imageslim)

> 图中绿色为`可达对象`，灰色为`不可达对象`。
>
> `GC Roots`包括但不限于以下：
>
> 1. 栈帧中引用的对象（局部变量、临时变量等）
> 2. 类中的`引用型静态变量`
> 3. `字符串常量池中的引用`。
> 4. 被`Synchronized`锁持有的对象。

---

### Java中的引用

传统的引用概念：若`reference`类型的数据中存储的数值是另一块内存的起始地址，就说明该`reference`数据是某个内存、某个对象的引用。

从`JDK1.2`开始，Java对引用的概念进行补充，将引用分为了：`强引用、软引用、弱引用和虚引用`四种。

- 强引用

即最传统引用的体现，比如`Object obj = new Object()`，只要强引用关系存在，那么垃圾回收器永远不会回收掉被引用的对象。

- 软引用

用于描述`还有用、但非必须的对象`，被软引用关联的对象，在OOM之前，会将这些对象进行二次回收，如果回收后仍没有足够内存，才会抛出OOM。Java中用`SoftReference`实现。

- 弱引用

相比`软引用`，被`弱引用`关联的对象只能生存到下一次垃圾收集，只要垃圾回收器工作，`弱引用`就会被回收。Java中用`WeakReference`实现。`ThreadLocal.ThreadLocalMap<k,v>中key就继承了弱引用`。

> 和软引用一样，弱引用也适合保存`可有可无的数据`，当系统内存不足的时候会被回收，内存充足的时候，缓存数据存在相当长的时间，达到让系统加速的作用。

- 虚引用

引用中最弱的一种，一个对象是否有`虚引用`的存在，对其生存时间不会产生影响，并且`无法通过虚引用获取对象实例`。唯一的作用就是为了在`该对象被回收时收到通知`。

```java
public class Phantom {
    // 实现 包含虚引用的对象在回收时接受通知
	public static void main(String[] args) {
        String hello = new String("hello");
        // 创建引用Queue
        ReferenceQueue<String> queue = new ReferenceQueue<>();
        PhantomReference<String> reference = new PhantomReference<>(hello, queue);
        new Thread(() -> {
            while (true) {
                Reference<? extends String> poll = queue.poll();
                if (poll != null) {
                    try {
                        // 此时说明hello对象被回收了
                        Field referent = Reference.class
                            .getDeclaredField("referent");
                        referent.setAccessible(true);
                        String str = (String) referent.get(poll);
                        System.out.println("GC Will Collect " + str);
                        break;
                    } catch (IllegalAccessException | NoSuchFieldException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        // 去除hello的引用
        hello = null;
        // 调用垃圾回收对象
        System.gc();
    }
}
```

> 当垃圾回收器准备回收一个对象时，发现它还有`软、弱、虚引用`，就会在回收对象之前，将该引用加入到与之关联的`引用队列ReferenceQueue`中去，这样就可以实现在引用对象回收前的相关操作。

![](https://image.leejay.top/image/20200813/yIcmomijhoVr.png?imageslim)

---

### 垃圾回收算法

- 标记清除法(Mark-Sweep)
  `分为`标记阶段`和`清除阶段`。在标记阶段，首先通过根节点标记所有从根节点开始的可达对象，未被标记的对象就是未被引用的垃圾对象。在清除阶段，清除所有未被标记的对象。`
  缺点:产生空间碎片。因为回收后的空间是不连续的，工作效率低于连续空间

- 复制算法(Copying)
  `将原有的内存空间分为两块，每次只使用其中一块，在进行垃圾回收时，将正在使用的内存中的活对象复制到未使用的内存块中，之后清除正在使用的内存块中的所有对象，交换两个内存的角色，完成垃圾回收。`
  优点: a. 通常新生代垃圾对象多余存活对象，所以使用复制算法效率高 b. 回收后的内存空间是没有碎片的
  缺点: 内存折半
  &emsp;`新生代串行垃圾回收器使用了该算法`: 分为eden、from(s0)和to(s1)，其流程: eden区和s0区的存活对象会被复制到s1区(如果是大对象或老年对象会直接进入老年代，如果s1区满了，对象也会进入老年代)，然后清空eden和s0区，然后将s0和s1区互相调换，保证s1永远是空的。

- 标记压缩算法(Mark-Compact)
  `从根节点开始，对所有可达对象做一次标记。在清除阶段，将所有的存活对象压缩到内存的一端，然后清除边界外的所有空间`
  优点: 避免了内存碎片的产生，不需要两块空间，效率高，是老年代的回收算法

- 分代算法(Generational Collecting)
  `它将内存区间根据对象的特点分成几块，根据每块内存区间的特点使用不同的回收算法，以提高回收的效率`
  &emsp;为了支持高频率的新生代回收，虚拟机可能使用一种叫做`卡表(Card table)`的数据结构。卡表为一个比特位集合，每一个比特位可以表示老年代的某个区域的所有对象是否持有新生代对象的引用，<font style="color: red">卡表位为0表示老年代区域没有任何对象指向新生代，为1表示老年代对象有指向新生代的应用。</font>在新生代GC的时候只需要扫描卡表位为1的老年代空间，有效提高回收效率。

- 分区算法(Region)
  `分区算法将整个堆空间分成连续的不同小区间，每个小区间都独立使用，独立回收`。优点: 控制一次回收小区间的数量，能够有效减少GC产生的停顿

- 主要垃圾回收算法图解

<img src="https://image.leejay.top/image/20191227/BXWEjuMyee3Q.png">

---

#### 对象被回收流程

  &emsp;在GC对对象A进行回收的时候，会先判断是否有`引用链`从GC Root指向对象A，如果有那么不需要进行回收。如果没有那么继续进行判断：如果<font color="red">对象A复写了finalize()方法且JVM之前没有调用过对象A的finalize()方法</font>，那么JVM就会将对象A放置到一个名叫`F-Queue`的队列中，稍后由JVM启动一个低优先级的Finalizer线程去执行对象A的finalize()方法，而此时对象A的finalize()是最后一次能够拯救对象A的途径，只需要在finalize()方法中`对象A重新与引用链建立联系即可`，否则对象A将被GC清理。
  <div style="text-align: center;" ><img border=1 src="https://image.leejay.top/image/20191227/6r25VFXqrL2q.png"/></div>
  > 可触性包含三种状态：
  > 1. 可触及的：从根节点开始，可以到达的这个对象。
  > 2. 可复活的：对象无引用链可达，但是对象可能在finalize()中复活。
  > 3. 不可触及的：对象的finalize()被调用后，对象没有复活，此时对象处于不可触及状态。此对象肯定不会复活，因为<font color="red">finazlie()只会被JVM调用一次。</font>

  下面通过代码来演示对象的复活与清理

  ``` java
  public class ReachabilityAnalysis {
      /**
       * 创建GC Root
       */
      private static ReachabilityAnalysis REACHABILITY_ANALYSIS = null;

      private void isAlive() {
          System.out.println("i'm still alive ...");
      }

      @Override
      protected void finalize() throws Throwable {
          super.finalize();
          System.out.println("execute finalize method ...");
          // 试图和引用链建立联系
          REACHABILITY_ANALYSIS = this;
      }

      public static void main(String[] args) throws InterruptedException {
          REACHABILITY_ANALYSIS = new ReachabilityAnalysis();

          // 去除引用链的联系, 便于测试
          REACHABILITY_ANALYSIS = null;
          // 调用gc时 对象第一次尝试自救
          System.gc();
          // 因为finalizer线程的低优先级, 需要休眠一会。JVM会先判断是否有必要执行finalizer方法, 并执行相应的finalize()方法
          Thread.sleep(1_000);

          if (null != REACHABILITY_ANALYSIS) {
              REACHABILITY_ANALYSIS.isAlive();
          } else {
              System.out.println("i'm dead ...");
          }

          // 第二次自救 用于判断是否会执行finalize方法两次
          REACHABILITY_ANALYSIS = null;
          System.gc();
          Thread.sleep(1_000);
          if (null != REACHABILITY_ANALYSIS) {
              REACHABILITY_ANALYSIS.isAlive();
          } else {
              System.out.println("i'm dead ...");
          }
          // 结论: 任何对象的finalize()方法只会被系统调用一次
      }
  }
  ```
  > 1. 不建议使用finalize()进行释放资源，因为可能发生引用外泄，无意中复活对象。
  > 2. finalize()调用时间不确定，相比之下更推荐finally释放资源。
