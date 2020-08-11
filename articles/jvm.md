### JVM内存区域概述

#### 虚拟机栈

虚拟机栈描述的是Java方法执行的`线程内存模型:`每个方法被执行时，JVM会同步创建一个`栈帧`，用于存储`局部变量表、操作数栈、动态链接、方法出口`等信息，虚拟机栈区域是`线程私有`的，它的生命周期与线程相同。

> 局部变量表：存放编译期可知的数据类型：`8种基本数据类型和对象引用类型`。这些数据类型在栈中用`slot`来表示，除了`long & double`占用`2个slot`，其余的都为1个。
>
> 虚拟机栈包含用于`执行native方法`的本地方法栈。它们都会抛出`OOM和StackOverFlow`异常。

#### 虚拟机堆

这是一块`线程共享`的内存区域，几乎全部的`对象实例、数组`都在堆上分配（小对象可以在`栈上分配`）。

> 从内存回收角度看， 堆被逻辑的分为：`年轻代（包括eden、from、to三个区域）、老年代`。
>
> 从内存分配角度看，堆被分为`多个线程私有的内存分配缓冲区（TLAB）`。

##### TLAB

Thread Local Allocation Buffer（本地线程缓冲区），原有的虚拟机给对象分配内存时，采用是`CAS + 失败重试`的方式。而`TLAB`是：

1. 通预先给每个线程在堆中分配一小块区域。
2. 哪个线程创建对象，就在哪个线程的TLAB中分配内存。
3. 如果这个线程的`TLAB`空间不够分配时，就通过`同步锁定`给这个线程分配新的`TLAB`。
4. `-XX:+/-UseTLAB`来开启和关闭TLAB。

#### 元数据区

`JDK1.8`起，方法区改名为`元数据区（MetaSpace）`，是`线程共享`的区域，是堆的一个`逻辑部分`，用于存储`JVM加载的类型信息、常量、静态变量及即时编译后的方法代码`等数据。会抛出`OOM`异常。

#### 运行时常量池

运行时常量池是方法区的一部分。Class文件除了有`类的版本、字段、方法、接口`等描述信息外，还包含`常量池表（Constant Pool Table）`，用于存放编译期生成的`各中字面量和符号引用`，这部分数据会在`类加载`后被放入常量池。

> `运行时常量池`相比`常量池表`更具动态性，但并非编译期生成的Class类常量池内容才能进入`方法区运行时常量池`，运行期间也可以进入，例如`String类的intern()方法`就可以实现。

---

### 程序计数器

`当前线程`所执行的字节码的行号指示器。分支、循环、异常处理都是依赖计数器实现，该区域是`线程私有`的。

### 直接内存

直接内存并不是JVM运行时数据区的一部分。常见于`NIO`类使用：通过`Native方法分配堆外内存`，在Java堆中持有该`内存区域的引用`实现操作，相比之前`在Java堆和Native堆之间来回复制`的方式，提升了效率。 

---

### JVM中的对象

#### 对象的创建

