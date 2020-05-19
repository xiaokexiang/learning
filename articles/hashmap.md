## HashMap源码解析

>  什么是HashMap?
>
> HashMap是基于哈希表的Map接口的非同步实现，其本质上是`数组 + 链表(或二叉树)`的结合，它允许使用null值和null键，且不保证映射顺序。
>
> HashMap采用的`链地址法的Hash算法`，即存在相同的hash值时使用next指针将Node节点构建成链表结构进行保存。

![HashMap结构](https://image.leejay.top/image/20200519/7iYthJhj1l38.png?imageslim)

### 1. 构造函数

```java
// 在构造函数未指定时使用的负载因子
static final float DEFAULT_LOAD_FACTOR = 0.75f;

// 最大容量， 初始容量范围：2 < initialCapacity <= 1<<30
static final int MAXIMUM_CAPACITY = 1 << 30;

// 下一次扩容的阈值，默认(capacity * load factor)计算得出
int threshold;

/**
 * new HashMap<>(16)初始化入口
 */
public HashMap(int initialCapacity) {
    // 调用了初始容量和默认负载因子(用于扩容)
    this(initialCapacity, DEFAULT_LOAD_FACTOR);
}

/**
 * 带有初始容量和负载因子的构造函数
 */
public HashMap(int initialCapacity, float loadFactor) {
    // 校验initialCapacity参数是否合法
    if (initialCapacity < 0)
        throw new IllegalArgumentException("Illegal initial capacity: " +
                                           initialCapacity);
    // 超过最大容量就设置成最大容量
    if (initialCapacity > MAXIMUM_CAPACITY)
        initialCapacity = MAXIMUM_CAPACITY;
    // 判断负载因子是否合法
    if (loadFactor <= 0 || Float.isNaN(loadFactor))
        throw new IllegalArgumentException("Illegal load factor: " + loadFactor);
    this.loadFactor = loadFactor;
    // 计算扩容阈值，赋值后构造函数执行完成
    this.threshold = tableSizeFor(initialCapacity);
}

/**
 * 基于给定的容量，返回大于等于initialCapacity的最小的2的幂次方。
 * cap的值必定 > 0
 * 如果 cap = 1，n = 0进行或运算结果还是0，最终返回1(0+1)
 * |：位或运算 n |= n >>> 1 即 n = n | n >>> 1
 */
static final int tableSizeFor(int cap) {
    int n = cap - 1;
    n |= n >>> 1;
    n |= n >>> 2;
    n |= n >>> 4;
    n |= n >>> 8;
    n |= n >>> 16;
    return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
}
```

> 总结：
>
> HashMap的`initCapacity`为大于等于0的整数，并且默认负载因子为`0.75F`，扩容阈值为 大于等于initialCapacity的最小的2的幂次方。

#### tableSizeFor()

1. 首先我们需要确认两个前提，`cap: [0, 1<<30] `以及如果`cap=0或1`时，返回1，结论仍成立。
2. `int n = cap - 1`，如果`cap=2^*(*>=0)`，此时如果不减1，则在最后返回的时候会返回`cap+1(即2^(*+1))`。
3. 我们假设`n>0`，所以n的二进制必定存在一个或者多个bit值为1，我们用`x来表示bit值1(1<=x<=30)`的情况，则有可能`n = 0x00 0000 0000 0000 0000 0000 0000 0000`。
4. `n | n >>> 1`即为`n = n | n >>> 1`，就是n 与 n无符号右移1位的值进行或运算，n | n >>> 1结果为x 和x-1位都为1，n | n >>> 2即为x、x-1、x-2和x-3位都是1，n|n >>>4 即为x、x-1、x-2、x-3、...、x-7都是1，依次类推最终x及x后的所有位都为1。
5. 最终如果除了`cap=0或1`及超过MAXIMUM_CAPACITY的情况，其他都返回n + 1`(0010 0000 0000 ... 0000)`这样的数据，也就是2^*幂次方。

---

### 2. put()

```java
public V put(K key, V value) {
    return putVal(hash(key), key, value, false, true);
}

// 计算object的hash值
static final int hash(Object key) {
    int h;
    // key.hashCode()是nactive方法，不同的JVM有不同的实现
    // 将h的高16位和低16位进行与运算，这样保证每个bit都能参与运算
    return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
}

// 构成HashMap数组中每一项的链表数据结构
static class Node<K,V> implements Map.Entry<K,V> {
    // 当前node的hash值
    final int hash;
    // key
    final K key;
    // value
    V value;
    // 当前node的下一个node
    Node<K,V> next;

    Node(int hash, K key, V value, Node<K,V> next) {
        this.hash = hash;
        this.key = key;
        this.value = value;
        this.next = next;
    }
    ....
}

// put()核心代码
final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
                   boolean evict) {
    Node<K,V>[] tab; Node<K,V> p; int n, i;
    if ((tab = table) == null || (n = tab.length) == 0)
        n = (tab = resize()).length;
    if ((p = tab[i = (n - 1) & hash]) == null)
        tab[i] = newNode(hash, key, value, null);
    else {
        Node<K,V> e; K k;
        if (p.hash == hash &&
            ((k = p.key) == key || (key != null && key.equals(k))))
            e = p;
        else if (p instanceof TreeNode)
            e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
        else {
            for (int binCount = 0; ; ++binCount) {
                if ((e = p.next) == null) {
                    p.next = newNode(hash, key, value, null);
                    if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                        treeifyBin(tab, hash);
                    break;
                }
                if (e.hash == hash &&
                    ((k = e.key) == key || (key != null && key.equals(k))))
                    break;
                p = e;
            }
        }
        if (e != null) { // existing mapping for key
            V oldValue = e.value;
            if (!onlyIfAbsent || oldValue == null)
                e.value = value;
            afterNodeAccess(e);
            return oldValue;
        }
    }
    ++modCount;
    if (++size > threshold)
        resize();
    afterNodeInsertion(evict);
    return null;
}
```
