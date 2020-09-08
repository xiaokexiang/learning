> Java虚拟机把`描述类的数据从Class文件加载到内存`，并对数据进行`校验、转换解析和初始化`，最终形成可以被虚拟机`直接使用`的Java类型。
>

### 1.类加载时机

#### 类加载的生命周期

![](https://image.leejay.top/Fp0TSKAc1i8rSPKcAe1t36qz4a7e)

> 类型被加载到虚拟机内存中开始，到卸载出内存为止，整个生命周期经历如上七个阶段。
>
> 其中`验证、准备、解析`统称为`连接`。
>
> 需要注意的是：`解析阶段顺序是不确定的`，它可以在`初始化阶段之后再开始`。

#### 类初始化的六种情况

**《Java虚拟机规范》**中规定了`六种`要立即`对类进行”初始化“(加载、验证、准备自然需要在此之前执行)`的情况：

- 遇到`new`、`getstatic`、`putstatic`、`invokestatic`这四条字节码指令时，类型没有过初始化，生成这四条字节码指令的场景有：

  - 使用`new`关键字实例化对象。

  ```java
  // 会初始化A
  A a = new A();
  ```

  - 读取或设置一个类型的`静态字段(final修饰、编译器进入常量池的静态字段除外)`。

  ```java
  class B {
      // 会导致A类被初始化  
      static A a = new A();
      public static void main(String[] args) {
          // 不会导致A类被初始化
          System.out.print(A.HELLO_WORLD);
      }
  }
  class A {
      static final String HELLO_WORLD = "hello_world";
  }
  ```

  > 引用`静态字符串常量`不会导致持有该常量的类初始化。

  - 调用一个类型的静态方法

  ```java
  class A {
      static void print() {
          System.out.print("hello");
      }
      public static void main(String[] args) {
          A.print();
      }
  }
  ```

  > 我们可以用过`-XX:+TraceClassLoading`来查看类是否被加载。

- 通过`java.lang.Reflect`对方法进行反射调用时

- 初始化类时发现其`父类`还没有初始化。

```java
public class SuperClass {
    static {
        System.out.println("Super class init");
    }

    public static int value = 123;
}

class SubClass extends SuperClass {
    static {
        System.out.println("Sub class init");
    }
}

class Test {
    public static void main(String[] args) {
        //  只会初始化父类，不会初始化子类
        System.out.println(SubClass.value);
    }
}
```

- 接口中定义的`default`方法，若该接口的实现类发生初始化，`default`方法在此之前要被初始化。

```java
public interface Father {
    default void print() {
        System.out.print("hello");
    }
}

public class Son implements Father {

}

class Test {
    public static void main(String[] args) {
        // 会初始化Father接口中的default方法
		Father f = new Son();
    }
}
```

> 如果是接口初始化，那么不会要求父接口也全部初始化，`真正使用到的父接口`才会初始化。

- `java.lang.invoke.MethodHandle`实例的解析结果为`REF_getstatic`、`REF_putstatic`、`REF_invokestatic`、`REF_newInvokeSpecial`四种类型的方法句柄还没有初始化时。

---

### 2.类加载流程

#### 加载

`加载`阶段是整个类加载生命周期的**第一个阶段**，Java虚拟机需要完成以下三件事情：

- 通过一个类的全限定名获取定义此类的二进制字节流。

> 《Java虚拟机规范》没有规定`二进制字节流`的具体获取方式，目前已知获取方式包括：`从zip包读取、运行时生成、加密文件获取等`。既可以通过`Java虚拟机内置的类加载器`，也可以通过`用户自定义的类加载器`来实现自定义的二进制字节流获取方式。
>
> 数组本身不通过类加载器创建，但`数组的类型`需要通过类加载器来完成加载。

- 将字节流代表的静态存储结构转换为`方法区运行时数据结构`。

- 堆中生成`java.lang.Class`对象作为这个类方法区各各种数据的访问入口。

#### 验证

- 文件格式验证

验证字节流是否符号`Class文件格式`的规范，并能够被当前版本的虚拟机处理。包括：`常量池中的常量是否有不被支持的常量类型`、`是否以魔数0XCAFEBABE开头`等验证点。

- 元数据验证

对字节码描述的信息进行予以分析，确保符合规范，包括：`此类是否有父类`、`是否继承了不被允许继承的类`、`是否缺少字段、方法`等验证点。

- 字节码验证

通过`数据流和控制流分析`，确保程序语义是合法、符合逻辑的。包括：`保证任意时刻操作数栈的数据类型与指令代码序列都能配合工作`、`保证方法中类型转换都是有效的`等验证点。

- 符号引用验证

目的是确保将`符号引用转为直接引用`的`解析`阶段能够顺利执行，对类自身以外的类信息进行匹配性校验。包括：`符号引用中通过字符串描述的权限定名能够找到对应的类`、`符号引用中的类、字段、方法的可访问性`等验证点。

```bash
# 关闭大部分的类验证以缩短虚拟机加载类时间
-Xverify:none
```

#### 准备

为类中定义的`静态变量`分配内存并设置`类变量初始值`的阶段。这些变量所使用的内存都会在`方法区`进行分配。

```java
class Test {
    // 类变量(静态变量)，初始值为0
    public static int value1 = 123;
    // 类变量，初始值为123
    public static final int value2 = 123;
    // 实例变量
    public Object obj;
}
```

> 类变量value1在`准备`阶段过后的`初始值为0`，赋值为123的操作要等到`初始化`阶段才会执行。
>
> 类变量value2在编译时会生成`ConstantValue`属性，在`准备`阶段虚拟机就会将value2设置为123。
>
> 实例变量会随着`对象Test的实例化`而一起分配在`堆中`。

#### 解析

Java虚拟机将`常量池内的符号引用替换为直接引用`的过程。《Java虚拟机规范》没有规定`解析`阶段发生的具体时间，只要求了执行`ldc、getfield、getstatic等17个指令前`，先对它们所使用的`符号引用`进行解析。所以虚拟机可以自行决定解析的触发时机是**类被加载器加载时**或**符号引用将被使用前**。

> 符号引用：以`一组符号`来描述所引用的目标。可以引用没有加载到内存中的内容。
>
> 直接引用：可以直接指向目标的指针、相对偏移量或者能够定位到目标的句柄池。

- 类或接口解析

基于以下代码，将从未解析过的`符号引用x`解析为类或接口的`直接引用`。包括三个步骤：

```java
class Test {
    A x = new A();
}
```

> 1. 若A不是数组类型，虚拟机将`x作为权限定名`交给`Test类的加载器`去加载`类A`，`类A`按照`类加载流程`执行类加载，若发生错误，那么解析失败。
>
> 2. 若A是数组类型(如Integer[])，那么虚拟机会将`x即[Ljava/lang/Integer`中的`Integer`类型交给`Test类加载器`去加载，再由虚拟机生成对应的`数组对象`。
>
> 3. 若前两步没有问题，在解析完成前还要进行`符号引用`验证，确保`Test对A`的访问权限。

- 字段解析

通过类的常量池表查找`字段所属的`类或接口的符号引用`(用A表示)`，并执行`类或接口的解析`。按照如下步骤执行：

1. 若A本身包含了`简单名称`和`字段描述`都与目标匹配的字段，返回该字段的直接引用。
2. 否则，若A实现了接口，会按照继承关系从下往上查找，重复步骤1。
3. 否则，若A不是`java.lang.Object`，会按照继承关系从下往上查找，重复步骤1。
4. 否则，查找失败，抛出`NoSuchFieldError`异常。
5. 最后对该字段进行`权限验证`，若不具备权限，抛出`java.lang.illegalAccessError`异常。

- 方法解析

与`字段解析`类似，都是要找到`方法所属的`类或接口的符号引用`(用A表示)`。按照如下步骤执行：

1. 如果A是个接口，那么直接抛出`java.lang.IncompatibleClasssChangeError`异常。
2. 否则，查找类A中是否存在`简单名称`和`字段描述`都与目标匹配的方法，返回该方法的直接引用。
3. 否则，查找类A`实现的接口及它们的父接口`中递归查找是否存在`简单名称`和`字段描述`都与目标匹配的方法，若存在匹配的方法，说明类A是个`抽象类`，抛出`java.lang.AbstractMethodError`异常。
4. 否则宣告查找失败，抛出`java.lang.NoSuchMethodError`异常。
5. 若返回了方法的直接引用，则需要进行`权限验证`，若不具备抛出`java.lang.illegalAccessError`异常。

- 接口方法解析

1. 如果A是个类，那么直接抛出`java.lang.IncompatibleClasssChangeError`异常。
2. 否则，查找接口A中是否存在`简单名称`和`字段描述`都与目标匹配的方法，返回该方法的直接引用。
3. 否则 ，在接口A的父接口中递归查找，直到`java.lang.Object类`为止，若存在`简单名称`和`字段描述`都与目标匹配的方法，返回该方法的直接引用。
4. 否则宣告查找失败，抛出`java.lang.NoSuchMethodError`异常。

#### 初始化

直接来说：`初始化`阶段就是执行类构造器`<clinit>`方法的过程。

> `<clinit>`方法是由编译器自动收集类中的所有`类变量的赋值动作`与`静态语句块(static {})`中的语句合并产生的。编译器的收集顺序由语句在`源文件`中出现的顺序决定的。

```java
class Test {
    static {
        // i=1执行先于i=0，但并不能修改成功
        i = 1;
        // 输出i，提示Illegal forward reference编译错误
        // System.out.println(i);
    }
    static int i = 0;

    public static void main(String[] args) {
        System.out.println(i); // 0
    }
}
```

> `static静态代码块`只能访问到定义在其之前的变量，定义在其之后的变量`能赋值但不能访问`。

##### clinit

- 与`类的构造函数<init>方法`不同，`<clinit>`不需要显示的调用父类构造器，Java虚拟机会保证子类的`<clinit>`方法执行前，父类的`<clinit>`已执行完毕，因此`java.lang.Object`类是Java虚拟机中第一个被执行的`<clinit>`方法的类型。

- 由于父类的`<clinit>`先执行，所以父类中的而静态语句块要先于子类的变量赋值操作。
- `<clinit>`方法对于接口或类来说不是必须的，如果没有静态语句块、也没有变量赋值操作，那么编译器不会为该类生成`<clinit>`方法。
- 接口中不能使用`静态语句块`，但能有静态变量赋值操作，所以接口也能生成`<clinit>`方法。但与类不同的是，执行接口的`<clinit>`方法不需要执行父接口的`<clinit>`方法，只有当父接口中定义的变量被使用时，父接口才会初始化。此外接口的实现类在初始化时也不会执行接口的`<clinit>`方法。
- Java虚拟机必须要保证一个类的`<clinit>`方法在多线程环境中，如果多个线程同时初始化一个类，那么只会有一个线程去执行`<clinit>`方法，其他线程都要阻塞等待，直到活动线程执行完`<clinit>`。若一个类的`<clinit>`方法存在耗时很长的操作，那么可能造成多线程阻塞（活动线程执行完，其他线程不会再进入`<clinit>`方法）。

```java
class Parent {
    static int A = 0;
    static {
        A = 2;
    }
}

class Son extends Parent {
    public static int B = A;
}

public class Test1 {
    public static void main(String[] args) {
        System.out.println(Son.B);// 2
    }
}
```

> 根据规则：父类的`<clinit>`先执行，所以父类的静态语句块先于子类而执行。`Son.B = 2`。

---

### 3.类加载器