![](https://image.leejay.top/image/20200806/5Yyjn8VqQBwt.png?imageslim)

> 1. 在`Class类的常量池`中寻找该类的`符号引用`，并通过该符号引用判断类是否被加载。
> 2. 如果类没有被加载，那么JVM就会执行相应的类加载过程。
> 3. 给对象分配内存空间共有两种方式：`指针碰撞 & 空闲列表`。
> 4. 在对象分配内存的线程安全问题，默认是通过`CAS + 失败重试`实现，也可以选择`TLAB`。
> 5. 初始化内存空间为零值，并对`Mark Word`进行必要设置（根据是否启动偏向锁设置信息）。
> 6. 最终调用对象的构造函数进行初始化。

#### 对象的构成

对象在堆中的布局分为三个部分：`对象头、实例数据和对齐填充`。而对象头中又包含：`对象自身的运行时数据(Mark Word)、对象指向它类型元数据的指针以及数组长度(如果对象是数组)`。

##### 对象头

- Mark Word

  用于记录存储对象自身运行时的数据。比如`HashCode、锁状态标识`等。

![](https://image.leejay.top/image/20200806/bf8MF7GVoqRP.png?imageslim)

- 类型指针

  `对象头中指向该对象类型元数据(元数据区)的指针`，通过类型指针，JVM可以判断当前对象是`哪个类的实例`。

  > 并不是所有的虚拟机都会在对象头中保留类型指针。此问题查看[对象的引用](#对象的引用)

- 数组长度

  如果当前对象是数组，那么在对象头中还有一部分用于`存储数组长度的数据`。

##### 实例数据

即保存代码中定义的`各种类型的字段内容（包括父类继承）`，其存储顺序除了受到代码中定义的影响，还由JVM参数`-XX:FiedlsAllocationStyle`决定。

##### 对齐填充

对齐填充并不是`必然存在`的，因为`HotSpot`要求`对象的大小必须是8的整数倍`，对象头已经是8的整数倍，如果实例数据不是8的整数倍，那么就需要使用对齐填充来补全。

#### 对象的引用

对象的创建是为了能够使用该对象，我们通过`栈上的reference数据`来操作堆上的具体对象。但对象的访问方式由虚拟机自行决定，目前主流的有两种：`句柄 & 指针`。

![](https://image.leejay.top/image/20200811/doWGxxuV17Ne.png?imageslim)

> 1. 句柄：就是在堆中额外划分一块内存作为句柄池，栈中的`reference`存放的就是句柄池地址。句柄池中包含`对象实例数据 & 类型数据的内存地址`。
> 2. 直接指针：栈中`reference`存放的是堆中的对象地址，对象头中又包含`对象类型数据指针`。
> 3. 句柄的优点在于GC回收移动对象时，只需要修改`句柄池中的实例数据指针`。而指针的优点在于`访问更快`，减少一次查找。

---

### 模拟各区域OOM

#### 堆

```java
/**
  * -Xmx10m 模拟堆OOM
  */
public static void main(String[] args) {
    List<Object> list = new ArrayList<>();
    while (true) {
        list.add(new Object());
    }
}
```

### 栈

- stackOverFlow

```java
/**
  * -Xss1m
  */
public static void main(String[] args) {
    Stack stack = new Stack();
    // stackOverFlow
    stack.stackOverFlow();
}

void stackOverFlow() {
    stackOverFlow();
}
```

- OOM

```java
/**
  * -Xss1m
  */
public static void main(String[] args) {
    Stack stack = new Stack();
    // oom
    stack.oom();
}
void oom() {
    while (true) {
        new Thread(() -> {
            while (true) {

            }
        }).start();
    }
}
```

> 相比OOM，stackOverFlow更容易发生。

#### 元数据区

- 字符串常量池OOM

```java
/**
  * 1.7前 -XX:MaxPermSize=10m
  * 1.7后 -Xmx10m
  */
public static void main(String[] args) {
    List<String> list = new ArrayList<>();
    int i = 0;
    while (true) {
        list.add(("hello" + i++).intern());
    }
}
```

> 需要注意在JDK7及以上版本中不会抛出之前的`PemGen space`异常，因为常量池被移到了`堆中`，如果我们限制堆的大小，会抛出`Java heap space`异常。

- 元数据OOM

```java
/**
  * -XX:MaxMetaspaceSize=10m
  */
public static void main(String[] args) {
    while (true) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(Object.class);
        enhancer.setUseCache(false);
        enhancer.setCallback((MethodInterceptor) (o, method, objects, methodProxy) ->
                             methodProxy.invoke(o, objects));
        enhancer.create();
    }
}
```

> 因为元数据区存放类型的相关信息：类名、方法描述等，通过大量创建cglib代理类实现`Metaspace OOM`。

#### 直接内存

```java
/**
  * -XX：MaxDirectMemorySize=10m
  */
public static void main(String[] args) throws IllegalAccessException {
    // 反射获取unsafe类
    Field unsafeField = Unsafe.class.getDeclaredFields()[0];
    unsafeField.setAccessible(true);
    Unsafe unsafe = (Unsafe)unsafeField.get(null);
    while (true) {
        // 分配直接内存
        unsafe.allocateMemory(1024 * 1024);
    }
}
```

> 直接内存由：`-XX：MaxDirectMemorySize`指定，如果不指定则和`-Xmx`一致。

---

### 面试题

```java
String s1 = "hello";
String s2 = new String("hello");
System.out.println(s1.equals(s2));// true
System.out.println(s1 == s2);// false
System.out.println(s1 == s2.intern());// true
```

> `String s1 = "hello"`：JVM编译期会去常量池中寻找是否存在`"hello"`这个字符串，如果存在就在`栈中`直接开辟空间`存放"hello"在常量池中的地址`。如果不存在就在常量池中开辟空间存放`"hello"`。
>
> `String s2 = new String("hello")`：JVM编译期先去常量池寻找是否存在`"hello"`，如果不存在会在常量池中开辟空间存放`"hello"`。在运行期间，通过`new String("hello")`将常量池中的`"hello"`复制到`堆中`，相应的在栈中开辟空间存放该`对象的地址值`。
>
> `String.intern()`会将`"hello"`添加到常量池，并返回该`常量在常量池中的引用`，如果已存在该常量，那么会直接返回已存在常量的地址值。

